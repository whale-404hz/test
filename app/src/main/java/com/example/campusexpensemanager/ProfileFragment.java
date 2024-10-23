package com.example.campusexpensemanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.helper.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private CircleImageView ivAvatar;
    private TextView tvUsername;
    private Button btnChangeAvatar;
    private DatabaseHelper dbHelper;
    private int userId;
    private Button btnLogout;
    private Switch switchDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Find views
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        btnLogout = view.findViewById(R.id.btnLogout);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        // Initialize database helper
        dbHelper = new DatabaseHelper(requireActivity());

        // Get user ID from shared preferences
        userId = getUserIdFromPreferences();

        // Load user info (username and avatar)
        loadUserInfo();

        // Get dark mode state from SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDarkModeEnabled = preferences.getBoolean("dark_mode", false);

        // Set initial dark mode
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Set switch state based on dark mode preference
        switchDarkMode.setChecked(isDarkModeEnabled);

        // Set switch listener to change dark mode
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save dark mode preference
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            // Apply dark mode change
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            // Recreate the activity to apply changes
            requireActivity().recreate();
        });

        // Change avatar on button click
        btnChangeAvatar.setOnClickListener(v -> openImagePicker());

        // Logout on button click
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private int getUserIdFromPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1);
    }

    private void loadUserInfo() {
        Cursor cursor = dbHelper.getUserById(userId);

        if (cursor != null && cursor.moveToFirst()) {
            int usernameColumnIndex = cursor.getColumnIndex("username");
            int profileImageColumnIndex = cursor.getColumnIndex("avatar");  // Correct column for avatar

            if (usernameColumnIndex != -1) {
                String name = cursor.getString(usernameColumnIndex);
                tvUsername.setText(name);
            }

            if (profileImageColumnIndex != -1) {
                byte[] avatarBytes = cursor.getBlob(profileImageColumnIndex);
                if (avatarBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                    ivAvatar.setImageBitmap(bitmap);
                } else {
                    ivAvatar.setImageResource(R.mipmap.ic_default_avatar_foreground);
                }
            } else {
                ivAvatar.setImageResource(R.mipmap.ic_default_avatar_foreground);
            }
        } else {
            Toast.makeText(requireActivity(), "User not found!", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    // Open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == requireActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            try {
                InputStream inputStream = requireActivity().getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Set the avatar to the selected image
                ivAvatar.setImageBitmap(bitmap);

                // Save the avatar to the database
                dbHelper.updateUserAvatar(userId, bitmap);

                Toast.makeText(requireActivity(), "Avatar updated successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireActivity(), "Error while selecting image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Logout and clear session
    private void logout() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Clear all data in session
        editor.apply();

        // Redirect to Login activity
        Intent intent = new Intent(requireContext(), Login.class);  // Update with actual Login activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();  // Finish the current activity
    }
}

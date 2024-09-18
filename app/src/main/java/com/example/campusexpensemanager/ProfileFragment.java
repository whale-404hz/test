package com.example.campusexpensemanager;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private Button btnChangeAvatar;
    private DatabaseHelper dbHelper;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);

        dbHelper = new DatabaseHelper(getActivity());
        userId = getUserIdFromPreferences();
        loadUserInfo();

        // Mở trình chọn ảnh khi người dùng nhấn nút thay đổi ảnh đại diện
        btnChangeAvatar.setOnClickListener(v -> openImagePicker());
        return view;
    }

    private int getUserIdFromPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1);
    }

    private void loadUserInfo() {
        Cursor cursor = dbHelper.getUserById(userId);

        if (cursor != null && cursor.moveToFirst()) {
            int usernameColumnIndex = cursor.getColumnIndex("username");
            int profileImageColumnIndex = cursor.getColumnIndex("avatar"); // Sửa cột cho đúng

            if (usernameColumnIndex != -1) {
                String name = cursor.getString(usernameColumnIndex);
                tvUsername.setText(name);
            }

            if (profileImageColumnIndex != -1) {
                byte[] avatarBytes = cursor.getBlob(profileImageColumnIndex); // Truy xuất Blob
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
            Toast.makeText(getActivity(), "User not found!", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    // Mở trình chọn ảnh
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            try {
                // Sử dụng InputStream để đọc dữ liệu từ URI
                InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Cập nhật hình ảnh vào ImageView
                ivAvatar.setImageBitmap(bitmap);

                // Lưu hình ảnh vào cơ sở dữ liệu dưới dạng Blob
                dbHelper.updateUserAvatar(userId, bitmap);

                Toast.makeText(getActivity(), "Avatar đã được thay đổi", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Lỗi khi chọn hình ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

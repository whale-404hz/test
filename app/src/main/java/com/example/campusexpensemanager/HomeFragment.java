package com.example.campusexpensemanager;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class HomeFragment extends Fragment {
    private TextView balanceTextView;
    private DatabaseHelper dbHelper;
    private EditText edtBalance;
    private Button btnSaveBalance;
    private TextView userIdTextView;
    private SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo các view
        edtBalance = view.findViewById(R.id.edtBalance);
        btnSaveBalance = view.findViewById(R.id.btnSaveBalance);
        balanceTextView = view.findViewById(R.id.textView); // Đảm bảo rằng bạn đã khai báo đúng ID trong XML
        userIdTextView = view.findViewById(R.id.userIdTextView);
        // Khởi tạo DatabaseHelper với Context
        dbHelper = new DatabaseHelper(requireContext());

        // Lấy user_id từ SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1); // Lấy user_id, giá trị mặc định là -1 nếu không có
        if (userId != -1) {
            // Lấy số dư từ bảng account theo user_id
            float balance = dbHelper.getAccountBalance(userId);

            // Hiển thị số dư trong TextView
            balanceTextView.setText("Balance: " + balance + " VND");
        } else {
            // Trường hợp không tìm thấy user_id
            balanceTextView.setText("User not logged in");
        }
        // Hiển thị user_id trong TextView
        if (userId != -1) {
            userIdTextView.setText("User ID: " + userId); // Hiển thị user_id
            // Lấy số dư từ bảng account theo user_id
            float balance = dbHelper.getAccountBalance(userId);
            // Hiển thị số dư trong TextView
            balanceTextView.setText("Balance: " + balance + " VND");
        } else {
            // Trường hợp không tìm thấy user_id
            userIdTextView.setText("User not logged in");
            balanceTextView.setText("User not logged in");
        }
        // Thiết lập hành động khi nhấn nút "Save Balance"
        btnSaveBalance.setOnClickListener(v -> {
            // Lấy giá trị từ EditText
            String balanceStr = edtBalance.getText().toString();

            if (!balanceStr.isEmpty()) {
                // Chuyển giá trị về dạng số
                float balance = Float.parseFloat(balanceStr);

                // Cập nhật balance vào cơ sở dữ liệu
                boolean isUpdated = dbHelper.updateAccountBalance(userId, balance);

                if (isUpdated) {
                    Toast.makeText(requireActivity(), "Account Balance updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireActivity(), "Failed to update Account Balance.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireActivity(), "Please enter a balance", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}

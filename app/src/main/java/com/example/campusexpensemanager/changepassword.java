package com.example.campusexpensemanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.helper.DatabaseHelper;

public class changepassword extends AppCompatActivity {

    private EditText etOldPassword, etNewPassword, etConfirmPassword,etUser;
    private Button btnChangePassword;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Nếu bạn dùng EdgeToEdge, hãy kiểm tra xem nó có hoạt động đúng hay không
        setContentView(R.layout.activity_changepassword);

        etOldPassword = findViewById(R.id.edtOldpass);
        etNewPassword = findViewById(R.id.edtNewpass);
        etConfirmPassword = findViewById(R.id.edtComfirm);
        btnChangePassword = findViewById(R.id.btnConfirm);
        etUser = findViewById(R.id.edtUsename); // Nếu bạn không cần trường này (do dùng SharedPreferences), bạn có thể bỏ qua

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Khởi tạo SharedPreferences để lấy thông tin người dùng hiện tại
        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);

        // Xử lý sự kiện khi bấm nút "Đổi mật khẩu"
        btnChangePassword.setOnClickListener(view -> changePassword());
    }

    // Phương thức xử lý đổi mật khẩu
    private void changePassword() {
        String username = etUser.getText().toString();
        String oldPassword = etOldPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Kiểm tra các trường có trống hay không
        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter complete information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp không
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Confirmation password does not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy mật khẩu hiện tại từ cơ sở dữ liệu và kiểm tra mật khẩu cũ
        String currentHashedPassword = dbHelper.getPasswordForUser(username);
        if (currentHashedPassword == null) {
            Toast.makeText(this, "Username does not exist!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dbHelper.hashPassword(oldPassword).equals(currentHashedPassword)) {
            Toast.makeText(this, "Old password is incorrect!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật mật khẩu mới
        boolean updateSuccess = dbHelper.updatePasswordForUser(username, newPassword);
        if (updateSuccess) {
            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
            finish(); // Kết thúc Activity sau khi đổi mật khẩu thành công
        } else {
            Toast.makeText(this, "Error changing password", Toast.LENGTH_SHORT).show();
        }
    }
}
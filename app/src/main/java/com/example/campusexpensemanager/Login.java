package com.example.campusexpensemanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.helper.DatabaseHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login extends AppCompatActivity {
    EditText etUsername, etPassword;
    ImageView Viewpass;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    Button btnLogin;
    TextView Register, ChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Khởi tạo các biến
        Viewpass = findViewById(R.id.viewpass);
        btnLogin = findViewById(R.id.btnlogin);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        Register = findViewById(R.id.register);
        ChangePassword = findViewById(R.id.changePassword);

        // Bật/tắt chế độ xem mật khẩu
        Viewpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Viewpass.getDrawable().getConstantState() == getResources().getDrawable(R.mipmap.viewpassword_foreground).getConstantState()) {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    Viewpass.setImageResource(R.mipmap.noviewpassword_foreground);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    Viewpass.setImageResource(R.mipmap.viewpassword_foreground);
                }
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        // Chuyển trang đổi mật khẩu
        ChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, changepassword.class);
                startActivity(intent);
            }
        });

        // Chuyển sang trang đăng ký
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, register.class);
                startActivity(intent);
            }
        });

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Xử lý sự kiện đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                int userId = checkLogin(username, password);
                if (userId != -1) {
                    // Lưu user_id vào SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.putInt("user_id", userId);
                    editor.apply(); // Lưu lại thay đổi

                    // Thông báo đăng nhập thành công
                    Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // Chuyển hướng người dùng đến màn hình chính
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Đóng màn hình đăng nhập
                } else {
                    Toast.makeText(Login.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Kiểm tra đăng nhập và trả về user_id nếu hợp lệ
    private int checkLogin(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Truy vấn lấy user_id và mật khẩu từ cơ sở dữ liệu
        String query = "SELECT user_id, password FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {
            try {
                // Lấy mật khẩu đã lưu trong cơ sở dữ liệu
                int passwordColumnIndex = cursor.getColumnIndexOrThrow("password");
                String storedHashedPassword = cursor.getString(passwordColumnIndex);

                // So sánh mật khẩu đã băm
                if (storedHashedPassword != null && storedHashedPassword.equals(hashPassword(password))) {
                    // Lấy user_id từ cơ sở dữ liệu
                    int userIdColumnIndex = cursor.getColumnIndexOrThrow("user_id");
                    int userId = cursor.getInt(userIdColumnIndex);
                    return userId; // Trả về user_id hợp lệ
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Lỗi: Không tìm thấy cột 'password' trong cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            } finally {
                cursor.close(); // Đảm bảo đóng Cursor
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return -1; // Trả về -1 nếu đăng nhập thất bại
    }

    // Hàm để băm mật khẩu (hash password) sử dụng SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}

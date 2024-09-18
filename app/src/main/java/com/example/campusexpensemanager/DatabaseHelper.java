package com.example.campusexpensemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "qlct.db"; // Tên cơ sở dữ liệu
    public static final int DATABASE_VERSION = 2; // Phiên bản cơ sở dữ liệu
    private final Context context;
    private String databasePath;
    private SQLiteDatabase database;

    // Bảng income
    private static final String TABLE_INCOME = "income";
    private static final String COLUMN_INCOME_DATE = "income_date";
    private static final String COLUMN_INCOME_AMOUNT = "amount";
    private static final String COLUMN_USER_ID = "user_id"; // Thêm user_id để xác định người dùng

    // Bảng users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_USER_ID_2 = "user_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    //bảng accounts
    private static final String TABLE_ACCOUNTS = "accounts";
    private static final String COLUMN_ACCOUNT_BALANCE = "account_balance"; // Định nghĩa COLUMN_ACCOUNT_BALANCE

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        databasePath = context.getDatabasePath(DATABASE_NAME).getPath();
        try {
            createDatabase();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Kiểm tra xem cơ sở dữ liệu đã tồn tại hay chưa
    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            String path = databasePath;
            checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // Cơ sở dữ liệu chưa tồn tại
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    // Sao chép cơ sở dữ liệu từ thư mục assets vào thư mục databases của ứng dụng
    private void copyDatabase() throws IOException {
        InputStream input = context.getAssets().open(DATABASE_NAME); // Đọc cơ sở dữ liệu từ assets
        String outFileName = databasePath; // Nơi cơ sở dữ liệu sẽ được lưu trữ

        File databaseDir = new File(databasePath).getParentFile();
        if (!databaseDir.exists()) {
            databaseDir.mkdirs();
        }

        OutputStream output = new FileOutputStream(outFileName); // Tạo luồng ghi vào database

        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        input.close();
    }

    // Tạo cơ sở dữ liệu nếu chưa tồn tại
    public void createDatabase() throws IOException {
        boolean dbExists = checkDatabase();
        if (!dbExists) {
            try {
                copyDatabase(); // Sao chép cơ sở dữ liệu từ thư mục assets nếu chưa tồn tại
            } catch (IOException e) {
                throw new Error("Lỗi khi sao chép cơ sở dữ liệu: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COLUMN_USER_ID_2 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT NOT NULL, "
                + COLUMN_EMAIL + " TEXT NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_INCOME_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_INCOME + " ("
                + "income_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER, " // Khóa ngoại user_id
                + COLUMN_INCOME_DATE + " TEXT NOT NULL, "
                + COLUMN_INCOME_AMOUNT + " REAL NOT NULL)";

        db.execSQL(CREATE_INCOME_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCOME);
        onCreate(db);
    }

    // Hàm băm mật khẩu bằng SHA-256
    public String hashPassword(String password) {
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
            throw new RuntimeException("Lỗi khi mã hóa mật khẩu", e);
        }
    }
    //
    public void updateUserProfileImage(int userId, String newAvatarPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("profile_image", newAvatarPath); // Giả định bạn có cột "profile_image"

        db.update("users", contentValues, "user_id = ?", new String[]{String.valueOf(userId)});
        db.close();
    }
    // Phương thức lấy mật khẩu băm hiện tại của người dùng bằng ID
    public String getPasswordForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = null;
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            hashedPassword = cursor.getString(0);
        }
        cursor.close();
        return hashedPassword;
    }
    // Cập nhật mật khẩu cho người dùng dựa trên tên người dùng
    public boolean updatePasswordForUser(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", hashPassword(newPassword)); // Băm mật khẩu trước khi lưu

        // Cập nhật mật khẩu cho user có tên username
        int rows = db.update("users", values, "username = ?", new String[]{username});
        return rows > 0;
    }

//
public boolean updateUserAvatar(int userId, String avatarPath) {
    // Mở kết nối đến cơ sở dữ liệu để cập nhật thông tin
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues contentValues = new ContentValues();
    contentValues.put("profile_image", avatarPath);

    // Cập nhật avatar theo userId trong bảng users
    int rows = db.update("users", contentValues, "user_id = ?", new String[]{String.valueOf(userId)});

    // Đóng kết nối cơ sở dữ liệu
    db.close();

    // Trả về true nếu ít nhất 1 hàng được cập nhật, ngược lại là false
    return rows > 0;
}

    // Xác thực đăng nhập của người dùng
    public int validateUserLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);  // Mã hóa mật khẩu đã nhập

        Cursor cursor = db.rawQuery("SELECT user_id FROM users WHERE username = ? AND password = ?",
                new String[]{username, hashedPassword});

        if (cursor != null && cursor.moveToFirst()) {
            int userIdColumnIndex = cursor.getColumnIndex("user_id");

            // Kiểm tra xem cột "user_id" có tồn tại hay không
            if (userIdColumnIndex != -1) {
                int userId = cursor.getInt(userIdColumnIndex); // Lấy user_id hợp lệ
                cursor.close();
                return userId; // Trả về user_id
            } else {
                Log.e("DB_ERROR", "Column 'user_id' not found in cursor");
            }

            cursor.close();
        }

        cursor.close();
        return -1;  // Trả về -1 nếu đăng nhập thất bại
    }
    // Phương thức lấy thông tin người dùng dựa trên userId
    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }

//    // Phương thức cập nhật ảnh đại diện người dùng dựa trên userId
//    public boolean updateUserAvatar(int userId, String profileImagePath) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("profile_image", profileImagePath); // Thêm đường dẫn ảnh vào cột profile_image
//
//        int rows = db.update("users", values, "user_id = ?", new String[]{String.valueOf(userId)});
//        return rows > 0;
//    }

    // Thêm người dùng vào cơ sở dữ liệu
    public boolean addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashPassword(password)); // Mã hóa mật khẩu trước khi lưu
//        values.put("account_balance", 0); // Đặt account_Balance về 0
        
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1; // Trả về true nếu thêm thành công
    }

    // Truy vấn dữ liệu thu nhập của người dùng dựa vào user_id
    public ArrayList<IncomeData> getIncomeData(int userId) {
        ArrayList<IncomeData> incomeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Chỉ truy vấn dữ liệu thuộc về người dùng đã đăng nhập
        Cursor cursor = db.rawQuery("SELECT income_date, amount FROM " + TABLE_INCOME + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                float amount = cursor.getFloat(1);
                incomeList.add(new IncomeData(date, amount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return incomeList;
    }

    // Cập nhật mật khẩu cho người dùng dựa trên ID người dùng
    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, hashPassword(newPassword)); // Mã hóa mật khẩu mới

        // Cập nhật mật khẩu cho người dùng có user_id tương ứng
        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_USER_ID_2 + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0; // Trả về true nếu cập nhật thành công
    }
    public float getAccountBalance(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn để lấy dữ liệu từ cột account_Balance theo user_id
        Cursor cursor = db.rawQuery("SELECT account_balance FROM accounts WHERE user_id = ?", new String[]{String.valueOf(userId)});

        float balance = 0;
        if (cursor.moveToFirst()) {
            balance = cursor.getFloat(0);  // Lấy giá trị account_Balance từ kết quả truy vấn
        }
        cursor.close();
        db.close();

        return balance;
    }
    // Phương thức để cập nhật giá trị account_balance
    public boolean updateAccountBalance(int userId, float newBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACCOUNT_BALANCE, newBalance);

        int result = db.update(TABLE_ACCOUNTS, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return result > 0; // Trả về true nếu cập nhật thành công
    }
    // Cập nhật avatar của người dùng dưới dạng Blob
    public void updateUserAvatar(int userId, Bitmap avatarBitmap) {
        SQLiteDatabase db = this.getWritableDatabase();
        byte[] avatarBytes = bitmapToByteArray(avatarBitmap);

        ContentValues values = new ContentValues();
        values.put("avatar", avatarBytes); // Lưu avatar dưới dạng Blob

        db.update("users", values, "user_id = ?", new String[]{String.valueOf(userId)});
        db.close();
    }


    // Chuyển đổi Bitmap thành mảng byte
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream); // Nén thành PNG
        return byteArrayOutputStream.toByteArray();
    }
}


// Lớp để lưu trữ dữ liệu thu nhập
class IncomeData {
    private String date;
    private float amount;

    public IncomeData(String date, float amount) {
        this.date = date;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public float getAmount() {
        return amount;
    }
}

package com.example.baitap1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

feature/chuc-nang-ghi-chu
    // --- CẤU HÌNH DATABASE ---
    public static final String DATABASE_NAME = "UserManager.db";
    // Quan trọng: Version phải là 2 (hoặc cao hơn 1) để cập nhật bảng mới
    public static final int DATABASE_VERSION = 2;

    // --- BẢNG 1: USERS (Dùng cho Đăng nhập/Đăng ký) ---
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "ID";
    public static final String COL_USERNAME = "USERNAME";
    public static final String COL_PASSWORD = "PASSWORD";

    // --- BẢNG 2: NOTES (Dùng cho Chức năng Ghi chú) ---
    public static final String TABLE_NOTES = "notes";
    public static final String COL_NOTE_ID = "ID";
    public static final String COL_NOTE_TITLE = "TITLE";
    public static final String COL_NOTE_CONTENT = "CONTENT";

    // Constructor
    public DatabaseHelper(Context context) {

    // ⭐ THAY ĐỔI 1: TĂNG VERSION DATABASE (QUAN TRỌNG) ⭐
    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "UserManager.db";

    // Bảng Users (code cũ)
    public static final String TABLE_NAME = "users";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "USERNAME";
    public static final String COL_3 = "PASSWORD";

    // ⭐ CODE MỚI: HẰNG SỐ CHO BẢNG CHI TIÊU ⭐
    public static final String EXPENSE_TABLE = "expenses";
    public static final String EXP_COL_1 = "ID"; // PRIMARY KEY
    public static final String EXP_COL_2 = "DESCRIPTION";
    public static final String EXP_COL_3 = "AMOUNT";
    public static final String EXP_COL_4 = "CATEGORY";


    public DatabaseHelper(Context context) {
        // Cập nhật version trong super()
main
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
 feature/chuc-nang-ghi-chu
        // 1. Tạo bảng Users
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createUsers);

        // 2. Tạo bảng Notes (MỚI)
        String createNotes = "CREATE TABLE " + TABLE_NOTES + " (" +
                COL_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NOTE_TITLE + " TEXT, " +
                COL_NOTE_CONTENT + " TEXT)";
        db.execSQL(createNotes);

        // Logic cũ: Tạo bảng Users
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_2 + " TEXT, " + COL_3 + " TEXT)");

        // ⭐ CODE MỚI: Thêm lệnh tạo bảng Chi Tiêu vào onCreate() ⭐
        db.execSQL("CREATE TABLE " + EXPENSE_TABLE + " (" +
                EXP_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXP_COL_2 + " TEXT, " +
                EXP_COL_3 + " REAL, " +
                EXP_COL_4 + " TEXT)");
 main
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 feature/chuc-nang-ghi-chu
        // Xóa bảng cũ nếu tồn tại để tạo lại cấu trúc mới
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ USER (ĐĂNG NHẬP/ĐĂNG KÝ)
    // ==========================================

        // ⭐ CODE MỚI: Logic onUpgrade để thêm bảng expenses nếu database cũ hơn version 2 ⭐
        if (oldVersion < 2) {
            // Thêm bảng expenses nếu nó chưa tồn tại
            db.execSQL("CREATE TABLE IF NOT EXISTS " + EXPENSE_TABLE + " (" +
                    EXP_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    EXP_COL_2 + " TEXT, " +
                    EXP_COL_3 + " REAL, " +
                    EXP_COL_4 + " TEXT)");
        }

        // Giữ lại logic cũ (DROP TABLE) cho bảng users nếu bạn vẫn muốn reset user khi nâng cấp
        // LƯU Ý: Nếu giữ dòng này, dữ liệu user sẽ bị mất khi nâng cấp
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // ⭐ CODE MỚI: Phương thức Thêm Chi Tiêu ⭐
    public boolean addExpense(String description, double amount, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(EXP_COL_2, description);
        contentValues.put(EXP_COL_3, amount);
        contentValues.put(EXP_COL_4, category);

        long result = db.insert(EXPENSE_TABLE, null, contentValues);
        return result != -1;
    }

    // ------------------------------------------------------------------
    // CÁC PHƯƠNG THỨC CŨ (GIỮ NGUYÊN)
    // ------------------------------------------------------------------
 main

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
 feature/chuc-nang-ghi-chu
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, contentValues);

        contentValues.put(COL_2, username);
        contentValues.put(COL_3, password);
        long result = db.insert(TABLE_NAME, null, contentValues);
 main
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
feature/chuc-nang-ghi-chu
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE USERNAME = ? AND PASSWORD = ?", new String[]{username, password});

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE USERNAME = ? AND PASSWORD = ?", new String[]{username, password});
main
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
 feature/chuc-nang-ghi-chu
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE USERNAME = ?", new String[]{username});

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE USERNAME = ?", new String[]{username});
main
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ GHI CHÚ (NOTES)
    // ==========================================

    // Thêm ghi chú mới
    public boolean addNote(String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NOTE_TITLE, title);
        contentValues.put(COL_NOTE_CONTENT, content);

        long result = db.insert(TABLE_NOTES, null, contentValues);
        return result != -1;
    }

    // Lấy tất cả ghi chú ra danh sách
    public Cursor getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Lấy dữ liệu mới nhất lên đầu (ORDER BY ID DESC)
        return db.rawQuery("SELECT * FROM " + TABLE_NOTES + " ORDER BY " + COL_NOTE_ID + " DESC", null);
    }
}
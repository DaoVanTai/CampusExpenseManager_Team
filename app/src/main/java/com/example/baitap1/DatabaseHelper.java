package com.example.baitap1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

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
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu tồn tại để tạo lại cấu trúc mới
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ USER (ĐĂNG NHẬP/ĐĂNG KÝ)
    // ==========================================

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE USERNAME = ? AND PASSWORD = ?", new String[]{username, password});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE USERNAME = ?", new String[]{username});
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
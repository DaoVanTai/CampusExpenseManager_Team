package com.example.baitap1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

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
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Logic cũ: Tạo bảng Users
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_2 + " TEXT, " + COL_3 + " TEXT)");

        // ⭐ CODE MỚI: Thêm lệnh tạo bảng Chi Tiêu vào onCreate() ⭐
        db.execSQL("CREATE TABLE " + EXPENSE_TABLE + " (" +
                EXP_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXP_COL_2 + " TEXT, " +
                EXP_COL_3 + " REAL, " +
                EXP_COL_4 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, username);
        contentValues.put(COL_3, password);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE USERNAME = ? AND PASSWORD = ?", new String[]{username, password});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE USERNAME = ?", new String[]{username});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }
}
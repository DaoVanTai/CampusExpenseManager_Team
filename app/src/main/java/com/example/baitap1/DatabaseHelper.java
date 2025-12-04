package com.example.baitap1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // ⭐ TĂNG VERSION LÊN 3 ĐỂ CẬP NHẬT TÍNH NĂNG MỚI ⭐
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "UserManager.db";

    // --- BẢNG USERS (GIỮ NGUYÊN) ---
    public static final String TABLE_NAME = "users";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "USERNAME";
    public static final String COL_3 = "PASSWORD";

    // --- BẢNG EXPENSES (CHI TIÊU) ---
    public static final String EXPENSE_TABLE = "expenses";
    public static final String EXP_COL_1 = "ID";
    public static final String EXP_COL_2 = "DESCRIPTION";
    public static final String EXP_COL_3 = "AMOUNT";
    public static final String EXP_COL_4 = "CATEGORY";
    // Thêm cột ngày tháng để thống kê
    public static final String EXP_COL_5_DATE = "DATE";

    // --- BẢNG RECURRING (ĐỊNH KỲ - MỚI) ---
    public static final String TABLE_RECURRING = "recurring_expenses";
    public static final String REC_COL_ID = "ID";
    public static final String REC_COL_DESC = "DESCRIPTION";
    public static final String REC_COL_AMOUNT = "AMOUNT";
    public static final String REC_COL_CATEGORY = "CATEGORY";
    public static final String REC_COL_DAY = "DAY_OF_MONTH";
    public static final String REC_COL_LAST_PAID = "LAST_PAID_MONTH";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Users
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_2 + " TEXT, " + COL_3 + " TEXT)");

        // Tạo bảng Expenses (Có thêm cột Date)
        db.execSQL("CREATE TABLE " + EXPENSE_TABLE + " (" +
                EXP_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXP_COL_2 + " TEXT, " +
                EXP_COL_3 + " REAL, " +
                EXP_COL_4 + " TEXT, " +
                EXP_COL_5_DATE + " TEXT)");

        // Tạo bảng Recurring
        db.execSQL("CREATE TABLE " + TABLE_RECURRING + " (" +
                REC_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                REC_COL_DESC + " TEXT, " +
                REC_COL_AMOUNT + " REAL, " +
                REC_COL_CATEGORY + " TEXT, " +
                REC_COL_DAY + " INTEGER, " +
                REC_COL_LAST_PAID + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Code cũ của bạn
            db.execSQL("CREATE TABLE IF NOT EXISTS " + EXPENSE_TABLE + " (" +
                    EXP_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    EXP_COL_2 + " TEXT, " +
                    EXP_COL_3 + " REAL, " +
                    EXP_COL_4 + " TEXT)");
        }

        // ⭐ NÂNG CẤP LÊN V3: Thêm cột Date và bảng Recurring ⭐
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + EXPENSE_TABLE + " ADD COLUMN " + EXP_COL_5_DATE + " TEXT");
            } catch (Exception e) {} // Bỏ qua nếu cột đã tồn tại

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECURRING + " (" +
                    REC_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    REC_COL_DESC + " TEXT, " +
                    REC_COL_AMOUNT + " REAL, " +
                    REC_COL_CATEGORY + " TEXT, " +
                    REC_COL_DAY + " INTEGER, " +
                    REC_COL_LAST_PAID + " TEXT)");
        }
    }

    // --- CÁC HÀM XỬ LÝ MỚI ---

    // Thêm chi tiêu (Có ngày tháng)
    public boolean addExpense(String description, double amount, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EXP_COL_2, description);
        contentValues.put(EXP_COL_3, amount);
        contentValues.put(EXP_COL_4, category);
        contentValues.put(EXP_COL_5_DATE, date);
        long result = db.insert(EXPENSE_TABLE, null, contentValues);
        return result != -1;
    }

    // Thêm chi tiêu (Giữ hàm cũ của bạn để không lỗi code cũ, tự động lấy ngày hiện tại)
    public boolean addExpense(String description, double amount, String category) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return addExpense(description, amount, category, date);
    }

    // Lấy tổng chi tiêu trong tháng (Vd: "2023-10")
    public double getMonthlyTotal(String yearMonth) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(" + EXP_COL_3 + ") FROM " + EXPENSE_TABLE +
                " WHERE " + EXP_COL_5_DATE + " LIKE ?", new String[]{yearMonth + "%"});
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // Lấy danh sách phân loại (Category Breakdown)
    public Cursor getCategoryBreakdown(String yearMonth) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + EXP_COL_4 + ", SUM(" + EXP_COL_3 + ") FROM " + EXPENSE_TABLE +
                " WHERE " + EXP_COL_5_DATE + " LIKE ? GROUP BY " + EXP_COL_4, new String[]{yearMonth + "%"});
    }

    // Thêm khoản định kỳ
    public boolean addRecurring(String desc, double amount, String category, int day) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(REC_COL_DESC, desc);
        values.put(REC_COL_AMOUNT, amount);
        values.put(REC_COL_CATEGORY, category);
        values.put(REC_COL_DAY, day);
        values.put(REC_COL_LAST_PAID, "");
        return db.insert(TABLE_RECURRING, null, values) != -1;
    }

    // Tự động quét và thêm expense
    public void checkAndAddRecurring() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECURRING, null);

        Date now = new Date();
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now);
        int currentDay = Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(now));

        if (cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(REC_COL_ID));
                    int dayPay = cursor.getInt(cursor.getColumnIndexOrThrow(REC_COL_DAY));
                    String lastPaid = cursor.getString(cursor.getColumnIndexOrThrow(REC_COL_LAST_PAID));

                    if (!currentMonth.equals(lastPaid) && currentDay >= dayPay) {
                        String desc = cursor.getString(cursor.getColumnIndexOrThrow(REC_COL_DESC));
                        double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(REC_COL_AMOUNT));
                        String cat = cursor.getString(cursor.getColumnIndexOrThrow(REC_COL_CATEGORY));

                        addExpense(desc + " (Auto)", amount, cat, today);

                        ContentValues values = new ContentValues();
                        values.put(REC_COL_LAST_PAID, currentMonth);
                        db.update(TABLE_RECURRING, values, REC_COL_ID + "=?", new String[]{String.valueOf(id)});
                    }
                } catch (Exception e) {}
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    // --- CÁC HÀM CŨ (GIỮ NGUYÊN) ---
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
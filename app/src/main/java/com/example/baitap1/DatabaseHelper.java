package com.example.baitap1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "UserManager.db";

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "ID";
    public static final String COL_USERNAME = "USERNAME";
    public static final String COL_PASSWORD = "PASSWORD";

    public static final String TABLE_EXPENSES = "expenses";
    public static final String EXP_COL_ID = "ID";
    public static final String EXP_COL_USER_ID = "USER_ID";
    public static final String EXP_COL_DESCRIPTION = "DESCRIPTION";
    public static final String EXP_COL_QUANTITY = "QUANTITY";
    public static final String EXP_COL_UNIT_PRICE = "UNIT_PRICE";
    public static final String EXP_COL_CATEGORY = "CATEGORY";
    public static final String EXP_COL_DATE = "DATE";
    public static final String EXP_COL_RECEIPT_PATH = "RECEIPT_PATH";

    public static final String TABLE_CAT_LIMITS = "category_limits";
    public static final String CAT_COL_NAME = "CATEGORY_NAME";
    public static final String CAT_COL_LIMIT = "LIMIT_AMOUNT";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_USERNAME + " TEXT, " + COL_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + " (" + EXP_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + EXP_COL_USER_ID + " INTEGER, " + EXP_COL_DESCRIPTION + " TEXT, " + EXP_COL_QUANTITY + " INTEGER, " + EXP_COL_UNIT_PRICE + " INTEGER, " + EXP_COL_CATEGORY + " TEXT, " + EXP_COL_DATE + " TEXT, " + EXP_COL_RECEIPT_PATH + " TEXT DEFAULT NULL, " + "FOREIGN KEY(" + EXP_COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" + ")";
        db.execSQL(CREATE_EXPENSES_TABLE);

        String CREATE_CAT_LIMITS_TABLE = "CREATE TABLE " + TABLE_CAT_LIMITS + " (" + CAT_COL_NAME + " TEXT PRIMARY KEY, " + CAT_COL_LIMIT + " INTEGER" + ")";
        db.execSQL(CREATE_CAT_LIMITS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            String CREATE_CAT_LIMITS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CAT_LIMITS + " (" + CAT_COL_NAME + " TEXT PRIMARY KEY, " + CAT_COL_LIMIT + " INTEGER" + ")";
            db.execSQL(CREATE_CAT_LIMITS_TABLE);
        }
    }

    // --- EXPENSES ---
    public boolean addExpense(long userId, String description, int quantity, long unitPrice, String category, String date, String receiptPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EXP_COL_USER_ID, userId);
        contentValues.put(EXP_COL_DESCRIPTION, description);
        contentValues.put(EXP_COL_QUANTITY, quantity);
        contentValues.put(EXP_COL_UNIT_PRICE, unitPrice);
        contentValues.put(EXP_COL_CATEGORY, category);
        contentValues.put(EXP_COL_DATE, date);
        contentValues.put(EXP_COL_RECEIPT_PATH, receiptPath);
        long result = db.insert(TABLE_EXPENSES, null, contentValues);
        db.close();
        return result != -1;
    }

    public List<Expense> getAllExpenses() {
        List<Expense> expenseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " ORDER BY " + EXP_COL_DATE + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(EXP_COL_ID));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_DESCRIPTION));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(EXP_COL_QUANTITY));
                long price = cursor.getLong(cursor.getColumnIndexOrThrow(EXP_COL_UNIT_PRICE));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_CATEGORY));
                // ⭐ Đọc thêm ngày
                String date = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_DATE));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_RECEIPT_PATH));

                // Truyền date vào constructor
                expenseList.add(new Expense(id, desc, qty, price, cat, date, path));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenseList;
    }

    public Expense getExpenseById(int expenseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Expense expense = null;
        Cursor cursor = db.query(TABLE_EXPENSES, null, EXP_COL_ID + " = ?", new String[]{String.valueOf(expenseId)}, null, null, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(EXP_COL_ID));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_DESCRIPTION));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(EXP_COL_QUANTITY));
            long price = cursor.getLong(cursor.getColumnIndexOrThrow(EXP_COL_UNIT_PRICE));
            String cat = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_CATEGORY));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_DATE)); // ⭐
            String path = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_RECEIPT_PATH));
            expense = new Expense(id, desc, qty, price, cat, date, path);
        }
        cursor.close();
        db.close();
        return expense;
    }

    // ⭐ CẬP NHẬT HÀM UPDATE ĐỂ NHẬN THÊM DATE ⭐
    public boolean updateExpense(int id, String description, int quantity, long unitPrice, String category, String date, String receiptPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EXP_COL_DESCRIPTION, description);
        contentValues.put(EXP_COL_QUANTITY, quantity);
        contentValues.put(EXP_COL_UNIT_PRICE, unitPrice);
        contentValues.put(EXP_COL_CATEGORY, category);
        contentValues.put(EXP_COL_DATE, date); // ⭐ Update ngày
        contentValues.put(EXP_COL_RECEIPT_PATH, receiptPath);
        int result = db.update(TABLE_EXPENSES, contentValues, EXP_COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public List<Expense> searchExpenses(String keyword) {
        List<Expense> resultList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EXPENSES + " WHERE " + EXP_COL_DESCRIPTION + " LIKE ? OR " + EXP_COL_CATEGORY + " LIKE ? ORDER BY " + EXP_COL_DATE + " DESC";
        String searchPattern = "%" + keyword + "%";
        Cursor cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(EXP_COL_ID));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_DESCRIPTION));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(EXP_COL_QUANTITY));
                long price = cursor.getLong(cursor.getColumnIndexOrThrow(EXP_COL_UNIT_PRICE));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_DATE)); // ⭐
                String path = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_RECEIPT_PATH));
                resultList.add(new Expense(id, desc, qty, price, cat, date, path));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return resultList;
    }

    // --- CATEGORY LIMITS ---
    public boolean setCategoryLimit(String category, long limit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CAT_COL_NAME, category);
        values.put(CAT_COL_LIMIT, limit);
        long result = db.replace(TABLE_CAT_LIMITS, null, values);
        db.close();
        return result != -1;
    }

    public long getCategoryLimit(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        long limit = 0;
        Cursor cursor = db.query(TABLE_CAT_LIMITS, new String[]{CAT_COL_LIMIT}, CAT_COL_NAME + " = ?", new String[]{category}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            limit = cursor.getLong(cursor.getColumnIndexOrThrow(CAT_COL_LIMIT));
            cursor.close();
        }
        db.close();
        return limit;
    }

    public long getTotalSpentByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        long total = 0;
        String query = "SELECT SUM(" + EXP_COL_QUANTITY + " * " + EXP_COL_UNIT_PRICE + ") FROM " + TABLE_EXPENSES + " WHERE " + EXP_COL_CATEGORY + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{category});
        if (cursor.moveToFirst()) total = cursor.getLong(0);
        cursor.close();
        db.close();
        return total;
    }

    // --- USER (Giữ nguyên) ---
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, contentValues);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?", new String[]{username, password});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public long getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;
        Cursor cursor = db.rawQuery("SELECT " + COL_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?", new String[]{username});
        if (cursor.moveToFirst()) userId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID));
        cursor.close();
        return userId;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?", new String[]{username});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }
}
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

    // THÔNG SỐ CƠ SỞ DỮ LIỆU
    public static final int DATABASE_VERSION = 4; // Tăng version để chứa ReceiptPath
    public static final String DATABASE_NAME = "UserManager.db";

    // BẢNG 1: USERS (Giữ lại định nghĩa cột)
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "ID";
    public static final String COL_USERNAME = "USERNAME";
    public static final String COL_PASSWORD = "PASSWORD";

    // BẢNG 2: EXPENSES (Chi tiêu)
    public static final String TABLE_EXPENSES = "expenses";
    public static final String EXP_COL_ID = "ID";
    public static final String EXP_COL_USER_ID = "USER_ID"; // Giữ lại để liên kết
    public static final String EXP_COL_DESCRIPTION = "DESCRIPTION";
    public static final String EXP_COL_QUANTITY = "QUANTITY";
    public static final String EXP_COL_UNIT_PRICE = "UNIT_PRICE";
    public static final String EXP_COL_CATEGORY = "CATEGORY";
    public static final String EXP_COL_DATE = "DATE";
    public static final String EXP_COL_RECEIPT_PATH = "RECEIPT_PATH"; // ⭐ CỘT MỚI: ĐƯỜNG DẪN BIÊN LAI ⭐

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TẠO BẢNG USERS
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT, "
                + COL_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // TẠO BẢNG EXPENSES VỚI CẤU TRÚC CUỐI CÙNG
        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + " ("
                + EXP_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EXP_COL_USER_ID + " INTEGER, "
                + EXP_COL_DESCRIPTION + " TEXT, "
                + EXP_COL_QUANTITY + " INTEGER, "
                + EXP_COL_UNIT_PRICE + " INTEGER, " // Giữ là INTEGER như bạn code
                + EXP_COL_CATEGORY + " TEXT, "
                + EXP_COL_DATE + " TEXT, "
                + EXP_COL_RECEIPT_PATH + " TEXT DEFAULT NULL, " // ⭐ CỘT BIÊN LAI ⭐
                + "FOREIGN KEY(" + EXP_COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" + ")";
        db.execSQL(CREATE_EXPENSES_TABLE);

        Log.d("DatabaseHelper", "Đã tạo các bảng Users và Expenses.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Tùy chỉnh: Nếu nâng cấp từ version cũ, thêm cột RECEIPT_PATH
        if (oldVersion < 4) {
            // Thao tác này chỉ cần thiết nếu bạn muốn giữ lại dữ liệu cũ,
            // nhưng theo code bạn cung cấp, bạn đang DROP TABLE.

            // Theo logic cũ của bạn (Xóa tất cả):
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
            onCreate(db);
        }
        Log.w("DatabaseHelper", "Nâng cấp cơ sở dữ liệu. ĐÃ XÓA TẤT CẢ DỮ LIỆU.");
    }

    // --- PHƯƠNG THỨC XỬ LÝ EXPENSE (YC Chính) ---

    /**
     * Thêm một mục chi tiêu mới bao gồm đường dẫn biên lai.
     * @param userId: ID người dùng (Nếu không dùng, truyền 0 hoặc -1)
     */
    public boolean addExpense(long userId, String description, int quantity, long unitPrice, String category, String date, String receiptPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(EXP_COL_USER_ID, userId);
        contentValues.put(EXP_COL_DESCRIPTION, description);
        contentValues.put(EXP_COL_QUANTITY, quantity);
        contentValues.put(EXP_COL_UNIT_PRICE, unitPrice);
        contentValues.put(EXP_COL_CATEGORY, category);
        contentValues.put(EXP_COL_DATE, date);
        contentValues.put(EXP_COL_RECEIPT_PATH, receiptPath); // ⭐ LƯU ĐƯỜNG DẪN ẢNH ⭐

        long result = db.insert(TABLE_EXPENSES, null, contentValues);
        db.close();
        return result != -1;
    }

    // Phương thức lấy tất cả chi tiêu dưới dạng List<Expense>
    public List<Expense> getAllExpenses() {
        List<Expense> expenseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " ORDER BY " + EXP_COL_DATE + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                // Đảm bảo chỉ mục cột là chính xác
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(EXP_COL_ID));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_DESCRIPTION));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(EXP_COL_QUANTITY));
                long price = cursor.getLong(cursor.getColumnIndexOrThrow(EXP_COL_UNIT_PRICE));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_CATEGORY));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(EXP_COL_RECEIPT_PATH)); // Lấy đường dẫn

                // Bỏ qua date và userId cho Expense Model đơn giản này

                // Tạo đối tượng Expense (Sử dụng constructor mới)
                Expense expense = new Expense(desc, qty, price, cat);
                expenseList.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenseList;
    }


    // --- PHƯƠNG THỨC XỬ LÝ USER (Giữ nguyên cho tính toàn vẹn) ---

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

    // (Bổ sung) Lấy ID người dùng (Cần thiết cho FK)
    public long getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;

        Cursor cursor = db.rawQuery("SELECT " + COL_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?", new String[]{username});

        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID));
        }
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
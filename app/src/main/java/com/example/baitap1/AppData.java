package com.example.baitap1;

import android.content.Context; // Cần thiết để khởi tạo DatabaseHelper
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppData {

    private static AppData instance;

    // ⭐ BIẾN MỚI: Database Helper ⭐
    private DatabaseHelper dbHelper;
    private Context context;

    public ArrayList<Expense> taskList;
    public ArrayList<RecurringExpense> recurringList;
    public static final int REQUEST_EDIT_TASK = 1;

    /**
     * Constructor phải nhận Context để khởi tạo DatabaseHelper.
     */
    private AppData(Context context) {
        this.context = context;
        taskList = new ArrayList<>();
        recurringList = new ArrayList<>();

        // ⭐ KHỞI TẠO DATABASE HELPER ⭐
        dbHelper = new DatabaseHelper(context);

        // Tải dữ liệu từ DB (thay thế dữ liệu mẫu)
        loadTasksFromDatabase();

        // Thêm mẫu định kỳ
        recurringList.add(new RecurringExpense("Tiền nhà", 5000000, "Thuê nhà", "01/01/2025", "31/12/2025"));
    }

    /**
     * Phương thức GET INSTANCE MỚI: Cần Context để khởi tạo lần đầu.
     */
    public static synchronized AppData getInstance(Context context) {
        if (instance == null) {
            // Sử dụng applicationContext để tránh memory leak
            instance = new AppData(context.getApplicationContext());
        }
        return instance;
    }

    // Giữ lại getInstance() cũ cho tính tương thích
    public static synchronized AppData getInstance() {
        if (instance == null) {
            Log.e("AppData", "AppData.getInstance() được gọi mà không có Context. Đã xảy ra lỗi.");
        }
        return instance;
    }

    // ⭐ PHƯƠNG THỨC QUAN TRỌNG: TẢI DỮ LIỆU TỪ DB VÀO taskList ⭐
    public void loadTasksFromDatabase() {
        taskList.clear(); // Xóa dữ liệu cũ trong bộ nhớ

        // Lấy dữ liệu từ SQLite
        List<Expense> expensesFromDB = dbHelper.getAllExpenses();
        taskList.addAll(expensesFromDB);

        Log.d("AppData", "Đã tải " + taskList.size() + " chi tiêu từ DB.");
    }

    // --- LOGIC TỰ ĐỘNG THÊM CHI TIÊU ĐỊNH KỲ (Giữ nguyên) ---
    public void checkAndAddRecurringExpenses() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar todayCal = Calendar.getInstance();
        Date today = removeTime(todayCal.getTime());
        int currentDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH);

        for (RecurringExpense item : recurringList) {
            try {
                Date start = removeTime(sdf.parse(item.getStartDate()));
                Date end = removeTime(sdf.parse(item.getEndDate()));

                if (!today.before(start) && !today.after(end)) {
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(start);
                    int dueDay = startCal.get(Calendar.DAY_OF_MONTH);

                    if (currentDayOfMonth == dueDay) {
                        boolean exists = false;
                        for (Expense ex : taskList) {
                            if (ex.getDescription().equals(item.getName()) && ex.getAmount() == item.getAmount()) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            // Cần đảm bảo constructor 4 tham số này vẫn tồn tại trong Expense.java
                            taskList.add(new Expense(item.getName(), 1, item.getAmount(), item.getCategory()));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
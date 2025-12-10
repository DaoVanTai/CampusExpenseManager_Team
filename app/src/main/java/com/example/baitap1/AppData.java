package com.example.baitap1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppData {
    private static final AppData instance = new AppData();

    public ArrayList<Expense> taskList;
    // MỚI: Danh sách quy tắc định kỳ
    public ArrayList<RecurringExpense> recurringList;

    public static final int REQUEST_EDIT_TASK = 1;

    private AppData() {
        taskList = new ArrayList<>();
        recurringList = new ArrayList<>();

        // Dữ liệu mẫu
        taskList.add(new Expense("Sản Phẩm 1", 1, 10000, "Thực phẩm"));
        taskList.add(new Expense("Sản Phẩm 2", 2, 25000, "Di chuyển"));

        // Mẫu định kỳ: Tiền nhà từ 01/01/2025 đến 31/12/2025
        recurringList.add(new RecurringExpense("Tiền nhà", 5000000, "Thuê nhà", "01/01/2025", "31/12/2025"));
    }

    public static AppData getInstance() {
        return instance;
    }

    // --- LOGIC TỰ ĐỘNG THÊM CHI TIÊU ĐỊNH KỲ ---
    public void checkAndAddRecurringExpenses() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar todayCal = Calendar.getInstance();
        Date today = removeTime(todayCal.getTime());
        int currentDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH);

        for (RecurringExpense item : recurringList) {
            try {
                Date start = removeTime(sdf.parse(item.getStartDate()));
                Date end = removeTime(sdf.parse(item.getEndDate()));

                // 1. Kiểm tra ngày hiện tại có nằm trong khoảng Start - End không
                if (!today.before(start) && !today.after(end)) {

                    // 2. Logic: Nếu hôm nay trùng với ngày (day) của StartDate thì thêm
                    // (Ví dụ: Start là 15/05/2024 -> Cứ ngày 15 hàng tháng sẽ thêm)
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(start);
                    int dueDay = startCal.get(Calendar.DAY_OF_MONTH);

                    if (currentDayOfMonth == dueDay) {
                        // 3. Kiểm tra trùng lặp (nếu đã thêm hôm nay rồi thì thôi)
                        boolean exists = false;
                        for (Expense ex : taskList) {
                            // So sánh Tên và Số tiền để tránh thêm lại
                            if (ex.getDescription().equals(item.getName()) && ex.getAmount() == item.getAmount()) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
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
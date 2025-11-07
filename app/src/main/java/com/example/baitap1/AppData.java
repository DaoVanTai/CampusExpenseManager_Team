package com.example.baitap1;
import java.util.ArrayList;
public class AppData {
    private static final AppData instance = new AppData();
    public ArrayList<String> taskList;
    public static final int REQUEST_EDIT_TASK = 1;
    private AppData() {
        taskList = new ArrayList<>();
        taskList.add("Tên: Sản Phẩm 1 - SL: 1 - Giá: 10000");
        taskList.add("Tên: Sản Phẩm 2 - SL: 2 - Giá: 25000");
    }
    public static AppData getInstance() {
        return instance;
    }
}
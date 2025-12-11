package com.example.baitap1;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    // Hàm mã hóa mật khẩu sang chuẩn SHA-256
    public static String hashPassword(String password) {
        try {
            // Tạo đối tượng MessageDigest sử dụng thuật toán SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Chuyển mật khẩu sang mảng byte và băm (hash)
            byte[] encodedhash = digest.digest(password.getBytes());

            // Chuyển đổi mảng byte thành chuỗi Hex (thập lục phân) để lưu vào DB/Prefs
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
package com.example.baitap1;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import java.util.Locale;

public class SpeechRecognizerHelper {

    // Mã yêu cầu (Request Code) để xác định kết quả quay lại Activity
    public static final int SPEECH_REQUEST_CODE = 1001;
    private final Context context;

    public SpeechRecognizerHelper(Context context) {
        // Context này PHẢI là một Activity để có thể gọi startActivityForResult
        this.context = context;
    }

    /**
     * Khởi động giao diện nhận dạng giọng nói của hệ thống Android.
     */
    public void startListening() {
        // Tạo Intent để gọi dịch vụ nhận dạng giọng nói của Google
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Đặt mô hình ngôn ngữ: Cho phép nhận dạng tự do (từ khóa và câu)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Đặt ngôn ngữ nhận dạng là Tiếng Việt (hoặc ngôn ngữ mặc định của thiết bị)
        // Thay đổi sang Locale.forLanguageTag("vi-VN") nếu bạn muốn bắt buộc là Tiếng Việt
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Tin nhắn gợi ý trên pop-up
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hãy nói chi tiêu theo cú pháp: Mô tả Số tiền Danh mục");

        try {
            // Khởi động Activity để nhận kết quả
            // CHỈ có thể gọi startActivityForResult nếu context là một Activity
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).startActivityForResult(intent, SPEECH_REQUEST_CODE);
            } else {
                Toast.makeText(context, "Lỗi: Phải khởi động từ ngữ cảnh Activity.", Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException e) {
            // Trường hợp thiết bị không hỗ trợ Google Speech Recognition
            Toast.makeText(context, "Thiết bị không hỗ trợ tính năng nhận dạng giọng nói.", Toast.LENGTH_LONG).show();
        }
    }
}

package com.example.baitap1;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class NoteListActivity extends AppCompatActivity {

    ListView listView;
    Button btnAdd;
    DatabaseHelper db;
    ArrayList<Note> noteList;
    ArrayAdapter<Note> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        db = new DatabaseHelper(this);
        listView = findViewById(R.id.listViewNotes);
        btnAdd = findViewById(R.id.buttonAddNote);
        noteList = new ArrayList<>();

        // Nút thêm mới
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoteListActivity.this, addNoteActivity.class);
                startActivity(intent);
            }
        });
    }

    // Hàm này chạy mỗi khi màn hình hiện lên (để load lại dữ liệu mới)
    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        noteList.clear();
        Cursor cursor = db.getAllNotes();
        if (cursor.getCount() == 0) {
            return;
        }
        while (cursor.moveToNext()) {
            // Cột 1 là Title, Cột 2 là Content
            noteList.add(new Note(cursor.getString(1), cursor.getString(2)));
        }

        // Tạo Adapter tùy chỉnh để hiển thị item_note.xml
        adapter = new ArrayAdapter<Note>(this, R.layout.item_note, noteList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_note, parent, false);
                }
                Note currentNote = getItem(position);

                TextView txtTitle = convertView.findViewById(R.id.textViewTitle);
                TextView txtContent = convertView.findViewById(R.id.textViewContent);

                txtTitle.setText(currentNote.getTitle());
                txtContent.setText(currentNote.getContent());

                return convertView;
            }
        };
        listView.setAdapter(adapter);
    }
}
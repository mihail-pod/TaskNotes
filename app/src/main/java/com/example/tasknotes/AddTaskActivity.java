package com.example.tasknotes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;

public class AddTaskActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "extra_task_id";

    private EditText etTitle, etDesc;
    private CheckBox cbDone;
    private Spinner spKind;
    private Button btnSave;
    private DBHelper dbHelper;
    private long editingTaskId = -1;
    private static final String TAG = "AddTaskActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task_dark);

        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        cbDone = findViewById(R.id.cbDoneInForm);
        spKind = findViewById(R.id.spKind);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = new DBHelper(this);

        ArrayAdapter<String> arr = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Заметка", "Задача"});
        arr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKind.setAdapter(arr);


        int preset = getIntent().getIntExtra("preset_kind", -1);
        if (preset >= 0) {
            spKind.setSelection(preset);
            spKind.setVisibility(View.GONE);
        }

        if (getIntent() != null && getIntent().hasExtra(EXTRA_TASK_ID)) {
            editingTaskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
            if (editingTaskId != -1) loadTaskForEditing(editingTaskId);
        }

        if (savedInstanceState != null) {
            etTitle.setText(savedInstanceState.getString("title", ""));
            etDesc.setText(savedInstanceState.getString("desc", ""));
            cbDone.setChecked(savedInstanceState.getBoolean("done", false));
            spKind.setSelection(savedInstanceState.getInt("kind", spKind.getSelectedItemPosition()));
        }

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            boolean done = cbDone.isChecked();
            int kind = spKind.getSelectedItemPosition();

            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Заголовок обязателен");
                return;
            }

            try {
                if (editingTaskId == -1) {
                    Task t = new Task(title, desc, done, kind);
                    long id = dbHelper.addTask(t);
                    if (id != -1) {
                        Toast.makeText(this, "Добавлено", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Task t = new Task(editingTaskId, title, desc, done, 0, kind);
                    boolean ok = dbHelper.updateTask(t);
                    if (ok) {
                        Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "save error", e);
                Toast.makeText(this, "Исключение при работе с БД", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTaskForEditing(long id) {
        try {
            Task t = dbHelper.getTaskById(id);
            if (t != null) {
                etTitle.setText(t.getTitle());
                etDesc.setText(t.getDescription());
                cbDone.setChecked(t.isDone());
                spKind.setSelection(t.getKind());
            } else {
                Toast.makeText(this, "Запись не найдена", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "loadTaskForEditing error", e);
            Toast.makeText(this, "Ошибка при загрузке", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        out.putString("title", etTitle.getText().toString());
        out.putString("desc", etDesc.getText().toString());
        out.putBoolean("done", cbDone.isChecked());
        out.putInt("kind", spKind.getSelectedItemPosition());
        super.onSaveInstanceState(out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}

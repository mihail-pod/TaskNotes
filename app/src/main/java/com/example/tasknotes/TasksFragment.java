package com.example.tasknotes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;


public class TasksFragment extends Fragment {
    private DBHelper db;
    private ListView lv;
    private TaskCardAdapter adapter;
    private EditText etSearch;
    private ImageView ivFilter;

    private boolean filterCompletedFirst = false;
    private boolean dateDesc = true;
    private String searchQuery = null;

    public TasksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tasks_dark, container, false);

        db = new DBHelper(getContext());
        lv = root.findViewById(R.id.lvTasks);
        etSearch = root.findViewById(R.id.etSearchTasks);
        ivFilter = root.findViewById(R.id.ivFilterTasks);

        etSearch.setHint("Поиск задач");

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = (s != null && s.toString().trim().length() > 0) ? s.toString().trim() : null;
                loadTasks();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ivFilter.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 1, 0, filterCompletedFirst ? "★ Выполненные сверху (вкл)" : "Выполненные сверху");
            popup.getMenu().add(0, 2, 1, dateDesc ? "Новые сверху (вкл)" : "Новые сверху");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    filterCompletedFirst = !filterCompletedFirst;
                    loadTasks();
                    return true;
                }
                if (item.getItemId() == 2) {
                    dateDesc = !dateDesc;
                    loadTasks();
                    return true;
                }
                return false;
            });
            popup.show();
        });


        lv.setOnItemClickListener((parent, view, position, id) -> {
            Object obj = parent.getItemAtPosition(position);
            if (!(obj instanceof Task)) return;
            Task t = (Task) obj;
            Intent i = new Intent(getContext(), AddTaskActivity.class);
            i.putExtra(AddTaskActivity.EXTRA_TASK_ID, t.getId());
            i.putExtra("preset_kind", 1);
            startActivity(i);
        });

        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            Object obj = parent.getItemAtPosition(position);
            if (!(obj instanceof Task)) return true;
            final Task t = (Task) obj;
            new AlertDialog.Builder(requireContext())
                    .setTitle("Удалить задачу")
                    .setMessage("Удалить \"" + t.getTitle() + "\"?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        try {
                            boolean ok = db.deleteTask(t.getId());
                            if (ok) {
                                loadTasks();
                                Toast.makeText(getContext(), "Задача удалена", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Исключение при удалении", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });

        loadTasks();
        return root;
    }


    private void loadTasks() {
        try {
            List<Task> tasks = db.getAllTasksOnly(searchQuery, filterCompletedFirst, dateDesc);
            if (tasks == null) tasks = new ArrayList<>();
            if (adapter == null) {
                adapter = new TaskCardAdapter(getContext(), tasks, db);
                lv.setAdapter(adapter);
            } else {
                adapter.clear();
                adapter.addAll(tasks);
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при загрузке задач", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) db.close();
    }
}

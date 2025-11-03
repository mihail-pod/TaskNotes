package com.example.tasknotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskCardAdapter extends ArrayAdapter<Task> {
    private final DBHelper db;
    private final Set<Long> expandedIds = new HashSet<>();

    public TaskCardAdapter(Context ctx, List<Task> data, DBHelper db) {
        super(ctx, 0, data != null ? data : new ArrayList<>());
        this.db = db;
    }

    private static class VH {
        CheckBox cbMain;
        TextView tvTitle;
        TextView tvCount;
        ImageView ivToggle;
        LinearLayout container;
    }

    private static class LocalSubtask {
        String text;
        boolean done;
        LocalSubtask(String t, boolean d) { text = t; done = d; }
    }

    private List<LocalSubtask> parseSubtasks(String desc) {
        List<LocalSubtask> out = new ArrayList<>();
        if (desc == null) return out;
        String[] lines = desc.split("\\r?\\n");
        for (String ln : lines) {
            if (ln == null) continue;
            String s = ln.trim();
            if (s.length() == 0) continue;
            if (s.startsWith("[x] ")) out.add(new LocalSubtask(s.substring(4), true));
            else if (s.startsWith("[ ] ")) out.add(new LocalSubtask(s.substring(4), false));
            else out.add(new LocalSubtask(s, false));
        }
        return out;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        Task task = getItem(pos);
        VH h;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_task_card, parent, false);
            h = new VH();
            h.cbMain = convertView.findViewById(R.id.cbMainDone);
            h.tvTitle = convertView.findViewById(R.id.tvMainTitle);
            h.tvCount = convertView.findViewById(R.id.tvCount);
            h.ivToggle = convertView.findViewById(R.id.ivToggle);
            h.container = convertView.findViewById(R.id.containerSubtasks);
            convertView.setTag(h);
        } else h = (VH) convertView.getTag();

        if (task == null) return convertView;

        h.tvTitle.setText(task.getTitle() != null ? task.getTitle() : "");

        List<LocalSubtask> subs = parseSubtasks(task.getDescription());
        int total = subs.size();
        int doneCount = 0;
        for (LocalSubtask s : subs) if (s.done) doneCount++;
        h.tvCount.setText(doneCount + "/" + total);

        h.cbMain.setOnCheckedChangeListener(null);
        h.cbMain.setFocusable(false);
        h.cbMain.setFocusableInTouchMode(false);
        h.cbMain.setClickable(true);
        h.cbMain.setChecked(task.isDone());
        h.cbMain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean ok = db.updateTaskStatus(task.getId(), isChecked);
            if (ok) {
                task.setDone(isChecked);
            } else {
                buttonView.setChecked(!isChecked);
                Toast.makeText(getContext(), "Ошибка при обновлении статуса", Toast.LENGTH_SHORT).show();
            }
        });

        h.container.removeAllViews();
        int densityPad = (int)(8 * getContext().getResources().getDisplayMetrics().density);

        for (int i = 0; i < subs.size(); i++) {
            LocalSubtask st = subs.get(i);
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(densityPad, densityPad, densityPad, densityPad);

            CheckBox cb = new CheckBox(getContext());
            cb.setFocusable(false);
            cb.setFocusableInTouchMode(false);
            cb.setChecked(st.done);

            TextView tv = new TextView(getContext());
            tv.setText(st.text);
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            tv.setTextSize(16f);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins((int)(12 * getContext().getResources().getDisplayMetrics().density), 0, 0, 0);
            tv.setLayoutParams(lp);

            row.addView(cb);
            row.addView(tv);
            h.container.addView(row);

            final int idx = i;
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                subs.get(idx).done = isChecked;

                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < subs.size(); j++) {
                    LocalSubtask s = subs.get(j);
                    sb.append((s.done ? "[x] " : "[ ] ") + s.text);
                    if (j < subs.size() - 1) sb.append("\n");
                }

                task.setDescription(sb.toString());
                boolean allDone = true;
                for (LocalSubtask s : subs) if (!s.done) { allDone = false; break; }
                task.setDone(allDone);

                boolean ok = db.updateTask(task);
                if (ok) {
                    h.tvCount.setText(countDone(subs) + "/" + subs.size());
                    h.cbMain.setOnCheckedChangeListener(null);
                    h.cbMain.setChecked(allDone);
                    h.cbMain.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                        boolean ok2 = db.updateTaskStatus(task.getId(), isChecked2);
                        if (ok2) task.setDone(isChecked2);
                        else buttonView2.setChecked(!isChecked2);
                    });
                } else {
                    Toast.makeText(getContext(), "Ошибка при сохранении подзадач", Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(!isChecked);
                }
            });
        }

        if (expandedIds.contains(task.getId())) {
            h.container.setVisibility(View.VISIBLE);
            h.ivToggle.setRotation(180f);
        } else {
            h.container.setVisibility(View.GONE);
            h.ivToggle.setRotation(0f);
        }

        h.ivToggle.setOnClickListener(v -> {
            if (h.container.getVisibility() == View.GONE) {
                h.container.setVisibility(View.VISIBLE);
                h.ivToggle.setRotation(180f);
                expandedIds.add(task.getId());
            } else {
                h.container.setVisibility(View.GONE);
                h.ivToggle.setRotation(0f);
                expandedIds.remove(task.getId());
            }
        });

        return convertView;
    }

    private int countDone(List<LocalSubtask> list) {
        int c = 0;
        for (LocalSubtask s : list) if (s.done) c++;
        return c;
    }
}


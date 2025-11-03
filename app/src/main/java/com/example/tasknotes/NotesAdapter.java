package com.example.tasknotes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.VH> {
    private final List<Task> items;
    private final Context ctx;
    private final DBHelper db;

    public NotesAdapter(List<Task> data, DBHelper db, Context ctx) {
        this.items = data != null ? data : new ArrayList<>();
        this.db = db;
        this.ctx = ctx;
    }

    public void update(List<Task> data) {
        this.items.clear();
        if (data != null) this.items.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_dark, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Task t = items.get(position);
        holder.title.setText(t.getTitle() != null ? t.getTitle() : "");
        String desc = t.getDescription() != null ? t.getDescription() : "";
        holder.preview.setText(desc.length() > 120 ? desc.substring(0, 120) + "…" : desc);

        if (t.getCreatedAt() > 0) {
            String s = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(new Date(t.getCreatedAt() * 1000L));
            holder.date.setText(s);
        } else holder.date.setText("");

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(t.isDone());

        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean ok = db.updateTaskStatus(t.getId(), isChecked);
            if (ok) {
                t.setDone(isChecked);
                if (isChecked)
                    holder.title.setPaintFlags(holder.title.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                else
                    holder.title.setPaintFlags(holder.title.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            } else {
                Toast.makeText(ctx, "Ошибка при обновлении статуса", Toast.LENGTH_SHORT).show();
                buttonView.setChecked(!isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, AddTaskActivity.class);
            i.putExtra(AddTaskActivity.EXTRA_TASK_ID, t.getId());
            ctx.startActivity(i);
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(ctx)
                    .setTitle("Удалить запись")
                    .setMessage("Удалить \"" + t.getTitle() + "\"?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        boolean ok = db.deleteTask(t.getId());
                        if (ok) {
                            int pos = holder.getAdapterPosition();
                            if (pos >= 0 && pos < items.size()) {
                                items.remove(pos);
                                notifyItemRemoved(pos);
                            } else {
                                notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(ctx, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox check;
        TextView title, preview, date;

        VH(View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.noteCheck);
            title = itemView.findViewById(R.id.noteTitle);
            preview = itemView.findViewById(R.id.notePreview);
            date = itemView.findViewById(R.id.noteDate);
        }
    }
}

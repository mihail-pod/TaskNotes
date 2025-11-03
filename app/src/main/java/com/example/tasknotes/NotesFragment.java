package com.example.tasknotes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import java.util.List;

public class NotesFragment extends Fragment {
    private DBHelper db;
    private RecyclerView rv;
    private NotesAdapter adapter;
    private SearchView searchView;
    private ImageView ivFilter;
    private boolean filterCompletedFirst = false;
    private boolean dateDesc = true;
    private String searchQuery = null;

    public NotesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notes_dark, container, false);

        db = new DBHelper(getContext());
        rv = root.findViewById(R.id.rvNotes);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        searchView = root.findViewById(R.id.searchNotes);
        ivFilter = root.findViewById(R.id.ivFilter);

        try {
            EditText svText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            if (svText != null) {
                svText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                svText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            }
        } catch (Exception ignored) {
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query != null && query.trim().length() > 0 ? query.trim() : null;
                loadNotes();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText != null && newText.trim().length() > 0 ? newText.trim() : null;
                loadNotes();
                return true;
            }
        });

        ivFilter.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 1, 0, (filterCompletedFirst ? "★ Выполненные сверху (вкл)" : "Выполненные сверху"));
            popup.getMenu().add(0, 2, 1, (dateDesc ? "Новые сверху (вкл)" : "Новые сверху"));
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    filterCompletedFirst = !filterCompletedFirst;
                    loadNotes();
                    return true;
                }
                if (item.getItemId() == 2) {
                    dateDesc = !dateDesc;
                    loadNotes();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        loadNotes();
        return root;
    }

    private void loadNotes() {
        List<Task> notes = db.getAllNotes(searchQuery, filterCompletedFirst, dateDesc);
        if (adapter == null) {
            adapter = new NotesAdapter(notes, db, getContext());
            rv.setAdapter(adapter);
        } else adapter.update(notes);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) db.close();
    }
}

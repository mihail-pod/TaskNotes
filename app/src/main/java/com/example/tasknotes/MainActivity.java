package com.example.tasknotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_NOTES = "notes";
    private static final String TAG_TASKS = "tasks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_host_dark);

        BottomNavigationView bottom = findViewById(R.id.bottomNavigation);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        if (savedInstanceState == null) {
            replaceFragment(new NotesFragment(), TAG_NOTES);
            bottom.setSelectedItemId(R.id.nav_notes);
        }

        bottom.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_notes) {
                replaceFragment(new NotesFragment(), TAG_NOTES);
                return true;
            } else if (item.getItemId() == R.id.nav_tasks) {
                replaceFragment(new TasksFragment(), TAG_TASKS);
                return true;
            }
            return false;
        });

        fab.setOnClickListener(v -> {
            int id = bottom.getSelectedItemId();
            Intent i = new Intent(MainActivity.this, AddTaskActivity.class);
            if (id == R.id.nav_notes) {
                i.putExtra("preset_kind", 0);
            } else if (id == R.id.nav_tasks) {
                i.putExtra("preset_kind", 1);
            }
            startActivity(i);
        });
    }

    private void replaceFragment(Fragment frag, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, frag, tag)
                .commit();
    }
}

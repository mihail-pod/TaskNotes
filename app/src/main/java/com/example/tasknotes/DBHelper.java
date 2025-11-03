package com.example.tasknotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DB_NAME = "tasks_notes.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "tasks";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESC = "description";
    public static final String COL_DONE = "done";
    public static final String COL_CREATED = "created_at";
    public static final String COL_KIND = "kind";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESC + " TEXT, "
                + COL_DONE + " INTEGER DEFAULT 0, "
                + COL_CREATED + " INTEGER, "
                + COL_KIND + " INTEGER DEFAULT 0"
                + ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
    }

    public long addTask(Task t) {
        long id = -1;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_TITLE, t.getTitle());
            cv.put(COL_DESC, t.getDescription());
            cv.put(COL_DONE, t.isDone() ? 1 : 0);
            long created = (t.getCreatedAt() > 0) ? t.getCreatedAt() : (System.currentTimeMillis() / 1000L);
            cv.put(COL_CREATED, created);
            cv.put(COL_KIND, t.getKind());
            id = db.insert(TABLE, null, cv);
        } catch (Exception e) {
            Log.e(TAG, "addTask error", e);
        }
        return id;
    }

    public boolean updateTask(Task t) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_TITLE, t.getTitle());
            cv.put(COL_DESC, t.getDescription());
            cv.put(COL_DONE, t.isDone() ? 1 : 0);
            cv.put(COL_KIND, t.getKind());
            SQLiteDatabase db = getWritableDatabase();
            int rows = db.update(TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(t.getId())});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "updateTask error", e);
            return false;
        }
    }

    public boolean updateTaskStatus(long id, boolean done) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_DONE, done ? 1 : 0);
            SQLiteDatabase db = getWritableDatabase();
            int rows = db.update(TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "updateTaskStatus error", e);
            return false;
        }
    }

    public boolean deleteTask(long id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            int r = db.delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
            return r > 0;
        } catch (Exception e) {
            Log.e(TAG, "deleteTask error", e);
            return false;
        }
    }

    public Task getTaskById(long id) {
        Cursor c = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            c = db.query(TABLE, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
            if (c != null && c.moveToFirst()) {
                String title = c.getString(c.getColumnIndexOrThrow(COL_TITLE));
                String desc = c.getString(c.getColumnIndexOrThrow(COL_DESC));
                boolean done = c.getInt(c.getColumnIndexOrThrow(COL_DONE)) == 1;
                long created = c.getLong(c.getColumnIndexOrThrow(COL_CREATED));
                int kind = c.getInt(c.getColumnIndexOrThrow(COL_KIND));
                return new Task(id, title, desc, done, created, kind);
            }
        } catch (Exception e) {
            Log.e(TAG, "getTaskById error", e);
        } finally {
            if (c != null) c.close();
        }
        return null;
    }


    public List<Task> getTasks(int kind, String search, boolean completedFirst, boolean dateDesc) {
        List<Task> list = new ArrayList<>();
        Cursor c = null;
        try {
            SQLiteDatabase db = getReadableDatabase();

            String selection = COL_KIND + "=?";
            ArrayList<String> selArgs = new ArrayList<>();
            selArgs.add(String.valueOf(kind));

            if (search != null && !search.trim().isEmpty()) {
                selection += " AND (" + COL_TITLE + " LIKE ? OR " + COL_DESC + " LIKE ?)";
                String q = "%" + search.trim() + "%";
                selArgs.add(q);
                selArgs.add(q);
            }

            String orderBy = (completedFirst ? (COL_DONE + " DESC") : (COL_DONE + " ASC"))
                    + ", " + COL_CREATED + (dateDesc ? " DESC" : " ASC");

            c = db.query(TABLE, null, selection, selArgs.toArray(new String[0]), null, null, orderBy);
            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
                    String title = c.getString(c.getColumnIndexOrThrow(COL_TITLE));
                    String desc = c.getString(c.getColumnIndexOrThrow(COL_DESC));
                    boolean done = c.getInt(c.getColumnIndexOrThrow(COL_DONE)) == 1;
                    long created = c.getLong(c.getColumnIndexOrThrow(COL_CREATED));
                    int k = c.getInt(c.getColumnIndexOrThrow(COL_KIND));
                    list.add(new Task(id, title, desc, done, created, k));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getTasks error", e);
        } finally {
            if (c != null) c.close();
        }
        return list;
    }

    public List<Task> getAllNotes(String search, boolean completedFirst, boolean dateDesc) {
        return getTasks(0, search, completedFirst, dateDesc);
    }

    public List<Task> getAllTasksOnly(String search, boolean completedFirst, boolean dateDesc) {
        return getTasks(1, search, completedFirst, dateDesc);
    }
}

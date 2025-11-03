package com.example.tasknotes;

public class Task {
    private long id;
    private String title;
    private String description;
    private boolean done;
    private long createdAt;
    private int kind;

    public Task() {
    }

    public Task(long id, String title, String description, boolean done, long createdAt, int kind) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.done = done;
        this.createdAt = createdAt;
        this.kind = kind;
    }

    public Task(String title, String description, boolean done, int kind) {
        this(-1, title, description, done, 0, kind);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }
}

package com.inspiredo.inspiredo;

/**
 * Created by erik on 8/25/14.
 */
public class TaskModel {
    private String mTitle;

    public TaskModel(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public String toString() {
        return mTitle;
    }
}

package com.inspiredo.inspiredo;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Binds an Array of Tasks to a view
 */
public class TaskListAdapter extends ArrayAdapter<TaskModel> {

    private int mActivePos;

    private int mRowResource;

    public TaskListAdapter(Context context, int resource, ListView list) {
        super(context, resource);
        mActivePos = list.getSelectedItemPosition();
        mRowResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Inflate the view if needed
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(mRowResource, null);
        }

        TaskModel task = getItem(position);
        boolean active = position == mActivePos;


        return convertView;
    }
}
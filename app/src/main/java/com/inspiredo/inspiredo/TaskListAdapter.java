package com.inspiredo.inspiredo;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Binds an Array of Tasks to a view
 */
public class TaskListAdapter extends ArrayAdapter<TaskModel> {

    private int mRowResource;

    public TaskListAdapter(Context context, int resource) {
        super(context, resource);
        mRowResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Inflate the view if needed
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(mRowResource, null);
        }

        // Get the Task
        TaskModel task = getItem(position);

        //Set all the TextViews
        TextView title = (TextView) convertView.findViewById(R.id.task_title);
        title.setText(task.getTitle());

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        TextView start = (TextView) convertView.findViewById(R.id.task_time_start);
        start.setText(format.format(task.getStart()));

        TextView end = (TextView) convertView.findViewById(R.id.task_time_end);
        end.setText(format.format(task.getEnd()));

        TextView reward = (TextView) convertView.findViewById(R.id.task_reward);
        reward.setText(Html.fromHtml(task.getReward() + "<small><sup>pts.</sup></small>"));

        TextView penalty = (TextView) convertView.findViewById(R.id.task_penalty);
        penalty.setText(Html.fromHtml(task.getPenalty() + "<small><sup>pts.</sup></small>"));

        // Set the indicator
        ImageView indicator = (ImageView) convertView.findViewById(R.id.task_complete_indicator);
        if (task.getComplete())
            indicator.setVisibility(View.VISIBLE);
        else
            indicator.setVisibility(View.GONE);

        // Set the details
        LinearLayout details = (LinearLayout) convertView.findViewById(R.id.task_details);
        if (task.getComplete())
            details.setVisibility(View.GONE);
        else
            details.setVisibility(View.VISIBLE);

        return convertView;
    }
}
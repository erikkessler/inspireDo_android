package com.inspiredo.inspiredo;

import android.content.Context;
import android.content.Intent;
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

    private ScheduleActivity mContext;

    private static SimpleDateFormat format = new SimpleDateFormat("hh:mm a");

    public TaskListAdapter(ScheduleActivity context, int resource) {
        super(context, resource);
        mRowResource = resource;
        mContext = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Inflate the view if needed
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(mRowResource, null);
        }

        // Get the Task
        final TaskModel task = getItem(position);

        //Set all the TextViews
        TextView title = (TextView) convertView.findViewById(R.id.task_title);
        title.setText(task.getTitle());


        TextView start = (TextView) convertView.findViewById(R.id.task_time_start);
        start.setText(format.format(task.getStart()));

        TextView end = (TextView) convertView.findViewById(R.id.task_time_end);
        end.setText(format.format(task.getEnd()));

        TextView reward = (TextView) convertView.findViewById(R.id.task_reward);
        reward.setText(Html.fromHtml(task.getReward() + "<small><sup>pts.</sup></small>"));

        TextView penalty = (TextView) convertView.findViewById(R.id.task_penalty);
        penalty.setText(Html.fromHtml(task.getPenalty() + "<small><sup>pts.</sup></small>"));

        setIndicators(convertView, task);

        // Timer
        ImageView timer = (ImageView) convertView.findViewById(R.id.timer_start);
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long endTime = task.getProjectedEnd().getTime();
                Intent i = new Intent(mContext, TimerActivity.class);
                i.putExtra(TimerActivity.END_TIME, endTime);
                i.putExtra(TimerActivity.TASK_NUM, position);
                mContext.startActivityForResult(i, ScheduleActivity.START_TIMER);
            }
        });

        return convertView;
    }

    public void setIndicators(View view, TaskModel task) {
        if (view == null)
            return;

        // Set the indicator
        ImageView indicator = (ImageView) view.findViewById(R.id.task_complete_indicator);
        if (task.getComplete()) {
            indicator.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.ic_action_accept_dark));
            indicator.setVisibility(View.VISIBLE);
        } else if (task.getState() == TaskModel.ENDED) {
            indicator.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.ic_action_cancel_dark));
            indicator.setVisibility(View.VISIBLE);
        } else
            indicator.setVisibility(View.GONE);

        // Set the details
        LinearLayout details = (LinearLayout) view.findViewById(R.id.task_details);
        if (task.getState() == TaskModel.UNSTARTED)
            details.setVisibility(View.VISIBLE);
        else
            details.setVisibility(View.GONE);

        // Set Time
        LinearLayout time = (LinearLayout) view.findViewById(R.id.actual_time);
        TextView aStart = (TextView) view.findViewById(R.id.actual_start);
        if (task.getAStart() != null )
            aStart.setText(format.format(task.getAStart()));

        TextView aEnd = (TextView) view.findViewById(R.id.actual_end);
        if (task.getAEnd() != null )
            aEnd.setText(format.format(task.getAEnd()));
        else if (task.getState() == TaskModel.STARTED)
            aEnd.setText(format.format(task.getProjectedEnd()));

        if (task.getState() == TaskModel.UNSTARTED)
            time.setVisibility(View.GONE);
        else
            time.setVisibility(View.VISIBLE);

        // Timer Icon
        ImageView timer = (ImageView) view.findViewById(R.id.timer_start);
        if (task.getState() == TaskModel.STARTED)
            timer.setVisibility(View.VISIBLE);
        else
            timer.setVisibility(View.GONE);
    }
}
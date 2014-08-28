package com.inspiredo.inspiredo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Launched Activity. Shows a list of today's tasks and allows user to select/deselect them
 */
public class ScheduleActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private Date mDate;

    private TextView mCurrentDayView;

    private  static  SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");

    // Task list and its adapter
    private TaskListAdapter mAdapter;
    private SwipeList mList;

    // Container for the list to allow for Swipe-to-refresh
    private SwipeRefreshLayout mSwipeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Instantiate the list and adapter
        mList = (SwipeList) findViewById(R.id.task_listview);
        mList.setScheduleActivity(this);
        mAdapter = new TaskListAdapter(this, R.layout.task_row);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                touchTask(i, view);
            }
        });

        // Setup the Swipe Layout
        mSwipeLayout = (SwipeRefreshLayout)
                findViewById(R.id.task_list_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.accent, R.color.primary,
                R.color.accent, R.color.primary);

        // Day navigation
        mCurrentDayView = (TextView) findViewById(R.id.current_day);
        final TextView rightDay = (TextView) findViewById(R.id.right_day);
        final TextView leftDay = (TextView) findViewById(R.id.left_day);

        // Move left/right
        leftDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shiftDay(-1);
            }
        });

        rightDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shiftDay(1);
            }
        });

        mCurrentDayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                mDate = new Date();
                fetchTasks(mDate);
                mCurrentDayView.setText(format.format(mDate));
            }
        });


        // Set to today's date
        mDate = new Date();
        mCurrentDayView.setText(format.format(mDate));

        // Fill the list
        fetchTasks(mDate);
    }

    public void shiftDay(int increment) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
        Calendar cal = Calendar.getInstance();
        cal.setTime ( mDate ); // convert your date to Calendar object
        int daysToDecrement = increment;
        cal.add(Calendar.DATE, daysToDecrement);
        mDate = cal.getTime();
        fetchTasks(mDate);
        mCurrentDayView.setText(format.format(mDate));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Makes a call to the api to get the tasks and then populates the list with the
     * response
     */
    public void fetchTasks(Date date) {
        // Show the refreshing animation
        mSwipeLayout.setRefreshing(true);

        // Get the URL
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String url = prefs.getString("api_source", null);

        // If no URL found
        if (url == null) {
            Toast.makeText(this, getString(R.string.no_url_error), Toast.LENGTH_LONG).show();
            mSwipeLayout.setRefreshing(false);
            return;
        }

        // No query params
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("date", date.toString()));

        // Handles the JSON response
        AsyncJSON.JSONParser p = new AsyncJSON.JSONParser() {
            @Override
            public void parseJSON(JSONObject json) {

                // Stop refreshing animation
                mSwipeLayout.setRefreshing(false);

                if (json != null) {
                    try {
                        // Get the tasks array
                        JSONArray array = json.getJSONArray("tasks");

                        mAdapter.clear();

                        // Date format for parsing
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));

                        // Loop and add each task to the adapter
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject task = array.getJSONObject(i);

                            // Get the Task properties
                            String title = task.getString("title");
                            Date start = df.parse(task.getString("start"));
                            Date end = df.parse(task.getString("end"));

                            Date aStart;
                            try {
                                aStart = df.parse(task.getString("ac_start"));
                            } catch (ParseException e) {
                                aStart = null;
                            }

                            Date aEnd;
                            try {
                                aEnd = df.parse(task.getString("ac_end"));
                            } catch (ParseException e) {
                                aEnd = null;
                            }

                            int reward = task.getInt("reward");
                            int penalty = task.getInt("penalty");
                            boolean complete = task.getBoolean("complete");

                            // Add new TaskModel to the adapter
                            mAdapter.add(new TaskModel(title, start, end,
                                    reward, penalty, complete, aStart, aEnd));

                        }

                        // Refresh the UI
                        mAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e("Fetch Tasks", e.getMessage());
                    } catch (ParseException e) {
                        Log.e("Fetch Tasks", e.getMessage());

                    }
                }

            }
        };

        // Start the Async task
        AsyncJSON jsonTask = new AsyncJSON(url, AsyncJSON.METHOD_GET, p, params);
        jsonTask.execute();
    }

    // Complete the Task at the given position
    private void touchTask(int pos, View row) {

        // Get the Task and its View
        TaskModel task = mAdapter.getItem(pos);

        // Get the URL
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String url = prefs.getString("api_source", null);

        // Handle no URL
        if (url == null) {
            Toast.makeText(this, getResources().getString(R.string.no_url_error), Toast.LENGTH_LONG).show();
            return;
        }

        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("task", pos + 1 + ""));


        if (task.getState() == TaskModel.UNSTARTED) {
            Date d = new Date();
            task.setAStart(d);
            task.setComplete(false);
            params.add(new BasicNameValuePair("start", d.toString()));
        } else if (task.getState() == TaskModel.STARTED) {
            Date d = new Date();
            task.setAEnd(d);
            if (d.getTime() >= task.getProjectedEnd().getTime() )
                task.setComplete(true);
            else
                task.setComplete(false);
            params.add(new BasicNameValuePair("end", d.toString()));
        } else {
            task.setComplete(false);
            task.setAStart(null);
            task.setAEnd(null);
            params.add(new BasicNameValuePair("end", ""));
            params.add(new BasicNameValuePair("start", ""));
        }


        // Handle the response: Alert connection Error
        final Context self = this;
        AsyncJSON.JSONParser p = new AsyncJSON.JSONParser() {
            @Override
            public void parseJSON(JSONObject json) {
                if (json == null) {
                    Toast.makeText(self, getString(R.string.connection_error),Toast.LENGTH_LONG).show();
                }

            }
        };

        // Start the Async Task
        AsyncJSON jsonTask = new AsyncJSON(url, AsyncJSON.METHOD_POST, p, params);
        jsonTask.execute();

        mAdapter.setIndicators(row, task);
    }

    @Override
    public void onRefresh() {
        fetchTasks(mDate);
    }
}

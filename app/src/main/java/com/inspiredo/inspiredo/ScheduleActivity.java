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
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Launched Activity. Shows a list of today's tasks and allows user to select/deselect them
 */
public class ScheduleActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    // Task list and its adapter
    private ArrayAdapter<TaskModel> mAdapter;
    private ListView mList;

    // Container for the list to allow for Swipe-to-refresh
    private SwipeRefreshLayout mSwipeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Instantiate the list and adapter
        mList = (ListView) findViewById(R.id.task_listview);
        mAdapter = new TaskListAdapter(this, R.layout.task_row);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                completeTask(i, view);
            }
        });

        // Setup the Swipe Layout
        mSwipeLayout = (SwipeRefreshLayout)
                findViewById(R.id.task_list_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.accent, R.color.primary,
                R.color.accent, R.color.primary);

        // Fill the list
        fetchTasks();
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
    public void fetchTasks() {
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
        BasicNameValuePair[] params = {};

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
                            int reward = task.getInt("reward");
                            int penalty = task.getInt("penalty");
                            boolean complete = task.getBoolean("complete");

                            // Add new TaskModel to the adapter
                            mAdapter.add(new TaskModel(title, start, end,
                                    reward, penalty, complete));

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
    private void completeTask(int pos, View row) {

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

        // Set the query params: Task number and if it complete
        BasicNameValuePair[] params = {
                new BasicNameValuePair("task", pos + 1 + ""),
                new BasicNameValuePair("complete", !task.getComplete() + "")
        };

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

        // Set the indicator and details
        int visibility = task.toggleComplete() ? View.VISIBLE : View.GONE;

        if (row == null)
            return; // If row not visible return

        ImageView indicator = (ImageView) row.findViewById(R.id.task_complete_indicator);
        indicator.setVisibility(visibility);

        LinearLayout details = (LinearLayout) row.findViewById(R.id.task_details);
        visibility = task.getComplete() ? View.GONE : View.VISIBLE;
        details.setVisibility(visibility);
    }

    @Override
    public void onRefresh() {
        fetchTasks();
    }
}

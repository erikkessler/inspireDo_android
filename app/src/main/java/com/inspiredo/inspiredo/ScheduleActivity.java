package com.inspiredo.inspiredo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
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
import java.util.Locale;
import java.util.TimeZone;


public class ScheduleActivity extends Activity {

    private ArrayAdapter<TaskModel> mAdapter;

    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mList = (ListView) findViewById(R.id.task_listview);

        mAdapter = new TaskListAdapter(this, R.layout.task_row, mList);

        mList.setAdapter(mAdapter);

        fetchTasks();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_complete);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fabClicked();
            }
        });
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

    public void fetchTasks() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String url = prefs.getString("api_source", null);

        if (url == null) {
            Toast.makeText(this, "Connection Error", Toast.LENGTH_LONG).show();
        }

        BasicNameValuePair[] params = {};
        AsyncJSON.JSONParser p = new AsyncJSON.JSONParser() {
            @Override
            public void parseJSON(JSONObject json) {
                if (json != null) {
                    try {
                        JSONArray array = json.getJSONArray("tasks");

                        mAdapter.clear();
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));


                        for (int i = 0; i < array.length(); i++) {
                            JSONObject task = array.getJSONObject(i);
                            String title = task.getString("title");
                            Date start = df.parse(task.getString("start"));
                            Date end = df.parse(task.getString("end"));
                            int reward = task.getInt("reward");
                            int penalty = task.getInt("penalty");
                            boolean complete = task.getBoolean("complete");
                            Log.d("New Task", title + "\n" + start + "\n" + end+ "\n" + reward + "\n" + penalty + "\n" + complete);
                            mAdapter.add(new TaskModel(title, start, end,
                                    reward, penalty, complete));

                        }

                        mAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {

                    } catch (ParseException e) {

                    }
                }

            }
        };
        AsyncJSON jsonTask = new AsyncJSON(url, AsyncJSON.METHOD_GET, p, params);
        jsonTask.execute();
    }

    private void fabClicked() {

        int pos = mList.getCheckedItemPosition();

        if (pos == AdapterView.INVALID_POSITION)
            return;

        TaskModel task = mAdapter.getItem(pos);
        LinearLayout row = (LinearLayout) mList.getChildAt(pos - mList.getFirstVisiblePosition());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String url = prefs.getString("api_source", null);

        if (url == null) {
            Toast.makeText(this, "Connection Error", Toast.LENGTH_LONG).show();
        }

        BasicNameValuePair[] params = {
                new BasicNameValuePair("task", pos + 1 + ""),
                new BasicNameValuePair("complete", !task.getComplete() + "")
        };

        final Context self = this;
        AsyncJSON.JSONParser p = new AsyncJSON.JSONParser() {
            @Override
            public void parseJSON(JSONObject json) {
                if (json == null) {
                    Toast.makeText(self, "Connection Error",Toast.LENGTH_LONG).show();
                }

            }
        };

        AsyncJSON jsonTask = new AsyncJSON(url, AsyncJSON.METHOD_POST, p, params);
        jsonTask.execute();

        int visibility = task.toggleComplete() ? View.VISIBLE : View.GONE;

        if (row == null)
            return;

        ImageView indicator = (ImageView) row.findViewById(R.id.task_complete_indicator);
        indicator.setVisibility(visibility);

    }

}

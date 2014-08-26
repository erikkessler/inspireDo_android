package com.inspiredo.inspiredo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mAdapter = new ArrayAdapter<TaskModel>(this, android.R.layout.simple_list_item_1);

        ListView taskList = (ListView) findViewById(R.id.task_listview);

        taskList.setAdapter(mAdapter);

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

}

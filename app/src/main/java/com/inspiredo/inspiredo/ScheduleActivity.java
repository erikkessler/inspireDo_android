package com.inspiredo.inspiredo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Launched Activity. Shows a list of today's tasks and allows user to select/deselect them
 */
public class ScheduleActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private Date mDate;

    private TextView mCurrentDayView;

    private  static  SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");

    // Task list and its adapter
    private ArrayAdapter<TaskModel> mAdapter;
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
                completeTask(i, view);
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

        mCurrentDayView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Date today = new Date();
                if (format.format(today).equals(format.format(mDate))) {
                    mCurrentDayView.setPaintFlags(
                            mCurrentDayView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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
        BasicNameValuePair[] params = {
                new BasicNameValuePair("date", date.toString().replace(" ", "%20"))};

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

    class ListViewTouchListener implements View.OnTouchListener {
        boolean firstTouch;
        int initialX;
        int initialY;

        int lastX;
        int lastY;

        boolean isSwiping;

        private BitmapDrawable mHoverCell;
        private Rect mHoverCellCurrentBounds;
        private Rect mHoverCellOriginalBounds;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    Log.d("MotionEvent", "Down");
                    initialX = lastX = (int) motionEvent.getX();
                    initialY = lastY = (int) motionEvent.getY();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    Log.d("MotionEvent", "Cancel");
                    break;
                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int) motionEvent.getX() - lastX;
                    int deltaY = (int) motionEvent.getY() - lastY;

                    if (!isSwiping) {
                        int totalX = (int) motionEvent.getX() - initialX;
                        int totalY = (int) motionEvent.getY() - initialY;
                        Log.d("MotionEvent", totalX + "");
                        if (totalX >= 60 && (totalX >= (totalY / 2))) {
                            Log.d("MotionEvent", "Swiping left");
                            mHoverCell = getAndAddHoverView(mList);
                            isSwiping = true;
                        }
                    }

                    lastX = (int) motionEvent.getX();
                    lastY = (int) motionEvent.getY();
                    Log.d("MotionEvent", "Move");
                    break;
                case MotionEvent.ACTION_UP:
                    isSwiping = false;
                    Log.d("MotionEvent", "Up");
                    break;

            }
            return false;
        }

        private BitmapDrawable getAndAddHoverView(View v) {

            int w = v.getWidth();
            int h = v.getHeight();
            int top = v.getTop();
            int left = v.getLeft();

            Bitmap b = getBitmapWithBorder(v);

            BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

            mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
            mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

            drawable.setBounds(mHoverCellCurrentBounds);

            return drawable;
        }

        /**
         * Draws a black border over the screenshot of the view passed in.
         */
        private Bitmap getBitmapWithBorder(View v) {
            Bitmap bitmap = getBitmapFromView(v);
            Canvas can = new Canvas(bitmap);

            can.drawBitmap(bitmap, 0, 0, null);

            return bitmap;
        }

        /**
         * Returns a bitmap showing a screenshot of the view passed in.
         */
        private Bitmap getBitmapFromView(View v) {
            Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(getResources().getColor(R.color.primary_lightest)); // Set BG color
            Canvas canvas = new Canvas(bitmap);
            v.draw(canvas);
            return bitmap;
        }
    }

    @Override
    public void onRefresh() {
        fetchTasks(mDate);
    }
}

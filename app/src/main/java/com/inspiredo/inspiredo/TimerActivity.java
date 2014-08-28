package com.inspiredo.inspiredo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Date;


public class TimerActivity extends Activity {

    private long mEnd = -1;
    private TextView mWatchFace;
    private int mTaskNum;

    public static final String END_TIME = "end_time";
    public static final String TASK_NUM = "task_num";

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = mEnd - System.currentTimeMillis();

            if (millis <= 0) {
                timerHandler.removeCallbacks(timerRunnable);
                timerOver();
                return;

            }
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            mWatchFace.setText(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mEnd = bundle.getLong(END_TIME, -1);
            mTaskNum = bundle.getInt(TASK_NUM, -1);
        }

        if (mEnd == -1 || mTaskNum == -1)
            return;

        mWatchFace = (TextView) findViewById(R.id.timer_time_left);
        timerHandler.postDelayed(timerRunnable, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void timerOver() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                Date d = new Date();
                Intent i = new Intent();
                i.putExtra(END_TIME, d.getTime());
                i.putExtra(TASK_NUM, mTaskNum);
                setResult(RESULT_OK, i);
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Done!")
                .setPositiveButton("Okay", dialogClickListener)
                .show();
    }
}

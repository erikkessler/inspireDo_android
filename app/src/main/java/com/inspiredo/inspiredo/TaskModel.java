package com.inspiredo.inspiredo;

import java.util.Date;

/**
 * Model fro a Task Object
 */
public class TaskModel {

    // Task properties
    private String mTitle;
    private Date mStart;
    private Date mEnd;
    private int mReward;
    private int mPenalty;
    private boolean mComplete;

    public TaskModel(String title, Date start, Date end) {
        this(title, start, end, 0, 0, false);
    }

    public TaskModel(String title, Date start, Date end,
                     int reward, int penalty, boolean complete) {
        mTitle = title;
        mStart = start;
        mEnd = end;
        mReward = reward;
        mPenalty = penalty;
        mComplete = complete;

    }

    // Title
    public String getTitle() {
        return mTitle;
    }
    public void setTitle(String title) { mTitle = title; }

    // Start time
    public Date getStart() {
        return mStart;
    }
    public void setStart(Date start) { mStart = start; }

    // End time
    public Date getEnd() {
        return mEnd;
    }
    public void setEnd(Date end) { mEnd = end;}

    // Reward
    public int getReward() { return mReward; }
    public void setReward(int reward) { mReward = reward; }

    // Penalty
    public int getPenalty() { return mPenalty; }
    public void setPenalty(int penalty) { mPenalty = penalty; }

    // Complete
    public boolean getComplete() { return mComplete; }
    public void setComplete(boolean complete) { mComplete = complete;}
    public boolean toggleComplete() {
        mComplete = !mComplete;
        return  mComplete;
    }

    @Override
    public String toString() {
        return mTitle;
    }
}

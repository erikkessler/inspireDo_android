package com.inspiredo.inspiredo;

import java.util.Date;

/**
 * Model fro a Task Object
 */
public class TaskModel {
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

    public String getTitle() {
        return mTitle;
    }
    public void setTitle(String title) { mTitle = title; }

    public Date getStart() {
        return mStart;
    }
    public void setStart(Date start) { mStart = start; }

    public Date getEnd() {
        return mEnd;
    }
    public void setEnd(Date end) { mEnd = end;}

    public int getReward() { return mReward; }
    public void setReward(int reward) { mReward = reward; }

    public int getPenalty() { return mPenalty; }
    public void setPenalty(int penalty) { mPenalty = penalty; }

    public boolean getComplete() { return mComplete; }
    public void setComplete(boolean complete) { mComplete = complete;}

    @Override
    public String toString() {
        return mTitle;
    }
}

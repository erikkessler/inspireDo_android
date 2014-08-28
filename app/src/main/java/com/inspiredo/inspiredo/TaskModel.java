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
    private Date mAStart;
    private Date mAEnd;

    public static int UNSTARTED = 0;
    public static int STARTED = 1;
    public static int ENDED = 3;

    public TaskModel(String title, Date start, Date end) {
        this(title, start, end, 0, 0, false, null, null);
    }

    public TaskModel(String title, Date start, Date end,
                     int reward, int penalty, boolean complete,
                     Date aStart, Date aEnd) {
        mTitle = title;
        mStart = start;
        mEnd = end;
        mReward = reward;
        mPenalty = penalty;
        mComplete = complete;
        mAStart = aStart;
        mAEnd = aEnd;

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

    // Actual Start/End
    public Date getAStart() { return mAStart; }
    public Date getAEnd() { return mAEnd; }
    public void setAStart(Date d) { mAStart = d; }
    public void setAEnd(Date d) { mAEnd = d; }
    public int getState() {
        if (mAStart == null && mAEnd == null)
            return UNSTARTED;
        else if (mAStart != null && mAEnd == null)
            return STARTED;
        else if (mAStart != null && mAEnd != null)
            return ENDED;
        else
            return UNSTARTED;
    }

    // Duration
    public Date getProjectedEnd() {
        long duration = mEnd.getTime() - mStart.getTime();

        if (mAStart != null)
            return new Date(mAStart.getTime() + duration);
        else
            return null;

    }

    @Override
    public String toString() {
        return mTitle;
    }
}

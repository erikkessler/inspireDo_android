package com.inspiredo.inspiredo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import java.util.logging.Logger;

/**
 * Created by erik on 8/27/14.
 */
public class SwipeList extends ListView {

    boolean firstTouch;
    int initialX;
    int initialY;

    int lastX;
    int lastY;

    int deltaX;

    boolean isSwiping;

    private BitmapDrawable mHoverCell;
    private Rect mHoverCellCurrentBounds;
    private Rect mHoverCellOriginalBounds;

    private int mWidth;

    private ScheduleActivity mScheduleActivity;

    public SwipeList(Context context) {
        super(context);
        init(context);
    }

    public SwipeList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SwipeList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mWidth = metrics.widthPixels;

    }

    public void setScheduleActivity(ScheduleActivity s) {
        mScheduleActivity = s;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        if (mHoverCell != null) {
            mHoverCell.draw(canvas);
        } else {
            super.dispatchDraw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.d("MotionEvent", "Down");
                initialX = lastX = (int) motionEvent.getX();
                initialY = lastY = (int) motionEvent.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (isSwiping) {
                    isSwiping = false;
                    swipeEnded();
                }
                Log.d("MotionEvent", "Cancel");
                break;
            case MotionEvent.ACTION_MOVE:
                deltaX = (int) motionEvent.getX() - lastX;
                int totalX = (int) motionEvent.getX() - initialX;
                int totalY = (int) motionEvent.getY() - initialY;
                lastX = (int) motionEvent.getX();
                lastY = (int) motionEvent.getY();

                Log.d("Delta", deltaX + "");

                if (!isSwiping) {

                    Log.d("MotionEvent", totalX + "");
                    if (Math.abs(totalX) >= 20 && (Math.abs(totalX) >= (Math.abs(totalY) * 2))) {
                        Log.d("MotionEvent", "Swiping left");
                        mHoverCell = getAndAddHoverView(this);
                        isSwiping = true;
                        return true;
                    }
                }

                if (isSwiping) {
                    mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left + totalX,
                            mHoverCellOriginalBounds.top);
                    mHoverCell.setBounds(mHoverCellCurrentBounds);
                    invalidate();
                    return true;
                }


                Log.d("MotionEvent", "Move");
                break;
            case MotionEvent.ACTION_UP:
                if (isSwiping) {
                    isSwiping = false;
                    swipeEnded();
                }
                Log.d("MotionEvent", "Up");
                this.setPressed(false);
                break;

        }
        return super.onTouchEvent(motionEvent);
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
        //bitmap.eraseColor(getResources().getColor(android.R.color.background_light)); // Set BG color
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private void swipeEnded() {
        Log.d("Bounds", "Current Right " + mHoverCellCurrentBounds.right + " Current left " + mHoverCellCurrentBounds.left + " width " + mWidth  );
        if(mHoverCellCurrentBounds.left > (mWidth / 2) || deltaX > 80) {
            mHoverCellCurrentBounds.offsetTo(mWidth, mHoverCellOriginalBounds.top);
            mScheduleActivity.shiftDay(-1);
        } else if (mHoverCellCurrentBounds.right < (mWidth / 2) || deltaX < -80) {
            mHoverCellCurrentBounds.offsetTo(-mWidth, mHoverCellCurrentBounds.top);
            mScheduleActivity.shiftDay(1);
        } else
            mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mHoverCellOriginalBounds.top);


        ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(mHoverCell, "bounds",
                sBoundEvaluator, mHoverCellCurrentBounds);
        hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                invalidate();
            }
        });
        hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHoverCell = null;
                setEnabled(true);
                invalidate();
            }
        });
        hoverViewAnimator.start();
    }

    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };
}

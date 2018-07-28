package com.elapse.easyweather.customView;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;

import com.elapse.easyweather.MainActivity;

/**
 * Created by YF_lala on 2018/7/21.
 * override intercepted & ontouchevent of drawer_list item
 */

public class DrawerItemLayout extends LinearLayout {
    private static final String TAG = "DrawerItemLayout";
    public boolean isOptionsShown = false;
    private int max;
    public DrawerItemLayout(Context context) {
        super(context);
    }

    public DrawerItemLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerItemLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int mLastInterceptX = 0 ;
    private int mLastInterceptY = 0;
    private int mLastX = 0;
    private int mLastY = 0;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
//                mLastX = (int) ev.getX();
//                mLastY = (int) ev.getY();
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
//                int moveY = (int) ev.getY();
//                int moveX = (int) ev.getX();
                int dx = x - mLastInterceptX ;
                int dy = y - mLastInterceptY ;
                Log.d(TAG, "onInterceptTouchEvent: "+dx+" "+dy);
                //设置拦截条件
                if ( Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > dip2px(getContext(),20) && dx < 0){
                    ViewParent parent = getParent();
                    parent.getParent().requestDisallowInterceptTouchEvent(true);
                    parent.requestDisallowInterceptTouchEvent(true);
                    intercepted = true;
                    Log.d(TAG, "onInterceptTouchEvent: intercepted");
                }else {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
               intercepted = false;
               default:
                   break;
        }
        mLastX = x;
        mLastY = y;
        mLastInterceptX = x;
        mLastInterceptY = y;
        return intercepted;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        max = getChildAt(1).getMeasuredWidth() + 10;
        v = getChildAt(1);
    }
    int deltaX = 0;
//    float mx = 0;
    View v;
    int X;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        X = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: executed");
//                X = (int) event.getX();
//                return false;
                break;
            case MotionEvent.ACTION_MOVE:
                deltaX = (X - mLastX);
                if (deltaX < -max)
                    deltaX = -max;
                v.setTranslationX(deltaX);
                Log.d(TAG, "onTouchEvent: move executed "+deltaX+" "+max);
                break;
            case MotionEvent.ACTION_UP:
                if (deltaX < -max / 2) {
                    ObjectAnimator.ofFloat(v, "translationX",
                            deltaX, -max).setDuration(300).start();
                    isOptionsShown = true;
                    MainActivity.instance.setSingleOption();
                } else {
                    ObjectAnimator.ofFloat(v, "translationX",
                            deltaX, 0).setDuration(300).start();
                    isOptionsShown = false;
                }
                break;
            }
//        return super.onTouchEvent(event);
//        mLastX = X;
        return  true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

}

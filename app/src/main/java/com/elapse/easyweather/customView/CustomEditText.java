package com.elapse.easyweather.customView;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.elapse.easyweather.R;

/**
 * Created by YF_lala on 2018/7/15.
 */

public class CustomEditText extends AppCompatEditText {
    private Drawable searchDrawable,clearDrawable;

    public CustomEditText(Context context) {
        super(context);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        searchDrawable = getResources().getDrawable(R.drawable.search);
        clearDrawable = getResources().getDrawable(R.drawable.delete);
        setCompoundDrawablesRelativeWithIntrinsicBounds(searchDrawable,null,null,null);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        setClearIconVisible(hasFocus() && text.length() > 0);
    }

    private void setClearIconVisible(boolean visible) {
        setCompoundDrawablesWithIntrinsicBounds(searchDrawable,null,
                visible?clearDrawable:null,null);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        setClearIconVisible(hasFocus() && length() > 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                Drawable drawable = clearDrawable;
                if (drawable != null && event.getX() <= getWidth()-getPaddingRight()
                        && event.getX() >= getWidth()-getPaddingRight()-drawable.getBounds().width()){
                    setText("");

                }
                break;
        }
        return super.onTouchEvent(event);
    }
}

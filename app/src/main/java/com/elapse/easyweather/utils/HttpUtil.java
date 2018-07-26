package com.elapse.easyweather.utils;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by YF_lala on 2018/5/16.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String address, Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}

//public class ParentOnTouchChildClickLinearLayout extends LinearLayout {
//
//
//    public ParentOnTouchChildClickLinearLayout(Context context) {
//        super(context);
//    }
//
//    public ParentOnTouchChildClickLinearLayout(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public ParentOnTouchChildClickLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    private int yyy = -1;
//    private int xxx = -1;
//    private boolean isMove = false;
//
//    /**
//     * 核心方法
//     *
//     * @param event
//     * @return
//     */
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                isMove = false;
//                //此处为break所以 onTouch中没有Down
//                break;
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP:
//                if (!isMove)
//                    return false;
//                isMove = false;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (!isMove) {
//                    yyy = (int) event.getRawY();
//                    xxx = (int) event.getRawX();
//                }
//                isMove = true;
//                //细节优化 短距离移除
//                float moveY = event.getRawY();
//                float moveX = event.getRawX();
//                //如果是非点击事件就拦截 让父布局接手onTouch 否则执行子ViewOnClick
//                if (Math.abs(moveY - yyy) > dip2px(getContext(), 20) || Math.abs(moveX - xxx) > dip2px(getContext(), 20)) {
//                    final ViewParent parent = getParent();
//                    if (parent != null) {
//                        parent.requestDisallowInterceptTouchEvent(true);
//                    }
//                    return true;
//                }
//                break;
//
//        }
//        return false;
//    }
//    /**
//     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
//     */
//    public static int dip2px(Context context, float dpValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (dpValue * scale + 0.5f);
//    }
//}

//<?xml version="1.0" encoding="utf-8"?>
//<com.rex.ParentOnTouchChildClickLinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
//        android:id="@+id/root"
//        android:layout_width="match_parent"
//        android:layout_height="wrap_content"
//        android:background="@android:color/holo_green_light"
//        android:clipChildren="false"
//        android:orientation="horizontal">
//
//<LinearLayout
//        android:id="@+id/llContext"
//                android:layout_width="match_parent"
//                android:layout_height="90dp"
//                android:layout_margin="1dp"
//                android:gravity="center_vertical"
//                android:orientation="horizontal">
//
//<TextView
//            android:layout_width="wrap_content"
//                    android:layout_height="wrap_content"
//                    android:text="测试文本"
//                    android:textColor="#fff"
//                    android:textSize="18sp"/>
//
//<TextView
//            android:id="@+id/tvTestClick"
//                    android:layout_width="wrap_content"
//                    android:layout_height="wrap_content"
//                    android:layout_margin="5dp"
//                    android:text="子view点击兼容性测试"
//                    android:textSize="18sp"/>
//
//</LinearLayout>
//
//<LinearLayout
//        android:id="@+id/llDelete"
//                android:layout_width="100dp"
//                android:layout_height="match_parent"
//                android:background="#456431">
//
//<TextView
//            android:layout_width="match_parent"
//                    android:layout_height="match_parent"
//                    android:background="@android:color/holo_orange_light"
//                    android:gravity="center"
//                    android:text="删除"
//                    android:textSize="20sp"/>
//
//</LinearLayout>
//</com.rex.ParentOnTouchChildClickLinearLayout>

//public class MainActivity extends Activity {
//
//    private float max = 300;//你想滑动的极限长度默认  本demo以删除布局宽度为max
//    private ArrayList<String> data = new ArrayList<String>() {{
//        add("str01");
//        add("str02");
//        add("str03");
//        add("str04");
//        add("str05");
//        add("str06");
//        add("str07");
//        add("str08");
//    }};
//    private ListView lv;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        lv = (ListView) findViewById(R.id.lv);
//        lv.setAdapter(new IAdapter());
//    }
//
//    class IAdapter extends BaseAdapter {
//
//        @Override
//        public int getCount() {
//            return data.size();
//        }
//
//        @Override
//        public Object getItem(int i) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return 0;
//        }
//
//        @Override
//        public View getView(final int position, View view, ViewGroup viewGroup) {
//            if (view == null) {
//                view = View.inflate(MainActivity.this, R.layout.item, null);
//            }
//            ParentOnTouchChildClickLinearLayout root = (ParentOnTouchChildClickLinearLayout) view.findViewById(R.id.root);
//            TextView tvTestClick = (TextView) view.findViewById(R.id.tvTestClick);
//            final LinearLayout llContext = (LinearLayout) view.findViewById(R.id.llContext);
//            final LinearLayout llDelete = (LinearLayout) view.findViewById(R.id.llDelete);
//
//            tvTestClick.setText(data.get(position));
//            ViewTreeObserver vto = llDelete.getViewTreeObserver();
//            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//
//                    //监听一次马上结束
//
//                    if (Build.VERSION.SDK_INT < 16) {
//                        llDelete.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                    } else {
//                        llDelete.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    }
//                    max = llDelete.getWidth();
//                    //得到删除按钮长度 得到最大拖动限定
//                    Log.i("rex", "max--" + max);
//
//                }
//            });
//
//
//            llContext.setTranslationX(0);
//            llDelete.setTranslationX(0);
//            view.setScaleY(1);
//            view.setTranslationY(0);
//
//            final View finalView = view;
//            llDelete.setOnClickListener(new View.OnClickListener() {
//                @Override
//
//                public void onClick(View v) {
//                    //删除
//                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(finalView, "scaleY", 1, 0);
//                    scaleY.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//                            data.remove(position);
//                            IAdapter.this.notifyDataSetChanged();
//                        }
//                    });
//                    scaleY.setDuration(800).start();
//                    for (int i = 1; i < lv.getChildCount() - position; i++) {
//                        ObjectAnimator.ofFloat(lv.getChildAt(i + position), "translationY", 0, -finalView.getMeasuredHeight()).setDuration(800).start();
//                    }
//
//
//                }
//            });
//            tvTestClick.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(MainActivity.this, "测试按钮被调用！", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//
//            llContext.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    Toast.makeText(MainActivity.this, "item 长按被调用！", Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//            });
//
//
//            //点击内容让item回到最初的位置
//            llContext.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //点击归位
//                    ObjectAnimator.ofFloat(llContext, "translationX", llContext.getTranslationX(), 0).setDuration(600).start();
//                    ObjectAnimator.ofFloat(llDelete, "translationX", llDelete.getTranslationX(), 0).setDuration(600).start();
//                }
//            });
//
//
//            root.setOnTouchListener(new View.OnTouchListener() {
//
//                private float diff;
//                float x = -1;
//                float mx;
//                boolean isMove;
//
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (max == 0) {
//                        return false;
//                    }
//                    //当按下时处理
//                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        //由于父onInterceptTouchEvent 为false所以down无效 且不需要 以-1作为初始X
//                        //这里类似一般写法的ACTION_DOWN初始化
//                        if (x == -1)
//                            x = event.getRawX();
//
//                        mx = event.getRawX();
//                        isMove = true;
//                        diff = mx - x;
//
//                        if (diff < -max)
//                            diff = -max;
//
//                        if (llContext.getTranslationX() > 0 && diff > llContext.getTranslationX())
//                            diff = llContext.getTranslationX();
//
//                        llContext.setTranslationX(diff);
//                        llDelete.setTranslationX(diff);
//
//                        return true;
//                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                        x = -1;
//                        if (isMove) {
//                            //自动归位  过半则全部显示删除布局  反之则回收为正常
//                            if (diff < -max / 2.0f) {
//                                ObjectAnimator.ofFloat(llContext, "translationX", diff, -max).setDuration(600).start();
//                                ObjectAnimator.ofFloat(llDelete, "translationX", diff, -max).setDuration(600).start();
//                            } else {
//                                ObjectAnimator.ofFloat(llContext, "translationX", diff, 0).setDuration(600).start();
//                                ObjectAnimator.ofFloat(llDelete, "translationX", diff, 0).setDuration(600).start();
//                            }
//                            return true;
//                        } else {
//                            return false;
//                        }
//
//                    } else {//其他模式
//                        //设置背景为未选中正常状态
//                        //v.setBackgroundResource(R.drawable.mm_listitem_simple);
//
//                    }
//                    return true;
//                }
//            });
//
//
//            return view;
//        }
//    }
//}
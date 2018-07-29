package com.elapse.easyweather.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elapse.easyweather.MainActivity;
import com.elapse.easyweather.R;
import com.elapse.easyweather.customView.DrawerItemLayout;
import com.elapse.easyweather.utils.WeatherConst;

import java.util.List;

/**
 * Created by YF_lala on 2018/7/21.
 */

public class DrawerListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "DrawerListAdapter";

    private int resourceId;
    private onItemOptionsClickListener listener;

    public DrawerListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

//    class ViewHolder{
//        TextView tv_cancel;
//        TextView tv_delete;
//        TextView tv_content;
//        DrawerItemLayout root;
//        LinearLayout options;
//    }
    //在此方法中不使用ViewHolder，原因是view复用会导致view的动画效果保存，造成pagerList的item不显示内容
    @SuppressLint({"ClickableViewAccessibility", "ViewHolder"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
         final String cityName = getItem(position);
         final int pos = position;
         View view;
//        final ViewHolder holder;
//        if (convertView == null){
        view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
//            holder = new ViewHolder();
            final DrawerItemLayout root = view.findViewById(R.id.root);
            TextView tv_content = view.findViewById(R.id.id_content);
            TextView tv_cancel = view.findViewById(R.id.id_cancel);
            TextView tv_delete = view.findViewById(R.id.id_delete);
            LinearLayout options = view.findViewById(R.id.id_options);
//            view.setTag(holder);
//        }else {
//            view = convertView;
//            holder = (ViewHolder) view.getTag();
//        }
        tv_content.setText(cityName);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel(root);
            }
        });
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(root, pos);
            }
        });

        tv_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: execute");
                listener.onContentChoose(cityName);
            }
        });

//        if (holder.root.isOptionsShown){
//            int old_index = WeatherConst.optionsItemIndex;
//            WeatherConst.optionsItemIndex = position;
//            if (old_index > 0 && old_index != WeatherConst.optionsItemIndex){
//                DrawerItemLayout view1 = (DrawerItemLayout) MainActivity.cur_pager_list.getChildAt(old_index);
//                closeOptions(view1);
//
//            }
//        }


//        holder.root.setOnTouchListener(new View.OnTouchListener() {
//            View view = holder.options;
//            int max = view.getMeasuredWidth()+10;
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.d(TAG, "onTouch: 92 "+max);
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        mLastX = (int) event.getX();
//                        return false;
////                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        deltaX = (int) (event.getX() - mLastX);
//                        Log.d(TAG, "onTouch: move "+deltaX);
//                        if (deltaX < -max)
//                            deltaX = -max;
//                        view.setTranslationX(deltaX);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        if (deltaX < -max / 2) {
//                            ObjectAnimator.ofFloat(view, "translationX",
//                                    deltaX, -max).setDuration(300).start();
//                        } else {
//                            ObjectAnimator.ofFloat(view, "translationX",
//                                    deltaX, 0).setDuration(300).start();
//                        }
//                        break;
//                }
//                return  true;
//            }
//        });
        return view;
    }

    //回调接口
    public interface onItemOptionsClickListener{
        void onCancel(DrawerItemLayout v2);
        void onDelete(DrawerItemLayout view,int pos);
        void onContentChoose(String cityName);
    }

    public void setOnItemOptionsClickListener(onItemOptionsClickListener l){
        listener = l;
    }

}

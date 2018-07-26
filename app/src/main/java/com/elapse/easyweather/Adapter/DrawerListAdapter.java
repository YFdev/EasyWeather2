package com.elapse.easyweather.Adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elapse.easyweather.R;
import com.elapse.easyweather.customView.DrawerItemLayout;

import java.util.List;

/**
 * Created by YF_lala on 2018/7/21.
 */

public class DrawerListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "DrawerListAdapter";

    private int resourceId;
    private onItemOptionsClickListener listener;
    private int mLastX ;
    private int deltaX ;

    public DrawerListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    class ViewHolder{
        TextView tv_cancel;
        TextView tv_delete;
        TextView tv_content;
        DrawerItemLayout root;
        LinearLayout options;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final String cityName = getItem(position);
        final int pos = position;
        View view;
        final ViewHolder holder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            holder = new ViewHolder();
            holder.root = view.findViewById(R.id.root);
            holder.tv_content = view.findViewById(R.id.id_content);
            holder.tv_cancel = view.findViewById(R.id.id_cancel);
            holder.tv_delete = view.findViewById(R.id.id_delete);
            holder.options = view.findViewById(R.id.id_options);
            view.setTag(holder);
        }else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.tv_content.setText(cityName);

        holder.tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel(holder.options);
            }
        });
        holder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(holder.root, pos);
            }
        });

        holder.tv_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: execute");
                listener.onContentChoose(cityName);
            }
        });
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

    public interface onItemOptionsClickListener{
        void onCancel(LinearLayout v2);
        void onDelete(View view,int pos);
        void onContentChoose(String cityName);
    }

    public void setOnItemOptionsClickListener(onItemOptionsClickListener l){
        listener = l;
    }

}

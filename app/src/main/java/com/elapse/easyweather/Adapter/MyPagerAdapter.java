package com.elapse.easyweather.Adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by YF_lala on 2018/7/10.
 */
@Deprecated
public class MyPagerAdapter extends PagerAdapter {

    private List<View> views;
    public MyPagerAdapter(List<View> views) {
        this.views = views;
    }

    /**
     * return pager count
     * @return
     */
    @Override
    public int getCount() {
        return views.size();
    }

    /**
     * if current view from object
     * @param view
     * @param object
     * @return
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));

    }
}

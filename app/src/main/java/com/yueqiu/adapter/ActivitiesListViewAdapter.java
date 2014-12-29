package com.yueqiu.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.yueqiu.R;

import java.util.ArrayList;

/**
 * Created by yinfeng on 14/12/18.
 */
public class ActivitiesListViewAdapter extends BaseAdapter {

    private ArrayList<String> mList;
    private Context mContext;

    public ActivitiesListViewAdapter(ArrayList<String> list,Context context)
    {
        this.mList = list;
        this.mContext = context;
        Log.i("TAG",mList.size()+"");
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if(view == null)
        {

            holder = new Holder();
            view = LayoutInflater.from(mContext).inflate(R.layout.activities_listview_item,null);
//            Holder.tv = (TextView)view.findViewById(R.id.activities_lv_item_title);
            view.setTag(holder);
        }
        else
        {
            holder = (Holder) view.getTag();
        }
//        holder.tv.setText(mList.get(i).toString());
        return view;
    }



    public static class Holder
    {
        public static TextView tv;
    }
}

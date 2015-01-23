package com.yueqiu.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.yueqiu.R;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.util.HttpUtil;

import java.io.InputStream;
import java.util.List;


public class PlayListViewAdapter extends BaseAdapter {

    private List<PlayInfo> mList;
    private Context mContext;

    public PlayListViewAdapter(Context context,List<PlayInfo> list)
    {
        this.mList = list;
        this.mContext = context;
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
            view = LayoutInflater.from(mContext).inflate(R.layout.play_listview_item,null);
            holder.tv_title = (TextView)view.findViewById(R.id.activities_lv_item_tv_title);
            holder.tv_content = (TextView)view.findViewById(R.id.activities_lv_item_tv_activities_time);
            holder.tv_time_day = (TextView)view.findViewById(R.id.activities_lv_item_tv_time_day);
            holder.tv_time_hour = (TextView)view.findViewById(R.id.activities_lv_item_tv_time_hour);
            holder.iv = (ImageView)view.findViewById(R.id.activities_lv_item_iv_head);
            view.setTag(holder);
        }
        else
        {
            holder = (Holder) view.getTag();
        }
        holder.tv_title.setText(mList.get(i).getTitle().toString().trim());
        holder.tv_content.setText(mList.get(i).getContent().toString().trim());
        if( null == mList.get(i).getImg_url() || mList.get(i).getImg_url().equals(""))
        {
            holder.iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_head));
        }
        else
        {
            holder.iv.setImageBitmap(bitmapFromInternet(mList.get(i).getImg_url()));
        }
        String times[] = mList.get(i).getCreate_time().split(" ");

        holder.tv_time_day.setText(times[1].substring(0,5).toString());
        holder.tv_time_hour.setText(times[0].substring(5, times[0].length()).toString());
        return view;
    }


    private Bitmap bitmapFromInternet(String filepath)
    {
        InputStream in = HttpUtil.getInputStream(filepath);
        Bitmap bp = BitmapFactory.decodeStream(in);
        return bp;
    }


    public static class Holder
    {
        public  TextView tv_title;
        public  TextView tv_content;
        public  TextView tv_time_day;
        public  TextView tv_time_hour;
        public  ImageView iv;
    }
}

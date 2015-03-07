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


import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.VolleySingleton;

import java.io.InputStream;
import java.util.List;


public class PlayListViewAdapter extends BaseAdapter {

    private List<PlayInfo> mList;
    private Context mContext;
    private ImageLoader mImgLoader;
    public PlayListViewAdapter(Context context,List<PlayInfo> list)
    {
        this.mList = list;
        this.mContext = context;
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
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
            view = LayoutInflater.from(mContext).inflate(R.layout.item_play,null);
            holder.tv_title = (TextView)view.findViewById(R.id.activities_lv_item_tv_title);
            holder.tv_content = (TextView)view.findViewById(R.id.activities_lv_item_tv_activities_time);
            holder.tv_time_day = (TextView)view.findViewById(R.id.activities_lv_item_tv_time_day);
            holder.tv_time_hour = (TextView)view.findViewById(R.id.activities_lv_item_tv_time_hour);
            holder.photo = (NetworkImageView) view.findViewById(R.id.activities_lv_item_iv_head);
            view.setTag(holder);
        }
        else
        {
            holder = (Holder) view.getTag();
        }
        holder.tv_title.setText(mList.get(i).getTitle().toString().trim());
        holder.tv_content.setText(mList.get(i).getContent().toString().trim());
        holder.photo.setDefaultImageResId(R.drawable.default_head);
        holder.photo.setErrorImageResId(R.drawable.default_head);
        holder.photo.setImageUrl(HttpConstants.IMG_BASE_URL + mList.get(i).getImg_url(), mImgLoader);
        String times[] = mList.get(i).getCreate_time().split(" ");


        holder.tv_time_day.setText(times[1].substring(0,5).toString());
        holder.tv_time_hour.setText(times[0].substring(5, times[0].length()).toString());
        return view;
    }


    public static class Holder
    {
        public  TextView tv_title;
        public  TextView tv_content;
        public  TextView tv_time_day;
        public  TextView tv_time_hour;
        public  NetworkImageView photo;
    }
}

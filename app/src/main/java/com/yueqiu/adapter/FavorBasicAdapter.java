package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.PublishedInfo;

import java.util.List;

/**
 * Created by wangyun on 15/1/4.
 */
public class FavorBasicAdapter extends BaseAdapter {
    private List<PublishedInfo.PublishedItemInfo> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public FavorBasicAdapter(Context context,List<PublishedInfo.PublishedItemInfo> list){
        this.mContext = context;
        this.mList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.published_item_layout,null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.published_item_image);
            holder.title = (TextView) convertView.findViewById(R.id.published_title);
            holder.content = (TextView) convertView.findViewById(R.id.published_content);
            holder.dateTime = (TextView) convertView.findViewById(R.id.published_time);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(mList.get(position).getTitle());
        holder.content.setText(mList.get(position).getContent());
        holder.dateTime.setText(mList.get(position).getDateTime());
        return convertView;
    }

    class ViewHolder{
        ImageView image;
        TextView  title;
        TextView  content;
        TextView  dateTime;
    }
}

package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.PublishedInfo;

import java.util.List;

/**
 * Created by wangyun on 15/1/4.
 */
public class FavorBasicAdapter extends BaseAdapter {
    private List<FavorInfo.FavorItemInfo> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public FavorBasicAdapter(Context context,List<FavorInfo.FavorItemInfo> list){
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
            convertView = mInflater.inflate(R.layout.my_collection_item_layout,null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.favor_title);
            holder.content = (TextView) convertView.findViewById(R.id.favor_content);
            holder.dateTime = (TextView) convertView.findViewById(R.id.favor_time);
            holder.whole_bg = (RelativeLayout) convertView.findViewById(R.id.favor_item_bg_view);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        PublishedInfo.PublishedItemInfo itemInfo = (PublishedInfo.PublishedItemInfo) getItem(position);
        if(itemInfo.isChecked()){
            holder.whole_bg.setBackgroundColor(mContext.getResources().getColor(R.color.actionbar_color));
        }else{
            holder.whole_bg.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.published_item_bg));
        }
        holder.title.setText(mList.get(position).getTitle());
        holder.content.setText(mList.get(position).getContent());
        holder.dateTime.setText(mList.get(position).getDateTime());
        return convertView;
    }

    class ViewHolder{
        TextView  title;
        TextView  content;
        TextView  dateTime;
        RelativeLayout whole_bg;
    }

    public void unCheckAll(){
        for(int i=0;i<getCount();i++){
            PublishedInfo.PublishedItemInfo item = (PublishedInfo.PublishedItemInfo) getItem(i);
            item.setChecked(false);
        }

    }
}

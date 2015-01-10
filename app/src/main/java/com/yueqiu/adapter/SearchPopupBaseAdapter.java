package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.yueqiu.R;

import java.util.List;


public class SearchPopupBaseAdapter extends BaseAdapter{

    private Context mContext;
    private List<String> mList;
    private LayoutInflater mInflater;

    public SearchPopupBaseAdapter(Context context,List<String> list){
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(context);
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
            convertView = mInflater.inflate(R.layout.search_popup_basic_item,null);
            holder = new ViewHolder();
            holder.button = (Button) convertView.findViewById(R.id.search_pop_base_button);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.button.setText(mList.get(position));
        return convertView;
    }
    private class ViewHolder{
        Button button;
    }
}

package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.bean.SearchDatingDetailedAlreadyBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/30.
 *
 * 用于展示SearchActivity当中的约球子Fragment当中的listView当中的具体条目展开的DatingDetailedActivity
 * 当中的GridView的adapter
 *
 */
public class SearchDatingDetailedGridAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;
    private List<SearchDatingDetailedAlreadyBean> mBeanList;

    public SearchDatingDetailedGridAdapter(Context context, ArrayList<SearchDatingDetailedAlreadyBean> list)
    {
        this.mInflater = LayoutInflater.from(context);
        mBeanList = list;
    }

    @Override
    public int getCount()
    {
        return mBeanList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mBeanList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.search_dating_grid_item_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mPhoto = (ImageView) convertView.findViewById(R.id.img_grid_search_dating_detailed_userphoto);
            viewHolder.mName = (TextView) convertView.findViewById(R.id.tv_grid_search_dating_detailed_username);
            convertView.setTag(viewHolder);
        } else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        SearchDatingDetailedAlreadyBean bean = mBeanList.get(position);

        // then, inflate the layout
        // TODO: the following user photo are the test data, and change them later
        viewHolder.mPhoto.setImageResource(R.drawable.default_head);
        viewHolder.mName.setText(bean.getUserName());

        return convertView;
    }

    private static class ViewHolder
    {
        public ImageView mPhoto;
        public TextView mName;
    }

}

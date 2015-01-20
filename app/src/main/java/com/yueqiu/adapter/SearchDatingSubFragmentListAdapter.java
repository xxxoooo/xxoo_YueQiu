package com.yueqiu.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.bean.SearchDatingSubFragmentDatingBean;
import com.yueqiu.util.VolleySingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/25.
 *
 * 用于SearchActivity当中的约球子Fragment的ListAdapter
 *
 */
public class SearchDatingSubFragmentListAdapter extends BaseAdapter
{
    private static final String TAG = "SearchDatingSubFragmentListAdapter";

    private List<SearchDatingSubFragmentDatingBean> mDatingBeanList;

    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;

    public SearchDatingSubFragmentListAdapter(Context context, ArrayList<SearchDatingSubFragmentDatingBean> beanList)
    {
        mImgLoader = VolleySingleton.getInstance().getImgLoader();
        mInflater = LayoutInflater.from(context);

        this.mDatingBeanList = beanList;
    }

    @Override
    public int getCount()
    {
        return mDatingBeanList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mDatingBeanList.get(position);
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
        if (null == convertView)
        {
            convertView = mInflater.inflate(R.layout.search_dating_fragment_listitem_layout, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.mUserPhoto = (NetworkImageView) convertView.findViewById(R.id.img_dating_subfragment_listitem_photo);
            viewHolder.mUserNickname = (TextView) convertView.findViewById(R.id.tv_dating_subfragment_listitem_nickname);
            viewHolder.mUserDeclareation = (TextView) convertView.findViewById(R.id.tv_dating_subfragment_listitem_declareation);
            viewHolder.mUserDistance = (TextView) convertView.findViewById(R.id.tv_dating_subfragment_listitem_distance_meter);
            convertView.setTag(viewHolder);
        } else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // then, inflate the layout
        SearchDatingSubFragmentDatingBean bean = mDatingBeanList.get(position);
        if (bean != null)
        {
            viewHolder.mUserPhoto.setDefaultImageResId(R.drawable.default_head);
            viewHolder.mUserPhoto.setImageUrl(bean.getUserPhoto(), mImgLoader);

            viewHolder.mUserNickname.setText(bean.getUserName());
            viewHolder.mUserDistance.setText(bean.getUserDistance());
            viewHolder.mUserDeclareation.setText(bean.getUserDeclare());
        }

        return convertView;
    }

    private static class ViewHolder
    {
        public NetworkImageView mUserPhoto;
        public TextView mUserNickname, mUserDeclareation, mUserDistance;
    }



}










































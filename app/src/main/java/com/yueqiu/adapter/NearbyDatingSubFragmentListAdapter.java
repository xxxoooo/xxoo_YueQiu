package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.bean.NearbyDatingSubFragmentDatingBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.VolleySingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/25.
 *
 * 用于SearchActivity当中的约球子Fragment的ListAdapter
 *
 */
public class NearbyDatingSubFragmentListAdapter extends BaseAdapter
{
    private static final String TAG = "NearbyDatingSubFragmentListAdapter";

    private List<NearbyDatingSubFragmentDatingBean> mDatingBeanList;

    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;
    private Context mContext;
    public NearbyDatingSubFragmentListAdapter(Context context, ArrayList<NearbyDatingSubFragmentDatingBean> beanList)
    {
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
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
            convertView = mInflater.inflate(R.layout.item_nearby_dating_layout, parent, false);
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
        NearbyDatingSubFragmentDatingBean bean = mDatingBeanList.get(position);
        if (bean != null)
        {
            viewHolder.mUserPhoto.setDefaultImageResId(R.drawable.default_head);
            viewHolder.mUserPhoto.setErrorImageResId(R.drawable.default_head);
            viewHolder.mUserPhoto.setImageUrl(HttpConstants.IMG_BASE_URL + bean.getUserPhoto(), mImgLoader);

            viewHolder.mUserNickname.setText(bean.getUserName());
            viewHolder.mUserDistance.setText(mContext.getString(R.string.in_meter,bean.getUserDistance()));
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










































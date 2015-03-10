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
import com.yueqiu.bean.NearbyCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/30.
 *
 * 用于SearchActivity当中的教练子Fragment当中的ListView的bean
 *
 */
public class NearbyCoauchSubFragmentListAdapter extends BaseAdapter
{
    private List<NearbyCoauchSubFragmentCoauchBean> mBeanList;
    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;
    private Context mContext;
    public NearbyCoauchSubFragmentListAdapter(Context context, ArrayList<NearbyCoauchSubFragmentCoauchBean> beanList)
    {
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
        this.mContext = context;
        this.mBeanList = beanList;
        this. mInflater = LayoutInflater.from(context);
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
            convertView = mInflater.inflate(R.layout.item_nearby_coauch_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mPhoto = (CustomNetWorkImageView) convertView.findViewById(R.id.img_coauch_subfragment_listitem_photo);
            viewHolder.mName = (TextView) convertView.findViewById(R.id.tv_coauch_subfragment_listitem_name);
            viewHolder.mDistance = (TextView) convertView.findViewById(R.id.tv_coauch_subfragment_listitem_distance);
            viewHolder.mKinds = (TextView) convertView.findViewById(R.id.tv_coauch_subfragment_listitem_kinds);
            viewHolder.mLevel = (TextView) convertView.findViewById(R.id.tv_coauch_subfragment_listitem_level);
            viewHolder.mGender = (TextView) convertView.findViewById(R.id.tv_coauch_subfragment_listitem_gender);
            viewHolder.mDistrict = (TextView) convertView.findViewById(R.id.tv_coauch_subfragment_listitem_district);
            convertView.setTag(viewHolder);
        } else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        NearbyCoauchSubFragmentCoauchBean bean = mBeanList.get(position);

        // then, inflate the layout of this list view item
        if(bean.getUserLevel().equals(mContext.getString(R.string.search_dating_popupwindow_other))){
            viewHolder.mLevel.setVisibility(View.GONE);
            if(!bean.getDistrict().equals("")){
                viewHolder.mDistrict.setText(bean.getDistrict());
                viewHolder.mDistrict.setVisibility(View.VISIBLE);
            }
        }else{
            viewHolder.mLevel.setText(bean.getUserLevel());
            viewHolder.mLevel.setVisibility(View.VISIBLE);

        }

        viewHolder.mKinds.setText(bean.getmBilliardKind());
        viewHolder.mDistance.setText(mContext.getString(R.string.in_meter, bean.getUserDistance()));
        viewHolder.mGender.setText(bean.getUserGender());
        viewHolder.mGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, NearbyFragmentsCommonUtils.parseGenderDrawable(bean.getUserGender()), 0);
        viewHolder.mGender.setCompoundDrawablePadding(6);
        viewHolder.mName.setText(bean.getUserName());
        viewHolder.mPhoto.setDefaultImageResId(R.drawable.default_head);
        viewHolder.mPhoto.setErrorImageResId(R.drawable.default_head);
        viewHolder.mPhoto.setImageUrl(HttpConstants.IMG_BASE_URL + bean.getUserPhoto(), mImgLoader);

        return convertView;
    }

    private static class ViewHolder
    {
        public CustomNetWorkImageView mPhoto;
        private TextView mName, mGender, mLevel, mKinds, mDistance,mDistrict;
    }
}

























































































































































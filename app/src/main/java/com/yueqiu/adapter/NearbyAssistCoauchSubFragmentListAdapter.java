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
import com.yueqiu.bean.NearbyAssistCoauchSubFragmentBean;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.VolleySingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/25.
 *
 * 用于SearchFragment当中的助教子Fragment当中的ListView的adapter
 *
 */
public class NearbyAssistCoauchSubFragmentListAdapter extends BaseAdapter
{

    private List<NearbyAssistCoauchSubFragmentBean> mBeanList;

    private LayoutInflater mInflater;

    private ImageLoader mImgLoader;
    public NearbyAssistCoauchSubFragmentListAdapter(Context context, ArrayList<NearbyAssistCoauchSubFragmentBean> beanList)
    {
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();

        this.mBeanList = beanList;
        // a better way to get the LAYOUT_INFLATE_SERVICE
        mInflater = LayoutInflater.from(context);
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
            convertView = mInflater.inflate(R.layout.item_nearby_assistcoauch_layout, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.mPhoto = (NetworkImageView) convertView.findViewById(R.id.img_assistcoauch_subfragment_listitem_photo);
            viewHolder.mNickname = (TextView) convertView.findViewById(R.id.tv_assistcoauch_subfragment_listitem_name);
            viewHolder.mGender = (TextView) convertView.findViewById(R.id.tv_assistcoauch_subfragment_listitem_gender);
            viewHolder.mKinds = (TextView) convertView.findViewById(R.id.tv_assistcoauch_subfragment_listitem_kinds);
            viewHolder.mPrice = (TextView) convertView.findViewById(R.id.tv_assistcoauch_subfragment_listitem_price);
            viewHolder.mDistance = (TextView) convertView.findViewById(R.id.tv_assistcoauch_subfragment_listitem_distance);

            convertView.setTag(viewHolder);
        } else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        NearbyAssistCoauchSubFragmentBean bean = mBeanList.get(position);

        // the user photo should be the image resource that can be retrieved from the network dynamically
        // here we set it as static data
        viewHolder.mPhoto.setDefaultImageResId(R.drawable.default_head);
        viewHolder.mPhoto.setImageUrl(bean.getPhoto(), mImgLoader);

        viewHolder.mNickname.setText(bean.getName());
        viewHolder.mGender.setText(bean.getGender());
        viewHolder.mGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, NearbyFragmentsCommonUtils.parseGenderDrawable(bean.getGender()), 0);
        viewHolder.mGender.setCompoundDrawablePadding(6);
        viewHolder.mKinds.setText(bean.getKinds());
        // TODO: the price here should use the String placeHolder to implement it,
        // TODO: otherwise, some exception would happen
        viewHolder.mPrice.setText(bean.getPrice());
        viewHolder.mDistance.setText(bean.getDistance());

        return convertView;
    }

    private static class ViewHolder
    {
        private NetworkImageView mPhoto;
        private TextView mNickname, mGender, mKinds, mPrice, mDistance;
    }
}

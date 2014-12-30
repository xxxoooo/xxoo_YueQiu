package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.adapter.bean.SearchAssistCoauchSubFragmentBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/25.
 *
 * 用于SearchFragment当中的助教子Fragment当中的ListView的adapter
 *
 */
public class SearchAssistCoauchSubFragmentListAdapter extends BaseAdapter
{

    private List<SearchAssistCoauchSubFragmentBean> mBeanList;

    private LayoutInflater mInflater;

    public SearchAssistCoauchSubFragmentListAdapter(Context context, ArrayList<SearchAssistCoauchSubFragmentBean> beanList)
    {
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
            convertView = mInflater.inflate(R.layout.search_assistcoauch_fragment_listitem_layout, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.mPhoto = (ImageView) convertView.findViewById(R.id.img_assistcoauch_subfragment_listitem_photo);
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

        SearchAssistCoauchSubFragmentBean bean = mBeanList.get(position);

        // the user photo should be the image resource that can be retrieved from the network dynamically
        // here we set it as static data
        viewHolder.mPhoto.setImageResource(R.drawable.ic_launcher);
        viewHolder.mNickname.setText(bean.getName());
        viewHolder.mGender.setText(bean.getGender());
        viewHolder.mKinds.setText(bean.getKinds());
        // TODO: the price here should use the String placeHolder to implement it,
        // TODO: otherwise, some exception would happen
        viewHolder.mPrice.setText(bean.getPrice());
        viewHolder.mDistance.setText(bean.getDistance());

        return convertView;
    }

    private static class ViewHolder
    {
        private ImageView mPhoto;
        private TextView mNickname, mGender, mKinds, mPrice, mDistance;
    }
}

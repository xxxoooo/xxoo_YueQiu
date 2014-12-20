package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by scguo on 14/12/19.
 *
 * 用于实现SearchActivity当中的球友Fragment的子Fragment当中的ListView的Adapter实现
 *
 */
public class SearchMateSubFragmentListAdapter extends BaseAdapter
{
    private static final String TAG = "SearchMateSubFragmentListAdapter";

    private LayoutInflater mInflater;
    private Context mContext;

    public SearchMateSubFragmentListAdapter(Context context)
    {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return 0;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {

        return 0;
    }


    /**
     * 我们采用R.layout.search_mate_fragment_listitem_layout作为这个ListView每一个Item的布局文件
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final ViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.search_mate_fragment_listitem_layout, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.mImgPhoto = (ImageView) convertView.findViewById(R.id.img_mate_subfragment_listitem_photo);
            viewHolder.mTvAccount = (TextView) convertView.findViewById(R.id.tv_mate_subfragment_listitem_account);
            viewHolder.mTvGender = (TextView) convertView.findViewById(R.id.tv_mate_subfragment_listitem_gender);
            viewHolder.mTvCondition = (TextView) convertView.findViewById(R.id.tv_mate_subfragment_listitem_detailedinfo);



            convertView.setTag(viewHolder);
        }

        return convertView;
    }


    private static class ViewHolder
    {
        public ImageView mImgPhoto;
        public TextView mTvAccount, mTvGender;
        // 这个字段值会根据Fragment的切换而获取不同的值
        public TextView mTvCondition;
    }
}

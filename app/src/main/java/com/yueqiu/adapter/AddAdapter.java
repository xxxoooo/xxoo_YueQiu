package com.yueqiu.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.bean.NearbyPeopleInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;

import java.util.List;

public class AddAdapter extends BaseAdapter {
    private Context mContext;
    private List<NearbyPeopleInfo.SearchPeopleItemInfo> mList;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    public AddAdapter(Context context, List<NearbyPeopleInfo.SearchPeopleItemInfo> list) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(mContext);
        this.mImageLoader = VolleySingleton.getInstance().getImgLoader();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_chatbar_account, null);
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (CustomNetWorkImageView) convertView.findViewById(R.id.chatbar_item_account_iv);
            viewHolder.mNickName = (TextView) convertView.findViewById(R.id.chatbar_item_account_tv);
            viewHolder.mGender = (TextView) convertView.findViewById(R.id.chatbar_item_gender_tv);
            viewHolder.mDistrict = (TextView) convertView.findViewById(R.id.chatbar_item_district_tv);
            //绑定viewholder对象
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mImageView.setDefaultImageResId(R.drawable.default_head);
        viewHolder.mImageView.setErrorImageResId(R.drawable.default_head);
        viewHolder.mImageView.setImageUrl("http://" + mList.get(position).getImg_url(), mImageLoader);
        viewHolder.mNickName.setText(mList.get(position).getUsername());
        viewHolder.mGender.setText(mList.get(position).getSex() == 1 ? mContext.getString(R.string.man) : mContext.getString(R.string.woman));
        viewHolder.mGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, NearbyFragmentsCommonUtils.parseGenderDrawable(mList.get(position).getSex() == 1 ? "男" : "女"), 0);
        String district = mList.get(position).getDistrict();
        if("".equals(district)){
            viewHolder.mDistrict.setVisibility(View.GONE);
        }else{
            viewHolder.mDistrict.setText(district);
            viewHolder.mDistrict.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    final class ViewHolder {
        public CustomNetWorkImageView mImageView;
        public TextView mNickName;
        public TextView mGender;
        public TextView mDistrict;
    }
}
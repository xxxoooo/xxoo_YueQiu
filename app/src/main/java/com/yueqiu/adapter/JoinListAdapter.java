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
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.VolleySingleton;

import java.util.List;

/**
 * Created by wangyun on 15/2/9.
 */
public class JoinListAdapter extends BaseAdapter{

    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;
    private List<UserInfo> mList;

    public JoinListAdapter(Context context, List<UserInfo> mList) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
        this.mList = mList;
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
            convertView = mInflater.inflate(R.layout.item_join_list,null);
            holder = new ViewHolder();
            holder.photo = (NetworkImageView) convertView.findViewById(R.id.join_photo);
            holder.name = (TextView) convertView.findViewById(R.id.join_user_name);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.photo.setDefaultImageResId(R.drawable.default_head);
        holder.photo.setErrorImageResId(R.drawable.default_head);
        holder.photo.setImageUrl(HttpConstants.IMG_BASE_URL + mList.get(position).getImg_url(),mImgLoader);
        holder.name.setText(mList.get(position).getUsername());
        return convertView;
    }

    class ViewHolder{
        NetworkImageView photo;
        TextView name;
    }
}

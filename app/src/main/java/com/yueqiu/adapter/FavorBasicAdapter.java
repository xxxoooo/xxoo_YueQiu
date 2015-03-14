package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.ISlideMenuBasic;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;

import java.util.List;

/**
 * Created by wangyun on 15/1/4.
 */
public class FavorBasicAdapter extends BaseAdapter {
    private List<ISlideMenuBasic> mList;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;

    public FavorBasicAdapter(Context context,List<ISlideMenuBasic> list){
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(mContext);
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
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
            convertView = mInflater.inflate(R.layout.item_favor_layout,null);
            holder = new ViewHolder();
            holder.imageView = (CustomNetWorkImageView) convertView.findViewById(R.id.favor_item_image);
            holder.title = (TextView) convertView.findViewById(R.id.favor_title);
            holder.content = (TextView) convertView.findViewById(R.id.favor_content);
            holder.dateTime = (TextView) convertView.findViewById(R.id.favor_time);
            holder.whole_bg = (RelativeLayout) convertView.findViewById(R.id.favor_item_bg_view);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        FavorInfo itemInfo = (FavorInfo) getItem(position);
        if(itemInfo.isChecked()){
            holder.whole_bg.setBackgroundColor(mContext.getResources().getColor(R.color.actionbar_color));
        }else{
            holder.whole_bg.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.published_item_bg));
        }
        holder.imageView.setDefaultImageResId(R.drawable.default_head);
        holder.imageView.setErrorImageResId(R.drawable.default_head);
        holder.imageView.setImageUrl("http://" + ((FavorInfo) mList.get(position)).getImg_url(),mImgLoader);
        holder.title.setText(((FavorInfo)mList.get(position)).getTitle());
        holder.content.setText(((FavorInfo)mList.get(position)).getContent());
        holder.dateTime.setText(((FavorInfo)mList.get(position)).getCreateTime());
        return convertView;
    }

    class ViewHolder{
        CustomNetWorkImageView imageView;
        TextView  title;
        TextView  content;
        TextView  dateTime;
        RelativeLayout whole_bg;
    }

    public void unCheckAll(){
        for(int i=0;i<getCount();i++){
            FavorInfo item = (FavorInfo) getItem(i);
            item.setChecked(false);
        }

    }
}

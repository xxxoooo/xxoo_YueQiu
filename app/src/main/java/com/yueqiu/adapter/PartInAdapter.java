package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.bean.ISlideMenuBasic;
import com.yueqiu.bean.PartInInfo;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.VolleySingleton;

import java.util.List;

/**
 * Created by wangyun on 15/2/7.
 */
public class PartInAdapter extends BaseAdapter{

    private List<ISlideMenuBasic> mList;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;

    public PartInAdapter(Context context,List<ISlideMenuBasic> list){
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
            convertView = mInflater.inflate(R.layout.item_published_info,null);
            holder = new ViewHolder();
            holder.image = (NetworkImageView) convertView.findViewById(R.id.published_item_image);
            holder.title = (TextView) convertView.findViewById(R.id.published_title);
            holder.content = (TextView) convertView.findViewById(R.id.published_content);
            holder.dateTime = (TextView) convertView.findViewById(R.id.published_time);
            holder.whole_bg = (RelativeLayout) convertView.findViewById(R.id.published_item_bg_view);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        PartInInfo itemInfo = (PartInInfo) getItem(position);
        if(itemInfo.isChecked()){
            holder.whole_bg.setBackgroundColor(mContext.getResources().getColor(R.color.actionbar_color));
        }else{
            holder.whole_bg.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.published_item_bg));
        }
        holder.image.setDefaultImageResId(R.drawable.default_head);
        holder.image.setErrorImageResId(R.drawable.default_head);
        holder.image.setImageUrl(HttpConstants.IMG_BASE_URL + ((PartInInfo) mList.get(position)).getImg_url(),mImgLoader);
        holder.title.setText(((PartInInfo) mList.get(position)).getTitle());
        holder.content.setText(((PartInInfo) mList.get(position)).getContent());
        holder.dateTime.setText(((PartInInfo) mList.get(position)).getDateTime());
        return convertView;
    }

    class ViewHolder{
        NetworkImageView image;
        TextView title;
        TextView  content;
        TextView  dateTime;
        RelativeLayout whole_bg;
    }
    public void unCheckAll(){
        for(int i=0;i<getCount();i++){
            PartInInfo item = (PartInInfo) getItem(i);
            item.setChecked(false);
        }
    }
}

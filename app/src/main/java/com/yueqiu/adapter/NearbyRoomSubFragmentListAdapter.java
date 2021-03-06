package com.yueqiu.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.yueqiu.R;
import com.yueqiu.bean.NearbyRoomBean;
import com.yueqiu.bean.NearbyRoomSubFragmentRoomBean;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomRoomNetView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/25.
 */
public class NearbyRoomSubFragmentListAdapter extends BaseAdapter{
    private List<NearbyRoomBean> mRoomList;
    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;

    private Context mContext;
    public NearbyRoomSubFragmentListAdapter(Context context, ArrayList<NearbyRoomBean> roomList){
        this.mContext = context;
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mRoomList = roomList;
    }

    @Override
    public int getCount(){
        return mRoomList.size();
    }

    @Override
    public Object getItem(int position){
        return mRoomList.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder;
        NearbyRoomBean item = mRoomList.get(position);
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_nearby_room_layout, parent, false);
            viewHolder.mRoomPhoto = (CustomRoomNetView) convertView.findViewById(R.id.img_room_subfragment_listitem_photo);
            viewHolder.mRoomName = (TextView) convertView.findViewById(R.id.tv_room_subfragment_listitem_roomname);
            viewHolder.mRoomAddress = (TextView) convertView.findViewById(R.id.tv_room_subfragment_listitem_roomaddress);
            viewHolder.mRoomLevel = (RatingBar) convertView.findViewById(R.id.rating_room_subfragment_listitem_rating);
            viewHolder.mRoomPrice = (TextView) convertView.findViewById(R.id.tv_room_subfragment_listitem_roomprice);
            viewHolder.mRoomDistance = (TextView) convertView.findViewById(R.id.tv_room_subfragment_listitem_roomdistance);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // then, init the elements in this layout
        viewHolder.mRoomPhoto.setDefaultImageResId(R.drawable.hall_default);
        viewHolder.mRoomPhoto.setDefaultImageResId(R.drawable.hall_default);
        viewHolder.mRoomPhoto.setImageUrl("http://" + item.getImg_url(), mImgLoader);
        viewHolder.mRoomLevel.setRating(Float.parseFloat(item.getOverall_rating()));
        viewHolder.mRoomLevel.setStepSize(0.02f); // 我们接受到的rating的值的总数为100，但是我们只有5个星星，所以我们每次移动的步骤就是5/100=0.02

        long distance = Long.valueOf(item.getRange());
        float show_distance = distance / 1000 ;
        if(show_distance > 10) {
            viewHolder.mRoomDistance.setText(mContext.getString(R.string.nearby_room_subfragment_listitem_range, show_distance));
        }else{
            viewHolder.mRoomDistance.setText(mContext.getString(R.string.in_meter,item.getRange()));
        }
        viewHolder.mRoomAddress.setText(item.getAddress());
        viewHolder.mRoomPrice.setText(String.valueOf(item.getPrice()));
        viewHolder.mRoomName.setText(item.getName());

        return convertView;
    }

    private static class ViewHolder{
        private CustomRoomNetView mRoomPhoto;
        private TextView mRoomName;
        private RatingBar mRoomLevel;
        private TextView mRoomPrice;
        private TextView mRoomAddress;
        private TextView mRoomDistance;
    }
}



































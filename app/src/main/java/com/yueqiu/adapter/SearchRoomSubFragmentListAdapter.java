package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.bean.SearchRoomSubFragmentRoomBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/25.
 */
public class SearchRoomSubFragmentListAdapter extends BaseAdapter
{
    private List<SearchRoomSubFragmentRoomBean> mRoomList;
    private LayoutInflater mInflater;
    public SearchRoomSubFragmentListAdapter(Context context, ArrayList<SearchRoomSubFragmentRoomBean> roomList)
    {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.mRoomList = roomList;
    }

    @Override
    public int getCount()
    {
        return mRoomList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mRoomList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        SearchRoomSubFragmentRoomBean item = mRoomList.get(position);

        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.search_room_fragment_listitem_layout, parent, false);

            viewHolder.mRoomPhoto = (ImageView) convertView.findViewById(R.id.img_room_subfragment_listitem_photo);
            viewHolder.mRoomName = (TextView) convertView.findViewById(R.id.tv_room_subfragment_listitem_roomname);
            viewHolder.mRoomAddress = (TextView) convertView.findViewById(R.id.tv_room_subfragment_listitem_roomaddress);
            viewHolder.mRoomLevel = (RatingBar) convertView.findViewById(R.id.rating_room_subfragment_listitem_rating);
            viewHolder.mRoomPrice = (TextView) convertView.findViewById(R.id.tv_room_subfragment_listitem_roomprice);
            viewHolder.mRoomDistance = (TextView) convertView.findViewById(R.id.tv_room_subfragment_listitem_roomdistance);

            convertView.setTag(viewHolder);
        } else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // then, init the elements in this layout
        // TODO: for now, we use the default image
//        viewHolder.mRoomPhoto.setImageResource();
        viewHolder.mRoomLevel.setRating(item.getLevel());
        viewHolder.mRoomDistance.setText(item.getDistance());
        viewHolder.mRoomAddress.setText(item.getDetailedAddress());
        viewHolder.mRoomPrice.setText(String.valueOf(item.getPrice()));
        viewHolder.mRoomName.setText(item.getRoomName());

        return convertView;
    }

    private static class ViewHolder
    {
        private ImageView mRoomPhoto;
        private TextView mRoomName;
        private RatingBar mRoomLevel;
        private TextView mRoomPrice;
        private TextView mRoomAddress;
        private TextView mRoomDistance;
    }



}



































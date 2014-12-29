package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.bean.ListItem;
import com.yueqiu.bean.SlideAccountItem;
import com.yueqiu.bean.SlideOtherItem;

import java.util.List;

/**
 * Created by wangyun on 14/12/29.
 */
public class SlideViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<ListItem> mList;
    private LayoutInflater mInflater;

    public SlideViewAdapter(Context context,List<ListItem> list){
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
    }
    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mList.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        ListItem item = (ListItem) getItem(position);
        int type = item.getType();
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }


    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = (ListItem) getItem(position);
        int type = item.getType();
        ViewAccountHolder accountHolder;
        ViewHolder holder;
        switch (type) {
           case ListItem.ITEM_ACCOUNT:
               if(convertView == null) {
                   convertView = mInflater.inflate(R.layout.more_account_layout, null);
                   accountHolder = new ViewAccountHolder();
                   accountHolder.image = (ImageView) convertView.findViewById(R.id.account_image);
                   accountHolder.name = (TextView) convertView.findViewById(R.id.account_name);
                   accountHolder.golden = (TextView) convertView.findViewById(R.id.account_golden);
                   convertView.setTag(accountHolder);
               }else{
                   accountHolder = (ViewAccountHolder) convertView.getTag();
               }
               SlideAccountItem accoutItem = (SlideAccountItem) item;
               accountHolder.image.setImageResource(accoutItem.getImgId());
               accountHolder.name.setText(accoutItem.getName());
               accountHolder.golden.setText(mContext.getString(R.string.slide_account_golden) + accoutItem.getGolden());
               break;
            case ListItem.ITEM_BASIC:
                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.more_other_layout,null);
                    holder = new ViewHolder();
                    holder.image = (ImageView) convertView.findViewById(R.id.other_image);
                    holder.name = (TextView) convertView.findViewById(R.id.other_name);
                    holder.hasMsg = (ImageView) convertView.findViewById(R.id.other_has_msg);
                    holder.bottom = (ImageView) convertView.findViewById(R.id.other_bottom);
                    convertView.setTag(holder);
                }else{
                    holder = (ViewHolder) convertView.getTag();
                }
                SlideOtherItem otherItem = (SlideOtherItem) item;
                holder.image.setImageResource(otherItem.getImgId());
                holder.name.setText(otherItem.getName());
                if(otherItem.hasMsg()){
                    holder.hasMsg.setVisibility(View.VISIBLE);
                }else{
                    holder.hasMsg.setVisibility(View.INVISIBLE);
                }
            break;
        }
        return convertView;
    }

    private class ViewAccountHolder{
        ImageView image;
        TextView  name;
        TextView  golden;
    }

    private class ViewHolder{
        ImageView image;
        TextView  name;
        ImageView hasMsg;
        ImageView bottom;
    }

}

package com.yueqiu.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yueqiu.R;
import com.yueqiu.activity.LoginActivity;
import com.yueqiu.bean.ISlideListItem;
import com.yueqiu.bean.SlideAccountItemISlide;
import com.yueqiu.bean.SlideOtherItemISlide;
import com.yueqiu.util.ImgUtil;
import com.yueqiu.util.VolleySingleton;

import java.util.List;

/**
 * Created by wangyun on 14/12/29.
 */
public class SlideViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<ISlideListItem> mList;
    private LayoutInflater mInflater;
    private ImageLoader mImgLoader;

    public SlideViewAdapter(Context context,List<ISlideListItem> list){
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();

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
        ISlideListItem item = (ISlideListItem) getItem(position);
        int type = item.getType();
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private static final String TAG_1 = "bitmap_debug";
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ISlideListItem item = (ISlideListItem) getItem(position);
        int type = item.getType();
        final ViewAccountHolder accountHolder;
        ViewHolder holder;
        switch (type) {
            case ISlideListItem.ITEM_ACCOUNT:
                if(convertView == null) {
                    convertView = mInflater.inflate(R.layout.item_more_account_layout, null);
                    accountHolder = new ViewAccountHolder();
                    accountHolder.image = (ImageView) convertView.findViewById(R.id.account_image);
                    accountHolder.label = (ImageView) convertView.findViewById(R.id.account_label);
                    accountHolder.name = (TextView) convertView.findViewById(R.id.account_name);
                    accountHolder.login = (TextView) convertView.findViewById(R.id.slide_login);
                    convertView.setTag(accountHolder);
                }else{
                    accountHolder = (ViewAccountHolder) convertView.getTag();
                }
                SlideAccountItemISlide accountItem = (SlideAccountItemISlide) item;
                int embedResId = R.drawable.lable_friend;
                if(accountItem.getTitle().equals(mContext.getString(R.string.nearby_billiard_assist_coauch_str))){
                    embedResId = R.drawable.lable_assistant;
                }else if(accountItem.getTitle().equals(mContext.getString(R.string.nearby_billiard_mate_str))){
                    embedResId = R.drawable.lable_friend;
                }else if(accountItem.getTitle().equals(mContext.getString(R.string.nearby_billiard_coauch_str))){
                    embedResId = R.drawable.lable_coach;
                }

                accountHolder.label.setVisibility(View.GONE);

                int user_id = accountItem.getUserId();
                if(user_id > 0){
                    accountHolder.login.setVisibility(View.GONE);
                    accountHolder.name.setVisibility(View.VISIBLE);
                    if(TextUtils.isEmpty(accountItem.getmNickName())) {
                        accountHolder.name.setText(accountItem.getName());
                    }else{
                        accountHolder.name.setText(accountItem.getmNickName());
                    }
                    final String img = accountItem.getImg();
                    String img_url = "http://" + img;
                   final int finallyEmbedResId = embedResId;
                   if (! TextUtils.isEmpty(img))
                   {
                       mImgLoader.get(
                               img_url, // pass this as test
                               new ImageLoader.ImageListener()
                               {
                                   @Override
                                   public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate)
                                   {
                                       Bitmap sourceBitmap = response.getBitmap();

                                       if (null != sourceBitmap)
                                       {
                                           Log.d("wy","bitmap is not null");
//                                           Bitmap sourceRound = ImgUtil.toRoundCorner(sourceBitmap,8);
                                           accountHolder.image.setImageBitmap(ImgUtil.embedBitmap(mContext.getResources(), sourceBitmap, finallyEmbedResId));
//                                           accountHolder.image.setImageBitmap(sourceBitmap);
                                       } else
                                       {
                                           Log.d("wy","bitmap is null");
                                           Bitmap tempSourceBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.head_img);
                                           accountHolder.image.setImageBitmap(ImgUtil.embedBitmap(mContext.getResources(), tempSourceBitmap, finallyEmbedResId));
//                                           accountHolder.image.setImageBitmap(tempSourceBitmap);
                                       }
                                   }

                                   @Override
                                   public void onErrorResponse(VolleyError error)
                                   {
                                        Log.d(TAG_1, " some error happened, and the detailed error info are: " + error.toString());
                                   }
                               },
                               mContext.getResources().getDimensionPixelOffset(R.dimen.account_photo_height),
                               mContext.getResources().getDimensionPixelOffset(R.dimen.account_photo_height)
                       );
                   } else
                   {
                       // 现在是没有Url的情况，即服务器端传递到的url为空的情况，我们需要在这里直接加载我们的默认图片
                       Bitmap tempSourceBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.head_img);
                       accountHolder.image.setImageBitmap(ImgUtil.embedBitmap(mContext.getResources(), tempSourceBitmap, finallyEmbedResId));
//                       accountHolder.image.setImageBitmap(tempSourceBitmap);
                   }
//                    accountHolder.label.setVisibility(View.VISIBLE);
//                    accountHolder.label.setImageResource(embedResId);

               } else {
                   accountHolder.name.setVisibility(View.GONE);
                   accountHolder.label.setVisibility(View.GONE);
                   accountHolder.login.setVisibility(View.VISIBLE);
                   accountHolder.login.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Intent intent = new Intent(mContext, LoginActivity.class);
                           mContext.startActivity(intent);
                       }
                   });
                   accountHolder.image.setImageResource(R.drawable.head_img);
               }
                //accountHolder.golden.setText(mContext.getString(R.string.slide_account_golden) + accountItem.getGolden());
                break;
            case ISlideListItem.ITEM_BASIC:
                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.item_more_other_layout,null);
                    holder = new ViewHolder();
                    holder.image = (ImageView) convertView.findViewById(R.id.other_image);
                    holder.name = (TextView) convertView.findViewById(R.id.other_name);
                    holder.hasMsg = (ImageView) convertView.findViewById(R.id.other_has_msg);
                    holder.bottom = convertView.findViewById(R.id.other_bottom);
                    convertView.setTag(holder);
                }else{
                    holder = (ViewHolder) convertView.getTag();
                }

                SlideOtherItemISlide otherItem = (SlideOtherItemISlide) item;
                holder.image.setImageResource(otherItem.getImgId());
                holder.name.setText(otherItem.getName());
                ///holder.hasMsg.setBackgroundColor(mContext.getResources().getColor(R.color.slide_drawer_more_other_has_msg));
                if(otherItem.hasMsg()){
                    holder.hasMsg.setVisibility(View.VISIBLE);
                }else{
                    holder.hasMsg.setVisibility(View.INVISIBLE);
                }
                if(position == mList.size()-1){
                    holder.bottom.setVisibility(View.INVISIBLE);
                }
                break;
        }
        return convertView;
    }

    private class ViewAccountHolder{
        ImageView image;
        ImageView label;
        TextView  name;
        TextView  golden;
        TextView login;
    }

    private class ViewHolder{
        ImageView image;
        TextView  name;
        ImageView hasMsg;
        View bottom;
    }



}

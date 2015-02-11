package com.yueqiu.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.activity.LoginActivity;
import com.yueqiu.bean.ISlideListItem;
import com.yueqiu.bean.SlideAccountItemISlide;
import com.yueqiu.bean.SlideOtherItemISlide;
import com.yueqiu.util.BitmapUtil;
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


    public SlideViewAdapter(Context context,List<ISlideListItem> list){
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
        ISlideListItem item = (ISlideListItem) getItem(position);
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
                   accountHolder.name = (TextView) convertView.findViewById(R.id.account_name);
                   accountHolder.login = (TextView) convertView.findViewById(R.id.slide_login);
                   convertView.setTag(accountHolder);
               }else{
                   accountHolder = (ViewAccountHolder) convertView.getTag();
               }
               SlideAccountItemISlide accountItem = (SlideAccountItemISlide) item;
               int embedResId = R.drawable.lable_friend;
               if(accountItem.getTitle().equals(mContext.getString(R.string.search_billiard_assist_coauch_str))){
                   embedResId = R.drawable.lable_assistant;
               }else if(accountItem.getTitle().equals(mContext.getString(R.string.search_billiard_mate_str))){
                   embedResId = R.drawable.lable_friend;
               }else if(accountItem.getTitle().equals(mContext.getString(R.string.search_billiard_coauch_str))){
                   embedResId = R.drawable.lable_coach;
               }

               int user_id = accountItem.getUserId();
               if(user_id > 0){



                   accountHolder.login.setVisibility(View.GONE);
                   accountHolder.name.setVisibility(View.VISIBLE);
                   accountHolder.name.setText(accountItem.getName());
                   String imgUrl = accountItem.getImg();
                   Bitmap source = null;
                   if(imgUrl.equals("")){
                       source = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.head_img);
                   }else {
//                       try {
//                           byte[] bitmapArray;
//                           bitmapArray = Base64.decode(img, Base64.DEFAULT);
//                           source = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
//                       } catch (Exception e) {
//                           e.printStackTrace();
//                       }
                       ////////////////////////////////////////////
//                       mImgLoader.queueImage(accountHolder.image,imgUrl);
                       ///////////////////////////////////////////
                   }
                   accountHolder.image.setImageBitmap(embedBitmap(mContext.getResources(),source,embedResId));
               }else {
                   accountHolder.name.setVisibility(View.GONE);
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

    private Bitmap embedBitmap(Resources resources,Bitmap source,int embedImgId){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
//            options.inMutable = true;
//        }
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //Bitmap source = BitmapFactory.decodeResource(resources,sourceId,options);
        Bitmap embedded;
        if(source.isMutable()){
            embedded = source;
        }else{
            embedded = source.copy(Bitmap.Config.ARGB_8888,true);
            source.recycle();
        }

        embedded.setHasAlpha(true);

        final int srcWidth = embedded.getWidth();
        final int srcHeight = embedded.getHeight();

        Canvas canvas = new Canvas(embedded);
        Bitmap mask = BitmapFactory.decodeResource(resources,embedImgId);
        final int maskWidth = mask.getWidth();
        final int maskHeight = mask.getHeight();

        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas.drawBitmap(mask,srcWidth-maskWidth,srcHeight-maskHeight,paint);

        mask.recycle();

        return embedded;
    }

}

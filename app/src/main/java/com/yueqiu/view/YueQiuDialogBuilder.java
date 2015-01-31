package com.yueqiu.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by wangyun on 15/1/12.
 */
public class YueQiuDialogBuilder extends AlertDialog.Builder{

    private View mTitleView;

    public YueQiuDialogBuilder(Context context) {
        super(context);

        mTitleView = View.inflate(context,R.layout.dialog_delete_title_layout,null);
        this.setCustomTitle(mTitleView);

    }
    public AlertDialog.Builder setTitle(String title){
        TextView title_tv = (TextView) mTitleView.findViewById(R.id.tv_title);
        title_tv.setText(title);
        return this;
    }
    public AlertDialog.Builder setTitle(CharSequence title){
        TextView title_tv = (TextView) mTitleView.findViewById(R.id.tv_title);
        title_tv.setText(title);
        return this;
    }
    public AlertDialog.Builder setTitle(int resouceId){
        TextView titleView = (TextView)mTitleView.findViewById(R.id.tv_title);
        titleView.setText(resouceId);
        return this;
    }
    public AlertDialog.Builder setIcon(Drawable drawable){
        ImageView iconView = (ImageView)mTitleView.findViewById(R.id.iv_title);
        iconView.setImageDrawable(drawable);
        return this;
    }
    public AlertDialog.Builder setIcon(int resouceId){
        ImageView iconView = (ImageView)mTitleView.findViewById(R.id.iv_title);
        iconView.setImageResource(resouceId);
        return this;
    }


}

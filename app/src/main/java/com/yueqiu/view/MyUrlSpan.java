package com.yueqiu.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.activity.ArticleReadActivity;

/**
 * Created by wangyun on 15/1/19.
 */
public class MyURLSpan extends URLSpan{

    private String mUrl;

    public MyURLSpan(String url){
        super(url);
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    /**
     * Performs the click action associated with this span.
     *
     * @param widget
     */
    @Override
    public void onClick(View widget) {

        Context context = widget.getContext();
        Intent intent = new Intent(context, ArticleReadActivity.class);
        intent.putExtra("url",getUrl());
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}

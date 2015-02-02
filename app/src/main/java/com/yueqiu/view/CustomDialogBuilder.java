package com.yueqiu.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.view.dlgeffect.BaseEffects;
import com.yueqiu.view.dlgeffect.ColorUtils;
import com.yueqiu.view.dlgeffect.EffectsType;

/**
 * Created by wangyun on 15/1/12.
 */
public class CustomDialogBuilder extends Dialog implements DialogInterface {



    private static Context sTmpContext;
    
    
    private EffectsType mType=null;
    
    private LinearLayout mLinearLayoutView;
    
    private RelativeLayout mRelativeLayoutView;
    
    private LinearLayout mLinearLayoutMsgView;
    
    private LinearLayout mLinearLayoutTopView;
    
    private FrameLayout mFrameLayoutCustomView;
    
    private View mDialogView;
    
    private View mDivider;
    
    private TextView mTitle;
    
    private TextView mMessage;
    
    private ImageView mIcon;
    
    private Button mSureButton;
    
    private Button mCancelButton;
    
    private int mDuration = -1;
    
    private static  int mOrientation=1;
    
    private boolean isCancelable=true;
    
    private static CustomDialogBuilder sInstance;
    
    public CustomDialogBuilder(Context context) {
        super(context);
        init(context);
        
    }
    public CustomDialogBuilder(Context context, int theme) {
        super(context, theme);
        init(context);
    }
    
    @Override
    protected void onCreate(Bundle savedsInstanceState) {
        super.onCreate(savedsInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width  = ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
        
    }
    
    public static CustomDialogBuilder getsInstance(Context context) {

        if (sInstance == null || !sTmpContext.equals(context)) {
            synchronized (CustomDialogBuilder.class) {
                if (sInstance == null || !sTmpContext.equals(context)) {
                    sInstance = new CustomDialogBuilder(context,R.style.dialog_untran);
                }
            }
        }
        sTmpContext = context;
        return sInstance;

    }
    
    private void init(Context context) {
        
        mDialogView = View.inflate(context, R.layout.dialog_layout, null);
        
        mLinearLayoutView=(LinearLayout)mDialogView.findViewById(R.id.parentPanel);
        mRelativeLayoutView=(RelativeLayout)mDialogView.findViewById(R.id.main);
        mLinearLayoutTopView=(LinearLayout)mDialogView.findViewById(R.id.topPanel);
        mLinearLayoutMsgView=(LinearLayout)mDialogView.findViewById(R.id.contentPanel);
        mFrameLayoutCustomView=(FrameLayout)mDialogView.findViewById(R.id.customPanel);
        
        mTitle = (TextView) mDialogView.findViewById(R.id.alertTitle);
        mMessage = (TextView) mDialogView.findViewById(R.id.message);
        mIcon = (ImageView) mDialogView.findViewById(R.id.icon);
        mDivider = mDialogView.findViewById(R.id.titleDivider);
        mSureButton=(Button)mDialogView.findViewById(R.id.sure_button);
        mCancelButton=(Button)mDialogView.findViewById(R.id.cancel_button);
        
        setContentView(mDialogView);
        
        this.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                
                mLinearLayoutView.setVisibility(View.VISIBLE);
                if(mType==null){
                    mType=EffectsType.SlideTop;
                }
                start(mType);
                
                
            }
        });

    }

    public CustomDialogBuilder withDividerColor(String colorString) {
        mDivider.setBackgroundColor(Color.parseColor(colorString));
        return this;
    }
    public CustomDialogBuilder withDividerColor(int color) {
        mDivider.setBackgroundColor(color);
        return this;
    }
    
    
    public CustomDialogBuilder withTitle(CharSequence title) {
        toggleView(mLinearLayoutTopView,title);
        mTitle.setText(title);
        return this;
    }
    
    public CustomDialogBuilder withTitleColor(String colorString) {
        mTitle.setTextColor(Color.parseColor(colorString));
        return this;
    }
    
    public CustomDialogBuilder withTitleColor(int color) {
        mTitle.setTextColor(color);
        return this;
    }
    
    public CustomDialogBuilder withMessage(int textResId) {
        toggleView(mLinearLayoutMsgView,textResId);
        mMessage.setText(textResId);
        return this;
    }
    
    public CustomDialogBuilder withMessage(CharSequence msg) {
        toggleView(mLinearLayoutMsgView,msg);
        mMessage.setText(msg);
        return this;
    }
    public CustomDialogBuilder withMessageColor(String colorString) {
        mMessage.setTextColor(Color.parseColor(colorString));
        return this;
    }
    public CustomDialogBuilder withMessageColor(int color) {
        mMessage.setTextColor(color);
        return this;
    }
    
    public CustomDialogBuilder withDialogColor(String colorString) {
        mLinearLayoutView.getBackground().setColorFilter(ColorUtils.getColorFilter(Color.parseColor(colorString)));
        return this;
    }
    
    public CustomDialogBuilder withDialogColor(int color) {
        //mLinearLayoutView.getBackground().setColorFilter(ColorUtils.getColorFilter(color));
        mLinearLayoutView.setBackgroundColor(sTmpContext.getResources().getColor(R.color.actionbar_color));
        return this;
    }
    
    public CustomDialogBuilder withIcon(int drawableResId) {
        mIcon.setImageResource(drawableResId);
        return this;
    }
    
    public CustomDialogBuilder withIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
        return this;
    }
    
    public CustomDialogBuilder withDuration(int duration) {
        this.mDuration=duration;
        return this;
    }
    
    public CustomDialogBuilder withEffect(EffectsType type) {
        this.mType=type;
        return this;
    }
    
    public CustomDialogBuilder withButtonDrawable(int resid) {
        mSureButton.setBackgroundResource(resid);
        mCancelButton.setBackgroundResource(resid);
        return this;
    }
    public CustomDialogBuilder withSureButtonText(CharSequence text) {
        mSureButton.setVisibility(View.VISIBLE);
        mSureButton.setText(text);
        
        return this;
    }
    public CustomDialogBuilder withCancelButtonText(CharSequence text) {
        mCancelButton.setVisibility(View.VISIBLE);
        mCancelButton.setText(text);
        return this;
    }
    public CustomDialogBuilder setSureButtonClick(View.OnClickListener click) {
        mSureButton.setOnClickListener(click);
        return this;
    }
    
    public CustomDialogBuilder setCancelButtonClick(View.OnClickListener click) {
        mCancelButton.setOnClickListener(click);
        return this;
    }

    public CustomDialogBuilder setSureButtonVisible(boolean visible){
        mSureButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    public CustomDialogBuilder setCancelButtonVisible(boolean visible){
        mCancelButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }
    
    
    public CustomDialogBuilder setCustomView(int resId, Context context) {
        View customView = View.inflate(context, resId, null);
        if (mFrameLayoutCustomView.getChildCount()>0){
            mFrameLayoutCustomView.removeAllViews();
        }
        mFrameLayoutCustomView.addView(customView);
        return this;
    }
    
    public CustomDialogBuilder setCustomView(View view, Context context) {
        if (mFrameLayoutCustomView.getChildCount()>0){
            mFrameLayoutCustomView.removeAllViews();
        }
        mFrameLayoutCustomView.addView(view);
        
        return this;
    }
    public CustomDialogBuilder isCancelableOnTouchOutside(boolean cancelable) {
        this.isCancelable=cancelable;
        this.setCanceledOnTouchOutside(cancelable);
        return this;
    }

    public CustomDialogBuilder isCancelable(boolean cancelable) {
        this.isCancelable=cancelable;
        this.setCancelable(cancelable);
        return this;
    }
    
    private void toggleView(View view,Object obj){
        if (obj==null){
            view.setVisibility(View.GONE);
        }else {
            view.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void show() {
        super.show();
    }
    
    private void start(EffectsType type){
        BaseEffects animator = type.getAnimator();
        if(mDuration != -1){
            animator.setDuration(Math.abs(mDuration));
        }
        animator.start(mRelativeLayoutView);
    }
    
    @Override
    public void dismiss() {
        super.dismiss();
        mSureButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
    }
}

package com.yueqiu.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.BitmapBean;
import com.yueqiu.bean.OnKeyboardHideListener;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.group.ImageFragment;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.ImgUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.CustomDialogBuilder;
import com.yueqiu.view.GroupTopicScrollView;
import com.yueqiu.view.IssueImageView;
import com.yueqiu.view.dlgeffect.EffectsType;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class FeedbackActivity extends FragmentActivity implements View.OnClickListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,EmojiconGridFragment.OnEmojiconClickedListener
{
    private static final String TAG = "FeedbackActivity";
    private static final String DIALOG_IMAGE = "image";
    private static final int CAMERA_REQUEST = 1;
    private static final int ALBUM_REQUEST = 2;
    private EditText mEtFeedbackTitle;
    private EmojiconEditText mEtFeedbackContent;
//    private ImageView mEmotionView,mBackupEmotionView;
    private TextView mDeletePhoto;
    private LinearLayout mAddImgContainer;
    private RelativeLayout mRootView,mExpressRelative;
    private IssueImageView mAddImg;
    private FrameLayout mEmotionContainer;
    private int mAddViewWidth, mAddViewHeight;
    private int mKeyboardHeight,mExpressionNewTop,mExpressionOldTop;
    private boolean mIsKeyboardShow,mIsEmotionShow;
    private ProgressBar mPreProgress;
    private TextView mPreTextView,mTakePhoto,mSelectPhoto;
    private Drawable mProgressDrawable;
    private GroupTopicScrollView mRootScrollView;

    private CustomDialogBuilder mDlgBuilder;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private EmojiconsFragment mEmojiFragment;

    private String mImgFilePath;

    private List<IssueImageView> mAddViewList = new ArrayList<IssueImageView>();
    private Map<String,String> mParamsMap = new HashMap<String, String>();
    private Map<String,String>  mUrlAndMethodMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mFragmentManager = getSupportFragmentManager();
        initActionBar();
        initView();

        ViewTreeObserver addImgObserver = mAddImg.getViewTreeObserver();
        addImgObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAddViewWidth = mAddImg.getWidth();
                mAddViewHeight = mAddImg.getHeight();
            }
        });
        ViewTreeObserver rootViewObserver = mRootView.getViewTreeObserver();
        rootViewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                mRootView.getRootView().getWindowVisibleDisplayFrame(rect);
                int screenHeight = mRootView.getRootView().getHeight();
                mKeyboardHeight = screenHeight - (rect.bottom - rect.top);
                Log.d(TAG,"keyboard height is ->" + mKeyboardHeight);
                //TODO:有问题，oldTop最后也有可能大于keyheight
                if(mExpressionNewTop > mKeyboardHeight && mKeyboardHeight > rect.top){
//                    mBackupEmotionView.setVisibility(View.VISIBLE);
//                    mEmotionView.setVisibility(View.INVISIBLE);
                }else{
//                    mBackupEmotionView.setVisibility(View.INVISIBLE);
//                    mEmotionView.setVisibility(View.VISIBLE);
                }

                if(mIsKeyboardShow){
                    mEmotionContainer.setVisibility(View.GONE);
                    mAddImgContainer.setVisibility(View.VISIBLE);
                    mIsEmotionShow = false;
                }else{
                    mOnKeyboardHideListener.onKeyBoardHide();
                }
            }
        });

        mExpressRelative.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.d(TAG,"expression new top is ->" + top);

                mExpressionNewTop = top;
                mExpressionOldTop = oldTop;

            }
        });
        mRootScrollView.setOnSizeChangeListener(new GroupTopicScrollView.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                if(oldh > h){
                    mIsKeyboardShow = true;
                }else{
                    mIsKeyboardShow = false;
                }
                Log.d(TAG,"isKeyBoardShow->" + mIsKeyboardShow);
            }
        });

    }

    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.search_feed_back_str));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initView(){
        mEtFeedbackTitle = (EditText) findViewById(R.id.feed_back_title);
        mEtFeedbackContent = (EmojiconEditText) findViewById(R.id.feed_back_content);
//        mEmotionView = (ImageView) findViewById(R.id.feed_back_emotion);
//        mEmotionView.setVisibility(View.GONE);
//        mBackupEmotionView = (ImageView) findViewById(R.id.feed_back_backup_expression);
        mAddImgContainer = (LinearLayout) findViewById(R.id.feed_back_add_img_container);
        mAddImg = (IssueImageView) findViewById(R.id.feed_back_add_img);
        mRootView = (RelativeLayout) findViewById(R.id.feedback_root_relative_view);
        mEmotionContainer = (FrameLayout) findViewById(R.id.feed_back_emotion_container);
        mRootScrollView = (GroupTopicScrollView) findViewById(R.id.feed_back_scroll_view);
        mExpressRelative = (RelativeLayout) findViewById(R.id.feed_back_emotion_relative);

        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreTextView = (TextView) findViewById(R.id.pre_text);
        mPreTextView.setText(getString(R.string.feed_backing));

        mAddImg.setOnClickListener(this);
//        mEmotionView.setOnClickListener(this);
//        mBackupEmotionView.setOnClickListener(this);

        mAddImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDlgBuilder = CustomDialogBuilder.getsInstance(FeedbackActivity.this);
                mDlgBuilder.withTitle(getString(R.string.select_photo))
                        .withTitleColor(Color.WHITE)
                        .withDividerColor(getResources().getColor(R.color.search_distance_color))
                        .withMessage(null)
                        .isCancelableOnTouchOutside(true)
                        .isCancelable(true)
                        .withDialogColor(R.color.actionbar_color)
                        .withDuration(700)
                        .withEffect(EffectsType.SlideLeft)
                        .setSureButtonVisible(false)
                        .withCancelButtonText(getString(R.string.btn_message_cancel))
                        .setCustomView(R.layout.dialog_long_click_view, v.getContext())
                        .setCancelButtonClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDlgBuilder.dismiss();
                            }
                        })
                        .show();

                mTakePhoto = (TextView) mDlgBuilder.findViewById(R.id.take_photo_now);
                mSelectPhoto = (TextView) mDlgBuilder.findViewById(R.id.select_photo_from_album);
                mDeletePhoto = (TextView) mDlgBuilder.findViewById(R.id.delete_photo);
                mTakePhoto.setOnClickListener(FeedbackActivity.this);
                mSelectPhoto.setOnClickListener(FeedbackActivity.this);
                mDeletePhoto.setOnClickListener(FeedbackActivity.this);
                return true;
            }
        });



    }

    private void submitFeedBack(){
        String title = mEtFeedbackTitle.getText().toString();
        if(TextUtils.isEmpty(title)){
            Utils.showToast(this,getString(R.string.please_input_title));
            return;
        }
        String content = mEtFeedbackContent.getText().toString();
        if(TextUtils.isEmpty(content)){
            Utils.showToast(this,getString(R.string.please_input_title));
            return;
        }

        mParamsMap.put(HttpConstants.FeedBack.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        mParamsMap.put(HttpConstants.FeedBack.TITLE,title);
        mParamsMap.put(HttpConstants.FeedBack.CONTENT,content);

        mPreProgress.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        HttpUtil.requestHttp(HttpConstants.FeedBack.URL,mParamsMap,HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","feedback response is ->" + response);
                try {
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS).sendToTarget();
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }
        });

    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    Utils.showToast(FeedbackActivity.this,getString(R.string.feed_back_success));
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(FeedbackActivity.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(FeedbackActivity.this, (String) msg.obj);
                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(FeedbackActivity.this,getString(R.string.http_request_time_out));
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mEmojiFragment = EmojiconsFragment.newInstance(false);
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.feed_back_emotion_container, mEmojiFragment).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImgUtil.clearImageView(mAddViewList);
        File dir = new File(FileUtil.getSdDirectory() + "/yueqiu/");
        if(dir.exists()){
            File[] files = dir.listFiles(new FileUtil.FileNameFilter("jpg"));
            Log.d(TAG, "files.size->" + files.length);
            for(File file : files){
                file.delete();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Utils.setFragmentActivityMenuColor(this);
        getMenuInflater().inflate(R.menu.feedback, menu);
        return true;
    }
//

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
            case R.id.feed_back_submit:
                if(Utils.networkAvaiable(this)) {
                    submitFeedBack();
                }else{
                    Utils.showToast(this,getString(R.string.network_not_available));
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(mIsEmotionShow){
                    mEmotionContainer.setVisibility(View.GONE);
                    mAddImgContainer.setVisibility(View.VISIBLE);
                    mIsEmotionShow = false;
                }else {
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                }
                break;
        }
        return true;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.feed_back_backup_expression:
            case R.id.feed_back_emotion:
                if(mIsEmotionShow){
                    mEmotionContainer.setVisibility(View.GONE);
                    mAddImgContainer.setVisibility(View.VISIBLE);
                    mIsEmotionShow = false;
                }
                //表情没有弹起
                else{
                    //设置表情弹起标志为true
                    mIsEmotionShow = true;
                    if(mIsKeyboardShow){
                        Utils.dismissInputMethod(this,mEtFeedbackContent);
                    }else{
                        mOnKeyboardHideListener.onKeyBoardHide();
                    }

                }
                break;
            case R.id.feed_back_add_img:
                if(mAddImg.getBitmapBean() != null){
                    BitmapBean bean = mAddImg.getBitmapBean();
                    if(bean == null)
                        return ;
                    FragmentManager fm = getSupportFragmentManager();
                    String imgPath = bean.imgFilePath;
                    Uri imgUri = bean.imgUri;
                    ImageFragment.newInstance(imgPath, imgUri == null ? null : imgUri.toString()).show(fm,DIALOG_IMAGE);
                }else {
                    mDlgBuilder = CustomDialogBuilder.getsInstance(this);
                    mDlgBuilder.withTitle(getString(R.string.select_photo))
                            .withTitleColor(Color.WHITE)
                            .withDividerColor(getResources().getColor(R.color.search_distance_color))
                            .withMessage(null)
                            .isCancelableOnTouchOutside(true)
                            .isCancelable(true)
                            .withDialogColor(R.color.actionbar_color)
                            .withDuration(700)
                            .withEffect(EffectsType.SlideBottom)
                            .setSureButtonVisible(false)
                            .withCancelButtonText(getString(R.string.btn_message_cancel))
                            .setCustomView(R.layout.dialog_select_photo, v.getContext())
                            .setCancelButtonClick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDlgBuilder.dismiss();
                                }
                            })
                            .show();

                    mTakePhoto = (TextView) mDlgBuilder.findViewById(R.id.take_photo_now);
                    mSelectPhoto = (TextView) mDlgBuilder.findViewById(R.id.select_photo_from_album);
                    mTakePhoto.setOnClickListener(this);
                    mSelectPhoto.setOnClickListener(this);
                }
                break;
            case R.id.delete_photo:
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mAddViewWidth,mAddViewHeight);
                params.setMargins(getResources().getDimensionPixelOffset(R.dimen.add_img_margin_left),0,0,0);
                params.gravity = Gravity.CENTER_VERTICAL;
                mAddImg.setLayoutParams(params);
                mAddImg.setBitmapBean(null);
                mAddImg.setImageResource(R.drawable.add_img_bg);
                mAddImg.setBackgroundColor(getResources().getColor(android.R.color.white));
                if(mDlgBuilder != null)
                    mDlgBuilder.dismiss();
                break;
            case R.id.take_photo_now:
                Uri imageFileUri = null;
                if(FileUtil.isSDCardReady()){
                    mImgFilePath = FileUtil.getSDCardPath() + "/yueqiu/" + UUID.randomUUID().toString() + ".jpg";
                    Log.d(TAG,"imgFilePath->" + mImgFilePath);
                    File imageFile = new File(mImgFilePath);
                    if(!imageFile.exists()){
                        imageFile.getParentFile().mkdirs();
                    }
                    imageFileUri = Uri.fromFile(imageFile);
                }
                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageFileUri);
                startActivityForResult(captureIntent, CAMERA_REQUEST);
                if(mDlgBuilder != null)
                    mDlgBuilder.dismiss();
                break;
            case R.id.select_photo_from_album:
                Intent albumIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(albumIntent,ALBUM_REQUEST);
                if(mDlgBuilder != null)
                    mDlgBuilder.dismiss();
                break;

        }
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mEtFeedbackContent);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mEtFeedbackContent,emojicon);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //capture photo from camera
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            BitmapDrawable bitmap = ImgUtil.getThumbnailScaledBitmap(this, mImgFilePath, mAddViewWidth, mAddViewHeight);
            BitmapBean bmpBean = new BitmapBean();
            bmpBean.bitmapDrawable = bitmap;
            bmpBean.imgFilePath = mImgFilePath;
            bmpBean.imgUri = null;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mAddViewWidth,mAddViewHeight);
            params.setMargins(getResources().getDimensionPixelOffset(R.dimen.add_img_margin_left),0,0,0);
            params.gravity = Gravity.CENTER_VERTICAL;
            mAddImg.setLayoutParams(params);
            mAddImg.setBitmapBean(bmpBean);
            mAddImg.setImageDrawable(bmpBean.bitmapDrawable);
            mAddViewList.add(mAddImg);
        }
        else if(requestCode == ALBUM_REQUEST && resultCode == RESULT_OK){
            Uri imageFileUri = data.getData();
            BitmapDrawable drawable = ImgUtil.getThumbnailScaleBitmapByUri(this,imageFileUri,mAddViewWidth,mAddViewHeight);
            BitmapBean bmpBean = new BitmapBean();
            bmpBean.bitmapDrawable = drawable;
            bmpBean.imgFilePath = null;
            bmpBean.imgUri = imageFileUri;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mAddViewWidth,mAddViewHeight);
            params.setMargins(getResources().getDimensionPixelOffset(R.dimen.add_img_margin_left),0,0,0);
            params.gravity = Gravity.CENTER_VERTICAL;
            mAddImg.setLayoutParams(params);
            mAddImg.setBitmapBean(bmpBean);
            mAddImg.setImageDrawable(bmpBean.bitmapDrawable);
            mAddImg.setBackgroundColor(getResources().getColor(android.R.color.black));
            mAddViewList.add(mAddImg);

        }
    }

    private OnKeyboardHideListener mOnKeyboardHideListener = new OnKeyboardHideListener() {
        @Override
        public void onKeyBoardHide() {
            if(mIsEmotionShow){
                mEmotionContainer.setVisibility(View.VISIBLE);
                mAddImgContainer.setVisibility(View.GONE);
            }else{
                mEmotionContainer.setVisibility(View.GONE);
                mAddImgContainer.setVisibility(View.VISIBLE);
            }
        }
    };
}

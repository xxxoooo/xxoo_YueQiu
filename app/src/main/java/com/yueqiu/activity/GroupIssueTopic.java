package com.yueqiu.activity;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.BitmapBean;
import com.yueqiu.bean.OnKeyboardHideListener;
import com.yueqiu.bean.SlideOtherItemISlide;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.group.ImageFragment;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.ImgUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.CustomDialogBuilder;
import com.yueqiu.view.GroupTopicScrollView;
import com.yueqiu.view.IssueImageView;
import com.yueqiu.view.dlgeffect.EffectsType;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupIssueTopic extends FragmentActivity implements View.OnClickListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,EmojiconGridFragment.OnEmojiconClickedListener
{
    private static final String TAG = "IssueTopic";
    private static final String DIALOG_IMAGE = "image";
    private static final int CAMERA_REQUEST = 1;
    private static final int ALBUM_REQUEST = 2;

    private static final int UPLOAD_SUCCESS = 42;
    private static final int UPLOAD_FAILED = 43;

    private EditText    mTitleEdit;
    private EmojiconEditText mContentEdit;
    private TextView    mTopicTypeTv,mTakePhoto,mSelectPhoto,mDeletePhoto;
    private ImageView   mIvExpression,mBackExpression;//TODO:图片按下的效果
    private IssueImageView mIvAddImg;
    private View        mLinearType;
//    private GridView    mGridView;
    private RelativeLayout mRootView,mExpressRelative;
    private GroupTopicScrollView mRootScrollView;
    private FrameLayout mEmotionContainer;
    private LinearLayout mAddImgContainer;

    private ProgressBar mPreProgress,mUploadProgress;
    private TextView mPreText;
    private Drawable mProgressDrawable;

    private String mTitle;
    private String mContent;
    private int mTopicType;
    private String mImgFilePath;
    private int mAddViewWidth,mAddViewHeight;
    private int mKeyboardHeight,mExpressionNewTop,mExpressionOldTop;

    private boolean mIsKeyboardShow;
    private boolean mIsEmotionShow;

    private CustomDialogBuilder mDlgBuilder;
//    private TopicImgAdapter mImgAdapter;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private EmojiconsFragment mEmojiFragment;

    private Map<String,String> mParamsMap = new HashMap<String, String>();
    private Map<String,String>  mUrlAndMethodMap = new HashMap<String, String>();
    private List<IssueImageView> mAddViewList = new ArrayList<IssueImageView>();
//    private List<BitmapBean> mBitmapBeanList = new ArrayList<BitmapBean>();
    private String mImg_url;
    private TextView mUploadFailedTv;
    private AsyncTask<Void,Void,Void> mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_issue_topic);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initView();
        mFragmentManager = getSupportFragmentManager();

        //TODO:这个viewTreeObserver是addImageView的treeObserver
        ViewTreeObserver addImgObserver = mIvAddImg.getViewTreeObserver();
        addImgObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAddViewWidth = mIvAddImg.getWidth();
                mAddViewHeight = mIvAddImg.getHeight();
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
                    mBackExpression.setVisibility(View.VISIBLE);
                    mIvExpression.setVisibility(View.INVISIBLE);
                }else{
                    mBackExpression.setVisibility(View.INVISIBLE);
                    mIvExpression.setVisibility(View.VISIBLE);
                }

                if(mIsKeyboardShow){
                    mEmotionContainer.setVisibility(View.GONE);
                    mAddImgContainer.setVisibility(View.VISIBLE);
//                    if(!mBitmapBeanList.isEmpty()){
//                        mGridView.setVisibility(View.GONE);
//                    }else{
//                        mGridView.setVisibility(View.VISIBLE);
//                    }
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
//                if(mIsKeyboardShow){
//                    mEmotionContainer.setVisibility(View.GONE);
//                    mIsEmotionShow = false;
//                }else{
//                    mOnKeyboardHideListener.onKeyBoardHide();
//                }
            }
        });
    }

    private void initView(){
        mTitleEdit = (EditText) findViewById(R.id.group_issue_title);
        mContentEdit = (EmojiconEditText) findViewById(R.id.group_issue_content);
        mTopicTypeTv = (TextView) findViewById(R.id.group_issue_type);
        mLinearType = findViewById(R.id.group_issue_type_linear);
//        mGridView = (GridView) findViewById(R.id.topic_grid_view);
//        mGridView.setVisibility(View.GONE);
        mRootView = (RelativeLayout) findViewById(R.id.topic_root_relative_view);
        mExpressRelative = (RelativeLayout) findViewById(R.id.topic_expression_relative);
        mRootScrollView = (GroupTopicScrollView) findViewById(R.id.topic_scroll_view);
        mEmotionContainer = (FrameLayout) findViewById(R.id.topic_emotion_container);
        mAddImgContainer = (LinearLayout) findViewById(R.id.topic_img_container);

        mIvExpression = (ImageView) findViewById(R.id.group_issue_express);
        mIvAddImg = (IssueImageView) findViewById(R.id.group_issue_add_img);
        mBackExpression = (ImageView) findViewById(R.id.topic_backup_expression);

        mPreText = (TextView) findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreText.setText(getString(R.string.activity_issuing));

        mUploadFailedTv = (TextView) findViewById(R.id.group_issue_upload_photo_fail_tv);
        mUploadProgress = (ProgressBar) findViewById(R.id.group_upload_photo_progress);


        mLinearType.setOnClickListener(this);
        mIvAddImg.setOnClickListener(this);
        mIvExpression.setOnClickListener(this);
        mBackExpression.setOnClickListener(this);

        mIvAddImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDlgBuilder = CustomDialogBuilder.getsInstance(GroupIssueTopic.this);
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
                mTakePhoto.setOnClickListener(GroupIssueTopic.this);
                mSelectPhoto.setOnClickListener(GroupIssueTopic.this);
                mDeletePhoto.setOnClickListener(GroupIssueTopic.this);
                return true;
            }
        });

    }
    private void requestPublish(){


        mTitle = mTitleEdit.getText().toString();
        if(TextUtils.isEmpty(mTitle)){
            Utils.showToast(this,getString(R.string.please_input_title));
            return;
        }
        mContent = mContentEdit.getText().toString();
        if(TextUtils.isEmpty(mContent)){
            Utils.showToast(this,getString(R.string.please_input_content));
            return;
        }

        if(TextUtils.isEmpty(mTopicTypeTv.getText())){
            Utils.showToast(this,getString(R.string.please_select_topic_type));
            return;
        }

        if(mTopicTypeTv.getText().equals(getString(R.string.billiard_get_master))){
            mTopicType = GroupSelectTopicTypeActivity.GET_MASTER;
        }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_be_master))){
            mTopicType = GroupSelectTopicTypeActivity.BE_MASTER;
        }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_find_friend))){
            mTopicType = GroupSelectTopicTypeActivity.FIND_FRIEND;
        }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_equipment))){
            mTopicType = GroupSelectTopicTypeActivity.EQUIP;
        }else{
            mTopicType = GroupSelectTopicTypeActivity.OTHER;
        }

        if(mIvAddImg.getBitmapBean() != null) {
            if (mUploadProgress.getVisibility() == View.VISIBLE) {
                Utils.showToast(GroupIssueTopic.this, getString(R.string.uploading_image));
                return;
            }else{
                Log.d("wy", "group img_url ->" + mImg_url);
                mParamsMap.put(HttpConstants.GroupIssue.IMG_URL,mImg_url);
            }
        }

        mParamsMap.put(HttpConstants.GroupIssue.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        mParamsMap.put(HttpConstants.GroupIssue.TYPE,String.valueOf(mTopicType));
        mParamsMap.put(HttpConstants.GroupIssue.TITLE,mTitle);
        mParamsMap.put(HttpConstants.GroupIssue.CONTENT,mContent);

        mPreProgress.setVisibility(View.VISIBLE);
        mPreText.setVisibility(View.VISIBLE);

        HttpUtil.requestHttp(HttpConstants.GroupIssue.URL,mParamsMap,HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
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
            mPreText.setVisibility(View.GONE);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    //TODO:如果后期需要缓存的话，这里还需要多做几步操作，向group表插入缓存数据同时还得向publish表插入数据
                    Intent broadCastIntent = new Intent(PublicConstant.SLIDE_PUBLISH_ACTION);
                    sendBroadcast(broadCastIntent);
                    Utils.showToast(GroupIssueTopic.this,getString(R.string.activity_submit_success));
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(GroupIssueTopic.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(GroupIssueTopic.this, (String) msg.obj);
                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(GroupIssueTopic.this,getString(R.string.http_request_time_out));
                    break;
                case UPLOAD_SUCCESS:
                    mUploadProgress.setVisibility(View.GONE);
                    mUploadFailedTv.setVisibility(View.GONE);
                    break;
                case UPLOAD_FAILED:
                    mUploadProgress.setVisibility(View.GONE);
                    mIvAddImg.setImageResource(R.drawable.add_img_bg);
                    mIvAddImg.setBitmapBean(null);
                    mUploadFailedTv.setVisibility(View.VISIBLE);
                    mUploadFailedTv.setText(getString(R.string.upload_image_fail));
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mEmojiFragment = EmojiconsFragment.newInstance(false);
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.topic_emotion_container, mEmojiFragment).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Utils.setFragmentActivityMenuColor(this);;
        getMenuInflater().inflate(R.menu.menu_group_issue_topic, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }else if(id == R.id.action_group_issue_topic){
            //TODO:如果是表情的话，给服务器传过去了，但是服务器返回来的是空值
            requestPublish();
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
//                    if(!mBitmapBeanList.isEmpty()){
//                        mGridView.setVisibility(View.VISIBLE);
//                    }else{
//                        mGridView.setVisibility(View.GONE);
//                    }
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
            case R.id.group_issue_type_linear:
                Intent intent = new Intent();
                intent.setClass(this,GroupSelectTopicTypeActivity.class);
                if(mTopicTypeTv.getText().equals(getString(R.string.billiard_get_master))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.GET_MASTER);
                }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_be_master))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.BE_MASTER);
                }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_find_friend))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.FIND_FRIEND);
                }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_equipment))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.EQUIP);
                }else{
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.OTHER);
                }
                startActivityForResult(intent,0);
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;
            case R.id.topic_backup_expression:
            case R.id.group_issue_express:
                //TODO:maybe has problem
                if(mIsEmotionShow){
                    mEmotionContainer.setVisibility(View.GONE);
                    mAddImgContainer.setVisibility(View.VISIBLE);
//                    if(!mBitmapBeanList.isEmpty()){
//                        mGridView.setVisibility(View.VISIBLE);
//                    }else{
//                        mGridView.setVisibility(View.GONE);
//                    }
                    mIsEmotionShow = false;
                }
                //表情没有弹起
                else{
                    //设置表情弹起标志为true
                    mIsEmotionShow = true;
                    if(mIsKeyboardShow){
                        Utils.dismissInputMethod(this,mContentEdit);
                    }else{
                        mOnKeyboardHideListener.onKeyBoardHide();
                    }

                }
                break;
            case R.id.delete_photo:
                mUploadProgress.setVisibility(View.GONE);
                if(mUploadTask != null){
                    mUploadTask.cancel(true);
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mAddViewWidth,mAddViewHeight);
                params.setMargins(getResources().getDimensionPixelOffset(R.dimen.add_img_margin_left),0,0,0);
                params.gravity = Gravity.CENTER_VERTICAL;
                mIvAddImg.setLayoutParams(params);
                mIvAddImg.setBitmapBean(null);
                mIvAddImg.setImageResource(R.drawable.add_img_bg);
                mIvAddImg.setBackgroundColor(getResources().getColor(android.R.color.white));
                if(mDlgBuilder != null)
                    mDlgBuilder.dismiss();
                break;
            case R.id.group_issue_add_img:
                if(mIvAddImg.getBitmapBean() != null){
                    BitmapBean bean = mIvAddImg.getBitmapBean();
                    if(bean == null)
                        return ;
                    FragmentManager fm = getSupportFragmentManager();
                    String imgPath = bean.imgFilePath;
                    Uri imgUri = bean.imgUri;
                    ImageFragment.newInstance(imgPath,imgUri == null ? null : imgUri.toString()).show(fm,DIALOG_IMAGE);
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
                            .withEffect(EffectsType.SlideLeft)
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"resultCode is ->" + resultCode);
        //select topic type
        if (requestCode == 0 && resultCode == RESULT_OK) {
            mTopicType = data.getIntExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.GET_MASTER);
            if(mTopicType == GroupSelectTopicTypeActivity.GET_MASTER){
                mTopicTypeTv.setText(getString(R.string.billiard_get_master));
            }else if(mTopicType == GroupSelectTopicTypeActivity.BE_MASTER){
                mTopicTypeTv.setText(getString(R.string.billiard_be_master));
            }else if(mTopicType == GroupSelectTopicTypeActivity.FIND_FRIEND){
                mTopicTypeTv.setText(getString(R.string.billiard_find_friend));
            }else if(mTopicType == GroupSelectTopicTypeActivity.EQUIP){
                mTopicTypeTv.setText(getString(R.string.billiard_equipment));
            }else{
                mTopicTypeTv.setText(getString(R.string.billiard_other));
            }
        }
        //capture photo from camera
        else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            Log.d(TAG,"camera request and result is ok");
            Log.d(TAG,"addViewWidth->" + mAddViewWidth );
            Log.d(TAG,"addViewHeight->" + mAddViewHeight );
            BitmapDrawable bitmap = ImgUtil.getThumbnailScaledBitmap(this, mImgFilePath, mAddViewWidth, mAddViewHeight);
            BitmapBean bmpBean = new BitmapBean();
            bmpBean.bitmapDrawable = bitmap;
            bmpBean.imgFilePath = mImgFilePath;
            bmpBean.imgUri = null;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mAddViewWidth,mAddViewHeight);
            params.setMargins(getResources().getDimensionPixelOffset(R.dimen.add_img_margin_left),0,0,0);
            params.gravity = Gravity.CENTER_VERTICAL;
            mIvAddImg.setLayoutParams(params);
            mIvAddImg.setBitmapBean(bmpBean);
            mIvAddImg.setImageDrawable(bmpBean.bitmapDrawable);
            mAddViewList.add(mIvAddImg);

            mUploadTask = new AsyncTask<Void,Void,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    uploadPhotoByPath(mImgFilePath);
                    return null;
                }
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mUploadProgress.setVisibility(View.VISIBLE);
                }
            };
            mUploadTask.execute();

        }
        else if(requestCode == ALBUM_REQUEST && resultCode == RESULT_OK){
            final Uri imageFileUri = data.getData();
            BitmapDrawable drawable = ImgUtil.getThumbnailScaleBitmapByUri(this,imageFileUri,mAddViewWidth,mAddViewHeight);
            BitmapBean bmpBean = new BitmapBean();
            bmpBean.bitmapDrawable = drawable;
            bmpBean.imgFilePath = null;
            bmpBean.imgUri = imageFileUri;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mAddViewWidth,mAddViewHeight);
            params.setMargins(getResources().getDimensionPixelOffset(R.dimen.add_img_margin_left),0,0,0);
            params.gravity = Gravity.CENTER_VERTICAL;
            mIvAddImg.setLayoutParams(params);
            mIvAddImg.setBitmapBean(bmpBean);
            mIvAddImg.setImageDrawable(bmpBean.bitmapDrawable);
            mIvAddImg.setBackgroundColor(getResources().getColor(android.R.color.black));
            mAddViewList.add(mIvAddImg);

            mUploadTask = new AsyncTask<Void,Void,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    uploadByUri(imageFileUri);
                    return null;
                }
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mUploadProgress.setVisibility(View.VISIBLE);
                }
            };
            mUploadTask.execute();
        }
    }

    private void uploadPhotoByPath(String path){
        File file = new File(path);
        FileInputStream in = null;
        byte[] buffer = new byte[(int) file.length()];
        StringBuilder imageStr = new StringBuilder();
        try {
            in = new FileInputStream(file);
            in.read(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        char[] hex = Hex.encodeHex(buffer);
        for(char c : hex){
            imageStr.append(c);
        }

        SyncHttpClient client = new SyncHttpClient();
        RequestParams params = new RequestParams();
        params .put(HttpConstants.ChangePhoto.IMG_DATA, imageStr.toString());
        params .put(HttpConstants.ChangePhoto.IMG_SUFFIX, "jpg");
        client.post("http://app.chuangyezheluntan.com/index.php/v1" + HttpConstants.ChangePhoto.URL,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","group upload response ->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(response.getJSONObject("result") != null) {
                                mImg_url = response.getJSONObject("result").getString("img_url");
                                mHandler.sendEmptyMessage(UPLOAD_SUCCESS);
                            }
                        }else{
                            mHandler.obtainMessage(UPLOAD_FAILED).sendToTarget();
                        }
                    }else{
                        mHandler.obtainMessage(UPLOAD_FAILED).sendToTarget();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.obtainMessage(UPLOAD_FAILED).sendToTarget();
            }
        });
    }

    private void uploadByUri(Uri uri){
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri,
                proj,                 // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null);                 // Order-by clause (ascending by name)

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        StringBuilder bitmapStr = new StringBuilder();
        File file = new File(cursor.getString(column_index));
        byte[] buffer = new byte[(int) file.length()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(buffer);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        char[] hex = Hex.encodeHex(buffer);
        for(int i=0;i<hex.length;i++) {
            bitmapStr.append(hex[i]);
        }
        SyncHttpClient client = new SyncHttpClient();
        RequestParams requestParams = new RequestParams();
        requestParams .put(HttpConstants.ChangePhoto.IMG_DATA, bitmapStr.toString());
        requestParams .put(HttpConstants.ChangePhoto.IMG_SUFFIX, "jpg");

        client.post("http://app.chuangyezheluntan.com/index.php/v1" + HttpConstants.ChangePhoto.URL,requestParams,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("wy","group upload response ->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(response.getJSONObject("result") != null) {
                                mImg_url = response.getJSONObject("result").getString("img_url");
                                mHandler.sendEmptyMessage(UPLOAD_SUCCESS);
                            }
                        }else{
                            mHandler.obtainMessage(UPLOAD_FAILED).sendToTarget();
                        }
                    }else{
                        mHandler.obtainMessage(UPLOAD_FAILED).sendToTarget();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

            }
        });
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mContentEdit);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mContentEdit,emojicon);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        ImgUtil.clearImageView(mAddViewList);
        File dir = new File(FileUtil.getSdDirectory() + "/yueqiu/");
        if(dir.exists()){
            File[] files = dir.listFiles(new FileUtil.FileNameFilter("jpg"));
            Log.d(TAG,"files.size->" + files.length);
            for(File file : files){
                file.delete();
            }
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

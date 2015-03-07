package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
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

import org.apache.commons.codec.binary.Hex;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wangyun on 15/3/4.
 */
public class DateIssueActivity extends FragmentActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,EmojiconGridFragment.OnEmojiconClickedListener{
    private static final int UPLOAD_SUCCESS = 42;
    private static final int UPLOAD_FAILED = 43;

    private static final int CAMERA_REQUEST = 10;
    private static final int ALBUM_REQUEST = 11;
    private static final String DIALOG_IMAGE = "image";
    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private static final int START_FLAG = 0;
    private static final int END_FLAG   = 1;
    private EditText mTitleEdit,mContactEdit,mPhoneEdit,mLocationEdit;
    private EmojiconEditText mIllustrationEdit;
    private TextView mStartTimeTv,mEndTimeTv,mChargeModuleTv,mPreTextView,mTakePhoto,mSelectPhoto,mDeletePhoto;
    private String mContactStr,mPhoneNumberStr;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private StringBuilder mStartTimeStr = new StringBuilder(),
            mEndTimeStr = new StringBuilder();
    private String mImgFilePath;
    private int mTimeFlag;
    private TextView mUploadFailedTv;
//    private ImageView mIvExpression;
    private IssueImageView mIvAddImg;
    //    private GridView mGridView;
    private LinearLayout mAddImgContainer;
    private GroupTopicScrollView mScrollView;
    private RelativeLayout mRootView;
    private FrameLayout mEmotionContainer;
    private static final int SELECT_TYPE = 0x02;
    private int mType = 0;
    private int mModel = 0;

    private int mAddViewWidth, mAddViewHeight;
    private int mKeyboardHeight;
    private boolean mIsKeyboardShow,mIsEmotionShow;

    private ProgressBar mPreProgress,mUploadProgress;
    private Drawable mProgressDrawable;
    //TODO:发布成功后他应该把发布成功的table_id返回来
//    private DateInfo mInfo;

    private CustomDialogBuilder mDlgBuilder;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private EmojiconsFragment mEmojiFragment;
//    private TopicImgAdapter mImgAdapter;

    private List<IssueImageView> mAddViewList = new ArrayList<IssueImageView>();
    private double mLatitude,mLongitude;
    private String mImg_url;
    private AsyncTask<Void,Void,Void> mUploadTask;


    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            switch (msg.what)
            {
                case PublicConstant.GET_SUCCESS:
                    Intent broadCastIntent = new Intent(PublicConstant.SLIDE_PUBLISH_ACTION);
                    sendBroadcast(broadCastIntent);
                    Toast.makeText(DateIssueActivity.this,
                            getString(R.string.activity_submit_success), Toast.LENGTH_SHORT).show();
                    //TODO:向publish数据库插入数据
                    //TODO:由于目前不需要缓存，所以这里先不操作数据库，后期
                    //TODO:再需要缓存时，再加入，不过这里的服务器返回的字段值缺少
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mPublishedDao.insertPublishInfo(setPublishInfo())
//                        }
//                    }).start();
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);

                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(msg.obj == null) {
                        Toast.makeText(DateIssueActivity.this,
                                getString(R.string.activity_submit_failed), Toast.LENGTH_LONG).show();
                    }else{
                        Utils.showToast(DateIssueActivity.this, (String) msg.obj);
                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Toast.makeText(DateIssueActivity.this,
                            getString(R.string.http_request_time_out), Toast.LENGTH_LONG).show();
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

    //TODO:不不太明白传经纬度有什么用
//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Bundle arg = intent.getExtras();
//            boolean isTimeout = arg.getBoolean(LocationUtil.ISTIMEOUT_KEY);
//            if(isTimeout){
//                mLatitude = 0;
//                mLongitude = 0;
//            }else{
//                Location location = arg.getParcelable(LocationUtil.LOCATION_KEY);
//                mLatitude = location.getLatitude();
//                mLongitude = location.getLongitude();
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_issue);
        initActionBar();
        initView();

        //获取当前经纬度
        //TODO:
//        startService(new Intent(DateIssueActivity.this, LocationUtil.class));

        mFragmentManager = getSupportFragmentManager();

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
        mScrollView.setOnSizeChangeListener(new GroupTopicScrollView.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                if(oldh > h){
                    mIsKeyboardShow = true;
                }else{
                    mIsKeyboardShow = false;
                }
            }
        });
    }

    private OnKeyboardHideListener mOnKeyboardHideListener = new OnKeyboardHideListener() {
        @Override
        public void onKeyBoardHide() {
            if(mIsEmotionShow){
                mEmotionContainer.setVisibility(View.VISIBLE);
                mAddImgContainer.setVisibility(View.GONE);
//                mGridView.setVisibility(View.GONE);
            }else{
                mEmotionContainer.setVisibility(View.GONE);
                mAddImgContainer.setVisibility(View.VISIBLE);
//                if(mBitmapBeanList.isEmpty()){
//                    mGridView.setVisibility(View.GONE);
//                }else{
//                    mGridView.setVisibility(View.VISIBLE);
//                }
            }
        }
    };
    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.issue_date));

    }
    private void initView(){

        mTitleEdit          = (EditText) findViewById(R.id.activitie_title_edit_text);
        mContactEdit    = (EditText) findViewById(R.id.activity_contact_edit_text);
        mPhoneEdit      = (EditText) findViewById(R.id.activity_contact_phone_edit_text);
        mIllustrationEdit   = (EmojiconEditText) findViewById(R.id.activity_illustrate_edit_text);

        mStartTimeTv      = (TextView) findViewById(R.id.activity_start_time_text);
        mEndTimeTv        = (TextView) findViewById(R.id.activity_end_time_text);
        mChargeModuleTv   = (TextView) findViewById(R.id.activity_charge_module_text);
        mLocationEdit       = (EditText) findViewById(R.id.activity_location_text);

//        mGridView = (GridView) findViewById(R.id.play_topic_grid_view);
//        mGridView.setVisibility(View.GONE);
        mAddImgContainer = (LinearLayout) findViewById(R.id.play_add_img_container);
        mScrollView = (GroupTopicScrollView) findViewById(R.id.play_topic_scroll_view);
        mRootView = (RelativeLayout) findViewById(R.id.play_topic_root_relative_view);
        mEmotionContainer = (FrameLayout) findViewById(R.id.play_topic_emotion_container);

        mIvAddImg = (IssueImageView) findViewById(R.id.activitiy_issues_iv_add_img);
//        mIvExpression = (ImageView) findViewById(R.id.activity_issues_expression);

        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreTextView = (TextView) findViewById(R.id.pre_text);
        mPreTextView.setText(getString(R.string.activity_issuing));

        mUploadFailedTv = (TextView) findViewById(R.id.play_issue_upload_photo_fail_tv);
        mUploadProgress = (ProgressBar) findViewById(R.id.play_upload_photo_progress);

        final Calendar calendar = Calendar.getInstance();

        mDatePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        mTimePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);

        mContactStr = YueQiuApp.sUserInfo.getUsername();
        mPhoneNumberStr = YueQiuApp.sUserInfo.getPhone();

        mContactEdit.setText(mContactStr);
        mPhoneEdit.setText(mPhoneNumberStr);

        mLocationEdit.setOnClickListener(this);
        mStartTimeTv.setOnClickListener(this);
        mEndTimeTv.setOnClickListener(this);
        mChargeModuleTv.setOnClickListener(this);
//        mIvExpression.setOnClickListener(this);
        mIvAddImg.setOnClickListener(this);

        mIvAddImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mUploadProgress.getVisibility() == View.GONE) {
                    mDlgBuilder = CustomDialogBuilder.getsInstance(DateIssueActivity.this);
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
                    mTakePhoto.setOnClickListener(DateIssueActivity.this);
                    mSelectPhoto.setOnClickListener(DateIssueActivity.this);
                    mDeletePhoto.setOnClickListener(DateIssueActivity.this);
                }
                return true;
            }
        });

//        mImgAdapter = new TopicImgAdapter();
//        mGridView.setAdapter(mImgAdapter);
//        mGridView.setOnItemClickListener(this);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
            case R.id.issue_activity:

                if(Utils.networkAvaiable(DateIssueActivity.this)) {
                    issueDate();
                }else{
                    Toast.makeText(DateIssueActivity.this,getString(R.string.network_not_available),Toast.LENGTH_SHORT).show();
                }

                break;
        }
        return true;
    }

    private void issueDate(){


        Map<String,Object> map = new HashMap<String, Object>();
        map.put(HttpConstants.DateIssue.USER_ID,YueQiuApp.sUserInfo.getUser_id());
//        if(mType == 0)
//        {
//            Toast.makeText(DateIssueActivity.this,getString(R.string.please_write_type),Toast.LENGTH_SHORT).show();
//            return;
//        }
//        map.put(HttpConstants.Play.TYPE,mType);
        String title = mTitleEdit.getText().toString().trim();
        if(title.equals(""))
        {
            Toast.makeText(DateIssueActivity.this,getString(R.string.activity_title_cannot_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        if(title.length() < 4){
            Toast.makeText(DateIssueActivity.this,getString(R.string.activity_title_length_less),Toast.LENGTH_SHORT).show();
            return;
        }
        if(title.length() > 30){
            Toast.makeText(DateIssueActivity.this,getString(R.string.activity_title_length_more),Toast.LENGTH_SHORT).show();
            return;
        }
        map.put(HttpConstants.DateIssue.TITLE, title);
        String address = mLocationEdit.getText().toString();
        if(TextUtils.isEmpty(address)){
            Utils.showToast(this,getString(R.string.activity_location_cannot_empty));
            return;
        }
        map.put(HttpConstants.DateIssue.ADDRESS,address);

        String beginTime = mStartTimeTv.getText().toString().trim();
        if(beginTime.equals(""))
        {
            Toast.makeText(DateIssueActivity.this,getString(R.string.activity_start_time_cannot_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        map.put(HttpConstants.DateIssue.BEGIN_TIME,beginTime);
        String datetime = mEndTimeTv.getText().toString().trim();
        if(datetime.equals(""))
        {
            Toast.makeText(DateIssueActivity.this,getString(R.string.activity_end_time_cannot_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if(Utils.stringToLong(beginTime,"yyyy-MM-dd HH-mm") > Utils.stringToLong(datetime,"yyyy-MM-dd HH-mm")){

                Toast.makeText(DateIssueActivity.this,getString(R.string.activity_start_cannot_more_than_end),Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        map.put(HttpConstants.DateIssue.END_TIME,datetime);

        if(mModel == 0)
        {
            Toast.makeText(DateIssueActivity.this,getString(R.string.activity_charge_module_cannot_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        map.put(HttpConstants.DateIssue.MODEL, mModel);
        String content = mIllustrationEdit.getText().toString();
//        if(content.equals(""))
//        {
//            Toast.makeText(DateIssueActivity.this,getString(R.string.please_write_content),Toast.LENGTH_SHORT).show();
//            return;
//        }
        map.put(HttpConstants.DateIssue.CONTENT, content);

        String name = mContactEdit.getText().toString();
        if(name.equals("")){
            Utils.showToast(DateIssueActivity.this,getString(R.string.please_write_name));
            return;
        }
        map.put(HttpConstants.DateIssue.NAME,name);
        String phone = mPhoneEdit.getText().toString();
        if(phone.equals("")){
            Utils.showToast(DateIssueActivity.this,getString(R.string.please_write_phone));
            return;
        }
        map.put(HttpConstants.DateIssue.PHONE,phone);

//
//        map.put(HttpConstants.Play.LAT,  0);
//        map.put(HttpConstants.Play.LNG, 0);

        if(mIvAddImg.getBitmapBean() != null) {
            if (mUploadProgress.getVisibility() == View.VISIBLE) {
                Utils.showToast(DateIssueActivity.this, getString(R.string.uploading_image));
                return;
            }else{
                Log.d("wy", "date img_url ->" + mImg_url);
                map.put(HttpConstants.DateIssue.IMG_URL,mImg_url);
            }
        }
        mPreProgress.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        HttpUtil.requestHttp(HttpConstants.DateIssue.URL, map, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy", "issue play response ->" + response);
                try {
                    if (!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            mHandler.sendEmptyMessage(PublicConstant.GET_SUCCESS);
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                            mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        } else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg"));
                        }
                    } else {
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setFragmentActivityMenuColor(this);
        getMenuInflater().inflate(R.menu.issue_activity,menu);
        return true;

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.activity_issues_expression:
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
                        Utils.dismissInputMethod(this,mIllustrationEdit);
                    }else{
                        mOnKeyboardHideListener.onKeyBoardHide();
                    }

                }
                break;
            case R.id.activitiy_issues_iv_add_img:

                if(mUploadFailedTv.getVisibility() == View.VISIBLE)
                    mUploadFailedTv.setVisibility(View.GONE);

                if(mIvAddImg.getBitmapBean() != null){
                    BitmapBean bean = mIvAddImg.getBitmapBean();
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
            case R.id.take_photo_now:
                Uri imageFileUri = null;
                if(FileUtil.isSDCardReady()){
                    mImgFilePath = FileUtil.getSDCardPath() + "/yueqiu/" + UUID.randomUUID().toString() + ".jpg";
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
            case R.id.activity_start_time_text:
                mDatePickerDialog.setVibrate(false);
                mDatePickerDialog.setYearRange(1985, 2028);
                mDatePickerDialog.setCloseOnSingleTapDay(false);
                mDatePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                mTimeFlag = START_FLAG;
                mStartTimeStr.delete(0,mStartTimeStr.length());
                break;
            case R.id.activity_end_time_text:
                mDatePickerDialog.setVibrate(false);
                mDatePickerDialog.setYearRange(1985, 2028);
                mDatePickerDialog.setCloseOnSingleTapDay(false);
                mDatePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                mTimeFlag = END_FLAG;
                mEndTimeStr.delete(0,mEndTimeStr.length());
                break;
            case R.id.activity_charge_module_text:
                Intent intent = new Intent();
                intent.setClass(this,SelectChargeModuleActivity.class);
                if(mChargeModuleTv.getText().equals(getString(R.string.charge_module_free))){
                    intent.putExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_FREE);
                    mModel = SelectChargeModuleActivity.MODULE_FREE;
                }else if(mChargeModuleTv.getText().equals(getString(R.string.charge_module_pay))){
                    intent.putExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_PAY);
                    mModel = SelectChargeModuleActivity.MODULE_PAY;
                }else{
                    intent.putExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_AA);
                    mModel = SelectChargeModuleActivity.MODULE_AA;
                }
                startActivityForResult(intent,0);
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            int module = data.getIntExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_FREE);
            if(module == SelectChargeModuleActivity.MODULE_FREE){
                mChargeModuleTv.setText(getString(R.string.charge_module_free));
                mModel = SelectChargeModuleActivity.MODULE_FREE;
            }else if(module == SelectChargeModuleActivity.MODULE_PAY){
                mModel = SelectChargeModuleActivity.MODULE_PAY;
                mChargeModuleTv.setText(getString(R.string.charge_module_pay));
            }else{
                mChargeModuleTv.setText(getString(R.string.charge_module_aa));
                mModel = SelectChargeModuleActivity.MODULE_AA;
            }
        }

        //capture photo from camera
        else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
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
//            mBitmapBeanList.add(bmpBean);
//            mImgAdapter.notifyDataSetChanged();
//            mGridView.setVisibility(View.VISIBLE);

            //TODO:上传图片

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
//            mBitmapBeanList.add(bmpBean);
//            mImgAdapter.notifyDataSetChanged();
//            mGridView.setVisibility(View.VISIBLE);
            //TODO:上传图片
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
            }.execute();
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
                Log.d("wy","play upload response ->" + response);
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
                Log.d("wy","play upload response ->" + response);
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
    protected void onStart() {
        super.onStart();
        mEmojiFragment = EmojiconsFragment.newInstance(false);
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.play_topic_emotion_container, mEmojiFragment).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImgUtil.clearImageView(mAddViewList);
        File dir = new File(FileUtil.getSdDirectory() + "/yueqiu/");
        if(dir.exists()){
            File[] files = dir.listFiles(new FileUtil.FileNameFilter("jpg"));
            for(File file : files){
                file.delete();
            }
        }
    }


    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mIllustrationEdit);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mIllustrationEdit,emojicon);
    }

    private String getType(String type)
    {
        if(type.equals(getString(R.string.group_activity)))
            return "0";
        else if(type.equals(getString(R.string.meet_star)))
            return "1";
        else if(type.equals(getString(R.string.billiard_show)))
            return "2";
        else if(type.equals(getString(R.string.complete)))
            return "3";
        else if(type.equals(getString(R.string.billiard_other)))
            return "4";
        else if(type.equals(""));
        return "0";
    }


    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        String monthStr = month < 9 ? "0" + ++month : String.valueOf(++month);
        String dayStr = day < 10 ? "0" + day : String.valueOf(day);
        if(mTimeFlag == START_FLAG){
            mStartTimeStr.append(year).append("-").append(monthStr).append("-").append(dayStr);
        }else if(mTimeFlag == END_FLAG){
            mEndTimeStr.append(year).append("-").append(monthStr).append("-").append(dayStr);
        }
        mTimePickerDialog.setVibrate(false);
        mTimePickerDialog.setCloseOnSingleTapMinute(false);
        mTimePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
    }


    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        String hourStr = hourOfDay < 10 ? "0" + hourOfDay : String.valueOf(hourOfDay);
        String minuteStr = minute < 10 ? "0" + minute : String.valueOf(minute);
        if(mTimeFlag == START_FLAG){
            mStartTimeStr.append(" ").append(hourStr).append("-").append(minuteStr);
            mStartTimeTv.setText(mStartTimeStr.toString());
        }else if(mTimeFlag == END_FLAG){
            mEndTimeStr.append(" ").append(hourStr).append("-").append(minuteStr);
            mEndTimeTv.setText(mEndTimeStr.toString());

        }
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

    
}

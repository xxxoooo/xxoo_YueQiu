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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
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
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.PublishedDao;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayIssueActivity extends FragmentActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,EmojiconGridFragment.OnEmojiconClickedListener
{

    private static final int CAMERA_REQUEST = 10;
    private static final int ALBUM_REQUEST = 11;
    private static final String DIALOG_IMAGE = "image";
    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private static final int START_FLAG = 0;
    private static final int END_FLAG   = 1;
    private EditText mTitleEdit,mContactEdit,mPhoneEdit,mLocationTv;
    private EmojiconEditText mIllustrationEdit;
    private TextView mStartTimeTv,mEndTimeTv,mChargeModuleTv,mPreTextView,mTakePhoto,mSelectPhoto,mDeletePhoto;
    private String mContactStr,mPhoneNumberStr;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private StringBuilder mStartTimeStr = new StringBuilder(),
            mEndTimeStr = new StringBuilder();
    private String mImgFilePath;
    private int mTimeFlag;
    private TextView mEtActivityType;
    private ImageView  mIvExpression;
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

    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    //TODO:发布成功后他应该把发布成功的table_id返回来
    private PlayInfo mInfo;

    private CustomDialogBuilder mDlgBuilder;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private EmojiconsFragment mEmojiFragment;
//    private TopicImgAdapter mImgAdapter;

    private List<IssueImageView> mAddViewList = new ArrayList<IssueImageView>();
//    private List<BitmapBean> mBitmapBeanList = new ArrayList<BitmapBean>();


    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case PublicConstant.GET_SUCCESS:
                    Intent broadCastIntent = new Intent(PublicConstant.SLIDE_PUBLISH_ACTION);
                    sendBroadcast(broadCastIntent);
                    Toast.makeText(PlayIssueActivity.this,
                            getString(R.string.activity_submit_success),Toast.LENGTH_SHORT).show();
                    mPreProgress.setVisibility(View.GONE);
                    mPreTextView.setVisibility(View.GONE);
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
                    Toast.makeText(PlayIssueActivity.this,
                            getString(R.string.activity_submit_failed), Toast.LENGTH_LONG).show();
                    mPreProgress.setVisibility(View.GONE);
                    mPreTextView.setVisibility(View.GONE);
                    break;
                case PublicConstant.TIME_OUT:
                    Toast.makeText(PlayIssueActivity.this,
                            getString(R.string.http_request_time_out), Toast.LENGTH_LONG).show();
                    mPreProgress.setVisibility(View.GONE);
                    mPreTextView.setVisibility(View.GONE);
                    break;

            }
        }
    };


    public PlayIssueActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_issues);
        initActionBar();
        initView();
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
        actionBar.setTitle(getString(R.string.search_publishing_dating_billiards_info_str));

    }
    private void initView(){
        mTitleEdit          = (EditText) findViewById(R.id.activitie_title_edit_text);
        mContactEdit    = (EditText) findViewById(R.id.activity_contact_edit_text);
        mPhoneEdit      = (EditText) findViewById(R.id.activity_contact_phone_edit_text);
        mIllustrationEdit   = (EmojiconEditText) findViewById(R.id.activity_illustrate_edit_text);

        mStartTimeTv      = (TextView) findViewById(R.id.activity_start_time_text);
        mEndTimeTv        = (TextView) findViewById(R.id.activity_end_time_text);
        mChargeModuleTv   = (TextView) findViewById(R.id.activity_charge_module_text);
        mLocationTv       = (EditText) findViewById(R.id.activity_location_text);

//        mGridView = (GridView) findViewById(R.id.play_topic_grid_view);
//        mGridView.setVisibility(View.GONE);
        mAddImgContainer = (LinearLayout) findViewById(R.id.play_add_img_container);
        mScrollView = (GroupTopicScrollView) findViewById(R.id.play_topic_scroll_view);
        mRootView = (RelativeLayout) findViewById(R.id.play_topic_root_relative_view);
        mEmotionContainer = (FrameLayout) findViewById(R.id.play_topic_emotion_container);

        mEtActivityType = (TextView) findViewById(R.id.activitie_title_edit_type);
        mIvAddImg = (IssueImageView) findViewById(R.id.activitiy_issues_iv_add_img);
        mIvExpression = (ImageView) findViewById(R.id.activity_issues_expression);

        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreTextView = (TextView) findViewById(R.id.pre_text);
        mPreTextView.setText(getString(R.string.activity_issuing));

        final Calendar calendar = Calendar.getInstance();

        mDatePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        mTimePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);

        mContactStr = YueQiuApp.sUserInfo.getUsername();
        mPhoneNumberStr = YueQiuApp.sUserInfo.getPhone();

        mContactEdit.setText(mContactStr);
        mPhoneEdit.setText(mPhoneNumberStr);

        mLocationTv.setOnClickListener(this);
        mStartTimeTv.setOnClickListener(this);
        mEndTimeTv.setOnClickListener(this);
        mChargeModuleTv.setOnClickListener(this);
        mEtActivityType.setOnClickListener(this);
        mIvExpression.setOnClickListener(this);
        mIvAddImg.setOnClickListener(this);

        mIvAddImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDlgBuilder = CustomDialogBuilder.getsInstance(PlayIssueActivity.this);
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
                mTakePhoto.setOnClickListener(PlayIssueActivity.this);
                mSelectPhoto.setOnClickListener(PlayIssueActivity.this);
                mDeletePhoto.setOnClickListener(PlayIssueActivity.this);
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
                final Map<String, Object> requests = getActivityInfo();
                if(Utils.networkAvaiable(PlayIssueActivity.this)) {
                    mPreProgress.setVisibility(View.VISIBLE);
                    mPreTextView.setVisibility(View.VISIBLE);
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();

                            if (requests != null) {
                                String result = HttpUtil.urlClient(HttpConstants.Play.PUBLISH, requests, HttpConstants.RequestMethod.GET);
                                if (result != null) {
                                    JSONObject object = Utils.parseJson(result);
                                    Message msg = new Message();
                                    try {
                                        if (!object.isNull("code")) {
                                            if (object.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                                                msg.what = PublicConstant.GET_SUCCESS;
                                            } else if (object.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                                                msg.what = PublicConstant.TIME_OUT;
                                            } else {
                                                msg.what = PublicConstant.REQUEST_ERROR;
                                            }
                                        }
                                        mHandler.sendMessage(msg);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }.start();
                }else{
                    Toast.makeText(PlayIssueActivity.this,getString(R.string.network_not_available),Toast.LENGTH_SHORT).show();
                }

                break;
        }
        return true;
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
            case R.id.delete_photo:
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
            case R.id.activitie_title_edit_type:
                Intent intentType = new Intent();
                intentType.setClass(this,PlaySelectType.class);
                intentType.putExtra("type",getType(mEtActivityType.getText().toString().trim()));
                startActivityForResult(intentType, SELECT_TYPE);
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;

            case R.id.activity_location_text:
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

    private Map<String, Object> getActivityInfo()
    {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("user_id",YueQiuApp.sUserInfo.getUser_id());
        if(mType == 0)
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.please_write_type),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("type",mType);
        String title = mTitleEdit.getText().toString().trim();
        if(title.equals(""))
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_title_cannot_empty),Toast.LENGTH_SHORT).show();
            return null;
        }
        if(title.length() < 4){
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_title_length_less),Toast.LENGTH_SHORT).show();
            return null;
        }
        if(title.length() > 30){
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_title_length_more),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("title", title);
        String address = mLocationTv.getText().toString();
        if(TextUtils.isEmpty(address)){
            Utils.showToast(this,getString(R.string.activity_location_cannot_empty));
            return null;
        }


        String beginTime = mStartTimeTv.getText().toString().trim();
        if(beginTime.equals(""))
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_start_time_cannot_empty),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("begin_time",beginTime);
        String datetime = mEndTimeTv.getText().toString().trim();
        if(datetime.equals(""))
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_end_time_cannot_empty),Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            if(Utils.stringToLong(beginTime,"yyyy-MM-dd HH-mm") > Utils.stringToLong(datetime,"yyyy-MM-dd HH-mm")){

                Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_start_cannot_more_than_end),Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        map.put("datetime",datetime);

        if(mModel == 0)
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_charge_module_cannot_empty),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("model", mModel);
        String content = mIllustrationEdit.getText().toString();
        if(content.equals(""))
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.please_write_content),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("content", content);
        map.put("lat",  0);
        map.put("lng", 0);
        return map;
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
        else if(requestCode == SELECT_TYPE && resultCode == RESULT_OK)
        {
            String type = data.getStringExtra("type");
            if(type.equals("0")) {
                mEtActivityType.setText(getString(R.string.group_activity));
                mType = 1;
            }
            else if(type.equals("1")) {
                mEtActivityType.setText(getString(R.string.meet_star));
                mType = 2;
            }
            else if(type.equals("2")) {
                mEtActivityType.setText(getString(R.string.billiard_show));
                mType = 3;
            }
            else if(type.equals("3")) {
                mEtActivityType.setText(getString(R.string.complete));
                mType = 4;
            }
            else if(type.equals("4")) {
                mEtActivityType.setText(getString(R.string.billiard_other));
                mType = 5;
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
            mIvAddImg.setLayoutParams(params);
            mIvAddImg.setBitmapBean(bmpBean);
            mIvAddImg.setImageDrawable(bmpBean.bitmapDrawable);
            mIvAddImg.setBackgroundColor(getResources().getColor(android.R.color.black));
            mAddViewList.add(mIvAddImg);
//            mBitmapBeanList.add(bmpBean);
//            mImgAdapter.notifyDataSetChanged();
//            mGridView.setVisibility(View.VISIBLE);
        }
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

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        BitmapBean bean = mBitmapBeanList.get(position);
//        if(bean == null)
//            return ;
//
//        FragmentManager fm = getSupportFragmentManager();
//        String imgPath = bean.imgFilePath;
//        Uri imgUri = bean.imgUri;
//        ImageFragment.newInstance(imgPath,imgUri == null ? null : imgUri.toString()).show(fm,DIALOG_IMAGE);
//    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mIllustrationEdit);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mIllustrationEdit,emojicon);
    }

//    private class TopicImgAdapter extends BaseAdapter {
//
//
//        @Override
//        public int getCount() {
//            return mBitmapBeanList.size();
//        }
//
//
//        @Override
//        public Object getItem(int position) {
//            return mBitmapBeanList.get(position);
//        }
//
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            if(convertView == null){
//                convertView = LayoutInflater.from(PlayIssueActivity.this).inflate(R.layout.item_topic_grid_view,null);
//                holder = new ViewHolder();
//                holder.imageView = (ImageView) convertView.findViewById(R.id.item_topic_imageview);
//                convertView.setTag(holder);
//            }else{
//                holder = (ViewHolder) convertView.getTag();
//            }
//            holder.imageView.setLayoutParams(new GridView.LayoutParams(mAddViewWidth,mAddViewHeight));
//            holder.imageView.getRootView().setBackgroundColor(getResources().getColor(android.R.color.black));
//            holder.imageView.setImageDrawable(mBitmapBeanList.get(position).bitmapDrawable);
//            mAddViewList.add(holder.imageView);
//            return convertView;
//        }
//
//        private class ViewHolder{
//            ImageView imageView;
//        }
//    }

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

    //TODO:由于目前不需要缓存，所以暂时先不调用该方法
    private List<PublishedInfo> setPublishInfo(PlayInfo info){
        List<PublishedInfo> list = new ArrayList<PublishedInfo>();
        PublishedInfo publishedInfo = new PublishedInfo();
        publishedInfo.setUser_id(YueQiuApp.sUserInfo.getUser_id());
        publishedInfo.setTable_id(info.getTable_id());
        publishedInfo.setType(PublicConstant.PUBLISHED_ACTIVITY_TYPE);
        publishedInfo.setTitle(info.getTitle());
        publishedInfo.setContent(info.getContent());
        publishedInfo.setDateTime(info.getCreate_time());
        //TODO:加入缓存后这个字段肯定要加入
//        publishedInfo.setSubType(Integer.parseInt(info.getType()));
        list.add(publishedInfo);
        return list;
    }
}

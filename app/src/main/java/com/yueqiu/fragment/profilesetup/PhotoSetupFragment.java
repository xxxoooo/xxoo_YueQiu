package com.yueqiu.fragment.profilesetup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.MyProfileActivity;
import com.yueqiu.bean.BitmapBean;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.group.ImageFragment;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.BitmapUtil;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.ImgUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomDialogBuilder;
import com.yueqiu.view.IssueImageView;
import com.yueqiu.view.dlgeffect.EffectsType;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by doushuqi on 15/1/4.
 */
public class PhotoSetupFragment extends Fragment implements View.OnClickListener{
    private static final int CAMERA_REQUEST = 1;
    private static final int ALBUM_REQUEST = 2;
    private static final String DIALOG_IMAGE = "image";
    private static final String DEFAULT_TAG = "default";
    private FragmentActivity mActivity;
    private NetworkImageView mPhotoView;
    private ImageView mAddImg;
    private View mView;
    private TextView mTakePhoto,mSelectPhoto;
    private RelativeLayout mPhotoContainer;
    private String mImgFilePath;
    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mPhotoViewDrawable,mProgressDrawable;
    private Bitmap mUploadBitmap;
    private ImageLoader mImgLoader;
    private CustomDialogBuilder mDlgBuilder;
    private int mAddViewWidth,mAddViewHeight;
    private List<IssueImageView> mAddViewList = new ArrayList<IssueImageView>();

    private Map<String,String> mParamMap = new HashMap<String, String>();
    private Map<String,String> mUrlAndMethodMap = new HashMap<String, String>();
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_upload_photo, container, false);

        mImgLoader = VolleySingleton.getInstance().getImgLoader();
        mPhotoView = (NetworkImageView) mView.findViewById(R.id.profile_photo_img);
        mAddImg = (ImageView) mView.findViewById(R.id.profile_photo_add_img);
        mPhotoContainer = (RelativeLayout) mView.findViewById(R.id.profile_photo_container);

        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        mPreTextView.setText(mActivity.getString(R.string.feed_backing));

        mProgressDrawable = new FoldingCirclesDrawable.Builder(mActivity).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        ViewTreeObserver addImgObserver = mPhotoView.getViewTreeObserver();
        addImgObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAddViewWidth = mPhotoView.getWidth();
                mAddViewHeight = mPhotoView.getHeight();
            }
        });

        mPhotoView.setDefaultImageResId(R.drawable.default_head);
        mPhotoView.setImageUrl("",mImgLoader);
        mAddImg.setOnClickListener(this);
        mPhotoView.setOnClickListener(this);



        return mView;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.profile_photo_add_img:
                mDlgBuilder = CustomDialogBuilder.getsInstance(mActivity);
                mDlgBuilder.withTitle(getString(R.string.select_photo))
                        .withTitleColor(Color.WHITE)
                        .withDividerColor(getResources().getColor(R.color.search_distance_color))
                        .withMessage(null)
                        .isCancelableOnTouchOutside(true)
                        .isCancelable(true)
                        .withDialogColor(R.color.actionbar_color)
                        .withDuration(700)
                        .withEffect(EffectsType.SlideRight)
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
                break;
            case R.id.profile_photo_img:
                mPhotoViewDrawable = mPhotoView.getDrawable();
                if(mPhotoViewDrawable != null){
                    DefaultImageFragment mDefault = new DefaultImageFragment();
                    mDefault.setStyle(DialogFragment.STYLE_NO_TITLE,0);
                    mDefault.show(mActivity.getSupportFragmentManager(),DEFAULT_TAG);

                }
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
        }
    }

    private  class DefaultImageFragment extends DialogFragment{


        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            ImageView view = new ImageView(getActivity());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params);
            view.setImageDrawable(mPhotoViewDrawable);
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            return view;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            BitmapDrawable bitmap = ImgUtil.getThumbnailScaledBitmap(mActivity, mImgFilePath, mAddViewWidth, mAddViewHeight);
            BitmapBean bmpBean = new BitmapBean();
            bmpBean.bitmapDrawable = bitmap;
            bmpBean.imgFilePath = mImgFilePath;
            bmpBean.imgUri = null;
            final IssueImageView image = new IssueImageView(mActivity);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mActivity.getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_width),
                    mActivity.getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_height));
            image.setLayoutParams(params);
            image.setBitmapBean(bmpBean);
            image.setImageDrawable(bitmap);
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            mPhotoContainer.removeViewAt(0);
            mPhotoContainer.addView(image,0);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BitmapBean bean = image.getBitmapBean();
                    if(bean == null)
                        return ;
                    FragmentManager fm = mActivity.getSupportFragmentManager();
                    String imgPath = bean.imgFilePath;
                    Uri imgUri = bean.imgUri;
                    ImageFragment.newInstance(imgPath, imgUri == null ? null : imgUri.toString()).show(fm,DIALOG_IMAGE);
                }
            });
            mAddViewList.add(image);
        }
        else if (requestCode == ALBUM_REQUEST && resultCode == Activity.RESULT_OK){
            Uri imageFileUri = data.getData();
            BitmapDrawable drawable = ImgUtil.getThumbnailScaleBitmapByUri(mActivity,imageFileUri,mAddViewWidth,mAddViewHeight);
            BitmapBean bmpBean = new BitmapBean();
            bmpBean.bitmapDrawable = drawable;
            bmpBean.imgFilePath = null;
            bmpBean.imgUri = imageFileUri;
            final IssueImageView image = new IssueImageView(mActivity);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mActivity.getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_width),
                    mActivity.getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_height));
            params.setMargins(getResources().getDimensionPixelOffset(R.dimen.add_img_margin_left),0,0,0);
            image.setLayoutParams(params);
            image.setBitmapBean(bmpBean);
            image.setImageDrawable(drawable);
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            image.setBackgroundColor(getResources().getColor(android.R.color.black));
            mPhotoContainer.removeViewAt(0);
            mPhotoContainer.addView(image, 0);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BitmapBean bean = image.getBitmapBean();
                    if(bean == null)
                        return ;
                    FragmentManager fm = mActivity.getSupportFragmentManager();
                    String imgPath = bean.imgFilePath;
                    Uri imgUri = bean.imgUri;
                    ImageFragment.newInstance(imgPath, imgUri == null ? null : imgUri.toString()).show(fm,DIALOG_IMAGE);
                }
            });
            mAddViewList.add(image);
        }
    }

    @Override
    public void onDestroy() {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.setup_confirm:
                changePhoto();
                break;
        }
        return true;
    }

    private void changePhoto(){

        View view = mPhotoContainer.getChildAt(0);
        if(view instanceof NetworkImageView){
            mActivity.finish();
        }else if(view instanceof IssueImageView){
            BitmapBean bean = ((IssueImageView)view).getBitmapBean();
            String imgFilePath = bean.imgFilePath;
            Uri imgUri = bean.imgUri;
            if(imgFilePath != null){
                mUploadBitmap = ImgUtil.getOriginBitmapByPath(mActivity,imgFilePath);
            }
            if(imgUri != null){
                mUploadBitmap = ImgUtil.getOriginBitmapByUri(mActivity,imgUri);
            }

            File file = new File(bean.imgFilePath);
            FileInputStream in = null;
            byte[] buffer = new byte[(int) file.length()];
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

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put(HttpConstants.ChangePhoto.IMG_DATA,new ByteArrayInputStream(buffer));
            params.put(HttpConstants.ChangePhoto.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
            params.put(HttpConstants.ChangePhoto.IMG_SUFFIX,"jpg");

            client.post("http://hxu0480201.my3w.com/index.php/v1" + HttpConstants.ChangePhoto.URL,params,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("wy","response is -> " + response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Log.d("wy","cacaca");
                }
            });

            //TODO:现在不知道是到底传什么？
//            if(bitmapStr != null){
//                mParamMap.put(HttpConstants.ChangePhoto.IMG_STREAM,bitmapStr);
//                mParamMap.put(HttpConstants.ChangePhoto.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
//
//                mUrlAndMethodMap.put(PublicConstant.URL,HttpConstants.ChangePhoto.URL);
//                mUrlAndMethodMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.POST);
//
//                new ChangePhotoTask(mParamMap).equals(mUrlAndMethodMap);
//            }
        }
    }

    private class ChangePhotoTask extends AsyncTaskUtil<String>{

        public ChangePhotoTask(Map<String, String> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgress.setVisibility(View.VISIBLE);
            mPreTextView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);

            try{
                if(!jsonObject.isNull("code")){
                    if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                        if(jsonObject.getString("result") != null) {
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS).sendToTarget();
                        }else{
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        }
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.REQUEST_ERROR){
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,jsonObject.getString("msg")).sendToTarget();
                    }
                }else{
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    Intent intent = new Intent();
                    intent.putExtra(MyProfileActivity.EXTRA_RESULT_ID, DEFAULT_TAG);
                    mActivity.setResult(Activity.RESULT_OK, intent);
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(mActivity, getString(R.string.http_request_time_out));
                    break;
                case PublicConstant.REQUEST_ERROR:
                    Utils.showToast(mActivity, getString(R.string.http_request_error));
                    break;

            }
        }
    };
}

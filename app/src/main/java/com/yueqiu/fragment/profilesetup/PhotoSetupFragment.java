package com.yueqiu.fragment.profilesetup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeGender;
import com.gotye.api.GotyeUser;
import com.gotye.api.Icon;
import com.gotye.api.PathUtil;
import com.gotye.api.listener.UserListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.MyProfileActivity;
import com.yueqiu.bean.BitmapBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.group.ImageFragment;
import com.yueqiu.util.BitmapUtil;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.ImgUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomDialogBuilder;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by doushuqi on 15/1/4.
 */
public class PhotoSetupFragment extends Fragment implements View.OnClickListener, UserListener {
    private static final int CAMERA_REQUEST = 1;
    private static final int ALBUM_REQUEST = 2;
    private static final String DIALOG_IMAGE = "image";
    private static final String DEFAULT_TAG = "default";
    private FragmentActivity mActivity;
    private NetworkImageView mPhotoView;
    private ImageView mAddImg;
    private View mView;
    private TextView mTakePhoto, mSelectPhoto;
    private RelativeLayout mPhotoContainer;
    private String mImgFilePath, mPhotoImgUrl;
    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mPhotoViewDrawable, mProgressDrawable;
    private Bitmap mUploadBitmap;
    private ImageLoader mImgLoader;
    private CustomDialogBuilder mDlgBuilder;
    private int mAddViewWidth, mAddViewHeight;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private List<IssueImageView> mAddViewList = new ArrayList<IssueImageView>();
    private GotyeAPI api;
    private GotyeUser mGotyeUser;

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

        Bundle args = getArguments();
        mPhotoImgUrl = "http://" + args.getString(PublicConstant.IMG_URL);

        mSharedPreferences = mActivity.getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

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
        mPhotoView.setErrorImageResId(R.drawable.default_head);
        mPhotoView.setImageUrl(mPhotoImgUrl, mImgLoader);
        mAddImg.setOnClickListener(this);
        mPhotoView.setOnClickListener(this);


        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        api = GotyeAPI.getInstance();
        api.addListerer(this);
        mGotyeUser = api.getCurrentLoginUser();
        api.requestUserInfo(mGotyeUser.name, true);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                if (mPhotoViewDrawable != null) {
                    DefaultImageFragment mDefault = new DefaultImageFragment();
                    mDefault.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    mDefault.show(mActivity.getSupportFragmentManager(), DEFAULT_TAG);

                }
                break;
            case R.id.take_photo_now:
                Uri imageFileUri = null;
                if (FileUtil.isSDCardReady()) {
                    mImgFilePath = FileUtil.getSDCardPath() + "/yueqiu/" + UUID.randomUUID().toString() + ".jpg";
                    File imageFile = new File(mImgFilePath);
                    if (!imageFile.exists()) {
                        imageFile.getParentFile().mkdirs();
                    }
                    imageFileUri = Uri.fromFile(imageFile);
                }
                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
                startActivityForResult(captureIntent, CAMERA_REQUEST);
                if (mDlgBuilder != null)
                    mDlgBuilder.dismiss();
                break;
            case R.id.select_photo_from_album:
                Intent albumIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(albumIntent, ALBUM_REQUEST);
                if (mDlgBuilder != null)
                    mDlgBuilder.dismiss();
        }
    }

    @Override
    public void onRequestUserInfo(int code, GotyeUser user) {
        Log.e("ddd","onRequestUserInfo");
    }

    @Override
    public void onModifyUserInfo(int code, GotyeUser user) {
        Log.e("ddd","onModifyUserInfo callback");
        if (code == 0) {
            Log.e("ddd", "Im 用户头像修改成功！！");
            Utils.showToast(getActivity(), "Im 用户头像修改成功！！");
        } else {
        }
    }

    @Override
    public void onSearchUserList(int code, List<GotyeUser> mList, int pagerIndex) {

    }

    @Override
    public void onAddFriend(int code, GotyeUser user) {

    }

    @Override
    public void onGetFriendList(int code, List<GotyeUser> mList) {

    }

    @Override
    public void onAddBlocked(int code, GotyeUser user) {

    }

    @Override
    public void onRemoveFriend(int code, GotyeUser user) {

    }

    @Override
    public void onRemoveBlocked(int code, GotyeUser user) {

    }

    @Override
    public void onGetBlockedList(int code, List<GotyeUser> mList) {

    }

    @Override
    public void onGetProfile(int code, GotyeUser user) {
        Log.e("ddd", "PhotoSetupFragment onGetProfile callback>>> code = " + code + " user = " + user);
    }

    private class DefaultImageFragment extends DialogFragment {


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

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
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
            mPhotoContainer.addView(image, 0);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BitmapBean bean = image.getBitmapBean();
                    if (bean == null)
                        return;
                    FragmentManager fm = mActivity.getSupportFragmentManager();
                    String imgPath = bean.imgFilePath;
                    Uri imgUri = bean.imgUri;
                    ImageFragment.newInstance(imgPath, imgUri == null ? null : imgUri.toString()).show(fm, DIALOG_IMAGE);
                }
            });
            mAddViewList.add(image);
        } else if (requestCode == ALBUM_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri imageFileUri = data.getData();
            BitmapDrawable drawable = ImgUtil.getThumbnailScaleBitmapByUri(mActivity, imageFileUri, mAddViewWidth, mAddViewHeight);
            BitmapBean bmpBean = new BitmapBean();
            bmpBean.bitmapDrawable = drawable;
            bmpBean.imgFilePath = null;
            bmpBean.imgUri = imageFileUri;
            final IssueImageView image = new IssueImageView(mActivity);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mActivity.getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_width),
                    mActivity.getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_height));
            params.setMargins(getResources().getDimensionPixelOffset(R.dimen.add_img_margin_left), 0, 0, 0);
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
                    if (bean == null)
                        return;
                    FragmentManager fm = mActivity.getSupportFragmentManager();
                    String imgPath = bean.imgFilePath;
                    Uri imgUri = bean.imgUri;
                    ImageFragment.newInstance(imgPath, imgUri == null ? null : imgUri.toString()).show(fm, DIALOG_IMAGE);
                }
            });
            mAddViewList.add(image);
        }

    }

    @Override
    public void onDestroy() {
        api.removeListener(this);
        super.onDestroy();
        ImgUtil.clearImageView(mAddViewList);
        File dir = new File(FileUtil.getSdDirectory() + "/yueqiu/");
        if (dir.exists()) {
            File[] files = dir.listFiles(new FileUtil.FileNameFilter("jpg"));
            for (File file : files) {
                file.delete();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.setup_confirm:
                changePhoto();
                break;
        }
        return true;
    }

    private void changePhoto() {

        View view = mPhotoContainer.getChildAt(0);
        if (view instanceof NetworkImageView) {
            mActivity.finish();
        } else if (view instanceof IssueImageView) {
            final BitmapBean bean = ((IssueImageView) view).getBitmapBean();
            final String imgFilePath = bean.imgFilePath;
            Log.e("ddd", "imgFilePath = " + imgFilePath);
            Uri imgUri = bean.imgUri;
//            if(imgFilePath != null){
//                mUploadBitmap = ImgUtil.getOriginBitmapByPath(mActivity,imgFilePath);
//            }
//            if(imgUri != null){
//                mUploadBitmap = ImgUtil.getOriginBitmapByUri(mActivity,imgUri);
//            }

            //TODO:拍照后上传
            if (bean.imgFilePath != null) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        File file = new File(bean.imgFilePath);
                        FileInputStream in = null;
                        byte[] buffer = new byte[(int) file.length()];
                        StringBuilder bitmapStr = new StringBuilder();
                        try {
                            in = new FileInputStream(file);
                            in.read(buffer);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        char[] hex = Hex.encodeHex(buffer);
                        for (char c : hex) {
                            bitmapStr.append(c);
                        }
                        SyncHttpClient client = new SyncHttpClient();
                        RequestParams requestParams = new RequestParams();
                        requestParams.put(HttpConstants.ChangePhoto.IMG_DATA, bitmapStr.toString());
                        requestParams.put(HttpConstants.ChangePhoto.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
                        requestParams.put(HttpConstants.ChangePhoto.IMG_SUFFIX, "jpg");

                        client.post("http://app.chuangyezheluntan.com/index.php/v1" + HttpConstants.ChangePhoto.URL, requestParams, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.d("wy", "response ->" + response);
                                try {
                                    if (!response.isNull("code")) {
                                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                                            if (response.getString("result") != null) {
                                                //TODO:upload IM service
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        setIMUserPhoto(imgFilePath);
                                                    }
                                                }).start();
                                                mHandler.obtainMessage(PublicConstant.GET_SUCCESS).sendToTarget();
                                            } else {
                                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                            }
                                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                                        } else if (response.getInt("code") == HttpConstants.ResponseCode.REQUEST_ERROR) {
                                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                                        } else {
                                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
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
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mPreProgress.setVisibility(View.VISIBLE);
                        mPreTextView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        mPreProgress.setVisibility(View.GONE);
                        mPreTextView.setVisibility(View.GONE);
                    }
                }.execute();
            }

            //TODO:选择图片的上传
            if (bean.imgUri != null) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getActivity().getContentResolver().query(bean.imgUri,
                                proj,                 // Which columns to return
                                null,       // WHERE clause; which rows to return (all rows)
                                null,       // WHERE clause selection arguments (none)
                                null);                 // Order-by clause (ascending by name)

                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();

                        StringBuilder bitmapStr = new StringBuilder();
                        final String filePath = cursor.getString(column_index);
                        File file = new File(filePath);
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
                        for (int i = 0; i < hex.length; i++) {
                            bitmapStr.append(hex[i]);
                        }
                        SyncHttpClient client = new SyncHttpClient();
                        RequestParams requestParams = new RequestParams();
                        requestParams.put(HttpConstants.ChangePhoto.IMG_DATA, bitmapStr.toString());
                        requestParams.put(HttpConstants.ChangePhoto.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
                        requestParams.put(HttpConstants.ChangePhoto.IMG_SUFFIX, "jpg");

                        client.post("http://app.chuangyezheluntan.com/index.php/v1" + HttpConstants.ChangePhoto.URL, requestParams, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.d("wy", "response ->" + response);
                                try {
                                    if (!response.isNull("code")) {
                                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                                            if (response.getJSONObject("result") != null) {
                                                String img_url = response.getJSONObject("result").getString("s_img_url");
                                                //TODO:上传IM服务器
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        setIMUserPhoto(filePath);
                                                    }
                                                }).start();
                                                mHandler.obtainMessage(PublicConstant.GET_SUCCESS, img_url).sendToTarget();
                                            } else {
                                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                            }
                                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                                        } else if (response.getInt("code") == HttpConstants.ResponseCode.REQUEST_ERROR) {
                                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                                        } else {
                                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
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

                            }
                        });
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        mPreProgress.setVisibility(View.GONE);
                        mPreTextView.setVisibility(View.GONE);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mPreProgress.setVisibility(View.VISIBLE);
                        mPreTextView.setVisibility(View.VISIBLE);
                    }

                }.execute();
            }
        }
    }

    private void setIMUserPhoto(String path) {
        Bitmap smaillBit = BitmapUtil.getSmallBitmap(path, 50, 50);
        String smallPath = BitmapUtil.saveBitmapFile(smaillBit);
        modifyUserIcon(smallPath);
    }

    private void modifyUserIcon(String smallImagePath) {
        mGotyeUser.setInfo("too young, too simple, too naive");// 修改扩展信息
        mGotyeUser.setNickname(mGotyeUser.name);
        Log.e("ddd", "icon = " + mGotyeUser.getIcon());
        mGotyeUser.setIcon(mGotyeUser.getIcon());
        Log.e("ddd", "name = " + mGotyeUser.name + "  info = " + mGotyeUser.getInfo() + " gender = " + mGotyeUser.getGender());
        Log.e("ddd", "smallImagePath = " + smallImagePath);
        Log.e("ddd", "mGotyeUser = " + mGotyeUser);
        int result = api.modifyUserInfo(mGotyeUser, smallImagePath);
        Log.e("ddd", "IM upload result = " + result);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PublicConstant.GET_SUCCESS:
                    String img_url = (String) msg.obj;
                    Log.e("ddd", "img_url = " + img_url);
                    mEditor.putString(DatabaseConstant.UserTable.IMG_URL, img_url);
                    mEditor.apply();

                    YueQiuApp.sUserInfo.setImg_url(img_url);

                    Intent intent = new Intent();
                    intent.putExtra(MyProfileActivity.EXTRA_RESULT_ID, DEFAULT_TAG);
                    intent.putExtra(PublicConstant.IMG_URL, img_url);
                    mActivity.setResult(Activity.RESULT_OK, intent);

                    Intent updatePhoto = new Intent();
                    updatePhoto.setAction(PublicConstant.SLIDE_ACCOUNT_ACTION);
                    mActivity.sendBroadcast(updatePhoto);
                    mActivity.finish();
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

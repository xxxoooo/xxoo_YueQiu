package com.yueqiu.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.BitmapBean;
import com.yueqiu.bean.INewPhotoItem;
import com.yueqiu.bean.NewPhotoAddImage;
import com.yueqiu.bean.NewPhotoShow;
import com.yueqiu.bean.NewPhotoUpload;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.group.ImageFragment;
import com.yueqiu.fragment.profilesetup.NewPhotoFragment;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomDialogBuilder;
import com.yueqiu.view.CustomNetWorkImageView;
import com.yueqiu.view.IssueImageView;
import com.yueqiu.view.dlgeffect.EffectsType;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wangyun on 15/3/30.
 */
public class NewPhotoAdapter extends BaseAdapter{
    private static final int CAMERA_REQUEST = 1;
    private static final int ALBUM_REQUEST = 2;
    private static final String DIALOG_IMAGE = "image";
    private FragmentActivity mContext;
    private LayoutInflater mInflater;
    private List<INewPhotoItem> mList;
    private ImageLoader mImgLoader;
    private CustomDialogBuilder mDlgBuilder;
    private TextView mTakePhoto, mSelectPhoto;
    public String mImgFilePath,mPhotoImgUrl;
    private NewPhotoFragment mFragment;
    public List<IssueImageView> mAddViewList = new ArrayList<IssueImageView>();

    public NewPhotoAdapter(FragmentActivity context,List<INewPhotoItem> list,NewPhotoFragment fragment) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mList = list;
        this.mImgLoader = VolleySingleton.getInstance().getImgLoader();
        this.mFragment = fragment;
    }

    @Override
    public int getCount() {
        return mList.size();
    }


    @Override
    public INewPhotoItem getItem(int position) {
        return mList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        INewPhotoItem item = getItem(position);
        int type = item.getType();
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewAddHolder addHolder;
        ViewUploadHolder uploadHolder;
        final ViewShowHolder showHolder;
        int type = getItemViewType(position);
        switch(type){
            case INewPhotoItem.ADD_IMG:
                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.item_new_photo_add_img,null);
                    addHolder = new ViewAddHolder();
                    addHolder.addView = (ImageView) convertView.findViewById(R.id.new_photo_add_img);
                    convertView.setTag(addHolder);
                }else{
                    addHolder = (ViewAddHolder) convertView.getTag();
                }
                NewPhotoAddImage addItem = (NewPhotoAddImage) getItem(position);
                addHolder.addView.setImageResource(R.drawable.add_img_bg);
                if(addItem.isEnable()){
                    addHolder.addView.setEnabled(true);
                }else{
                    addHolder.addView.setEnabled(false);
                }
                addHolder.addView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDlgBuilder = CustomDialogBuilder.getsInstance(mContext);
                        mDlgBuilder.withTitle(mContext.getString(R.string.select_photo))
                                .withTitleColor(Color.WHITE)
                                .withDividerColor(mContext.getResources().getColor(R.color.search_distance_color))
                                .withMessage(null)
                                .isCancelableOnTouchOutside(true)
                                .isCancelable(true)
                                .withDialogColor(R.color.actionbar_color)
                                .withDuration(700)
                                .withEffect(EffectsType.SlideRight)
                                .setSureButtonVisible(false)
                                .withCancelButtonText(mContext.getString(R.string.btn_message_cancel))
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
                        mTakePhoto.setOnClickListener(mDlgClickListener);
                        mSelectPhoto.setOnClickListener(mDlgClickListener);
                    }
                });
                break;
            case INewPhotoItem.SHOW_IMG:
                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.item_new_photo_show,null);
                    showHolder = new ViewShowHolder();
                    showHolder.showView = (CustomNetWorkImageView) convertView.findViewById(R.id.new_photo_show);
                    convertView.setTag(showHolder);
                }else{
                    showHolder = (ViewShowHolder) convertView.getTag();
                }
                showHolder.showView.setDefaultImageResId(R.drawable.default_head);
                showHolder.showView.setErrorImageResId(R.drawable.default_head);
                final NewPhotoShow showItem = (NewPhotoShow) getItem(position);
                final String img_url = "http://app.chuangyezheluntan.com/index.php/v1/system/getImg/img_url/" + showItem.getImgUrl();
                showHolder.showView.setImageUrl(img_url,mImgLoader);
                showHolder.showView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPhotoImgUrl = img_url;
                        Drawable mPhotoViewDrawable = showHolder.showView.getDrawable();
                        if (mPhotoViewDrawable != null) {
                            DefaultImageFragment mDefault = new DefaultImageFragment();
                            mDefault.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                            mDefault.show(mContext.getSupportFragmentManager(), "new_photo");

                        }
                    }
                });

                showHolder.showView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new AlertDialog.Builder(mContext).setIcon(android.R.drawable.ic_delete)
                                .setTitle(mContext.getString(R.string.action_delete))
                                .setMessage(mContext.getString(R.string.delete_photo))
                                .setPositiveButton(mContext.getString(R.string.sure),new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(Utils.networkAvaiable(mContext)){
                                            deletePhoto(position,showItem.getImgUrl());
                                        }else{
                                            Toast.makeText(mContext, mContext.getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).setNegativeButton(mContext.getString(R.string.published_cancel_str),null).create().show();
                        return true;
                    }
                });
                break;
            case INewPhotoItem.UPLOAD_IMG:
                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.item_new_photo_upload,null);
                    uploadHolder = new ViewUploadHolder();
                    uploadHolder.uploadView = (IssueImageView) convertView.findViewById(R.id.new_photo_upload);
                    convertView.setTag(uploadHolder);
                }else{
                    uploadHolder = (ViewUploadHolder) convertView.getTag();
                }
                final NewPhotoUpload uploadItem = (NewPhotoUpload) getItem(position);
                final BitmapBean bean = uploadItem.getBitmapBean();
                uploadHolder.uploadView.setBitmapBean(bean);
                uploadHolder.uploadView.setImageDrawable(bean.bitmapDrawable);
                uploadHolder.uploadView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bean == null)
                            return;
                        FragmentManager fm = mContext.getSupportFragmentManager();
                        String imgPath = bean.imgFilePath;
                        Uri imgUri = bean.imgUri;
                        ImageFragment.newInstance(imgPath, imgUri == null ? null : imgUri.toString()).show(fm, DIALOG_IMAGE);
                    }
                });

                uploadHolder.uploadView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                            new AlertDialog.Builder(mContext).setIcon(android.R.drawable.ic_delete)
                                    .setTitle(mContext.getString(R.string.action_delete))
                                    .setMessage(mContext.getString(R.string.delete_photo))
                                    .setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (uploadItem.isUploaded()) {
                                                if (Utils.networkAvaiable(mContext)) {
                                                    deletePhoto(position, uploadItem.getImgUrl());
                                                } else {
                                                    Toast.makeText(mContext, mContext.getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                                                }
                                            }else{
                                                mList.remove(position);
                                                notifyDataSetChanged();
                                            }
                                        }
                                    }).setNegativeButton(mContext.getString(R.string.published_cancel_str), null).create().show();
                            return true;
                    }
                });
                mAddViewList.add(uploadHolder.uploadView);
                break;
        }

        return convertView;
    }


    private View.OnClickListener mDlgClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
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
                    mFragment.startActivityForResult(captureIntent, CAMERA_REQUEST);
                    if (mDlgBuilder != null)
                        mDlgBuilder.dismiss();
                    break;
                case R.id.select_photo_from_album:
                    Intent albumIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    mFragment.startActivityForResult(albumIntent, ALBUM_REQUEST);
                    if (mDlgBuilder != null)
                        mDlgBuilder.dismiss();
            }
        }
    };
    class ViewUploadHolder{
        IssueImageView uploadView;
    }

    class ViewShowHolder{
        CustomNetWorkImageView showView;
    }
     class ViewAddHolder{
         ImageView addView;
     }

    private class DefaultImageFragment extends DialogFragment {

        @Override
        public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
            final ImageView view = new ImageView(getActivity());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params);
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            if (! TextUtils.isEmpty(mPhotoImgUrl))
            {
                mImgLoader.get(
                        mPhotoImgUrl, // pass this as test
                        new ImageLoader.ImageListener()
                        {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate)
                            {
                                Bitmap sourceBitmap = response.getBitmap();
                                if (null != sourceBitmap)
                                {
                                    view.setImageBitmap(sourceBitmap);
                                } else
                                {
                                    Bitmap tempSourceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head_img);
                                    view.setImageBitmap(tempSourceBitmap);
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError error)
                            {


                            }
                        },
                        800,
                        800
                );
            } else
            {
                // 现在是没有Url的情况，即服务器端传递到的url为空的情况，我们需要在这里直接加载我们的默认图片
                Bitmap tempSourceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head_img);
                view.setImageBitmap(tempSourceBitmap);
            }

            return view;
        }
    }

    private void deletePhoto(final int position,String img_name){
        Map<String,String> param = new HashMap<String, String>();

        param.put(HttpConstants.DELETE_PHOTO.USER_ID,String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        param.put(HttpConstants.DELETE_PHOTO.IMG_NAME,img_name);

        HttpUtil.requestHttp(HttpConstants.DELETE_PHOTO.URL, param, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy", "upload name response ->" + response);
                mList.remove(position);
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Utils.showToast(mContext,mContext.getString(R.string.delete_photo_fail));
            }
        });
    }
}

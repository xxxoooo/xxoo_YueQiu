package com.yueqiu.fragment.profilesetup;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.NewPhotoAdapter;
import com.yueqiu.bean.BitmapBean;
import com.yueqiu.bean.INewPhotoItem;
import com.yueqiu.bean.NewPhotoAddImage;
import com.yueqiu.bean.NewPhotoShow;
import com.yueqiu.bean.NewPhotoUpload;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.group.ImageFragment;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.ImgUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.CustomNetWorkImageView;
import com.yueqiu.view.IssueImageView;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.Header;
import org.json.JSONArray;
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

/**
 * Created by wangyun on 15/3/30.
 */
public class NewPhotoFragment extends Fragment{
    private static final int CAMERA_REQUEST = 1;
    private static final int ALBUM_REQUEST = 2;
    private static final int UPLOAD_SUCCESS = 10;
    private static final int UPLOAD_FAIL = 11;
    private GridView mGridView;
    private NewPhotoAdapter mAdapter;
    private ProgressBar mPreProgressBar;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;
    private NewPhotoAddImage mAddItem;
    private NewPhotoUpload mUploadItem;
    private List<INewPhotoItem> mList = new ArrayList<INewPhotoItem>();
    private List<String> mImgUrlList = new ArrayList<String>();
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_new_photo,null);
        mGridView = (GridView) view.findViewById(R.id.new_photo_grid);
        registerForContextMenu(mGridView);

        mPreProgressBar = (ProgressBar) view.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) view.findViewById(R.id.pre_text);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgressBar.getIndeterminateDrawable().getBounds();
        mPreProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mPreProgressBar.getIndeterminateDrawable().setBounds(bounds);
        mPreTextView.setText(getActivity().getString(R.string.feed_backing));

        mAddItem = new NewPhotoAddImage();
        mAddItem.setEnable(true);
        mList.add(mAddItem);
        mAdapter = new NewPhotoAdapter(getActivity(),mList,this);
        mGridView.setAdapter(mAdapter);

        if(YueQiuApp.sUserInfo.getImg_count() > 0){
            if(Utils.networkAvaiable(getActivity())){
                getImgList();
            }else{
                Toast.makeText(getActivity(),getString(R.string.network_not_available),Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }



    private void getImgList(){
        Map<String,Integer> param = new HashMap<String,Integer>();

        param.put(HttpConstants.GET_NEW_PHOTO.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        Log.d("wy","new photo para is ->" + param);

        HttpUtil.requestHttp(HttpConstants.GET_NEW_PHOTO.URL,param,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","new photo response ->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(!response.get("result").toString().equals("null")){
                                JSONArray result = response.getJSONArray("result");
                                for(int i=0;i<result.length();i++){
                                    String img_url = result.getJSONObject(i).getString("img_url");
                                    mImgUrlList.add(img_url);
                                }
                                mHandler.sendEmptyMessage(PublicConstant.GET_SUCCESS);
                            }
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });
    }

    private void uploadImgName(String img_name){
        Map<String,String> param = new HashMap<String, String>();

        param.put(HttpConstants.ADD_IMG.USER_ID,String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        param.put(HttpConstants.ADD_IMG.IMG_NAME,img_name);

        HttpUtil.requestHttp(HttpConstants.ADD_IMG.URL,param,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","upload name response ->" + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

            }
        });
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    for(int i=mImgUrlList.size() - 1 ; i >= 0 ; i--){
                        NewPhotoShow shotItem = new NewPhotoShow();
                        shotItem.setImgUrl(mImgUrlList.get(i));
                        mList.add(0,shotItem);
                    }
                    mAdapter.notifyDataSetChanged();
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(msg.obj != null){
                        Utils.showToast(getActivity(), (String) msg.obj);
                    }else {
                        Utils.showToast(getActivity(), getString(R.string.http_request_error));
                    }
                    break;
                case PublicConstant.NO_NETWORK:
                    Toast.makeText(getActivity(), getActivity().getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                    break;
                case UPLOAD_SUCCESS:
                    String img_name = (String) msg.obj;
                    if(Utils.networkAvaiable(getActivity())) {
                        uploadImgName(img_name);
                    }
                    if(mUploadItem != null){
                        mUploadItem.setUploaded(true);
                        mUploadItem.setImgUrl(img_name);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case UPLOAD_FAIL:
                    Utils.showToast(getActivity(),getActivity().getString(R.string.upload_photo_fail));
                    break;
            }


        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            BitmapDrawable bitmap = ImgUtil.getThumbnailScaledBitmap(getActivity(), mAdapter.mImgFilePath,
                    getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_width), getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_width));
            BitmapBean bmpBean = new BitmapBean();
            bmpBean.bitmapDrawable = bitmap;
            bmpBean.imgFilePath = mAdapter.mImgFilePath;
            bmpBean.imgUri = null;
            mUploadItem = new NewPhotoUpload();
            mUploadItem.setBitmapBean(bmpBean);
            mList.add(0,mUploadItem);
            mAdapter.notifyDataSetChanged();
            if(Utils.networkAvaiable(getActivity())) {
                uploadPhoto(bmpBean);
            }else{
                Toast.makeText(getActivity(),getString(R.string.network_not_available),Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ALBUM_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri imageFileUri = data.getData();
            BitmapDrawable drawable = ImgUtil.getThumbnailScaleBitmapByUri(getActivity(), imageFileUri, getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_width),
                    getResources().getDimensionPixelOffset(R.dimen.listview_item_user_photo_width));
            BitmapBean bmpBean = new BitmapBean();
            bmpBean.bitmapDrawable = drawable;
            bmpBean.imgFilePath = null;
            bmpBean.imgUri = imageFileUri;
            mUploadItem = new NewPhotoUpload();
            mUploadItem.setBitmapBean(bmpBean);
            mList.add(0,mUploadItem);
            mAdapter.notifyDataSetChanged();
            if(Utils.networkAvaiable(getActivity())) {
                uploadPhoto(bmpBean);
            }else{
                Toast.makeText(getActivity(),getString(R.string.network_not_available),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ImgUtil.clearImageView(mAdapter.mAddViewList);
        File dir = new File(FileUtil.getSdDirectory() + "/yueqiu/");
        if (dir.exists()) {
            File[] files = dir.listFiles(new FileUtil.FileNameFilter("jpg"));
            for (File file : files) {
                file.delete();
            }
        }
    }


    private void uploadPhoto(final BitmapBean bean) {
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
//                        requestParams.put(HttpConstants.ChangePhoto.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
                        requestParams.put(HttpConstants.ChangePhoto.IMG_SUFFIX, "jpg");

                        client.post("http://app.chuangyezheluntan.com/index.php/v1" + HttpConstants.ChangePhoto.URL, requestParams, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.d("wy", "upload response ->" + response);
                                try {
                                    if (!response.isNull("code")) {
                                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                                            if (response.getString("result") != null) {
                                                String img_url = response.getJSONObject("result").getString("img_url");
                                                mHandler.obtainMessage(UPLOAD_SUCCESS,img_url).sendToTarget();
                                            } else {
                                                mHandler.sendEmptyMessage(UPLOAD_FAIL);
                                            }
                                        }  else {
                                            mHandler.obtainMessage(UPLOAD_FAIL).sendToTarget();
                                        }
                                    } else {
                                        mHandler.obtainMessage(UPLOAD_FAIL).sendToTarget();
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
                        mPreProgressBar.setVisibility(View.VISIBLE);
                        mPreTextView.setVisibility(View.VISIBLE);
                        mAddItem.setEnable(false);
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        mPreProgressBar.setVisibility(View.GONE);
                        mPreTextView.setVisibility(View.GONE);
                        mAddItem.setEnable(true);
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
//                        requestParams.put(HttpConstants.ChangePhoto.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
                        requestParams.put(HttpConstants.ChangePhoto.IMG_SUFFIX, "jpg");

                        client.post("http://app.chuangyezheluntan.com/index.php/v1" + HttpConstants.ChangePhoto.URL, requestParams, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.d("wy", "upload response ->" + response);
                                try {
                                    if (!response.isNull("code")) {
                                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                                            if (response.getString("result") != null) {
                                                String img_url = response.getJSONObject("result").getString("img_url");
                                                mHandler.obtainMessage(UPLOAD_SUCCESS,img_url).sendToTarget();
                                            } else {
                                                mHandler.sendEmptyMessage(UPLOAD_FAIL);
                                            }
                                        }  else {
                                            mHandler.obtainMessage(UPLOAD_FAIL).sendToTarget();
                                        }
                                    } else {
                                        mHandler.obtainMessage(UPLOAD_FAIL).sendToTarget();
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
                        mPreProgressBar.setVisibility(View.GONE);
                        mPreTextView.setVisibility(View.GONE);
                        mAddItem.setEnable(false);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mPreProgressBar.setVisibility(View.VISIBLE);
                        mPreTextView.setVisibility(View.VISIBLE);
                        mAddItem.setEnable(true);
                    }

                }.execute();
            }
    }


}

package com.yueqiu.fragment.profilesetup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.MyProfileActivity;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/4.
 */
public class MyProfileRadioSetupFragment extends Fragment implements View.OnClickListener {
    private String[] mTexts;
    private int mCurrentIndex;
    private MyProfileSetupListener mListener;
    private View mView1, mView2, mView3, mThirdLine, mView4, mView5, mView6;
    private TextView mTextView1, mTextView2, mTextView3, mTextView4, mTextView5, mTextView6;
    private ImageView mImageView1, mImageView2, mImageView3, mImageView4, mImageView5, mImageView6;
    private String mSubmitId,mTypeStr;

    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public MyProfileRadioSetupFragment(String[] text, int index,String typeStr) {
        this.mTexts = text;
        this.mCurrentIndex = index;
        this.mTypeStr = typeStr;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio_profile_setup, container, false);
        setHasOptionsMenu(true);
        mSharedPreferences = getActivity().getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        initView(view);
        initData();
        setListener();
        return view;
    }

    private void initView(View view) {

        mView1 = view.findViewById(R.id.the_first_content);
        mView2 = view.findViewById(R.id.the_second_content);
        mView3 = view.findViewById(R.id.the_third_content);
        mView4 = view.findViewById(R.id.the_fourth_content);
        mView5 = view.findViewById(R.id.the_fifth_content);
        mView6 = view.findViewById(R.id.the_sixth_content);

        if(mTexts.length <= 3){
            mView4.setVisibility(View.GONE);
            mView5.setVisibility(View.GONE);
            mView6.setVisibility(View.GONE);
        }

        mThirdLine = view.findViewById(R.id.the_third_line);
        mTextView1 = (TextView) view.findViewById(R.id.text_view1);
        mTextView2 = (TextView) view.findViewById(R.id.text_view2);
        mTextView3 = (TextView) view.findViewById(R.id.text_view3);
        mTextView4 = (TextView) view.findViewById(R.id.text_view4);
        mTextView5 = (TextView) view.findViewById(R.id.text_view5);
        mTextView6 = (TextView) view.findViewById(R.id.text_view6);

        mImageView1 = (ImageView) view.findViewById(R.id.the_first_iv);
        mImageView2 = (ImageView) view.findViewById(R.id.the_second_iv);
        mImageView3 = (ImageView) view.findViewById(R.id.the_third_iv);
        mImageView4 = (ImageView) view.findViewById(R.id.the_fourth_iv);
        mImageView5 = (ImageView) view.findViewById(R.id.the_fifth_iv);
        mImageView6 = (ImageView) view.findViewById(R.id.the_sixth_iv);

        mPreProgress = (ProgressBar) view.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) view.findViewById(R.id.pre_text);
        mPreTextView.setText(getString(R.string.feed_backing));

        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        if (mCurrentIndex == 1)
            setVisible(mImageView1, mImageView2, mImageView3,mImageView4,mImageView5,mImageView6);
        else if (mCurrentIndex == 2)
            setVisible(mImageView2, mImageView1, mImageView3,mImageView4,mImageView5,mImageView6);
        else if (mCurrentIndex == 3)
            setVisible(mImageView3, mImageView1, mImageView2,mImageView4,mImageView5,mImageView6);
        else if (mCurrentIndex == 4)
            setVisible(mImageView4, mImageView1, mImageView2,mImageView3,mImageView5,mImageView6);
        else if(mCurrentIndex == 5)
            setVisible(mImageView5, mImageView1, mImageView2,mImageView3,mImageView4,mImageView6);
        else
            setVisible(mImageView6, mImageView1, mImageView2,mImageView3,mImageView4,mImageView5);
    }

    private void setVisible(ImageView v1, ImageView v2, ImageView v3,
                            ImageView v4, ImageView v5, ImageView v6) {
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
        v3.setVisibility(View.GONE);
        v4.setVisibility(View.GONE);
        v5.setVisibility(View.GONE);
        v6.setVisibility(View.GONE);
    }

    private void initData() {
        if (mTexts.length < 3) {
            mThirdLine.setVisibility(View.GONE);
            mView3.setVisibility(View.GONE);
            mTextView1.setText(mTexts[0]);
            mTextView2.setText(mTexts[1]);
        } else if(mTexts.length == 6){
            mTextView1.setText(mTexts[0]);
            mTextView2.setText(mTexts[1]);
            mTextView3.setText(mTexts[2]);
            mTextView4.setText(mTexts[3]);
            mTextView5.setText(mTexts[4]);
            mTextView6.setText(mTexts[5]);
        }else if(mTexts.length == 4){
            mTextView1.setText(mTexts[0]);
            mTextView2.setText(mTexts[1]);
            mTextView3.setText(mTexts[2]);
            mTextView4.setText(mTexts[3]);
        }
        else{
            mTextView1.setText(mTexts[0]);
            mTextView2.setText(mTexts[1]);
            mTextView3.setText(mTexts[2]);
        }
    }

    private void setListener() {
        mView1.setOnClickListener(this);
        mView2.setOnClickListener(this);
        mView3.setOnClickListener(this);

        mView4.setOnClickListener(this);
        mView5.setOnClickListener(this);
        mView6.setOnClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MyProfileSetupListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement MyProfileSetupListener!");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.the_first_content:
                setVisible(mImageView1, mImageView2, mImageView3,mImageView4,mImageView5,mImageView6);
                mListener.setOnSetupListener("1");
                mSubmitId = "1";
                break;
            case R.id.the_second_content:
                setVisible(mImageView2, mImageView1, mImageView3,mImageView4,mImageView5,mImageView6);
                mListener.setOnSetupListener("2");
                mSubmitId = "2";
                break;
            case R.id.the_third_content:
                setVisible(mImageView3, mImageView1, mImageView2,mImageView4,mImageView5,mImageView6);
                mListener.setOnSetupListener("3");
                mSubmitId = "3";
                break;
            case R.id.the_fourth_content:
                setVisible(mImageView4, mImageView1, mImageView2,mImageView3,mImageView5,mImageView6);
                mListener.setOnSetupListener("4");
                mSubmitId = "4";
                break;
            case R.id.the_fifth_content:
                setVisible(mImageView5, mImageView1, mImageView2,mImageView3,mImageView4,mImageView6);
                mListener.setOnSetupListener("5");
                mSubmitId = "5";
                break;
            case R.id.the_sixth_content:
                setVisible(mImageView6, mImageView1, mImageView2,mImageView3,mImageView4,mImageView5);
                mListener.setOnSetupListener("6");
                mSubmitId = "6";
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.setup_confirm:
                submitAttr();
                break;
        }
        return true;
    }
    private void submitAttr(){
        if(Utils.networkAvaiable(getActivity())) {
            mPreProgress.setVisibility(View.VISIBLE);
            mPreTextView.setVisibility(View.VISIBLE);

            Map<String, String> params = new HashMap<String, String>();
            params.put(HttpConstants.SetAttr.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
            if (mTypeStr.equals(getString(R.string.level))) {
                params.put(HttpConstants.SetAttr.LEVELS, mSubmitId);
            } else if (mTypeStr.equals(getString(R.string.ball_type))) {
                params.put(HttpConstants.SetAttr.CLASS, mSubmitId);
            } else if (mTypeStr.equals(getString(R.string.billiards_cue))) {
                params.put(HttpConstants.SetAttr.BALLARM, mSubmitId);
            } else if (mTypeStr.equals(R.string.cue_habits)) {
                params.put(HttpConstants.SetAttr.USED_TYPE, mSubmitId);
            } else if (mTypeStr.equals(getString(R.string.type))) {
                params.put(HttpConstants.SetAttr.MYTYPE, mSubmitId);
            } else if(mTypeStr.equals(getString(R.string.zizhi))){
                params.put(HttpConstants.SetAttr.ZIZHI,mSubmitId);
            }

            HttpUtil.requestHttp(HttpConstants.SetAttr.URL, params, HttpConstants.RequestMethod.POST, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d("wy", "attr response ->" + response);
                    try {
                        if (!response.isNull("code")) {
                            if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                                mHandler.sendEmptyMessage(PublicConstant.GET_SUCCESS);
                            } else {
                                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
                            }
                        } else {
                            mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
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
        }else{
            Utils.showToast(getActivity(),getString(R.string.network_not_available));
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    if(mTypeStr.equals(getString(R.string.level))){
                        mEditor.putString(DatabaseConstant.UserTable.LEVEL,mSubmitId);
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setLevel(Integer.valueOf(mSubmitId));
                    }else if(mTypeStr.equals(getString(R.string.ball_type))){
                        mEditor.putString(DatabaseConstant.UserTable.BALL_TYPE,mSubmitId);
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setBall_type(Integer.valueOf(mSubmitId));
                    }else if(mTypeStr.equals(getString(R.string.billiards_cue))){
                        mEditor.putString(DatabaseConstant.UserTable.BALLARM,mSubmitId);
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setBallArm(Integer.valueOf(mSubmitId));
                    }else if(mTypeStr.equals(getString(R.string.cue_habits))){
                        mEditor.putString(DatabaseConstant.UserTable.USERDTYPE,mSubmitId);
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setUsedType(Integer.valueOf(mSubmitId));
                    }else if(mTypeStr.equals(getString(R.string.type))){
                        mEditor.putString(DatabaseConstant.UserTable.MY_TYPE,mSubmitId);
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setMy_type(Integer.valueOf(mSubmitId));
                    }else if(mTypeStr.equals(getString(R.string.zizhi))){
                        mEditor.putInt(DatabaseConstant.UserTable.ZIZHI,Integer.valueOf(mSubmitId));
                        mEditor.apply();
                    }

                    Intent intent = new Intent();
                    intent.putExtra(MyProfileActivity.EXTRA_RESULT_ID, mSubmitId);
                    getActivity().setResult(Activity.RESULT_OK, intent);

                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    Utils.showToast(getActivity(), getString(R.string.http_request_error));
                    break;

            }
        }
    };
}

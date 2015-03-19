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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeGender;
import com.gotye.api.GotyeUser;
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
public class MyProfileTextSetupFragment extends Fragment {
    private static final String TAG = "MyProfileTextSetupFragment";
    private String mText;
    public MyProfileTextSetupFragment(String text) {
        mText = text;
    }
    private EditText mEditText;
    private MyProfileSetupListener mListener;
    private Map<String,String> mParamMap = new HashMap<String, String>();

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;
    private GotyeAPI mApi;
    private GotyeUser mGotyeUser;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_setup, container, false);
        setHasOptionsMenu(true);

        mSharedPreferences = getActivity().getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        mEditText = (EditText) view.findViewById(R.id.my_profile_setup_text);
        mPreProgress = (ProgressBar) view.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) view.findViewById(R.id.pre_text);
        mPreTextView.setText(getString(R.string.feed_backing));

        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);


        mApi = GotyeAPI.getInstance();
        mGotyeUser = mApi.getCurrentLoginUser();

        Bundle args = getArguments();
        if(mText.equals(getString(R.string.nick_name))){
            String nick = args.getString(DatabaseConstant.UserTable.NICK);
            mEditText.setText(nick);
        }else if(mText.equals(getString(R.string.region))){
            String region = args.getString(DatabaseConstant.UserTable.DISTRICT);
            mEditText.setText(region);
        }else if(mText.equals(getString(R.string.play_age))){
            String ball_age = args.getString(DatabaseConstant.UserTable.BALLAGE);
            mEditText.setText(ball_age);
        }else if(mText.equals(getString(R.string.idol))){
            String idol = args.getString(DatabaseConstant.UserTable.IDOL);
            mEditText.setText(idol);
        }else if(mText.equals(getString(R.string.sign))){
            String sign = args.getString(DatabaseConstant.UserTable.IDOL_NAME);
            mEditText.setText(sign);
        }else if(mText.equals(getString(R.string.cost))){
            String cost = args.getString(DatabaseConstant.UserTable.COST);
            mEditText.setText(cost);
        }else if(mText.equals(getString(R.string.profession_experiences))){
            String work_live = args.getString(DatabaseConstant.UserTable.WORK_LIVE);
            mEditText.setText(work_live);
        }


        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mListener.setOnSetupListener(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.setup_confirm:
                if(mText.equals(getString(R.string.nick_name))){
                    submitAttr();
                }else if(mText.equals(getString(R.string.region))){
                    submitAttr();
                }else if(mText.equals(getString(R.string.play_age))){
                    submitAttr();
                }else if(mText.equals(getString(R.string.idol))){
                    submitAttr();
                }else if(mText.equals(getString(R.string.sign))){
                    submitAttr();
                }else if(mText.equals(getString(R.string.cost))){
                    submitAttr();
                }else if(mText.equals(getString(R.string.profession_experiences))){
                    submitAttr();
                }
                break;
        }
        return true;
    }

    private void submitNickName(){
        if(Utils.networkAvaiable(getActivity())) {
            mPreProgress.setVisibility(View.VISIBLE);
            mPreTextView.setVisibility(View.VISIBLE);

            mParamMap.put(HttpConstants.SetNickName.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
            mParamMap.put(HttpConstants.SetNickName.NICKNAME, mEditText.getText().toString());

            HttpUtil.requestHttp(HttpConstants.SetNickName.URL, mParamMap, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "response -> " + response);
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
    private void submitAttr(){

        if(Utils.networkAvaiable(getActivity())) {

            mParamMap.put(HttpConstants.SetAttr.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
            if (mText.equals(getString(R.string.region))) {
                mParamMap.put(HttpConstants.SetAttr.DISTRICT, mEditText.getText().toString());
            } else if (mText.equals(getString(R.string.play_age))) {
                mParamMap.put(HttpConstants.SetAttr.AGE, mEditText.getText().toString());
            } else if (mText.equals(getString(R.string.idol))) {
                mParamMap.put(HttpConstants.SetAttr.IDOL, mEditText.getText().toString());
            } else if (mText.equals(getString(R.string.sign))) {
                mParamMap.put(HttpConstants.SetAttr.IDOL_NAME, mEditText.getText().toString());
            } else if (mText.equals(getString(R.string.cost))) {
                mParamMap.put(HttpConstants.SetAttr.MONEYS, mEditText.getText().toString());
            } else if (mText.equals(getString(R.string.profession_experiences))) {
                mParamMap.put(HttpConstants.SetAttr.WORK_LIVE, mEditText.getText().toString());
            } else if (mText.equals(getString(R.string.nick_name))){
                mParamMap.put(HttpConstants.SetNickName.NICKNAME,mEditText.getText().toString());
            }


            HttpUtil.requestHttp(HttpConstants.SetAttr.URL, mParamMap, HttpConstants.RequestMethod.POST, new JsonHttpResponseHandler() {
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

    private void modifyUser() {
        int split = YueQiuApp.sUserInfo.getImg_url().lastIndexOf("/");
        String img_url = YueQiuApp.sUserInfo.getImg_url().substring(split + 1);
        mGotyeUser.setNickname(YueQiuApp.sUserInfo.getNick() + "|" + img_url);

        mGotyeUser.setGender(YueQiuApp.sUserInfo.getSex() == 1 ? GotyeGender.Male : GotyeGender.Femal);
        Log.e("cao", " text modify mGotyeUser = " + mGotyeUser);
        int result = mApi.requestModifyUserInfo(mGotyeUser, null);

        Log.d("cao","text modify result" + result);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    if(mText.equals(getString(R.string.nick_name))){
                        mEditor.putString(DatabaseConstant.UserTable.NICK,mEditText.getText().toString());
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setNick(mEditText.getText().toString());

                        modifyUser();

                        Intent broadIntent = new Intent(PublicConstant.SLIDE_ACCOUNT_ACTION);
                        getActivity().sendBroadcast(broadIntent);
                    }else if(mText.equals(getString(R.string.region))){
                        mEditor.putString(DatabaseConstant.UserTable.DISTRICT,mEditText.getText().toString());
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setDistrict(mEditText.getText().toString());
                    }else if(mText.equals(getString(R.string.play_age))){
                        mEditor.putString(DatabaseConstant.UserTable.BALLAGE,mEditText.getText().toString());
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setBallAge(mEditText.getText().toString());
                    }else if(mText.equals(getString(R.string.idol))){
                        mEditor.putString(DatabaseConstant.UserTable.IDOL,mEditText.getText().toString());
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setIdol(mEditText.getText().toString());
                    }else if(mText.equals(getString(R.string.sign))){
                        mEditor.putString(DatabaseConstant.UserTable.IDOL_NAME,mEditText.getText().toString());
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setIdol_name(mEditText.getText().toString());
                    }
                    else if(mText.equals(getString(R.string.cost))){
                        mEditor.putString(DatabaseConstant.UserTable.COST,mEditText.getText().toString());
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setCost(mEditText.getText().toString());
                    }else if(mText.equals(getString(R.string.profession_experiences))){
                        mEditor.putString(DatabaseConstant.UserTable.WORK_LIVE,mEditText.getText().toString());
                        mEditor.apply();
                        YueQiuApp.sUserInfo.setWork_live(mEditText.getText().toString());
                    }
                    Intent intent = new Intent();
                    intent.putExtra(MyProfileActivity.EXTRA_RESULT_ID, mEditText.getText().toString());
                    getActivity().setResult(Activity.RESULT_OK, intent);

                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(msg.obj == null){
                        Utils.showToast(getActivity(), (String) msg.obj);
                    }else {
                        Utils.showToast(getActivity(), getString(R.string.http_request_error));
                    }
                    break;

            }

        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MyProfileSetupListener) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement MyProfileSetupListener!");
        }
    }


}

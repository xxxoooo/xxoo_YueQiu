package com.yueqiu.fragment.addfriend;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by doushuqi on 15/1/6.
 */
public class VerificationFragment extends Fragment {
    private static final String TAG = "VerificationFragment";

    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;
    public static final String ARGUMENTS_KEY = "com.yueqiu.fragment.requestaddfriend.arguments_key";
    private CustomNetWorkImageView mPhoto;
    private String mAccount;
    private String mGender;
    private String mDistrict;
    private String mFriendUserId;
    private TextView mAccountTextView, mGenderTextView, mDistrictTextView;
    private EditText mEditText;
    private String mNews;
    private String mPhotoUrl;
    private ImageLoader mImageLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();

            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        setHasOptionsMenu(true);
        mFragmentManager = getActivity().getSupportFragmentManager();
        mAccount = getArguments().getString(FriendProfileFragment.ACCOUNT_KEY);
        mGender = getArguments().getString(FriendProfileFragment.GENDER_KEY);
        mDistrict = getArguments().getString(FriendProfileFragment.DISTRICT_KEY);
        mFriendUserId = getArguments().getString(FriendProfileFragment.FRIEND_USER_ID);
        mPhotoUrl = getArguments().getString(FriendProfileFragment.IMG_URL_REAL_KEY);
        mImageLoader = VolleySingleton.getInstance().getImgLoader();
        mNews = getString(R.string.who_i_am,YueQiuApp.sUserInfo.getUsername());
        Log.e(TAG, "mPhotoUrl = " + mPhotoUrl);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_identity_verification, container, false);
        mEditText = (EditText) view.findViewById(R.id.verification_news_ed);
        mEditText.setHint(mNews);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mNews = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        init(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mActionBar.setTitle(R.string.identity_verify);
    }

    private void init(View v) {
        mPhoto = (CustomNetWorkImageView) v.findViewById(R.id.account_iv);
        mAccountTextView = (TextView) v.findViewById(R.id.account_tv);
        mGenderTextView = (TextView) v.findViewById(R.id.gender_tv);
        mDistrictTextView = (TextView) v.findViewById(R.id.district_tv);
        if(mDistrict.equals("")) {
            mDistrictTextView = (TextView) v.findViewById(R.id.district_tv);
            mDistrictTextView.setVisibility(View.VISIBLE);
        }else{
            mDistrictTextView.setVisibility(View.GONE);
        }
        mAccountTextView.setText(mAccount);
        mGenderTextView.setText(mGender);
        mDistrictTextView.setText(mDistrict);
        mPhoto.setDefaultImageResId(R.drawable.default_head);
        mPhoto.setErrorImageResId(R.drawable.default_head);
        mPhoto.setImageUrl(HttpConstants.IMG_BASE_URL + mPhotoUrl, mImageLoader);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                        .remove(this).commit();
                mFragmentManager.popBackStack();
                Utils.dismissInputMethod(getActivity(), mEditText);
                return true;
            case R.id.next:
                sendRequest();

                Utils.dismissInputMethod(getActivity(), mEditText);
                return true;
            default:
                Utils.dismissInputMethod(getActivity(), mEditText);
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Utils.setFragmentActivityMenuColor(getActivity());
        getActivity().getMenuInflater().inflate(R.menu.next, menu);
    }

    /**
     * post请求
     */
    private void sendRequest() {
        final Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(HttpConstants.FriendSendAsk.MY_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));//
        requestMap.put(HttpConstants.FriendSendAsk.ASK_ID, mFriendUserId);
        requestMap.put(HttpConstants.FriendSendAsk.NEWS, mNews);

        if(Utils.networkAvaiable(getActivity())) {
            HttpUtil.requestHttp(HttpConstants.FriendSendAsk.URL, requestMap, HttpConstants.RequestMethod.POST, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d("wy","verify response ->" + response);
                    try {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            mHandler.sendEmptyMessage(PublicConstant.GET_SUCCESS);
                        } else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
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
            Toast.makeText(getActivity(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
        }


    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    Bundle args = new Bundle();
                    args.putInt(ARGUMENTS_KEY, 1);
                    args.putString(FriendProfileFragment.FRIEND_USER_ID, mFriendUserId);
                    Fragment fragment = new FriendManageFragment();
                    fragment.setArguments(args);
                    FragmentTransaction ft = mFragmentManager.beginTransaction();
                    ft.addToBackStack("com.yueqiu.activity.RequestAddFriendActivity");
                    ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                    ft.replace(R.id.fragment_container, fragment).commit();
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(getActivity(),getActivity().getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(getActivity(), (String) msg.obj);
                    }
                    break;
            }
        }
    };


}

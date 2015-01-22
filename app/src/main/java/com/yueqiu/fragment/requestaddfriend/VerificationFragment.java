package com.yueqiu.fragment.requestaddfriend;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;

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
    private ImageView mPhoto;
    private String mAccount;
    private String mGender;
    private String mDistrict;
    private String mFriendUserId;
    private TextView mAccountTextView, mGenderTextView, mDistrictTextView;
    private EditText mEditText;
    private String mNews = "我是谁";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
            mActionBar.setTitle(R.string.identity_verify);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        setHasOptionsMenu(true);
        mFragmentManager = getActivity().getSupportFragmentManager();
        mAccount = getArguments().getString(FriendProfileFragment.ACCOUNT_KEY);
        mGender = getArguments().getString(FriendProfileFragment.GENDER_KEY);
        mDistrict = getArguments().getString(FriendProfileFragment.DISTRICT_KEY);
        mFriendUserId = getArguments().getString(FriendProfileFragment.FRIEND_USER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_identity_verification, container, false);
        mEditText = (EditText) view.findViewById(R.id.verification_news_ed);
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

    private void init(View v) {
        mPhoto = (ImageView) v.findViewById(R.id.account_iv);
        mAccountTextView = (TextView) v.findViewById(R.id.account_tv);
        mGenderTextView = (TextView) v.findViewById(R.id.gender_tv);
        mDistrictTextView = (TextView) v.findViewById(R.id.district_tv);
        mAccountTextView.setText(mAccount);
        mGenderTextView.setText(mGender);
        mDistrictTextView.setText(mDistrict);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            case R.id.next:
                sendRequest();
                Bundle args = new Bundle();
                args.putInt(ARGUMENTS_KEY, 1);
                args.putString(FriendProfileFragment.FRIEND_USER_ID, mFriendUserId);
                Fragment fragment = new FriendManageFragment();
                fragment.setArguments(args);
                mFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                return true;
            default:
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
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(HttpConstants.FriendSendAsk.MY_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));//
        requestMap.put(HttpConstants.FriendSendAsk.ASK_ID, mFriendUserId);
        requestMap.put(HttpConstants.FriendSendAsk.NEWS, mNews);

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put(PublicConstant.URL, HttpConstants.FriendSendAsk.URL);
        paramMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.POST);
        if (Utils.networkAvaiable(getActivity())) {
            new VerificationAsyncTask(requestMap, null, null).execute(paramMap);
        } else {
            Toast.makeText(getActivity(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    private class VerificationAsyncTask extends AsyncTaskUtil<String> {

        public VerificationAsyncTask(Map<String, String> map, ProgressBar progressBar, TextView textView) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if (jsonObject.getInt("code") != HttpConstants.ResponseCode.NORMAL) {
                    Log.d(TAG, "好友请求发送失败！->" + jsonObject.getString("msg"));
                } else {
                    Log.d(TAG, "好友请求发送成功");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}

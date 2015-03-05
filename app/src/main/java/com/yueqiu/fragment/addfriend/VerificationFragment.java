package com.yueqiu.fragment.addfriend;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;

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
    private NetworkImageView mPhoto;
    private String mAccount;
    private String mGender;
    private String mDistrict;
    private String mFriendUserId;
    private TextView mAccountTextView, mGenderTextView, mDistrictTextView;
    private EditText mEditText;
    private String mNews = "我是谁";
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
        Log.e(TAG, "mPhotoUrl = " + mPhotoUrl);
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

    @Override
    public void onStart() {
        super.onStart();
        mActionBar.setTitle(R.string.identity_verify);
    }

    private void init(View v) {
        mPhoto = (NetworkImageView) v.findViewById(R.id.account_iv);
        mAccountTextView = (TextView) v.findViewById(R.id.account_tv);
        mGenderTextView = (TextView) v.findViewById(R.id.gender_tv);
        mDistrictTextView = (TextView) v.findViewById(R.id.district_tv);
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
                Bundle args = new Bundle();
                args.putInt(ARGUMENTS_KEY, 1);
                args.putString(FriendProfileFragment.FRIEND_USER_ID, mFriendUserId);
                Fragment fragment = new FriendManageFragment();
                fragment.setArguments(args);
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.addToBackStack("com.yueqiu.activity.RequestAddFriendActivity");
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.fragment_container, fragment).commit();
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

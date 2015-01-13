package com.yueqiu.fragment.requestaddfriend;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.FriendSetGroupActivity;
import com.yueqiu.activity.FriendsApplicationActivity;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/6.
 */
public class FriendManageFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "FriendManageFragment";
    private ActionBar mActionBar;
    private FragmentManager mFragmentManager;
    private Fragment mFragment;
    private int whoCreate;
    private TextView mTextView;
    private EditText mEditText;
    private int mGroupId;//分组id
    private String mComment = "";//备注
    private int mLabel;//标签:1师傅2徒弟3高手4菜鸟
    private static final int REQUEST_CODE = 0;
    private String mFriendUserId;
    private View mMasterIndicator, mStudentIndicator, mExpertIndicator, mBeginnerIndictor,
            mMaster, mStudent, mExpert, mBeginner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActionBar = getActivity().getActionBar();
        mActionBar.setTitle(R.string.qiuyou_manage);
        mFragmentManager = getActivity().getSupportFragmentManager();
        whoCreate = getArguments().getInt(VerificationFragment.ARGUMENTS_KEY);
        mFriendUserId = getArguments().getString(FriendProfileFragment.FRIEND_USER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_friend, container, false);
        initView(view);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "s = " + s);
                mComment = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        view.findViewById(R.id.friend_manage_set_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendSetGroupActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        return view;
    }

    private void initView(View v) {
        mTextView = (TextView) v.findViewById(R.id.friend_group);
        mEditText = (EditText) v.findViewById(R.id.add_friend_comment_et);
        mMaster = v.findViewById(R.id.label_master);
        mStudent = v.findViewById(R.id.label_student);
        mExpert = v.findViewById(R.id.label_expert);
        mBeginner = v.findViewById(R.id.label_beginner);
        mMasterIndicator = v.findViewById(R.id.label_indicator_master);
        mStudentIndicator = v.findViewById(R.id.label_indicator_student);
        mExpertIndicator = v.findViewById(R.id.label_indicator_expert);
        mBeginnerIndictor = v.findViewById(R.id.label_indicator_beginner);
        mMaster.setOnClickListener(this);
        mStudent.setOnClickListener(this);
        mExpert.setOnClickListener(this);
        mBeginner.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            mGroupId = data.getIntExtra(FriendSetGroupFragment.RESULT_KEY, -1);
            mTextView.setText(mGroupId == 0 ? R.string.search_billiard_mate_str : (mGroupId == 1)
                    ? R.string.search_billiard_assist_coauch_str : R.string.search_billiard_coauch_str);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Utils.setFragmentActivityMenuColor(getActivity());
        switch (whoCreate) {
            case 0:
                getActivity().getMenuInflater().inflate(R.menu.finish, menu);
                break;
            case 1:
                getActivity().getMenuInflater().inflate(R.menu.send, menu);
                break;
            default:
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (whoCreate == 0)
                    ((FriendsApplicationActivity) getActivity()).switchFragment(FriendsApplicationActivity.sFriendsApplication);
                else getActivity().finish();
                return true;
            case R.id.qiuyou_manage_finish:
                //TODO:
//                mFragmentManager.beginTransaction().replace(R.id.fragment_container, FriendsApplicationActivity.sCurrentFragment).commit();
                ((FriendsApplicationActivity) getActivity()).switchFragment(FriendsApplicationActivity.sFriendsApplication);
                return true;
            case R.id.send:
                //发送好友请求
                sendRequest();
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * post请求
     */
    private void sendRequest() {
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(HttpConstants.FrendManage.MY_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));//
        requestMap.put(HttpConstants.FrendManage.ASK_ID, mFriendUserId);
        requestMap.put(HttpConstants.FrendManage.GROUP_ID, String.valueOf(mGroupId + 1));
        requestMap.put(HttpConstants.FrendManage.REMARK, mComment);
        requestMap.put(HttpConstants.FrendManage.TAG, String.valueOf(mLabel));

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put(PublicConstant.URL, HttpConstants.FrendManage.URL);
        paramMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.POST);
        if (Utils.networkAvaiable(getActivity())) {
            new FriendManageAsyncTask(requestMap, null, null).execute(paramMap);
        } else {
            Toast.makeText(getActivity(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.label_master:
                mLabel = 1;
                mMasterIndicator.setVisibility(View.VISIBLE);
                mStudentIndicator.setVisibility(View.GONE);
                mExpertIndicator.setVisibility(View.GONE);
                mBeginnerIndictor.setVisibility(View.GONE);
                break;
            case R.id.label_student:
                mLabel = 2;
                mMasterIndicator.setVisibility(View.GONE);
                mStudentIndicator.setVisibility(View.VISIBLE);
                mExpertIndicator.setVisibility(View.GONE);
                mBeginnerIndictor.setVisibility(View.GONE);
                break;
            case R.id.label_expert:
                mLabel = 3;
                mMasterIndicator.setVisibility(View.GONE);
                mStudentIndicator.setVisibility(View.GONE);
                mExpertIndicator.setVisibility(View.VISIBLE);
                mBeginnerIndictor.setVisibility(View.GONE);
                break;
            case R.id.label_beginner:
                mLabel = 4;
                mMasterIndicator.setVisibility(View.GONE);
                mStudentIndicator.setVisibility(View.GONE);
                mExpertIndicator.setVisibility(View.GONE);
                mBeginnerIndictor.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private class FriendManageAsyncTask extends AsyncTaskUtil<String> {

        public FriendManageAsyncTask(Map<String, String> map, ProgressBar progressBar, TextView textView) {
            super(map, progressBar, textView);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;//接口有问题
            }

            try {
                Log.e(TAG, "jsonObject = " + jsonObject);
                if (jsonObject.getInt("code") != HttpConstants.ResponseCode.NORMAL) {
                    Log.d(TAG, "好友管理请求发送失败！->" + jsonObject.getString("msg"));
                } else {
                    Log.d(TAG, "好友管理请求发送成功");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

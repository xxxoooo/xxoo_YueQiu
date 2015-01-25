package com.yueqiu.fragment.addfriend;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.FriendSetGroupActivity;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.ApplicationDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

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
    private int whoCreate;//0:处理请求；1：发送请求
    private TextView mTextView;
    private EditText mEditText;
    private int mGroupId;//分组id
    private String mComment = "";//备注
    private int mLabel;//标签:1师傅2徒弟3高手4菜鸟
    private static final int REQUEST_CODE = 0;
    private String mFriendUserId;//处理好友申请时该字段为申请id;发送好友验证下一步时该字段为ask_id被请求添加的好友id
    private View mMasterIndicator, mStudentIndicator, mExpertIndicator, mBeginnerIndicator,
            mMaster, mStudent, mExpert, mBeginner;

    private ApplicationDao mApplicationDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActionBar = getActivity().getActionBar();
        mActionBar.setTitle(R.string.qiuyou_manage);
        mFragmentManager = getActivity().getSupportFragmentManager();
        whoCreate = getArguments().getInt(VerificationFragment.ARGUMENTS_KEY);
        mFriendUserId = getArguments().getString(FriendProfileFragment.FRIEND_USER_ID);
        if (whoCreate == 0)
            mApplicationDao = DaoFactory.getApplication(getActivity());
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
                intent.putExtra(FriendSetGroupFragment.RESULT_KEY, mGroupId);
                startActivityForResult(intent, REQUEST_CODE);
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
        mBeginnerIndicator = v.findViewById(R.id.label_indicator_beginner);
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
//                if (whoCreate == 0)
//                    ((FriendsApplicationActivity) getActivity()).switchFragment(FriendsApplicationActivity.sFriendsApplication);
//                else getActivity().finish();
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            case R.id.qiuyou_manage_finish:
                //TODO:
                mApplicationDao.updateFriendsApplication(mFriendUserId, 1);
                mFragmentManager.beginTransaction().
                        replace(R.id.fragment_container, new FriendsApplicationFragment()).commit();
                //处理好友请求
                handleRequest();
                getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            case R.id.send:
                //发送好友请求
                sendRequest();
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
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
        requestMap.put(HttpConstants.FriendManage.MY_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));//
        requestMap.put(HttpConstants.FriendManage.ASK_ID, mFriendUserId);
        requestMap.put(HttpConstants.FriendManage.GROUP_ID, String.valueOf(mGroupId + 1));
        requestMap.put(HttpConstants.FriendManage.REMARK, mComment);
        requestMap.put(HttpConstants.FriendManage.TAG, String.valueOf(mLabel));

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put(PublicConstant.URL, HttpConstants.FriendManage.URL);
        paramMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.POST);
        if (Utils.networkAvaiable(getActivity())) {
            new FriendManageAsyncTask(requestMap, null, null).execute(paramMap);
        } else {
            Toast.makeText(getActivity(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * post请求
     */
    private void handleRequest() {
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(HttpConstants.AskManage.ASK_ID, mFriendUserId);
        requestMap.put(HttpConstants.AskManage.GROUP_ID, String.valueOf(mGroupId + 1));
        requestMap.put(HttpConstants.AskManage.REMARK, mComment);
        requestMap.put(HttpConstants.AskManage.TAG, String.valueOf(mLabel));

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put(PublicConstant.URL, HttpConstants.AskManage.URL);
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
                mBeginnerIndicator.setVisibility(View.GONE);
                break;
            case R.id.label_student:
                mLabel = 2;
                mMasterIndicator.setVisibility(View.GONE);
                mStudentIndicator.setVisibility(View.VISIBLE);
                mExpertIndicator.setVisibility(View.GONE);
                mBeginnerIndicator.setVisibility(View.GONE);
                break;
            case R.id.label_expert:
                mLabel = 3;
                mMasterIndicator.setVisibility(View.GONE);
                mStudentIndicator.setVisibility(View.GONE);
                mExpertIndicator.setVisibility(View.VISIBLE);
                mBeginnerIndicator.setVisibility(View.GONE);
                break;
            case R.id.label_beginner:
                mLabel = 4;
                mMasterIndicator.setVisibility(View.GONE);
                mStudentIndicator.setVisibility(View.GONE);
                mExpertIndicator.setVisibility(View.GONE);
                mBeginnerIndicator.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private class FriendManageAsyncTask extends AsyncTaskUtil<String> {

        public FriendManageAsyncTask(Map<String, String> map, ProgressBar progressBar, TextView textView) {
            super(map);
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

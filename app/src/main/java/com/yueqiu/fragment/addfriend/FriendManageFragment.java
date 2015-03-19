package com.yueqiu.fragment.addfriend;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.FriendSetGroupActivity;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/6.
 */
public class FriendManageFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "FriendManageFragment";
    private static final int HANDLE_SUCCESS = 42;
    private static final int SEND_SUCCES = 43;
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
    private String mFriendUsername;
    private View mMasterIndicator, mStudentIndicator, mExpertIndicator, mBeginnerIndicator,
            mMaster, mStudent, mExpert, mBeginner;

//    private ApplicationDao mApplicationDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActionBar = getActivity().getActionBar();

        mFragmentManager = getActivity().getSupportFragmentManager();
        whoCreate = getArguments().getInt(VerificationFragment.ARGUMENTS_KEY);
        mFriendUserId = getArguments().getString(FriendProfileFragment.FRIEND_USER_ID);
        mFriendUsername = getArguments().getString(FriendProfileFragment.USER_NAME_KEY);
//        if (whoCreate == 0)
//            mApplicationDao = DaoFactory.getApplication(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        mActionBar.setTitle(R.string.qiuyou_manage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_friend, container, false);
        initView(view);

//        mEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.d(TAG, "s = " + s);
//                mComment = s.toString();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
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
            mTextView.setText(mGroupId == 0 ? R.string.nearby_billiard_mate_str : (mGroupId == 1)
                    ? R.string.nearby_billiard_assist_coauch_str : R.string.nearby_billiard_coauch_str);
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
                mFragmentManager.beginTransaction().setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                        .remove(this).commit();
                mFragmentManager.popBackStackImmediate();
                Utils.dismissInputMethod(getActivity(), mEditText);
                return true;
            case R.id.qiuyou_manage_finish:
                //TODO:
//                mApplicationDao.updateFriendsApplication(mFriendUserId, 1);
//                mFragmentManager.beginTransaction()
//                        .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
//                        .replace(R.id.fragment_container, new FriendsApplicationFragment())
//                        .commit();
                //处理好友请求
                handleRequest();
                Utils.dismissInputMethod(getActivity(), mEditText);
                return true;
            case R.id.send:
                //发送好友请求
                sendRequest();

                return true;
            default:
                Utils.dismissInputMethod(getActivity(), mEditText);
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * post请求
     */
    private void sendRequest() {

        if(mLabel == 0){
            Utils.showToast(getActivity(),getString(R.string.please_select_group));
            return;
        }

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
            HttpUtil.requestHttp(HttpConstants.FriendManage.URL,requestMap,HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d("wy","friend send response ->" + response);
                    try {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            mHandler.sendEmptyMessage(SEND_SUCCES);
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
        } else {
            Toast.makeText(getActivity(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * post请求
     */
    private void handleRequest() {

//        mComment = mEditText.getText().toString();
//        if(TextUtils.isEmpty(mComment)){
//            Utils.showToast(getActivity(),getString(R.string.please_write_comment));
//            return;
//        }

        if(mLabel == 0){
            Utils.showToast(getActivity(),getString(R.string.please_select_group));
            return;
        }

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(HttpConstants.AskManage.ASK_ID, mFriendUserId);
        requestMap.put(HttpConstants.AskManage.GROUP_ID, String.valueOf(mGroupId + 1));
        requestMap.put(HttpConstants.AskManage.REMARK, mComment);
        requestMap.put(HttpConstants.AskManage.TAG, String.valueOf(mLabel));
        Log.d("wy","friend manager request ->" + requestMap);

        if(Utils.networkAvaiable(getActivity())) {
            HttpUtil.requestHttp(HttpConstants.AskManage.URL, requestMap, HttpConstants.RequestMethod.POST, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d("wy","friends handle manager response ->" + response);
                    try{
                        if(!response.isNull("code")){
                            if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                                mHandler.sendEmptyMessage(HANDLE_SUCCESS);
                            }else{
                                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                            }
                        }else{
                            mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                        }
                    }catch(JSONException e){
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


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.REQUEST_ERROR:
                    if (null == msg.obj) {
                        Utils.showToast(getActivity(), getString(R.string.http_request_error));
                    } else {
                        Utils.showToast(getActivity(), (String) msg.obj);
                    }
                    break;
                case HANDLE_SUCCESS:
                    Utils.showToast(getActivity(),getString(R.string.you_are_friends));
                    FriendsApplicationFragment applicationFragment = new FriendsApplicationFragment();
                    Bundle args = new Bundle();
                    args.putString(FriendProfileFragment.USER_NAME_KEY,mFriendUsername);
                    applicationFragment.setArguments(args);
                    mFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                            .replace(R.id.fragment_container, applicationFragment)
                            .commit();
                    Intent intent = new Intent();
                    intent.setAction(PublicConstant.CHAT_HAS_NO_MSG);
                    getActivity().sendBroadcast(intent);
                    break;
                case SEND_SUCCES:
                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    Utils.dismissInputMethod(getActivity(), mEditText);
                    break;
            }
        }
    };
}

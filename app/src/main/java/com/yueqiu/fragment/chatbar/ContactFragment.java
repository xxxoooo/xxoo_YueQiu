package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatTarget;
import com.gotye.api.GotyeGender;
import com.gotye.api.GotyeUser;
import com.gotye.api.Icon;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.ExpAdapter;
import com.yueqiu.bean.ContactsList;
import com.yueqiu.im.ChatPage;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.ContactsDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.util.AsyncTaskBase;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.contacts.IphoneTreeView;
import com.yueqiu.view.contacts.LoadingView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧联系人fragment
 */
public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";
    private ActionBar mActionBar;
    private Context mContext;
    private View mBaseView;
    private LoadingView mLoadingView;
    private IphoneTreeView mIphoneTreeView;
    private ExpAdapter mExpAdapter;
    private HashMap<Integer, List<ContactsList.Contacts>> mMaps;
    private Map<String, String> mMapArgument = new HashMap<String, String>();
//    private ContactsDao mContactsDao;
    private List<GotyeChatTarget> mTargets;
    private GotyeUser mGotyeUser;
    private GotyeAPI mApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mContactsDao = DaoFactory.getContacts(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        mBaseView = inflater.inflate(R.layout.fragment_chatbar_contact, null);
        mApi = GotyeAPI.getInstance();
        findView();
        init();
        initData();
        return mBaseView;
    }

    private void findView() {
        mLoadingView = (LoadingView) mBaseView.findViewById(R.id.loadingView);
        mIphoneTreeView = (IphoneTreeView) mBaseView.findViewById(R.id.iphone_tree_view);
    }

    private void init() {
//        mIphoneTreeView.setHeaderView(LayoutInflater.from(mContext).inflate(
//                R.layout.fragment_constact_head_view, mIphoneTreeView, false));
        mIphoneTreeView.setGroupIndicator(null);
//		mExpAdapter = new ExpAdapter(mContext, maps, mIphoneTreeView,mSearchView);
        mExpAdapter = new ExpAdapter(mContext, mIphoneTreeView);
        mIphoneTreeView.setAdapter(mExpAdapter);
//		new AsyncTaskLoading(mLoadingView).execute(0);


        mIphoneTreeView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ContactsList.Contacts contacts = (ContactsList.Contacts) mExpAdapter.getChild(groupPosition, childPosition);
                //TODO:传入待聊天好友的userid
                if (TextUtils.isEmpty(contacts.getUsername()))
                    return false;

                //TODO:逻辑还要变
                Intent intent = new Intent(getActivity(), ChatPage.class);
                GotyeUser user = new GotyeUser(contacts.getUsername());
//                modifyUser(user,contacts.getImg_url(),contacts.getUsername(),Integer.valueOf(contacts.getSex()));

                intent.putExtra("user", user);
                intent.putExtra("from", 200);
//                intent.putExtra(MessageFragment.FRIEND_USER_ID, contacts.getUser_id());//fake date
//                intent.putExtra(MessageFragment.FRIEND_USER_NAME, contacts.getUsername());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                return true;
            }
        });
    }

    private void initData() {
        if (!Utils.networkAvaiable(getActivity())) {
            //本地获取联系人列表
//            mMaps = mContactsDao.getContactList();
            mExpAdapter.setData(mMaps);
            mExpAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
            return;
        }
        getContactList();
//        getAllContact();
    }

    /**
     * 加载最近的聊天记录
     */
    private class AsyncTaskLoading extends AsyncTaskBase {
        public AsyncTaskLoading(LoadingView loadingView) {
            super(loadingView);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            int result = -1;
//            maps.put("球友", TestData.getRecentChats());
//            maps.put("助教", TestData.getRecentChats());
//            maps.put("教练", TestData.getRecentChats());
            result = 1;
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }

    /**
     * 从网络中获取好友列表信息
     */
    private void getContactList() {
        final HashMap<Integer, List<ContactsList.Contacts>> maps = new HashMap<Integer, List<ContactsList.Contacts>>();
        for (int i = 0; i < 3; i++) {
            final Map<String, Integer> map = new HashMap<String, Integer>();
            map.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
            map.put(HttpConstants.ContactsList.GROUP_ID, i + 1);

            final int key = i;
            HttpUtil.requestHttp(HttpConstants.ContactsList.URL, map, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d("wy","contact response ->" + response);
                    try{
                        if(!response.isNull("code")){
                            if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                                JSONObject result = response.getJSONObject("result");
                                if(result.get("list_data").toString().equals("null")) {
                                    Log.d(TAG, "list_data is null");
                                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                }else{
                                    ContactsList contactsList = new ContactsList();
                                    JSONArray list_data = result.getJSONArray("list_data");
                                    for (int j = 0; j < list_data.length(); j++) {
                                        mMapArgument.put(DatabaseConstant.FriendsTable.USER_ID, list_data.getJSONObject(j).getString(DatabaseConstant.FriendsTable.USER_ID));
                                        mMapArgument.put(DatabaseConstant.FriendsTable.GROUP_ID, list_data.getJSONObject(j).getString(DatabaseConstant.FriendsTable.GROUP_ID));
                                        mMapArgument.put(DatabaseConstant.FriendsTable.USERNAME, list_data.getJSONObject(j).getString(DatabaseConstant.FriendsTable.USERNAME));
                                        mMapArgument.put(DatabaseConstant.FriendsTable.IMG_URL, list_data.getJSONObject(j).getString(DatabaseConstant.FriendsTable.IMG_URL));
                                        mMapArgument.put(DatabaseConstant.FriendsTable.LAST_MESSAGE, list_data.getJSONObject(j).getString(DatabaseConstant.FriendsTable.LAST_MESSAGE));
                                        mMapArgument.put(DatabaseConstant.FriendsTable.DATETIME, list_data.getJSONObject(j).getString(DatabaseConstant.FriendsTable.DATETIME));
//                                      mContactsDao.insertContact(mMapArgument);
                                        ContactsList.Contacts contacts = contactsList.new Contacts();
                                        contacts.setUser_id(list_data.getJSONObject(j).getInt("user_id"));
                                        contacts.setGroup_id(list_data.getJSONObject(j).getInt("group_id"));
                                        contacts.setUsername(list_data.getJSONObject(j).getString("username"));
                                        contacts.setImg_url(list_data.getJSONObject(j).getString("img_url"));
                                        contacts.setContent(list_data.getJSONObject(j).getString("content"));
                                        contacts.setCreate_time(list_data.getJSONObject(j).getString("create_time"));
                                        contactsList.mList.add(contacts);

                                        maps.put(key, contactsList.mList);
                                    }
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, maps).sendToTarget();
                                }
                            }
                            //http请求超时
                            else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                                mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                            }//无数据，json中的code值为1005
                            else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT) {
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            } else {
                                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
                            }
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                }
            });



        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PublicConstant.GET_SUCCESS:
                    mMaps = (HashMap<Integer, List<ContactsList.Contacts>>) msg.obj;
                    mExpAdapter.setData(mMaps);
                    mExpAdapter.notifyDataSetChanged();
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if (null == msg.obj) {
                        Utils.showToast(getActivity(), getString(R.string.http_request_error));
                    } else {
                        Utils.showToast(getActivity(), (String) msg.obj);
                    }
                    break;
                case PublicConstant.NO_RESULT:
//                    Utils.showToast(getActivity(), getString(R.string.no_contact_info));
                    break;
                default:
                    break;
            }

        }
    };

    private void modifyUser(GotyeUser user,String img_url,String nickName,int gender) {
        int split = img_url.lastIndexOf("/");
        String url = YueQiuApp.sUserInfo.getImg_url().substring(split + 1);
        user.setNickname(nickName + ":" + url);

        user.setGender(gender == 1 ? GotyeGender.Male : GotyeGender.Femal);
        Log.e("cao", " contact modify mGotyeUser = " + user);
        int result = mApi.modifyUserInfo(user, null);

        Log.d("cao","contact modify result" + result);
    }
}

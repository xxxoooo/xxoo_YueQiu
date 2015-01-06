package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.ExpAdapter;
import com.yueqiu.bean.ContactsList;
import com.yueqiu.bean.RecentChat;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AsyncTaskBase;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.view.contacts.IphoneTreeView;
import com.yueqiu.view.contacts.LoadingView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final int GET_SUCCESS = 0;
    private List<ContactsList.Contacts> mList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        mBaseView = inflater.inflate(R.layout.fragment_chatbar_contact, null);
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
        mIphoneTreeView.setHeaderView(LayoutInflater.from(mContext).inflate(
                R.layout.fragment_constact_head_view, mIphoneTreeView, false));
        mIphoneTreeView.setGroupIndicator(null);
//		mExpAdapter = new ExpAdapter(mContext, maps, mIphoneTreeView,mSearchView);
        mExpAdapter = new ExpAdapter(mContext, mIphoneTreeView);
        mIphoneTreeView.setAdapter(mExpAdapter);
//		new AsyncTaskLoading(mLoadingView).execute(0);
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContactList();
            }
        }).start();
    }

    /**
     * 加载最近的聊天记录！！
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
        HashMap<Integer, List<ContactsList.Contacts>> maps = new HashMap<Integer, List<ContactsList.Contacts>>();
        for (int i = 0; i < 3; i++) {
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put(PublicConstant.USER_ID, YueQiuApp.sUserInfo.getUser_id());
            map.put(HttpConstants.ContactsList.GROUP_ID, i + 1);
            String result = HttpUtil.urlClient(HttpConstants.ContactsList.URL, map, HttpConstants.RequestMethod.GET);

            try {
                JSONObject jsonResult = new JSONObject(result);

                if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                    ContactsList contactsList = new ContactsList();
//                    contactsList.setCount(jsonResult.getJSONObject("result").getInt("count"));
                    JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
                    for (int j = 0; j < list_data.length(); j++) {
                        ContactsList.Contacts contacts = contactsList.new Contacts();
                        contacts.setUser_id(list_data.getJSONObject(i).getInt("user_id"));
                        contacts.setGroup_id(list_data.getJSONObject(i).getInt("group_id"));
                        contacts.setUsername(list_data.getJSONObject(i).getString("username"));
                        contacts.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                        contacts.setContent(list_data.getJSONObject(i).getString("content"));
                        contacts.setCreate_time(list_data.getJSONObject(i).getString("create_time"));
                        contactsList.mList.add(contacts);
                    }
                    maps.put(i, contactsList.mList);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException: " + e);
            }
            mHandler.obtainMessage(GET_SUCCESS, maps).sendToTarget();
        }

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SUCCESS:
                    mMaps = (HashMap<Integer, List<ContactsList.Contacts>>) msg.obj;
//                    mExpAdapter = new ExpAdapter(mContext, mMaps, mIphoneTreeView);
                    mExpAdapter.setData(mMaps);
                    mExpAdapter.notifyDataSetChanged();
                    break;
                default:

                    break;
            }

        }
    };
}

package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatTarget;
import com.gotye.api.GotyeChatTargetType;
import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeRoom;
import com.gotye.api.GotyeUser;
import com.gotye.api.listener.DownloadListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.FriendsApplicationActivity;
import com.yueqiu.adapter.MessageListAdapter;
import com.yueqiu.bean.FriendsApplication;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.im.ChatPage;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

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
 * 聊吧消息Fragment
 */
public class MessageFragment extends Fragment implements DownloadListener{
    private static final String TAG = "MessageFragment";
    private static final int AGREE = 1;
    private static final int UNAGREE = 0;
    private ListView mListView;
    private LinearLayout mSearch;
    private ActionBar mActionBar;
    public static final String FRIEND_USER_ID = "com.yueqiu.fragment.chatbar.MessageFragment.friend_user_id";
    public static final String FRIEND_USER_NAME = "com.yueqiu.fragment.chatbar.MessageFragment.friend_user_name";
    public static final String fixName = "验证消息";//验证消息
    private GotyeAPI mApi = GotyeAPI.getInstance();
    public MessageListAdapter mAdapter;
    private GotyeChatTarget mTarget;
    private Intent mBroadcastIntent;

    private MessageListAdapter.Verification mVerification;
    private int mIsAgree;
    private  List<GotyeChatTarget> mSessions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbar_message, null);
        mBroadcastIntent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
        }
        mListView = (ListView) view.findViewById(R.id.chatbar_message_lv_account);
        registerForContextMenu(mListView);

        mTarget = new GotyeUser(fixName);
        mVerification = new MessageListAdapter.Verification();
        mVerification.hasNewMsg = false;
        mVerification.newFriend = null;
        mVerification.title = getString(R.string.new_friend);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mApi.addListener(this);
        updateList();
        setListener();
        if(Utils.networkAvaiable(getActivity())) {
            getFriendApplication();
        }
    }


    private void updateList() {
       mSessions = mApi.getSessionList();

        Log.d("offLine", "List--sessions" + mSessions);

        if (mSessions == null) {
            mSessions = new ArrayList<GotyeChatTarget>();
            mSessions.add(mTarget);
        } else {
            mSessions.add(0, mTarget);

        }
        if (mAdapter == null) {
            mAdapter = new MessageListAdapter(MessageFragment.this, mSessions);
            mAdapter.setVertification(mVerification);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(mSessions);
        }

    }

    public void refresh() {
        updateList();
    }

    private void setListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                GotyeChatTarget target =  mAdapter.getItem(arg2);
                //TODO:fixName = "通知列表"
                if (target.getName().equals(fixName)) {
                    Intent i = new Intent(getActivity(), FriendsApplicationActivity.class);
                    startActivity(i);
                } else {
                    /**
                     * 下面这句是用来标记消息为已读的
                     */
                    GotyeAPI.getInstance().markMessagesAsRead(target);
                    //单人聊天
                    if (target.getType() == GotyeChatTargetType.GotyeChatTargetTypeUser) {
                        Log.e(TAG, "------------p2p chat-------------");
                        Intent toChat = new Intent(getActivity(),ChatPage.class);
                        GotyeUser user = mApi.requestUserInfo(target.getName(),true);
                        toChat.putExtra("user",  user);
                        startActivity(toChat);

                        Intent intent = new Intent();
                        intent.setAction(PublicConstant.CHAT_HAS_NO_MSG);
                        getActivity().sendBroadcast(intent);
                        // updateList();
                    }
                    //聊天室聊天
                    else if (target.getType() == GotyeChatTargetType.GotyeChatTargetTypeRoom) {
                        Intent toChat = new Intent(getActivity(),ChatPage.class);
                        toChat.putExtra("room",  (GotyeRoom)target);
                        startActivity(toChat);
                        //群组聊天
                    } else if (target.getType() == GotyeChatTargetType.GotyeChatTargetTypeGroup) {
                        Intent toChat = new Intent(getActivity(),ChatPage.class);
                        toChat.putExtra("group",  (GotyeGroup)target);
                        startActivity(toChat);
                    }
                    refresh();
                }
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    public void onDestroy() {
        mApi.removeListener(this);
        super.onDestroy();

    }

    @Override
    public void onDownloadMedia(int code, String path, String url) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.chat_message_item_context, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        switch (item.getItemId()) {
            case R.id.menu_item_delete_message:
                if (position == 0)
                    return false;
                GotyeChatTarget target = mAdapter.getItem(position);
                mApi.deleteSession(target, false);
                updateList();
                return true;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void getFriendApplication() {
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(HttpConstants.GetAsk.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));


        HttpUtil.requestHttp(HttpConstants.GetAsk.URL, requestMap, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Log.d("wy","message get friend apply ->" + response);
                try {
                    if (!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            JSONObject result = response.getJSONObject("result");

                            if (result.get("list_data").toString().equals("null")) {
                                Log.d(TAG, "list_data is null");
                            } else {
                                JSONArray list_data = result.getJSONArray("list_data");
                                if(list_data.length() >= 1) {
                                    for(int i=0;i<list_data.length();i++) {
                                        FriendsApplication application = new FriendsApplication();
                                        String id = list_data.getJSONObject(i).getString("id");
                                        String nick = list_data.getJSONObject(i).getString("nick");
                                        String username = list_data.getJSONObject(i).getString("username");
                                        String create_time = list_data.getJSONObject(i).getString("create_time");
                                        String img_url = list_data.getJSONObject(i).getString("img_url");
                                        mIsAgree = list_data.getJSONObject(i).getInt("state");
                                        application.setId(id);
                                        application.setNick(nick);
                                        application.setUsername(username);
                                        application.setCreate_time(create_time);
                                        application.setImg_url(img_url);
                                        application.setIsAgree(mIsAgree);

                                        if (mIsAgree == UNAGREE) {
                                            mVerification.hasNewMsg = true;
                                            mVerification.newFriend = application;
                                            mVerification.title = getString(R.string.verify_msg);
//
//                                            mSessions.remove(0);
//                                            GotyeUser user = new GotyeUser(fixName);
//                                            mSessions.add(0,user);
                                            mAdapter.setVertification(mVerification);
                                            mBroadcastIntent.setAction(PublicConstant.CHAT_HAS_NEW_MSG);
                                            getActivity().sendBroadcast(mBroadcastIntent);
                                            break;
                                        }
                                    }
                                    if(mIsAgree == AGREE){

                                        mVerification.hasNewMsg = false;
                                        mVerification.newFriend = null;
                                        mVerification.title = getActivity().getString(R.string.new_friend);

//
//                                        mSessions.remove(0);
//                                        GotyeUser user = new GotyeUser(fixName);
//                                        mSessions.add(0,user);

                                        mAdapter.setVertification(mVerification);
                                        mBroadcastIntent.setAction(PublicConstant.CHAT_HAS_NO_MSG);
                                        getActivity().sendBroadcast(mBroadcastIntent);
                                    }

                                }
                            }
                        }

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException->" + e);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });

    }

}

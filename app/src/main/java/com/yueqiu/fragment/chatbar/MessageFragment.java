package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.yueqiu.ChatBarActivity;
import com.yueqiu.R;
import com.yueqiu.activity.ChatActivity;
import com.yueqiu.activity.FriendsApplicationActivity;
import com.yueqiu.adapter.ChatBarItemAdapter;
import com.yueqiu.adapter.MessageListAdapter;
import com.yueqiu.chatbar.ChatPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧消息Fragment
 */
public class MessageFragment extends Fragment implements DownloadListener{
    private static final String TAG = "MessageFragment";
    Resources resources;
    private ListView mListView;
    private LinearLayout mSearch;
    private ActionBar mActionBar;
    public static final String FRIEND_USER_ID = "com.yueqiu.fragment.chatbar.MessageFragment.friend_user_id";
    public static final String FRIEND_USER_NAME = "com.yueqiu.fragment.chatbar.MessageFragment.friend_user_name";
    public static final String fixName = "通知列表";//验证消息
    private GotyeAPI api = GotyeAPI.getInstance();
    private MessageListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbar_message, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
        }
        resources = getResources();
        mListView = (ListView) view.findViewById(R.id.chatbar_message_lv_account);
//        final ChatBarItemAdapter adapter = new ChatBarItemAdapter(getActivity());
//        mListView.setAdapter(adapter);
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                if (0 == position) {
//                    startActivity(new Intent(getActivity(), FriendsApplicationActivity.class));
//                }else{
//                    //TODO:传入待聊天好友的userid
//                    Intent intent = new Intent(getActivity(), ChatActivity.class);
//                    adapter.getItem(position);
//                    intent.putExtra(FRIEND_USER_ID, 1);//fake date
//                    intent.putExtra(FRIEND_USER_NAME, "小明");
//                    startActivity(intent);
//                }
//                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
//            }
//        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateList();
        setListener();
    }

    private void updateList() {
        List<GotyeChatTarget> sessions = api.getSessionList();
        Log.d("offLine", "List--sessions" + sessions);

        GotyeChatTarget target = new GotyeChatTarget();
        target.name = fixName;

        if (sessions == null) {
            sessions = new ArrayList<GotyeChatTarget>();
            sessions.add(target);
        } else {
            sessions.add(0, target);
        }
        if (mAdapter == null) {
            mAdapter = new MessageListAdapter(MessageFragment.this, sessions);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(sessions);
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
                GotyeChatTarget target = (GotyeChatTarget) mAdapter.getItem(arg2);
                //TODO:fixName = "通知列表"
                if (target.name.equals(fixName)) {
//                    Intent i = new Intent(getActivity(), NotifyListPage.class);
//                    startActivity(i);
                } else {
                    /**
                     * 下面这句是用来标记消息为已读的
                     */
                    GotyeAPI.getInstance().markMeeagesAsread(target);
                    //单人聊天
                    if (target.type == GotyeChatTargetType.GotyeChatTargetTypeUser) {
                        Intent toChat = new Intent(getActivity(),ChatPage.class);
                        toChat.putExtra("user", (GotyeUser) target);
                        startActivity(toChat);
                        // updateList();
                    }
                    //聊天室聊天
                    else if (target.type == GotyeChatTargetType.GotyeChatTargetTypeRoom) {
                        Intent toChat = new Intent(getActivity(),ChatPage.class);
                        toChat.putExtra("room", (GotyeRoom) target);
                        startActivity(toChat);
                        //群组聊天
                    } else if (target.type == GotyeChatTargetType.GotyeChatTargetTypeGroup) {
                        Intent toChat = new Intent(getActivity(),ChatPage.class);
                        toChat.putExtra("group", (GotyeGroup) target);
                        startActivity(toChat);
                    }
                    refresh();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        GotyeAPI.getInstance().removeListener(this);
        super.onDestroy();

    }

    @Override
    public void onDownloadMedia(int code, String path, String url) {
        // TODO Auto-generated method stub
        mAdapter.notifyDataSetChanged();
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

}

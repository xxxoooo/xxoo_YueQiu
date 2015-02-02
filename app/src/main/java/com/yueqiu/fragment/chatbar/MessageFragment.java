package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.yueqiu.ChatBarActivity;
import com.yueqiu.R;
import com.yueqiu.activity.ChatActivity;
import com.yueqiu.activity.FriendsApplicationActivity;
import com.yueqiu.adapter.ChatBarItemAdapter;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧消息Fragment
 */
public class MessageFragment extends Fragment {
    private static final String TAG = "MessageFragment";
    Resources resources;
    private ListView mListView;
    private LinearLayout mSearch;
    private ActionBar mActionBar;
    public static final String FRIEND_USER_ID = "com.yueqiu.fragment.chatbar.MessageFragment.friend_user_id";
    public static final String FRIEND_USER_NAME = "com.yueqiu.fragment.chatbar.MessageFragment.friend_user_name";

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
        final ChatBarItemAdapter adapter = new ChatBarItemAdapter(getActivity());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (0 == position) {
                    startActivity(new Intent(getActivity(), FriendsApplicationActivity.class));
                }else{
                    //TODO:传入待聊天好友的userid
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    adapter.getItem(position);
                    intent.putExtra(FRIEND_USER_ID, 1);//fake date
                    intent.putExtra(FRIEND_USER_NAME, "小明");
                    startActivity(intent);
                }
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        return view;
    }

}

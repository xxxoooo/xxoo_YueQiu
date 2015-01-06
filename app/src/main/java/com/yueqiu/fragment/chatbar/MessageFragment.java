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

import com.yueqiu.AddFriendsActivity;
import com.yueqiu.R;
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
        ChatBarItemAdapter adapter = new ChatBarItemAdapter(getActivity());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                startActivity(new Intent(getActivity(), AddFriendsActivity.class));
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chatbar_menu_search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

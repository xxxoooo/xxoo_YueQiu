package com.yueqiu.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_message, null);
		resources = getResources();
        mListView = (ListView) view.findViewById(R.id.chatbar_message_lv_account);
        ChatBarItemAdapter adapter = new ChatBarItemAdapter(getActivity());
        mListView.setAdapter(adapter);
		return view;
	}

}

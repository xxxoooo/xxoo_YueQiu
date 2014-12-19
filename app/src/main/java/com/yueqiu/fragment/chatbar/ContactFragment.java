package com.yueqiu.fragment.chatbar;

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
 * 聊吧联系人fragment
 */
public class ContactFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_contact, null);
        ListView listView = (ListView) view.findViewById(R.id.chatbar_contact_lv_account);
        ChatBarItemAdapter adapter = new ChatBarItemAdapter(getActivity());
        listView.setAdapter(adapter);
		return view;
	}

}

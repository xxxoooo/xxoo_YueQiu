package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yueqiu.ChatBarSearchResultActivity;
import com.yueqiu.R;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧添加好友Fragment
 */
public class AddPersonFragment extends Fragment {
    private static final String TAG = "AddPersonFragment";
    Button mSearch;
    private ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        setMenuVisibility(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbar_add_persion, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
        }
        view.findViewById(R.id.chatbar_add_persion_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:result of search，new Activity or fragment
                Intent intent = new Intent(getActivity(), ChatBarSearchResultActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

}

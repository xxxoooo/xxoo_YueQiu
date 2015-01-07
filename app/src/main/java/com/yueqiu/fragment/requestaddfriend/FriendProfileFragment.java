package com.yueqiu.fragment.requestaddfriend;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yueqiu.R;

/**
 * Created by doushuqi on 15/1/6.
 */
public class FriendProfileFragment extends Fragment {

    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;
    private Button mButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getActivity().getActionBar();
        setHasOptionsMenu(true);
        mFragmentManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);
        mButton = (Button) view.findViewById(R.id.add_to_friends);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new VerificationFragment();
                mFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });
        return view;
    }
}

package com.yueqiu.fragment.requestaddfriend;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;
import com.yueqiu.activity.FriendsApplicationActivity;

/**
 * Created by doushuqi on 15/1/6.
 */
public class FriendManageFragment extends Fragment {
    private ActionBar mActionBar;
    private FragmentManager mFragmentManager;
    private Fragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActionBar = getActivity().getActionBar();
        mActionBar.setTitle(R.string.qiuyou_manage);
        mFragmentManager = getActivity().getSupportFragmentManager();
        mFragment = mFragmentManager.findFragmentById(R.id.fragment_container);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_friend, container, false);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.finish, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((FriendsApplicationActivity)getActivity()).switchFragment(FriendsApplicationActivity.sFriendsApplication);
                return true;
            case R.id.qiuyou_manage_finish:
                //TODO:
//                mFragmentManager.beginTransaction().replace(R.id.fragment_container, FriendsApplicationActivity.sCurrentFragment).commit();
                ((FriendsApplicationActivity)getActivity()).switchFragment(FriendsApplicationActivity.sFriendsApplication);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

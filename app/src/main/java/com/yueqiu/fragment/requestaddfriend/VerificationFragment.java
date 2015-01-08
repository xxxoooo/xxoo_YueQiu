package com.yueqiu.fragment.requestaddfriend;

import android.app.ActionBar;
import android.os.Build;
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
import com.yueqiu.util.Utils;

/**
 * Created by doushuqi on 15/1/6.
 */
public class VerificationFragment extends Fragment {

    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;
    public static final String ARGUMENTS_KEY = "com.yueqiu.fragment.requestaddfriend.arguments_key";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
            mActionBar.setTitle(R.string.identity_verify);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        setHasOptionsMenu(true);
        mFragmentManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_identity_verification, container, false);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            case R.id.next:
                Bundle args = new Bundle();
                args.putInt(ARGUMENTS_KEY, 1);
                Fragment fragment = new FriendManageFragment();
                fragment.setArguments(args);
                mFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Utils.setFragmentActivityMenuColor(getActivity());
        getActivity().getMenuInflater().inflate(R.menu.next, menu);
    }

}

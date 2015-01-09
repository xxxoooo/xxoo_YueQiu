package com.yueqiu.fragment.requestaddfriend;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.activity.FriendSetGroupActivity;
import com.yueqiu.activity.FriendsApplicationActivity;
import com.yueqiu.util.Utils;

import org.w3c.dom.Text;

/**
 * Created by doushuqi on 15/1/6.
 */
public class FriendManageFragment extends Fragment {
    private ActionBar mActionBar;
    private FragmentManager mFragmentManager;
    private Fragment mFragment;
    private int whoCreate;
    private TextView mTextView;
    private int mGroupId;//分组id
    private String mComment;//备注
    private String[] mLabel;//标签
    private static final int REQUEST_CODE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActionBar = getActivity().getActionBar();
        mActionBar.setTitle(R.string.qiuyou_manage);
        mFragmentManager = getActivity().getSupportFragmentManager();
        whoCreate = getArguments().getInt(VerificationFragment.ARGUMENTS_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_friend, container, false);
        mTextView = (TextView) view.findViewById(R.id.friend_group);
        view.findViewById(R.id.friend_manage_set_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendSetGroupActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            mGroupId = data.getIntExtra(FriendSetGroupFragment.RESULT_KEY, -1);
            mTextView.setText(mGroupId == 0 ? R.string.search_billiard_mate_str : (mGroupId == 1)
                    ? R.string.search_billiard_assist_coauch_str : R.string.search_billiard_coauch_str);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Utils.setFragmentActivityMenuColor(getActivity());
        switch (whoCreate) {
            case 0:
                getActivity().getMenuInflater().inflate(R.menu.finish, menu);
                break;
            case 1:
                getActivity().getMenuInflater().inflate(R.menu.send, menu);
                break;
            default:
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (whoCreate == 0)
                    ((FriendsApplicationActivity) getActivity()).switchFragment(FriendsApplicationActivity.sFriendsApplication);
                else getActivity().finish();
                return true;
            case R.id.qiuyou_manage_finish:
                //TODO:
//                mFragmentManager.beginTransaction().replace(R.id.fragment_container, FriendsApplicationActivity.sCurrentFragment).commit();
                ((FriendsApplicationActivity) getActivity()).switchFragment(FriendsApplicationActivity.sFriendsApplication);
                return true;
            case R.id.send:
                //发送好友请求

                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

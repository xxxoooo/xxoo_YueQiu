package com.yueqiu.fragment.requestaddfriend;

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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.activity.FriendsApplicationActivity;
import com.yueqiu.util.Utils;

/**
 * Created by doushuqi on 15/1/7.
 */
public class FriendsApplicationFragment extends Fragment {
    private ListView mListView;
    private MyAdapter mMyAdapter;
    private FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFragmentManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getActionBar().setTitle(R.string.qiuyou_application);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_application, container, false);
        mListView = (ListView) view.findViewById(R.id.friends_application_container);
        mMyAdapter = new MyAdapter();
        mListView.setAdapter(mMyAdapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Utils.setFragmentActivityMenuColor(getActivity());
        getActivity().getMenuInflater().inflate(R.menu.clear, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.qiuyou_application_clear:

                mMyAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_friends_application, null);
                viewHolder = new ViewHolder();
                viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.friends_application_icon);
                viewHolder.mAccount = (TextView) convertView.findViewById(R.id.friends_application_username);
                viewHolder.mMessage = (TextView) convertView.findViewById(R.id.friends_application_message);
                viewHolder.mSendTime = (TextView) convertView.findViewById(R.id.friends_application_time);
                viewHolder.mAgree = (Button) convertView.findViewById(R.id.friends_application_agree);
                viewHolder.mAgree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //点击同意按钮
                        Bundle args = new Bundle();
                        args.putInt(VerificationFragment.ARGUMENTS_KEY, 0);
                        Fragment fragment = new FriendManageFragment();
                        fragment.setArguments(args);
                        ((FriendsApplicationActivity)getActivity()).switchFragment(fragment);
                    }
                });
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //TODO:设置布局中控件的内容,need data!
//        viewHolder.mAccount.setText();
//        viewHolder.mLastMessage.setText();

            return convertView;
        }

        final class ViewHolder {
            public ImageView mImageView;
            public TextView mAccount;
            public TextView mMessage;
            public TextView mSendTime;
            public Button mAgree;
        }
    }
}

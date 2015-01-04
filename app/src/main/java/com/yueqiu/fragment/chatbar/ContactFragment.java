package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yueqiu.R;
import com.yueqiu.adapter.ExpAdapter;
import com.yueqiu.bean.RecentChat;
import com.yueqiu.test.TestData;
import com.yueqiu.util.AsyncTaskBase;
import com.yueqiu.view.contacts.IphoneTreeView;
import com.yueqiu.view.contacts.LoadingView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧联系人fragment
 */
public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";
    private ActionBar mActionBar;
    private Context mContext;
    private View mBaseView;
    private LoadingView mLoadingView;
    private IphoneTreeView mIphoneTreeView;
    private ExpAdapter mExpAdapter;
    private HashMap<String, List<RecentChat>> maps = new HashMap<String, List<RecentChat>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        mBaseView = inflater.inflate(R.layout.fragment_chatbar_contact, null);
        findView();
        init();
        return mBaseView;
    }

    private void findView() {
        mLoadingView = (LoadingView) mBaseView.findViewById(R.id.loadingView);
        mIphoneTreeView = (IphoneTreeView) mBaseView.findViewById(R.id.iphone_tree_view);
    }

    private void init() {
        mIphoneTreeView.setHeaderView(LayoutInflater.from(mContext).inflate(
                R.layout.fragment_constact_head_view, mIphoneTreeView, false));
        mIphoneTreeView.setGroupIndicator(null);
//		mExpAdapter = new ExpAdapter(mContext, maps, mIphoneTreeView,mSearchView);
        mExpAdapter = new ExpAdapter(mContext, maps, mIphoneTreeView);
        mIphoneTreeView.setAdapter(mExpAdapter);
//		new AsyncTaskLoading(mLoadingView).execute(0);
    }

    /**
     * 加载最近的聊天记录！！
     */
    private class AsyncTaskLoading extends AsyncTaskBase {
        public AsyncTaskLoading(LoadingView loadingView) {
            super(loadingView);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            int result = -1;
            maps.put("球友", TestData.getRecentChats());
            maps.put("助教", TestData.getRecentChats());
            maps.put("教练", TestData.getRecentChats());
            result = 1;
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }

}

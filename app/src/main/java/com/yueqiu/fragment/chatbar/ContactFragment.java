package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.adapter.ChatBarItemAdapter;
import com.yueqiu.view.IphoneTreeView;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧联系人fragment
 */
public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";
    private ActionBar mActionBar;
    private IphoneTreeView mIphoneTreeView;
//    private ExpAdapter mExpAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_chatbar_contact, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
        }
        ListView listView = (ListView) view.findViewById(R.id.chatbar_contact_lv_account);
        ChatBarItemAdapter adapter = new ChatBarItemAdapter(getActivity());
        listView.setAdapter(adapter);

        mIphoneTreeView = (IphoneTreeView) view.findViewById(R.id.iphone_tree_view);
        init();

		return view;
	}

    private void init() {
        mIphoneTreeView.setHeaderView(LayoutInflater.from(getActivity()).inflate(
                R.layout.fragment_constact_head_view, mIphoneTreeView, false));
        mIphoneTreeView.setGroupIndicator(null);
//        mExpAdapter = new ExpAdapter(mContext, maps, mIphoneTreeView,mSearchView);
//        mIphoneTreeView.setAdapter(mExpAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.chatbar_search, menu);
        /** Get the action view of the menu item whose id is search */
        View v = (View) menu.findItem(R.id.chatbar_menu_search).getActionView();

        /** Get the edit text from the action view */
        EditText txtSearch = ( EditText ) v.findViewById(R.id.txt_search);

        /** Setting an action listener */
        txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Toast.makeText(getActivity(), "Search : " + v.getText(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}

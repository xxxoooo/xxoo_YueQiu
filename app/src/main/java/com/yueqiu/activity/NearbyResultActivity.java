package com.yueqiu.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import com.yueqiu.R;
import android.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

/**
 * Created by wangyun on 14/12/30.
 */
public class NearbyResultActivity extends Activity implements SearchView.OnQueryTextListener {
    private SearchView mSearchView;
    private ActionBar mActionBar;
    private String mQueryResult;
    private TextView mResultView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result_layout);
        handleIntent(getIntent());
        mResultView = (TextView) findViewById(R.id.query_result);
        mResultView.setText(mQueryResult);
        initActionBar();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQueryResult = intent.getStringExtra(SearchManager.QUERY);

        }
    }

    private void initActionBar(){

        mActionBar = getActionBar();

        View customSearchView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar_layout, null);
        int searchViewWidth = getResources().getDimensionPixelSize(R.dimen.search_view_width);
        if (searchViewWidth == 0) {
            searchViewWidth = ActionBar.LayoutParams.MATCH_PARENT;
        }
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(searchViewWidth, ActionBar.LayoutParams.WRAP_CONTENT);
        mSearchView = (SearchView) customSearchView.findViewById(R.id.search_view);
        mSearchView.setIconified(true);
        View backView =  customSearchView.findViewById(R.id.back_menu_item);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        mSearchView.setOnCloseListener(this);
        mSearchView.setQuery(mQueryResult, false);
        mSearchView.setOnQueryTextListener(this);
        mActionBar.setCustomView(customSearchView, layoutParams);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
    }


    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        mResultView.setText(newText);
        return false;
    }
}

package com.yueqiu.activity.searchmenu;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.yueqiu.R;


public class FeedbackActivity extends Activity
{
    private static final String TAG = "FeedbackActivity";

    private EditText mEtFeedbackTitle, mEtFeedbackContent, mEtFeedbackContact;
    private static final int SUBMIT_ACTION = 1 << 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initActionBar();

        mEtFeedbackTitle = (EditText) findViewById(R.id.et_feedback_title);
        mEtFeedbackContent = (EditText) findViewById(R.id.et_feedback_content);
        mEtFeedbackContact = (EditText) findViewById(R.id.et_feedback_contact);

        (findViewById(R.id.btn_feedback_submit)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sSubmitHandler.sendEmptyMessage(SUBMIT_ACTION);
            }
        });
    }

    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.custom_actionbar_layout, null);
            View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            saveMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FeedbackActivity.this.finish();
                }
            });
            TextView title = (TextView) customActionBarView.findViewById(R.id.action_bar_title);
            title.setText(getString(R.string.search_feed_back_str));
            actionBar.setDisplayShowCustomEnabled(true);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(customActionBarView,params);
        }
    }


    private static Handler sSubmitHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == SUBMIT_ACTION)
            {
                // perform the submit action

            }
        }
    };


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.feedback, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}

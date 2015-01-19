package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.yueqiu.R;
import com.yueqiu.util.Utils;


public class FeedbackActivity extends Activity
{
    private static final String TAG = "FeedbackActivity";

    private EditText mEtFeedbackTitle, mEtFeedbackContent;
    private static final int SUBMIT_ACTION = 1 << 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initActionBar();

    }

    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.search_feed_back_str));
        actionBar.setDisplayHomeAsUpEnabled(true);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Utils.setActivityMenuColor(this);
        getMenuInflater().inflate(R.menu.feedback, menu);
        return true;
    }
//

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}

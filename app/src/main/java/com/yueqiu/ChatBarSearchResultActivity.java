package com.yueqiu;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.yueqiu.adapter.ChatBarSearchResultAdapter;

/**
 * Created by doushuqi on 14/12/18.
 * 聊吧添加好友搜索结果Activity
 */
public class ChatBarSearchResultActivity extends Activity {
    private static final String TAG = "SearchResultActivity";
    private TextView mBack;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbar_searchresult);
        init();

        ChatBarSearchResultAdapter adapter = new ChatBarSearchResultAdapter(this);
        Log.e(TAG, "listview = " + mListView + "   adapter" + adapter);
        mListView.setAdapter(adapter);

    }

    private void init(){
        mListView = (ListView) findViewById(R.id.chatbar_searchresult_lv_account);
        mBack = (TextView)findViewById(R.id.chatbar_searchresult_btn_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}

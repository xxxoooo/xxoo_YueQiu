package com.yueqiu;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.yueqiu.adapter.ChatBarSearchResultAdapter;

/**
 * Created by doushuqi on 14/12/18.
 * 聊吧添加好友搜索结果Activity
 */
public class SearchResultActivity extends Activity {
    private static final String TAG = "SearchResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresult);

        ListView listView = (ListView) findViewById(R.id.chatbar_searchresult_lv_account);
        ChatBarSearchResultAdapter adapter = new ChatBarSearchResultAdapter(this);
        Log.e(TAG, "listview = " + listView + "   adapter" + adapter);
        listView.setAdapter(adapter);
    }

    private void createDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("消息验证")

                .create();
    }


}

package com.yueqiu;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by administrator on 14/12/25.
 */
public class ChatBarSearchActivity extends Activity {
    private static final String TAG = "ChatBarSearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_chatbar_search);
    }
}

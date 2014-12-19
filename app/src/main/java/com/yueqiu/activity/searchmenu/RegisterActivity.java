package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.yueqiu.R;

/**
 * Created by yinfeng on 14/12/18.
 */
public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

    }
}

package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.yueqiu.R;

/**
 * Created by yinfeng on 14/12/19.
 */
public class ActivitiesIssueActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activities_issue);
    }
}

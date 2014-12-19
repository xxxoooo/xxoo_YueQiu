package com.yueqiu;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;

import com.yueqiu.fragment.search.BilliardsAssistCoachFragment;
import com.yueqiu.fragment.search.BilliardsCoachFragment;
import com.yueqiu.fragment.search.BilliardsDatingFragment;
import com.yueqiu.fragment.search.BilliardsMateFragment;
import com.yueqiu.fragment.search.BilliardsRoomFragment;

public class BilliardSearchActivity extends FragmentActivity
{
    private static final String TAG = "BilliardSearchActivity";

    private static final int NUM_OF_FRAGMENTS = 5;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private RadioGroup mRadioGroup;

    // make the instances of the basic fragment that directly loaded in the BilliardSearchActivity
    private BilliardsAssistCoachFragment mAssistCoachFragment;
    private BilliardsMateFragment mMateFragment;
    private BilliardsRoomFragment mRoomFragment;
    private BilliardsDatingFragment mDatingFragment;
    private BilliardsCoachFragment mCoachFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_search);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.billiard_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}


























































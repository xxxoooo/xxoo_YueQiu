package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.yueqiu.R;


public class PlaySelectType extends Activity implements View.OnClickListener {
    private static final int PLAY_GROUP          = 0;
    private static final int PLAY_MEET_STAR      = 1;
    private static final int PLAY_BILLIARD_SHOW  = 2;
    private static final int PLAY_COMPETITION    = 3;
    private static final int PLAY_OTHER          = 4;
    private View mGroup, mMeetStar, mBilliardShow, mCompetition, mOther;
    private static final String TYPE = "type";
    private ImageView mGroupIv, mMeetStarIv, mBilliardIv, mCompetitonIv, mOtherIv;
    private ActionBar mActionBar;
    private Intent mIntent;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_select_type);
        initActionBar();
        initView();
        mIntent = getIntent();
        if (mIntent.getStringExtra(TYPE) == null || mIntent.getStringExtra(TYPE).equals("")) {
            mGroupIv.setVisibility(View.VISIBLE);
            index = 0;
        } else if (mIntent.getStringExtra(TYPE).equals("1")) {
            mMeetStarIv.setVisibility(View.VISIBLE);
            index = 1;
        } else if (mIntent.getStringExtra(TYPE).equals("2")) {
            mBilliardIv.setVisibility(View.VISIBLE);
            index = 2;
        } else if (mIntent.getStringExtra(TYPE).equals("3")) {
            mCompetitonIv.setVisibility(View.VISIBLE);
            index = 3;
        } else if (mIntent.getStringExtra(TYPE).equals("4")) {
            mOther.setVisibility(View.VISIBLE);
            index = 4;
        } else {
            mGroupIv.setVisibility(View.VISIBLE);
            index = 0;
        }

    }

    private void initView() {
        mGroup =  findViewById(R.id.play_group);
        mMeetStar =  findViewById(R.id.play_meet_star);
        mBilliardShow =  findViewById(R.id.play_billiard_show);
        mCompetition =  findViewById(R.id.play_competition);
        mOther = findViewById(R.id.play_other);
        mGroupIv = (ImageView) findViewById(R.id.play_group_iv);
        mMeetStarIv = (ImageView) findViewById(R.id.play_meet_star_iv);
        mBilliardIv = (ImageView) findViewById(R.id.play_billiard_show_iv);
        mCompetitonIv = (ImageView) findViewById(R.id.play_competition_iv);
        mOtherIv = (ImageView) findViewById(R.id.play_other_iv);
        mGroup.setOnClickListener(this);
        mMeetStar.setOnClickListener(this);
        mBilliardShow.setOnClickListener(this);
        mCompetition.setOnClickListener(this);
        mOther.setOnClickListener(this);

    }


    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.select_activity_type));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finishThis(index);
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finishThis(index);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void finishThis(int index) {
        mIntent = new Intent();
        mIntent.putExtra(TYPE,String.valueOf(index));
        setResult(RESULT_OK, mIntent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.play_group:
                finishThis(PLAY_GROUP);
                break;
            case R.id.play_meet_star:
                finishThis(PLAY_MEET_STAR);
                break;
            case R.id.play_billiard_show:
                finishThis(PLAY_BILLIARD_SHOW);
                break;
            case R.id.play_competition:
                finishThis(PLAY_COMPETITION);
                break;
            case R.id.play_other:
                finishThis(PLAY_OTHER);
                break;
        }
    }
}

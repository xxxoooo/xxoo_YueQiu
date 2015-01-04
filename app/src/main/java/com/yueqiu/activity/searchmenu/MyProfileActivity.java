package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.yueqiu.ProfileSetupActivity;
import com.yueqiu.R;

import android.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by doushuqi on 14/12/19.
 * 我的资料主Activity
 */
public class MyProfileActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MyProfileActivity";
    private Button mAssistant, mCoach;
    private RelativeLayout mPhoto, mAccount, mGender, mNickName, mRegion, mLevel, mBallType,
            mBilliardsCue, mCueHabits, mPlayAge, mIdol, mSign, mTheNewestPost;
    private TextView mNickNameTextView, mRegionTextView, mLevelTextView, mBallTypeTextView,
            mBilliardsCueTextView, mCueHabitsTextView, mPlayAgeTextView, mIdolTextView,
            mSignTextView;
    private ImageView mTheNewestPostImageView;

    public static final String EXTRA_FRAGMENT_ID =
            "com.yueqiu.activity.searchmenu.myprofileactivity.fragment_id";
    public static int EXTRA_REQUEST_ID = 0;
    public static String EXTRA_RESULT_ID = "com.yueqiu.activity.searchmenu.myprofileactivity.result_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.search_my_profile_str));
        initView();
        setClickListener();


    }

    private void initView() {
        mAssistant = (Button) findViewById(R.id.update_assistant_btn);
        mCoach = (Button) findViewById(R.id.update_coach_btn);
        mPhoto = (RelativeLayout) findViewById(R.id.my_profile_photo);
        mAccount = (RelativeLayout) findViewById(R.id.my_profile_account);
        mGender = (RelativeLayout) findViewById(R.id.my_profile_gender);
        mNickName = (RelativeLayout) findViewById(R.id.my_profile_nick_name);
        mRegion = (RelativeLayout) findViewById(R.id.my_profile_region);
        mLevel = (RelativeLayout) findViewById(R.id.my_profile_level);
        mBallType = (RelativeLayout) findViewById(R.id.my_profile_ball_type);
        mBilliardsCue = (RelativeLayout) findViewById(R.id.my_profile_billiards_cue);
        mCueHabits = (RelativeLayout) findViewById(R.id.my_profile_cue_habits);
        mPlayAge = (RelativeLayout) findViewById(R.id.my_profile_play_age);
        mIdol = (RelativeLayout) findViewById(R.id.my_profile_idol);
        mSign = (RelativeLayout) findViewById(R.id.my_profile_sign);
        mTheNewestPost = (RelativeLayout) findViewById(R.id.my_profile_the_new_post);

        mNickNameTextView = (TextView) findViewById(R.id.my_profile_nick_name_tv);
        mRegionTextView = (TextView) findViewById(R.id.my_profile_region_tv);
        mLevelTextView = (TextView) findViewById(R.id.my_profile_level_tv);
        mBallTypeTextView = (TextView) findViewById(R.id.my_profile_ball_type_tv);
        mBilliardsCueTextView = (TextView) findViewById(R.id.my_profile_billiards_cue_tv);
        mCueHabitsTextView = (TextView) findViewById(R.id.my_profile_cue_habits_tv);
        mPlayAgeTextView = (TextView) findViewById(R.id.my_profile_play_age_tv);
        mIdolTextView = (TextView) findViewById(R.id.my_profile_idol_tv);
        mSignTextView = (TextView) findViewById(R.id.my_profile_sign_tv);
    }

    private void setClickListener() {
        mAssistant.setOnClickListener(this);
        mCoach.setOnClickListener(this);
        mPhoto.setOnClickListener(this);
        mAccount.setOnClickListener(this);
        mGender.setOnClickListener(this);
        mNickName.setOnClickListener(this);
        mRegion.setOnClickListener(this);
        mLevel.setOnClickListener(this);
        mBallType.setOnClickListener(this);
        mBilliardsCue.setOnClickListener(this);
        mCueHabits.setOnClickListener(this);
        mPlayAge.setOnClickListener(this);
        mIdol.setOnClickListener(this);
        mSign.setOnClickListener(this);
        mTheNewestPost.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.update_assistant_btn:
                //TODO:升级助教界面
//                Intent intent = new Intent(this,);
                break;
            case R.id.update_coach_btn:
                //TODO:升级教练界面
                break;
            case R.id.my_profile_photo:
                startMyActivity(0);
                break;
            case R.id.my_profile_account:
                startMyActivity(1);
                break;
            case R.id.my_profile_gender:
                startMyActivity(2);
                break;
            case R.id.my_profile_nick_name:
                startMyActivity(3);
                break;
            case R.id.my_profile_region:
                startMyActivity(4);
                break;
            case R.id.my_profile_level:
                startMyActivity(5);
                break;
            case R.id.my_profile_ball_type:
                startMyActivity(6);
                break;
            case R.id.my_profile_billiards_cue:
                startMyActivity(7);
                break;
            case R.id.my_profile_cue_habits:
                startMyActivity(8);
                break;
            case R.id.my_profile_play_age:
                startMyActivity(9);
                break;
            case R.id.my_profile_idol:
                startMyActivity(10);
                break;
            case R.id.my_profile_sign:
                startMyActivity(11);
                break;
            case R.id.my_profile_the_new_post:
                startMyActivity(12);
                break;
        }
    }

    private void startMyActivity(int id) {
        Intent i = new Intent(this, ProfileSetupActivity.class);
        i.putExtra(EXTRA_FRAGMENT_ID, id);
        startActivityForResult(i, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        String str = data.getStringExtra(EXTRA_RESULT_ID);
        if ("".equals(str) || null == str)
            return;
        switch (requestCode) {
            case 0:

                break;
            case 1:

                break;
            case 2:

                break;
            case 3:
                mNickNameTextView.setText(str);
                break;
            case 4:
                mRegionTextView.setText(str);
                break;
            case 5:
                mLevelTextView.setText(str);
                break;
            case 6:
                mBallTypeTextView.setText(str);
                break;
            case 7:
                mBilliardsCueTextView.setText(str);
                break;
            case 8:
                mCueHabitsTextView.setText(str);
                break;
            case 9:
                mPlayAgeTextView.setText(str);
                break;
            case 10:
                mIdolTextView.setText(str);
                break;
            case 11:
                mSignTextView.setText(str);
                break;
            case 12:

                break;

        }
    }


}

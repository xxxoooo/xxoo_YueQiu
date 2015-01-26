package com.yueqiu.fragment.addfriend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yueqiu.R;

/**
 * Created by doushuqi on 15/1/8.
 */
public class FriendSetGroupFragment extends Fragment implements View.OnClickListener{

    private ImageView mQiuyouImageView, mAssistCoachImageView, mCoachImageView;
    private View mQiuyou, mAssistCoach, mCoach;
    private int group_id;
    public static final String RESULT_KEY = "com.yueqiu.fragment.requestaddfriend.result_key";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setTitle(R.string.set_group);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_move_group, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        mQiuyouImageView = (ImageView) view.findViewById(R.id.friends_group_qiuyou_iv);
        mAssistCoachImageView = (ImageView) view.findViewById(R.id.friends_group_assist_coach_iv);
        mCoachImageView = (ImageView) view.findViewById(R.id.friends_group_coach_iv);

        group_id = getActivity().getIntent().getIntExtra(RESULT_KEY, -1);
        switch (group_id) {
            case 0:
                mQiuyouImageView.setVisibility(View.VISIBLE);
                break;
            case 1:
                mAssistCoachImageView.setVisibility(View.VISIBLE);
                break;
            case 2:
                mCoachImageView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

        mQiuyou = view.findViewById(R.id.friends_group_qiuyou);
        mAssistCoach = view.findViewById(R.id.friends_group_assist_coach);
        mCoach = view.findViewById(R.id.friends_group_coach);
        mQiuyou.setOnClickListener(this);
        mAssistCoach.setOnClickListener(this);
        mCoach.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                dismiss();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dismiss() {
        finish();
        getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public void finish(){
        Intent intent = new Intent();
        intent.putExtra(RESULT_KEY, group_id);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.friends_group_qiuyou:
                group_id = 0;
                mQiuyouImageView.setVisibility(View.VISIBLE);
                mAssistCoachImageView.setVisibility(View.GONE);
                mCoachImageView.setVisibility(View.GONE);
                dismiss();
                break;
            case R.id.friends_group_assist_coach:
                group_id = 1;
                mQiuyouImageView.setVisibility(View.GONE);
                mAssistCoachImageView.setVisibility(View.VISIBLE);
                mCoachImageView.setVisibility(View.GONE);
                dismiss();
                break;
            case R.id.friends_group_coach:
                group_id = 2;
                mQiuyouImageView.setVisibility(View.GONE);
                mAssistCoachImageView.setVisibility(View.GONE);
                mCoachImageView.setVisibility(View.VISIBLE);
                dismiss();
                break;
            default:
                break;
        }
    }

}

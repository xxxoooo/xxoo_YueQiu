package com.yueqiu.fragment.requestaddfriend;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.FriendsApplicationActivity;
import com.yueqiu.bean.FriendsApplication;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.ApplicationDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/7.
 */
public class FriendsApplicationFragment extends Fragment {
    private ListView mListView;
    private MyAdapter mMyAdapter;
    private FragmentManager mFragmentManager;
    private static final int GET_ASK_SUCCESS = 0;
    private int applicationId;
    private List<FriendsApplication> mList;
    private ApplicationDao mApplicationDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFragmentManager = getActivity().getSupportFragmentManager();
        mApplicationDao = DaoFactory.getApplication(getActivity());
        getFriendApplication();//todo:logic confirm
        //伪造一条数据
        FriendsApplication a = new FriendsApplication("1", "jay", "xiaoming", "2012-4-5", "", 0);
        mApplicationDao.insertApplication(a);
        FriendsApplication b = new FriendsApplication("2", "ddd", "张学友", "2014-4-5", "", 0);
        mApplicationDao.insertApplication(b);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getActionBar().setTitle(R.string.qiuyou_application);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_application, container, false);
        mListView = (ListView) view.findViewById(R.id.friends_application_container);
        mList = mApplicationDao.getApplication();
        mMyAdapter = new MyAdapter(getActivity(), mList);
        mListView.setAdapter(mMyAdapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Utils.setFragmentActivityMenuColor(getActivity());
        getActivity().getMenuInflater().inflate(R.menu.clear, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.qiuyou_application_clear:
                mList.clear();
                mMyAdapter.notifyDataSetChanged();
                //清理数据库数据
                mApplicationDao.clearData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * get
     */
    private void getFriendApplication() {
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(HttpConstants.GetAsk.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put(PublicConstant.URL, HttpConstants.GetAsk.URL);
        paramMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.GET);
        if (Utils.networkAvaiable(getActivity())) {
            new FriendsApplicationAsyncTask(requestMap, null, null).execute(paramMap);
        } else {
            Toast.makeText(getActivity(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    private class FriendsApplicationAsyncTask extends AsyncTaskUtil<String> {

        public FriendsApplicationAsyncTask(Map<String, String> map, ProgressBar progressBar, TextView textView) {
            super(map, progressBar, textView);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                if (jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL) {

                    List<FriendsApplication> list = new ArrayList<FriendsApplication>();
                    JSONObject result = jsonObject.getJSONObject("result");
                    JSONObject object = result.getJSONObject("list_data");
                    JSONArray array = result.getJSONArray("list_data");
                    for (int i = 0; i < array.length(); i++) {
                        FriendsApplication application = Utils.mapingObject(FriendsApplication.class, object);
                        list.add(application);
                    }
                    mApplicationDao.insertApplication(list);
//                    mHandler.obtainMessage(GET_ASK_SUCCESS, list).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mList = (List<FriendsApplication>) msg.obj;
                    mMyAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    private class MyAdapter extends BaseAdapter {
        private Context mContext;
        private List<FriendsApplication> mList;
        private LayoutInflater mLayoutInflater;

        MyAdapter(Context context, List<FriendsApplication> list) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_friends_application, null);
                viewHolder = new ViewHolder();
                viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.friends_application_icon);
                viewHolder.mAccount = (TextView) convertView.findViewById(R.id.friends_application_username);
                viewHolder.mMessage = (TextView) convertView.findViewById(R.id.friends_application_message);
                viewHolder.mSendTime = (TextView) convertView.findViewById(R.id.friends_application_time);
                viewHolder.mAgree = (Button) convertView.findViewById(R.id.friends_application_agree);
                viewHolder.mAgree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putInt(VerificationFragment.ARGUMENTS_KEY, 0);
                        String id = mList.get(position).getId();
                        args.putString(FriendProfileFragment.FRIEND_USER_ID, id);//申请id，由GET/friend/getAsk获得
                        Fragment fragment = new FriendManageFragment();
                        fragment.setArguments(args);
//                        ((FriendsApplicationActivity) getActivity()).switchFragment(fragment);
                        FragmentTransaction ft = mFragmentManager.beginTransaction();
//                        ft.addToBackStack("FriendsApplicationActivity");
                        ft.replace(R.id.fragment_container, fragment).commit();
                    }
                });
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
//            viewHolder.mImageView.setImageDrawable();
            viewHolder.mAccount.setText(mList.get(position).getUsername());
            viewHolder.mMessage.setText(mList.get(position).getNick());
            viewHolder.mSendTime.setText(mList.get(position).getCreate_time());
            if (mList.get(position).getIsAgree() == 1) {
                viewHolder.mAgree.setText("已同意");
                viewHolder.mAgree.setBackground(getResources().getDrawable(R.drawable.unclickable_btn_bg));
                viewHolder.mAgree.setTextColor(getResources().getColor(R.color.devide_line));
                viewHolder.mAgree.setClickable(false);
            }
            return convertView;
        }

        final class ViewHolder {
            public ImageView mImageView;
            public TextView mAccount;
            public TextView mMessage;
            public TextView mSendTime;
            public Button mAgree;
        }
    }
}

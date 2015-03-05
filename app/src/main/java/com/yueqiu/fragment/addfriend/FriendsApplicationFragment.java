package com.yueqiu.fragment.addfriend;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.FriendsApplication;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.ApplicationDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/7.
 */
public class FriendsApplicationFragment extends Fragment {
    private static final String TAG = "FriendsApplicationFragment";
    private static final int AGREED = 1;
    private static final int UNAGREED = 0;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private TextView mEmptyView, mProgressBarText;
    private ProgressBar mProgressBar;
    private Drawable mProgressDrawable;
    private MyAdapter mMyAdapter;
    private FragmentManager mFragmentManager;
    private static final int GET_ASK_SUCCESS = 0;
    private int applicationId;
    private List<FriendsApplication> mList = new ArrayList<FriendsApplication>();
    private String mAgreedUsername;
//    private ApplicationDao mApplicationDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFragmentManager = getActivity().getSupportFragmentManager();
//        mApplicationDao = DaoFactory.getApplication(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Utils.networkAvaiable(getActivity())){
            getFriendApplication();//todo:logic confirm
        }else{
            Toast.makeText(getActivity(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getActionBar().setTitle(R.string.qiuyou_application);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_application, container, false);
        init(view);
//        mList = mApplicationDao.getApplication();
        mMyAdapter = new MyAdapter(getActivity(), mList);
        mListView.setAdapter(mMyAdapter);

        Bundle args = getArguments();
        if(args != null){
            mAgreedUsername = args.getString(FriendProfileFragment.USER_NAME_KEY);
        }else{
            mAgreedUsername = null;
        }

        return view;
    }

    private void init(View view) {

        mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.friends_application_container);
        mListView = mPullToRefreshListView.getRefreshableView();
        mListView.setDivider(getResources().getDrawable(R.drawable.friend_listview_divider));
        mListView.setDividerHeight(getResources().getDimensionPixelOffset(R.dimen.friend_listview_divider_height));
        mPullToRefreshListView.setOnRefreshListener(mRefreshListener);

        mProgressBar = (ProgressBar) view.findViewById(R.id.pre_progress);
        mProgressBarText = (TextView) view.findViewById(R.id.pre_text);
        mProgressBarText.setText(getString(R.string.xlistview_header_hint_loading));
        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds();
        mProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mProgressBar.getIndeterminateDrawable().setBounds(bounds);
        mEmptyView = new TextView(getActivity());
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
                getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            case R.id.qiuyou_application_clear:
                mList.clear();
                mMyAdapter.notifyDataSetChanged();
                //清理数据库数据
//                mApplicationDao.clearData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showProgressBar(boolean isShow) {
        mEmptyView.setVisibility(View.GONE);
        if (isShow) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBarText.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mProgressBarText.setVisibility(View.GONE);
        }
    }

    /**
     * get
     */
    private void getFriendApplication() {

        showProgressBar(true);

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(HttpConstants.GetAsk.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));


        HttpUtil.requestHttp(HttpConstants.GetAsk.URL,requestMap,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if(!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {

                            LinkedHashMap<String,FriendsApplication> map = new LinkedHashMap<String, FriendsApplication>();
                            JSONObject result = response.getJSONObject("result");

                            if(result.get("list_data").toString().equals("null")){
                                Log.d(TAG,"list_data is null");
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }else{
                                JSONArray list_data = result.getJSONArray("list_data");
                                for(int i=0;i<list_data.length();i++){
                                    FriendsApplication application = new FriendsApplication();
                                    String id = list_data.getJSONObject(i).getString("id");
                                    String nick = list_data.getJSONObject(i).getString("nick");
                                    String username = list_data.getJSONObject(i).getString("username");
                                    String create_time = list_data.getJSONObject(i).getString("create_time");
                                    String img_url = list_data.getJSONObject(i).getString("img_url");

                                    if(mAgreedUsername != null && mAgreedUsername.equals(username)){
                                        application.setIsAgree(AGREED);
                                    }else{
                                        application.setIsAgree(UNAGREED);
                                    }
                                    application.setId(id);
                                    application.setNick(nick);
                                    application.setUsername(username);
                                    application.setCreate_time(create_time);
                                    application.setImg_url(img_url);

                                    map.put(username,application);
                                }
                                //                            mApplicationDao.insertApplication(list);
                                mHandler.obtainMessage(PublicConstant.GET_SUCCESS, map).sendToTarget();
                            }
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT) {
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        } else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException->" + e);
                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });

    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mPullToRefreshListView.isRefreshing()){
                mPullToRefreshListView.onRefreshComplete();
            }
            showProgressBar(false);
            switch (msg.what) {
                case PublicConstant.GET_SUCCESS:
                    LinkedHashMap<String,FriendsApplication> map = (LinkedHashMap<String, FriendsApplication>) msg.obj;
                    Iterator iterator = map.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String,FriendsApplication> entry = (Map.Entry<String,FriendsApplication>) iterator.next();
                        if(mList.contains(entry.getValue())){
                            int index = mList.indexOf(entry.getValue());
                            if(!entry.getValue().getImg_url().equals(mList.get(index).getImg_url())) {
                                mList.remove(index);
                                mList.add(index,entry.getValue());
                            }
                        }else{
                            mList.add(entry.getValue());
                        }
                    }
                    mMyAdapter.notifyDataSetChanged();
                    showEmptyView(false);
                    break;
                case PublicConstant.NO_RESULT:
                    showEmptyView(true);
                    Toast.makeText(getActivity(), getString(R.string.no_friend_apply_info), Toast.LENGTH_LONG).show();
                    break;
                case PublicConstant.TIME_OUT:
                    showEmptyView(true);
                    Utils.showToast(getActivity(), getString(R.string.http_request_time_out));
                    break;
                case PublicConstant.REQUEST_ERROR:
                    showEmptyView(true);
                    if (null == msg.obj) {
                        Utils.showToast(getActivity(), getString(R.string.http_request_error));
                    } else {
                        Utils.showToast(getActivity(), (String) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void showEmptyView(boolean isShow) {
        if (isShow) {
            mEmptyView.setGravity(Gravity.CENTER);
            mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
            mEmptyView.setTextColor(getResources().getColor(R.color.md__defaultBackground));
            mEmptyView.setText(getActivity().getString(R.string.no_friend_apply_info));
            mPullToRefreshListView.setEmptyView(mEmptyView);
        }else {
            if(null != mEmptyView){
                mEmptyView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mList.clear();
    }



    private class MyAdapter extends BaseAdapter {
        private Context mContext;
        private List<FriendsApplication> mList;
        private LayoutInflater mLayoutInflater;
        private ImageLoader mImageLoader;

        MyAdapter(Context context, List<FriendsApplication> list) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
            mList = list;
            mImageLoader = VolleySingleton.getInstance().getImgLoader();
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
                viewHolder.mImageView = (NetworkImageView) convertView.findViewById(R.id.friends_application_icon);
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
                        args.putString(FriendProfileFragment.USER_NAME_KEY,mList.get(position).getUsername());
                        Fragment fragment = new FriendManageFragment();
                        fragment.setArguments(args);
//                        ((FriendsApplicationActivity) getActivity()).switchFragment(fragment);
                        FragmentTransaction ft = mFragmentManager.beginTransaction();
                        ft.addToBackStack("com.yueqiu.fragment.addfriend.FriendsApplicationActivity");
                        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                        ft.replace(R.id.fragment_container, fragment).commit();
//                        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                });
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.mImageView.setDefaultImageResId(R.drawable.default_head);
            viewHolder.mImageView.setErrorImageResId(R.drawable.default_head);
            viewHolder.mImageView.setImageUrl("http://" + mList.get(position).getImg_url(),mImageLoader);
            viewHolder.mAccount.setText(mList.get(position).getUsername());
            viewHolder.mMessage.setText(mList.get(position).getNick());
            viewHolder.mSendTime.setText(mList.get(position).getCreate_time());
            if (mList.get(position).getIsAgree() == AGREED) {
                viewHolder.mAgree.setText(getString(R.string.already_agreed));
                viewHolder.mAgree.setBackground(getResources().getDrawable(R.drawable.unclickable_btn_bg));
                viewHolder.mAgree.setTextColor(getResources().getColor(R.color.devide_line));
                viewHolder.mAgree.setClickable(false);
            }
            return convertView;
        }

        final class ViewHolder {
            public NetworkImageView mImageView;
            public TextView mAccount;
            public TextView mMessage;
            public TextView mSendTime;
            public Button mAgree;
        }
    }

    private PullToRefreshListView.OnRefreshListener<ListView> mRefreshListener = new PullToRefreshBase.OnRefreshListener<ListView>() {
        @Override
        public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            if(Utils.networkAvaiable(getActivity())){
                getFriendApplication();
            }else{
                Toast.makeText(getActivity(), getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
            }
        }
    };
}

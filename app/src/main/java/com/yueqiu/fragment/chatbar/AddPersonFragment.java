package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.RequestAddFriendActivity;
import com.yueqiu.bean.SearchPeopleInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.LocationUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧添加好友Fragment
 */
public class AddPersonFragment extends Fragment {
    private static final String TAG = "AddPersonFragment";
    //    private static final int GET_SUCCESS = 0;
//    private static final int GET_FAIL = 1;
    private ActionBar mActionBar;
    private LinearLayout mLinearLayout;
    private ProgressBar mProgressBar;
    private List<SearchPeopleInfo.SearchPeopleItemInfo> mList;
    private ListView mListView;
    private LocationManager mLocationManager;
    private Drawable mProgressDrawable;
    private double mLatitude, mLongitude;
    public static final String FRIEND_INFO_USER_ID = "com.yueqiu.fragment.chatbar.friend_info.user_id";
    public static final String FRIEND_INFO_USERNAME = "com.yueqiu.fragment.chatbar.friend_info.username";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbar_add_persion, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
        }
        mListView = (ListView) view.findViewById(R.id.search_result_container);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds();
        mProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mProgressBar.getIndeterminateDrawable().setBounds(bounds);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.search_friend_nearby);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.networkAvaiable(getActivity())) {
                    Utils.showToast(getActivity(), getString(R.string.network_not_available));
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                getActivity().startService(new Intent(getActivity(), LocationUtil.class));
            }
        });
        return view;
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            boolean isTimeout = bundle.getBoolean(LocationUtil.ISTIMEOUT_KEY);
            if (isTimeout) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                Location location = bundle.getParcelable(LocationUtil.LOCATION_KEY);
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
                Log.d(TAG, "位置信息：latitude = " + mLatitude + " longitude = " + mLongitude);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        searchFriendsByLocation(mLatitude, mLongitude);
                    }
                }).start();
            }

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mBroadcastReceiver, new IntentFilter(LocationUtil.BROADCAST_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
//        mLocationManager.removeUpdates(this);
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        //get friend by phone number or account number
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        searchFriendsByKeyWords(searchView.getQuery().toString());
                    }
                }).start();
                if (null != mList && 0 != mList.size()) {
                    mList.clear();
                    mListView.deferNotifyDataSetChanged();
                }
                mProgressBar.setVisibility(View.VISIBLE);
                ((InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PublicConstant.GET_SUCCESS:
                    mProgressBar.setVisibility(View.GONE);
                    SearchPeopleInfo searchPeopleInfo = (SearchPeopleInfo) msg.obj;
                    mList = searchPeopleInfo.mList;
                    MyAdapter adapter = new MyAdapter(getActivity(), mList);
                    mListView.setAdapter(adapter);
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), RequestAddFriendActivity.class);
                            int friendUserId = mList.get(position).getUser_id();
                            String username = mList.get(position).getUsername();
                            intent.putExtra(FRIEND_INFO_USER_ID, friendUserId);
                            intent.putExtra(FRIEND_INFO_USERNAME, username);
                            startActivity(intent);
                        }
                    });
                    break;
                case PublicConstant.NO_RESULT:
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "当前位置暂无球友！", Toast.LENGTH_LONG).show();
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(getActivity(), getString(R.string.http_request_time_out));
                    break;
                case PublicConstant.REQUEST_ERROR:
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

    private void searchFriendsByLocation(double latitude, double longitude) {
        Map<String, Double> map = new HashMap<String, Double>();
        map.put(HttpConstants.SearchPeopleByNearby.USER_ID, Double.parseDouble(String.valueOf(YueQiuApp.sUserInfo.getUser_id())));
        map.put(HttpConstants.SearchPeopleByNearby.LAT, latitude);
        map.put(HttpConstants.SearchPeopleByNearby.LNG, longitude);
        String result = HttpUtil.urlClient(HttpConstants.SearchPeopleByNearby.URL, map, HttpConstants.RequestMethod.GET);
        try {
            JSONObject jsonResult = new JSONObject(result);
            if (!jsonResult.isNull("code")) {
                if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                    SearchPeopleInfo searchPeople = new SearchPeopleInfo();
//                searchPeople.setCount(jsonResult.getJSONObject("result").getInt("count"));
                    JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
                    for (int i = 0; i < list_data.length(); i++) {
                        SearchPeopleInfo.SearchPeopleItemInfo itemInfo = searchPeople.new SearchPeopleItemInfo();
                        itemInfo.setUser_id(list_data.getJSONObject(i).getInt("user_id"));
                        itemInfo.setUsername(list_data.getJSONObject(i).getString("username"));
                        itemInfo.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                        itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                        itemInfo.setDatetime(list_data.getJSONObject(i).getString("datetime"));
                        searchPeople.mList.add(itemInfo);
                    }
                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, searchPeople).sendToTarget();
                } else if (jsonResult.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                    mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                } else if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NO_RESULT) {
                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                } else {
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, jsonResult.getString("msg")).sendToTarget();
                }
            } else {
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }

        } catch (JSONException e) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                    Utils.showToast(getActivity(), getString(R.string.no_data));
                }
            }, 100);
            Log.e(TAG, "JSONException>>" + e.toString());
        }
    }


    /**
     * 通过关键字查询好友
     *
     * @param keyWords
     */
    public void searchFriendsByKeyWords(String keyWords) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(HttpConstants.SearchPeopleByKeyword.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));//
        map.put(HttpConstants.SearchPeopleByKeyword.KEYWORDS, keyWords);
        String result = HttpUtil.urlClient(HttpConstants.SearchPeopleByKeyword.URL, map, HttpConstants.RequestMethod.GET);
        try {
            JSONObject jsonResult = new JSONObject(result);
            if (!jsonResult.isNull("code")) {
                if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                    SearchPeopleInfo searchPeople = new SearchPeopleInfo();
//                searchPeople.setCount(jsonResult.getJSONObject("result").getInt("count"));
                    JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
                    for (int i = 0; i < list_data.length(); i++) {
                        SearchPeopleInfo.SearchPeopleItemInfo itemInfo = searchPeople.new SearchPeopleItemInfo();
                        itemInfo.setUser_id(list_data.getJSONObject(i).getInt("user_id"));
                        itemInfo.setUsername(list_data.getJSONObject(i).getString("username"));
                        itemInfo.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                        itemInfo.setSex(list_data.getJSONObject(i).getInt("sex"));
                        itemInfo.setDistrict(list_data.getJSONObject(i).getString("district"));
                        searchPeople.mList.add(itemInfo);
                    }
                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, searchPeople).sendToTarget();
                } else if (jsonResult.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                    mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                } else if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NO_RESULT) {
                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                } else {
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, jsonResult.getString("msg")).sendToTarget();
                }
            } else {
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    class MyAdapter extends BaseAdapter {
        private Context mContext;
        private List<SearchPeopleInfo.SearchPeopleItemInfo> mList;
        private LayoutInflater mInflater;

        MyAdapter(Context context, List<SearchPeopleInfo.SearchPeopleItemInfo> list) {
            this.mContext = context;
            this.mList = list;
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_chatbar_account, null);
                viewHolder = new ViewHolder();
                viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.chatbar_item_account_iv);
                viewHolder.mNickName = (TextView) convertView.findViewById(R.id.chatbar_item_account_tv);
                viewHolder.mGender = (TextView) convertView.findViewById(R.id.chatbar_item_gender_tv);
                viewHolder.mDistrict = (TextView) convertView.findViewById(R.id.chatbar_item_district_tv);
                //绑定viewholder对象
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Log.e(TAG, String.valueOf(convertView));
//            viewHolder.mImageView.setImageDrawable();//设置头像
            viewHolder.mNickName.setText(mList.get(position).getUsername());
            viewHolder.mGender.setText(mList.get(position).getSex() == 1 ? getString(R.string.man) : getString(R.string.woman));
            String district = mList.get(position).getDistrict();
            viewHolder.mDistrict.setText("".equals(district) ? "未知" : district);
            return convertView;
        }

        final class ViewHolder {
            public ImageView mImageView;
            public TextView mNickName;
            public TextView mGender;
            public TextView mDistrict;
        }
    }

}

package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import com.yueqiu.util.HttpUtil;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧添加好友Fragment
 */
public class AddPersonFragment extends Fragment implements LocationListener {
    private static final String TAG = "AddPersonFragment";
    private static final int GET_SUCCESS = 0;
    private static final int GET_FAIL = 1;
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
                //通过坐标查找好友
                mProgressBar.setVisibility(View.VISIBLE);
                getLocationInfo();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
//        mLocationManager.removeUpdates(this);
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
                case GET_SUCCESS:
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
                case GET_FAIL:
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "该位置暂无球友！", Toast.LENGTH_LONG).show();
                default:
                    break;
            }
        }
    };

    private void searchFriendsByLocation(double latitude, double longitude) {
        Map<String, Double> map = new HashMap<String, Double>();
        map.put(HttpConstants.SearchPeopleByNearby.LAT, latitude);
        map.put(HttpConstants.SearchPeopleByNearby.LNG, longitude);
        String result = HttpUtil.urlClient(HttpConstants.SearchPeopleByNearby.URL, map, HttpConstants.RequestMethod.GET);
        try {
            JSONObject jsonResult = new JSONObject(result);

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
                mHandler.obtainMessage(GET_SUCCESS, searchPeople).sendToTarget();
            } else {
                mHandler.obtainMessage(GET_FAIL).sendToTarget();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getLocationInfo() {
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        /*
         * TODO:这里逻辑有待确认
         */
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            return;
        } else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        } else {
            Toast.makeText(getActivity(), "请检查GPS和网络是否可用！", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        Log.d(TAG, "位置信息：latitude = " + mLatitude + " longitude = " + mLongitude);
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchFriendsByLocation(mLatitude, mLongitude);
            }
        }).start();
        mLocationManager.removeUpdates(this);//停止定位
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * 通过关键字查询好友
     *
     * @param keyWords
     */
    public void searchFriendsByKeyWords(String keyWords) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(HttpConstants.SearchPeopleByKeyword.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        map.put(HttpConstants.SearchPeopleByKeyword.KEYWORDS, keyWords);
        String result = HttpUtil.urlClient(HttpConstants.SearchPeopleByKeyword.URL, map, HttpConstants.RequestMethod.GET);
        try {
            JSONObject jsonResult = new JSONObject(result);

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
                mHandler.obtainMessage(GET_SUCCESS, searchPeople).sendToTarget();
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

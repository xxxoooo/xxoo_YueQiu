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
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.RequestAddFriendActivity;
import com.yueqiu.bean.NearbyPeopleInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.LocationUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧添加好友Fragment
 */
public class AddPersonFragment extends Fragment {
    private static final String TAG = "AddPersonFragment";
    private static final int GET_FRIEND_BY_LOCATION = 42;
    private ActionBar mActionBar;
    private LinearLayout mLinearLayout;
    private ProgressBar mProgressBar;
    private ListView mListView;
    private Drawable mProgressDrawable;
    private double mLatitude, mLongitude;
    public static final String FRIEND_INFO_USER_ID = "com.yueqiu.fragment.chatbar.friend_info.user_id";
    public static final String FRIEND_INFO_USERNAME = "com.yueqiu.fragment.chatbar.friend_info.username";
    private TextView mEmptyView;
    private TextView mProgressBarText;
    private ImageLoader mImageLoader;
    private LocationManagerProxy mLocationManagerProxy;
    private MyAdapter mAdapter;
    private List<NearbyPeopleInfo.SearchPeopleItemInfo> mList = new ArrayList<NearbyPeopleInfo.SearchPeopleItemInfo>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mImageLoader = VolleySingleton.getInstance().getImgLoader();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbar_add_persion, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
        }
        init(view);
        //初始化查询
        getLocation();
//        getActivity().startService(new Intent(getActivity(), LocationUtil.class));
        return view;
    }

    private void init(View view) {
        mEmptyView = (TextView) view.findViewById(R.id.empty_view);
        mEmptyView.setVisibility(View.GONE);

        mListView = (ListView) view.findViewById(R.id.search_result_container);
        mAdapter= new MyAdapter(getActivity(), mList);
        mListView.setAdapter(mAdapter);

        mProgressBar = (ProgressBar) view.findViewById(R.id.pre_progress);
        mProgressBarText = (TextView) view.findViewById(R.id.pre_text);
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
                    mListView.setEmptyView(mEmptyView);
                    return;
                }
                if (mList != null && mList.size() > 0) {
                    mList.clear();
                }
                showProgressBar(true);
                //TODO:该方法总是会获取到0，所以先不用，待日后完善
//                getActivity().startService(new Intent(getActivity(), LocationUtil.class));
                getLocation();
            }
        });
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

//    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Bundle bundle = intent.getExtras();
//            boolean isTimeout = bundle.getBoolean(LocationUtil.ISTIMEOUT_KEY);
//            if (isTimeout) {
//                showProgressBar(false);
//            } else {
//                Location location = bundle.getParcelable(LocationUtil.LOCATION_KEY);
//                mLatitude = location.getLatitude();
//                mLongitude = location.getLongitude();
//                Log.d(TAG, "位置信息：latitude = " + mLatitude + " longitude = " + mLongitude);
//                if (Utils.networkAvaiable(getActivity())) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            searchFriendsByLocation(mLatitude, mLongitude);
//                        }
//                    }).start();
//                } else {
//                    stopViewSearch();
//                }
//
//            }
//
//        }
//    };

    @Override
    public void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(getActivity())
//                .registerReceiver(mBroadcastReceiver, new IntentFilter(LocationUtil.BROADCAST_FILTER));


    }

    @Override
    public void onPause() {
        super.onPause();
//        mLocationManager.removeUpdates(this);
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final SearchView searchView = (SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFriendsByKeyWords(searchView.getQuery().toString());
                if (null != mList && 0 != mList.size()) {
                    mList.clear();
                    mListView.deferNotifyDataSetChanged();
                }
                showProgressBar(true);
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
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
            showProgressBar(false);
            switch (msg.what) {
                case PublicConstant.GET_SUCCESS:
                    NearbyPeopleInfo searchPeopleInfo = (NearbyPeopleInfo) msg.obj;
                    mList.addAll(searchPeopleInfo.mList);
                    if (mList.size() > 0)
                        mEmptyView.setVisibility(View.GONE);
                    else
                        mEmptyView.setVisibility(View.VISIBLE);

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), RequestAddFriendActivity.class);
                            int friendUserId = mList.get(position).getUser_id();
                            String username = mList.get(position).getUsername();
                            intent.putExtra(FRIEND_INFO_USER_ID, friendUserId);
                            intent.putExtra(FRIEND_INFO_USERNAME, username);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        }
                    });
                    break;
                case PublicConstant.NO_RESULT:
                    if (mList.size() > 0) {
                        mEmptyView.setVisibility(View.GONE);
                        Utils.showToast(getActivity(), getActivity().getString(R.string.not_found_friend));
                    }else {
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
//
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if (null == msg.obj) {
                        mEmptyView.setText(getString(R.string.http_request_error));
                    } else {
                        mEmptyView.setText((String) msg.obj);
                    }
                    if (mList.size() > 0) {
                        mEmptyView.setVisibility(View.GONE);
                        if(null == msg.obj){
                            Utils.showToast(getActivity(), getString(R.string.http_request_error));
                        }else{
                            Utils.showToast(getActivity(), (String) msg.obj);
                        }
                    }else {
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
                    break;
                case GET_FRIEND_BY_LOCATION:
                    searchFriendsByLocation(mLatitude,mLongitude);
                    break;
                default:
                    break;
            }
            mAdapter.notifyDataSetChanged();
            if (mList == null || mList.size() <= 0) {
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    };

    private void searchFriendsByLocation(double latitude, double longitude) {
        
        Map<String, Double> map = new HashMap<String, Double>();
        map.put(HttpConstants.SearchPeopleByNearby.USER_ID, Double.parseDouble(String.valueOf(YueQiuApp.sUserInfo.getUser_id())));
        map.put(HttpConstants.SearchPeopleByNearby.LAT, latitude);
        map.put(HttpConstants.SearchPeopleByNearby.LNG, longitude);

        mProgressBarText.setText(getString(R.string.quering));

        HttpUtil.requestHttp(HttpConstants.SearchPeopleByNearby.URL, map, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","add person location ->" + response);
                try {
                    if (!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            NearbyPeopleInfo searchPeople = new NearbyPeopleInfo();
//                searchPeople.setCount(response.getJSONObject("result").getInt("count"));
                            JSONArray list_data = response.getJSONObject("result").getJSONArray("list_data");
                            for (int i = 0; i < list_data.length(); i++) {
                                NearbyPeopleInfo.SearchPeopleItemInfo itemInfo = searchPeople.new SearchPeopleItemInfo();
                                itemInfo.setUser_id(list_data.getJSONObject(i).getInt("user_id"));
                                itemInfo.setUsername(list_data.getJSONObject(i).getString("username"));
                                itemInfo.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                                itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                                itemInfo.setDatetime(list_data.getJSONObject(i).getString("datetime"));
                                searchPeople.mList.add(itemInfo);
                            }
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, searchPeople).sendToTarget();
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT) {
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        } else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
                        }
                    } else {
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }

                } catch (JSONException e) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopViewSearch();
                        }
                    }, 100);
                    Log.e(TAG, "JSONException>>" + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

            }
        });
        
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


        HttpUtil.requestHttp(HttpConstants.SearchPeopleByKeyword.URL, map, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","add friend response ->" + response);
                try{
                    if (!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            NearbyPeopleInfo searchPeople = new NearbyPeopleInfo();
//                searchPeople.setCount(response.getJSONObject("result").getInt("count"));
                            JSONArray list_data = response.getJSONObject("result").getJSONArray("list_data");
                            for (int i = 0; i < list_data.length(); i++) {
                                NearbyPeopleInfo.SearchPeopleItemInfo itemInfo = searchPeople.new SearchPeopleItemInfo();
                                itemInfo.setUser_id(list_data.getJSONObject(i).getInt("user_id"));
                                itemInfo.setUsername(list_data.getJSONObject(i).getString("username"));
                                itemInfo.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                                itemInfo.setSex(list_data.getJSONObject(i).getInt("sex"));
                                itemInfo.setDistrict(list_data.getJSONObject(i).getString("district"));
                                searchPeople.mList.add(itemInfo);
                            }
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, searchPeople).sendToTarget();
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT) {
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        } else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
                        }
                    } else {
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopViewSearch();
                        }
                    }, 100);
                    Log.e(TAG, "JSONException>>" + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }
        });
    }

    private void stopViewSearch() {
        showProgressBar(false);
        mEmptyView.setVisibility(View.VISIBLE);
        Utils.showToast(getActivity(), getString(R.string.no_data));
    }

    class MyAdapter extends BaseAdapter {
        private Context mContext;
        private List<NearbyPeopleInfo.SearchPeopleItemInfo> mList;
        private LayoutInflater mInflater;

        MyAdapter(Context context, List<NearbyPeopleInfo.SearchPeopleItemInfo> list) {
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
                viewHolder.mImageView = (NetworkImageView) convertView.findViewById(R.id.chatbar_item_account_iv);
                viewHolder.mNickName = (TextView) convertView.findViewById(R.id.chatbar_item_account_tv);
                viewHolder.mGender = (TextView) convertView.findViewById(R.id.chatbar_item_gender_tv);
                viewHolder.mDistrict = (TextView) convertView.findViewById(R.id.chatbar_item_district_tv);
                //绑定viewholder对象
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Log.e(TAG, String.valueOf(convertView));
            viewHolder.mImageView.setDefaultImageResId(R.drawable.default_head);
            viewHolder.mImageView.setErrorImageResId(R.drawable.default_head);
            viewHolder.mImageView.setImageUrl(HttpConstants.IMG_BASE_URL + mList.get(position).getImg_url(), mImageLoader);
            viewHolder.mNickName.setText(mList.get(position).getUsername());
            viewHolder.mGender.setText(mList.get(position).getSex() == 1 ? getString(R.string.man) : getString(R.string.woman));
            viewHolder.mGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, NearbyFragmentsCommonUtils.parseGenderDrawable(mList.get(position).getSex() == 1 ? "男" : "女"), 0);
            String district = mList.get(position).getDistrict();
            if("".equals(district)){
                viewHolder.mDistrict.setVisibility(View.GONE);
            }else{
                viewHolder.mDistrict.setText(getActivity().getString(R.string.unknown));
                viewHolder.mDistrict.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        final class ViewHolder {
            public NetworkImageView mImageView;
            public TextView mNickName;
            public TextView mGender;
            public TextView mDistrict;
        }
    }


    /**
     * 初始化定位,用高德SDK获取经纬度，准确率貌似更高点，
     * 之后可能会加功能，会用到高德的SDK
     */
    private void getLocation() {

        mLocationManagerProxy = LocationManagerProxy.getInstance(getActivity());

        mProgressBarText.setText(getString(R.string.getting_location));
        showProgressBar(true);
        //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
        //在定位结束后，在合适的生命周期调用destroy()方法
        //其中如果间隔时间为-1，则定位只定一次
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, -1, new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation aMapLocation) {
                        if(aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0){
                            //获取位置信息
                            mLatitude = aMapLocation.getLatitude();
                            mLongitude = aMapLocation.getLongitude();

                            Log.d("wy","add person latitude ->" + mLatitude);
                            Log.d("wy","add person longitude ->" + mLongitude);
                            // 我们此时可以将我们获取到的当前用户的位置信息用来进行球厅的位置筛选操作

//                            searchFriendsByLocation(mLatitude,mLongitude);
                            mHandler.sendEmptyMessage(GET_FRIEND_BY_LOCATION);

                        }
                    }

                    @Override
                    public void onLocationChanged(Location location) {

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
                });

        mLocationManagerProxy.setGpsEnable(false);
    }

}

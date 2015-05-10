package com.yueqiu.fragment.chatbar;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.android.volley.toolbox.ImageLoader;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.RequestAddFriendActivity;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.adapter.AddAdapter;
import com.yueqiu.bean.NearbyPeopleInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
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
    private AddAdapter mAdapter;
    private List<NearbyPeopleInfo.SearchPeopleItemInfo> mList = new ArrayList<NearbyPeopleInfo.SearchPeopleItemInfo>();
    private SearchView mSearchView;
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
        if(Utils.networkAvaiable(getActivity())) {
            getLocation();
        }else{
            setEmptyViewVisible();
            mEmptyView.setText(getString(R.string.network_not_available));
        }
//        mSearchView = ChatBarActivity.mSearchView;
        return view;
    }

    private void setEmptyViewVisible(){
//        mEmptyView.setGravity(Gravity.CENTER);
//        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
//        mEmptyView.setTextColor(getResources().getColor(R.color.md__defaultBackground));
//        mEmptyView.setText(getString(R.string.nearby_no_user));
//        mListView.setEmptyView(mEmptyView);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    private void init(View view) {
        mEmptyView = (TextView) view.findViewById(R.id.empty_view);
//        mEmptyView = new TextView(getActivity());

        mListView = (ListView) view.findViewById(R.id.search_result_container);
        mAdapter= new AddAdapter(getActivity(), mList);
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

                    if(mList.isEmpty()){
                        setEmptyViewVisible();
                    }else{
                        Utils.showToast(getActivity(), getString(R.string.network_not_available));
                    }

                    return;
                }
                if (mList != null && mList.size() > 0) {
                    mList.clear();
                    mAdapter.notifyDataSetChanged();
                }
                showProgressBar(true);
                getLocation();
            }
        });
    }

    private void showProgressBar(boolean isShow) {
        mEmptyView.setVisibility(View.GONE);
        mListView.setEmptyView(null);
        if (isShow) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBarText.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mProgressBarText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("wy","onResume");
        if(mSearchView != null){
            mSearchView.clearFocus();

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mSearchView != null){
            mSearchView.clearFocus();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d("wy","onCreateOptionMenu");
        menu.findItem(R.id.near_nemu_search).collapseActionView();
        mSearchView = (SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        mSearchView.setIconified(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (Utils.networkAvaiable(getActivity())) {
                    Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                    Bundle args = new Bundle();
                    args.putInt(PublicConstant.SEARCH_TYPE, PublicConstant.SEARCH_FRIEND);
                    args.putString(PublicConstant.SEARCH_KEYWORD, query);
                    intent.putExtras(args);
                    startActivity(intent);
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    Utils.showToast(getActivity(), getString(R.string.network_not_available));
                }
                mSearchView.clearFocus();
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
                    for(NearbyPeopleInfo.SearchPeopleItemInfo info : searchPeopleInfo.mList ){
                        if(!mList.contains(info)){
                            mList.add(info);
                        }
                    }
                    if (mList.size() > 0) {
                        mEmptyView.setVisibility(View.GONE);
                        mListView.setEmptyView(null);
                    }else {
                        setEmptyViewVisible();
                    }

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
                        setEmptyViewVisible();
                    }
//
                    break;
                case PublicConstant.REQUEST_ERROR:

                    if (mList.size() > 0) {
                        mEmptyView.setVisibility(View.GONE);
                        if(null == msg.obj){
                            Utils.showToast(getActivity(), getString(R.string.http_request_error));
                        }else{
                            Utils.showToast(getActivity(), (String) msg.obj);
                        }
                    }else {
                        setEmptyViewVisible();
                        if (null == msg.obj) {
                            mEmptyView.setText(getString(R.string.http_request_error));
                        } else {
                            mEmptyView.setText((String) msg.obj);
                        }
                    }
                    break;
                case GET_FRIEND_BY_LOCATION:
                    mProgressBarText.setText(getString(R.string.quering));
                    searchFriendsByLocation(mLatitude,mLongitude);
                    break;
                default:
                    break;
            }
            mAdapter.notifyDataSetChanged();
//            if (mList == null || mList.size() <= 0) {
//                mEmptyView.setVisibility(View.VISIBLE);
//            }
        }
    };

    private void searchFriendsByLocation(double latitude, double longitude) {
        
        Map<String, Double> map = new HashMap<String, Double>();
        map.put(HttpConstants.SearchPeopleByNearby.USER_ID, Double.parseDouble(String.valueOf(YueQiuApp.sUserInfo.getUser_id())));
        map.put(HttpConstants.SearchPeopleByNearby.LAT, latitude);
        map.put(HttpConstants.SearchPeopleByNearby.LNG, longitude);


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
                                itemInfo.setSex(list_data.getJSONObject(i).getInt("sex"));
                                itemInfo.setDistrict(list_data.getJSONObject(i).getString("district"));
                                //TODO:等加上
                                itemInfo.setDistance(list_data.getJSONObject(i).getString("range"));
//                                itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
//                                itemInfo.setDatetime(list_data.getJSONObject(i).getString("datetime"));
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
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }
        });
        
    }

    private void stopViewSearch() {
        showProgressBar(false);
        if(mList.isEmpty()) {
            setEmptyViewVisible();
            mEmptyView.setText(getString(R.string.no_data));
        }else {
            Utils.showToast(getActivity(), getString(R.string.no_data));
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
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, -1,mAmapLocationListener);

        mLocationManagerProxy.setGpsEnable(false);
        CountDownTimer timer = new CountDownTimer(8100,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if(mLatitude == 0 && mLongitude == 0){
                    mLocationManagerProxy.removeUpdates(mAmapLocationListener);
                    mHandler.sendEmptyMessage(GET_FRIEND_BY_LOCATION);
                }
            }
        };
        timer.start();

    }

    private AMapLocationListener mAmapLocationListener = new AMapLocationListener() {
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
    };

}

package com.yueqiu.fragment.nearby;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.activity.NearbyRoomDetailActivity;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.adapter.NearbyRoomSubFragmentListAdapter;
import com.yueqiu.bean.NearbyRoomBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.fragment.nearby.common.NearbyParamsPreference;
import com.yueqiu.fragment.nearby.common.NearbyPopBasicClickListener;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BilliardsNearbyRoomFragment extends Fragment{
    private static final String TAG = "BilliardsNearbyRoomFragment";
    private static final String TAG_1 = "filter_param_test";

    private static final String ARG_PARAM1 = "param1";
    public static BilliardsNearbyRoomFragment newInstance(Context context, String param1){
//        mContext = context;
        BilliardsNearbyRoomFragment fragment = new BilliardsNearbyRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }
    private Context mContext;
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.mContext = activity;
    }

    private WorkerHandlerThread mWorkerThread;

    private static NearbyParamsPreference sParamsPreference = NearbyParamsPreference.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    private PullToRefreshListView mRoomListView;
    private View mView;
//    private ArrayList<NearbyRoomSubFragmentRoomBean> mRoomList = new ArrayList<NearbyRoomSubFragmentRoomBean>();
//    private ArrayList<NearbyRoomSubFragmentRoomBean> mCachedList = new ArrayList<NearbyRoomSubFragmentRoomBean>();
    private ArrayList<NearbyRoomBean> mRoomList = new ArrayList<NearbyRoomBean>();
    private ArrayList<NearbyRoomBean> mCachedList = new ArrayList<NearbyRoomBean>();
    private NearbyRoomSubFragmentListAdapter mSearchRoomAdapter;
    // 加载王赟开发的ProgressBar
    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    private TextView mPreTextView;

    private NearbyPopBasicClickListener mClickListener;
    private NearbyFragmentsCommonUtils.ControlPopupWindowCallback mCallback;

    // 定义的用于下拉刷新过程当中需要用到的变量
    private boolean mLoadMore, mRefresh, mIsSavedInstance,mIsListEmpty;
    private int mBeforeCount, mAfterCount;
    // 以下是我们用于跟踪page的值的请求(用于大众点评的分页请求过程)
//    private int mPage = 1;
    private LocationManagerProxy mLocationManagerProxy;
    private int mCurrentPos;
    private SearchView mSearchView;
    private double mGetLat,mGetLng;
    private int mStartNum = 0;
    private int mEndNum = 9;
    private int mRequestFlag;
    private String mArgs;
    private float mLat;
    private float mLng;
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        mView = inflater.inflate(R.layout.fragment_nearby_room_layout, container, false);
        setHasOptionsMenu(true);

        NearbyFragmentsCommonUtils commonUtils = new NearbyFragmentsCommonUtils(mContext);
        commonUtils.initViewPager(mContext, mView);

        mClickListener = new NearbyPopBasicClickListener(mContext, mUIEventsHandler, sParamsPreference);
        (mView.findViewById(R.id.btn_room_district)).setOnClickListener(mClickListener);
        (mView.findViewById(R.id.btn_room_distance)).setOnClickListener(mClickListener);
        (mView.findViewById(R.id.btn_room_price)).setOnClickListener(mClickListener);
        (mView.findViewById(R.id.btn_room_apprisal)).setOnClickListener(mClickListener);
        mEmptyView = new TextView(getActivity());

        mCallback = mClickListener;

        mRoomListView = (PullToRefreshListView) mView.findViewById(R.id.search_room_subfragment_listview);
        mRoomListView.setMode(PullToRefreshBase.Mode.BOTH);
        mRoomListView.setOnRefreshListener(onRefreshListener);

        mSearchRoomAdapter = new NearbyRoomSubFragmentListAdapter(mContext,  mRoomList);
        mRoomListView.setAdapter(mSearchRoomAdapter);

        // 初始化ProgressBar
        mPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();

        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mRoomListView.requestLayout();
        mSearchRoomAdapter.notifyDataSetChanged();

        mRoomListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NearbyRoomBean bean = mRoomList.get(position - 1);
                Bundle bundle = new Bundle();
                bundle.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHOTO, bean.getImg_url());
                bundle.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_NAME, bean.getName());
                bundle.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_LEVEL, bean.getOverall_rating());
                bundle.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PRICE, bean.getPrice());
                bundle.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_ADDRESS, bean.getAddress());
                bundle.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_DETAILED_INFO, bean.getDetail_info());
                bundle.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHONE, bean.getTelephone());
                bundle.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_SHOP_HOURS,bean.getShop_hours());
                Intent intent = new Intent(mContext, NearbyRoomDetailActivity.class);
                intent.putExtra(NearbyFragmentsCommonUtils.KEY_BUNDLE_SEARCH_ROOM_FRAGMENT, bundle);

                mContext.startActivity(intent);
            }
        });

        if (null != savedInstanceState){
            mRefresh = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_REFRESH);
            mLoadMore = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_LOAD_MORE);
            mIsSavedInstance = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_INSTANCE);

            mCachedList = savedInstanceState.getParcelableArrayList(NearbyFragmentsCommonUtils.KEY_SAVED_LISTVIEW);
            mUIEventsHandler.obtainMessage(PublicConstant.USE_CACHE, mCachedList).sendToTarget();
        }


        return mView;
    }
    /**
     * 初始化定位,用高德SDK获取经纬度，准确率貌似更高点，
     * 之后可能会加功能，会用到高德的SDK
     */
    private void getLocation() {
        mLocationManagerProxy = LocationManagerProxy.getInstance(getActivity());

        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15,mAmapLocationListener);
        mLocationManagerProxy.setGpsEnable(false);
        CountDownTimer timer = new CountDownTimer(8100,100) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                if(mGetLng == 0 && mGetLat == 0){
                    mLocationManagerProxy.removeUpdates(mAmapLocationListener);

                    sParamsPreference.ensurePreference(mContext);
                    sParamsPreference.setRoomLati(mContext, (float) mGetLat);
                    sParamsPreference.setRoomLongi(mContext, (float) mGetLng);

                    Bundle args = new Bundle();
                    args.putFloat("lat", (float) mGetLat);
                    args.putFloat("lng", (float) mGetLng);
                    mUIEventsHandler.obtainMessage(LOCATION_HAS_GOT, args).sendToTarget();
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
                mGetLat = aMapLocation.getLatitude();
                mGetLng = aMapLocation.getLongitude();

                // 我们此时可以将我们获取到的当前用户的位置信息用来进行球厅的位置筛选操作
                sParamsPreference.ensurePreference(mContext);
                sParamsPreference.setRoomLati(mContext, (float) mGetLat);
                sParamsPreference.setRoomLongi(mContext, (float) mGetLng);

                Bundle args = new Bundle();
                args.putFloat("lat", (float) mGetLat);
                args.putFloat("lng", (float) mGetLng);
                mUIEventsHandler.obtainMessage(LOCATION_HAS_GOT,args).sendToTarget();

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

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mReceiver,filter);
        if(mSearchView != null){
            mSearchView.clearFocus();
        }
        mWorkerThread = new WorkerHandlerThread();
        mLoadMore = false;
        mRefresh = false;
        if (mWorkerThread.getState() == Thread.State.NEW){
            // 这里的WorkThread必须调用了start()方法之后，位于WorkThread当中的workHandler才可以正常工作
            mWorkerThread.start();
        }
        if (!Utils.networkAvaiable(mContext)){
            mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(NearbyFragmentsCommonUtils.KEY_SAVED_LISTVIEW, mRoomList);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_LOAD_MORE, mLoadMore);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_REFRESH, mRefresh);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_INSTANCE, true);
    }

    /**
     * 使用服务端提供的接口
     */
    private void getRoomList(String region,String range,String price,String evaluate,int start,int end){
        if (!Utils.networkAvaiable(getActivity())){
            mUIEventsHandler.obtainMessage(PublicConstant.NO_NETWORK,mContext.getResources().getString(R.string.network_not_available)).sendToTarget();
            // 在请求网络任务的刚开始的时候，我们已经打开了ProgressDialog，现在既然已经确定无法继续请求，所以应该先把已经打开的ProgressDialog关闭
            mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
            return;
        }
        Map<String,String> params = new HashMap<String, String>();
        params.put(HttpConstants.GET_ROOM.START_NO,String.valueOf(start));
        params.put(HttpConstants.GET_ROOM.END_NO,String.valueOf(end));
        float lng = sParamsPreference.getRoomLongi(mContext);
        float lat = sParamsPreference.getRoomLati(mContext);
        params.put(HttpConstants.GET_ROOM.LAT,String.valueOf(lat));
        params.put(HttpConstants.GET_ROOM.LNG,String.valueOf(lng));
        if(!TextUtils.isEmpty(price)) {
            params.put(HttpConstants.GET_ROOM.PRICE,String.valueOf(price));
        }
        if(!TextUtils.isEmpty(evaluate)){
            params.put(HttpConstants.GET_ROOM.EVALUATE,String.valueOf(evaluate));
        }
        if(!TextUtils.isEmpty(region)){
            params.put(HttpConstants.GET_ROOM.REGION,region);
        }
        if(!TextUtils.isEmpty(range)){
            params.put(HttpConstants.GET_ROOM.RANGE,range);
        }
        mUIEventsHandler.sendEmptyMessage(SET_PULLREFRESH_DISABLE);
        final List<NearbyRoomBean> roomList = new ArrayList<NearbyRoomBean>();
        HttpUtil.requestHttp(HttpConstants.GET_ROOM.URL,params,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","room response ->" + response);
                try{
                    if(!response.isNull("code")){

                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(response.get("result").equals("null")){
                                mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                            }else{
                                JSONArray list_data = response.getJSONArray("result");
                                for (int i = 0; i < list_data.length(); i++) {
                                    NearbyRoomBean room = new NearbyRoomBean();
                                    room.setId(list_data.getJSONObject(i).getString("id"));
                                    room.setName(list_data.getJSONObject(i).getString("name"));
                                    room.setAddress(list_data.getJSONObject(i).getString("address"));
                                    room.setTelephone(list_data.getJSONObject(i).getString("telephone"));
                                    room.setDetail_info(list_data.getJSONObject(i).getString("detail_info"));
                                    room.setPrice(list_data.getJSONObject(i).getString("price"));
                                    room.setShop_hours(list_data.getJSONObject(i).getString("shop_hours"));
                                    room.setRange(list_data.getJSONObject(i).getString("range"));
                                    room.setOverall_rating(list_data.getJSONObject(i).getString("overall_rating"));
                                    room.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                                    roomList.add(room);
                                }

                                if (roomList.isEmpty()) {
                                    mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                } else {
                                    mUIEventsHandler.obtainMessage(PublicConstant.GET_SUCCESS, roomList).sendToTarget();
                                    // 进行到这里，我们基本上也已经把所有的数据都解析完并且也加载完了。现在我们可以通过UI线程停止显示Dialog了
                                    mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                                }
                            }
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                        }
                        else{
                            mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                            mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                        }
                    }
                    else{
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                        mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                    mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
            }
        });

    }

    @Override
    public void onPause(){
        mCallback.closePopupWindow();

        if (mWorkerThread != null){
            mWorkerThread.interrupt();
            mWorkerThread = null;
        }
        super.onPause();
    }

    @Override
    public void onStop(){
        mCallback.closePopupWindow();
        super.onStop();
    }

    @Override
    public void onDestroy(){
        sParamsPreference.setRoomApprisal(mContext, "");
        sParamsPreference.setRoomPrice(mContext, "");
        sParamsPreference.setRoomRegion(mContext, "");
        sParamsPreference.setRoomRange(mContext, "");
        super.onDestroy();
    }

    private static final String WORKER_HANDLER_THREAD_NAME = "workerHandlerThread";
    private static final int UI_SHOW_DIALOG = 1 << 6;
    private static final int UI_HIDE_DIALOG = 1 << 7;
    private static final int REQUEST_ALL_ROOM_INFO = 1 << 1;
    // 以下是按FIlterButton当中当中弹出的List进行筛选时的List的点击的事件的处理,
    // 由于这些常量值被定义到同一个地方进行使用，所以我们将球厅Fragment当中的常量值定义为从50开始
    public static final int REQUEST_ROOM_INFO_REGION_FILTERED = 50 << 2;
    public static final int REQUEST_ROOM_INFO_RANGE_FILTERED = 50 << 3;
    public static final int REQUEST_ROOM_INFO_PRICE_FILTERED = 50 << 4;
    public static final int REQUEST_ROOM_INFO_APPRISAL_FILTERED = 50 << 5;

    public static final int GET_LOCATION = 43;
    public static final int LOCATION_HAS_GOT = 44;
    private static final int DATA_HAS_BEEN_UPDATED = 1 << 10;
    private static final int SET_PULLREFRESH_DISABLE = 42;


    private Handler mUIEventsHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(mRoomListView.getMode() == PullToRefreshBase.Mode.DISABLED){
                mRoomListView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            if (mRoomListView.isRefreshing()){
                mRoomListView.onRefreshComplete();
            }
            switch (msg.what){
                case UI_SHOW_DIALOG:
                    showProgress();
                    if(mEmptyView.getVisibility() == View.VISIBLE){
                        mRoomListView.setEmptyView(null);
                        mEmptyView.setVisibility(View.GONE);
                    }
                    break;
                case UI_HIDE_DIALOG:
                    // 此时数据已经加载完毕，我们需要通知List的Adpter数据源已经发生改变
                    mSearchRoomAdapter.notifyDataSetChanged();
                    hideProgress();
                    break;
                case PublicConstant.USE_CACHE:
                    // 首先将我们的EmptyView隐藏掉
                    setEmptyViewGone();
                    ArrayList<NearbyRoomBean> cachedList = (ArrayList<NearbyRoomBean>) msg.obj;
                    mRoomList.addAll(cachedList);
                    mSearchRoomAdapter.notifyDataSetChanged();
                    break;
                case PublicConstant.GET_SUCCESS:
                    setEmptyViewGone();
                    mBeforeCount = mRoomList.size();
                    mIsListEmpty = mRoomList.isEmpty();
                    List<NearbyRoomBean> roomList = (List<NearbyRoomBean>) msg.obj;
                    mBeforeCount = mRoomList.size();
                    for (NearbyRoomBean roomBean : roomList){
                        if (! mRoomList.contains(roomBean)){
                            if (mRefresh && !mIsListEmpty){
                                mRoomList.add(0, roomBean);
                            } else {
                                if (mIsSavedInstance){
                                    mRoomList.add(0, roomBean);
                                } else{
                                    mRoomList.add(roomBean);
                                }
                            }
                        }
                    }
                    mAfterCount = mRoomList.size();
                    if (mRoomList.isEmpty()){
                        setEmptyViewVisible();
                    } else{
                        if (mRefresh){
                            if (mAfterCount == mBeforeCount){
                                Utils.showToast(mContext, mContext.getString(R.string.no_newer_info));
                            } else{
                                Utils.showToast(mContext, mContext.getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }
                    mSearchRoomAdapter.notifyDataSetChanged();

                    break;
                case GET_LOCATION:
                    showProgress();
                    mRoomListView.setMode(PullToRefreshBase.Mode.DISABLED);
                    if(mEmptyView.getVisibility() == View.VISIBLE){
                        mRoomListView.setEmptyView(null);
                        mEmptyView.setVisibility(View.GONE);
                    }
                    mRequestFlag = msg.arg1;
                    mArgs = (String) msg.obj;
                    getLocation();
                    break;
                case LOCATION_HAS_GOT:
                    Bundle args = (Bundle) msg.obj;
                    mLat = args.getFloat("lat");
                    mLng = args.getFloat("lng");
                    String region = sParamsPreference.getRoomRegion(getActivity());
                    String range = sParamsPreference.getRoomRange(getActivity());
                    String price = sParamsPreference.getRoomPrice(getActivity());
                    String evaluate = sParamsPreference.getRoomApprisal(getActivity());
                    switch(mRequestFlag){
                        case REQUEST_ALL_ROOM_INFO:
                            getRoomList(mArgs,range,price,evaluate,mStartNum,mEndNum);
                            break;
                        case REQUEST_ROOM_INFO_REGION_FILTERED:
                            if (!mRoomList.isEmpty()){
                                mRoomList.clear();
                                mSearchRoomAdapter.notifyDataSetChanged();
                            }
                            getRoomList(mArgs,range,price,evaluate,0,9);
                            break;
                        case REQUEST_ROOM_INFO_RANGE_FILTERED:
                            if (!mRoomList.isEmpty()){
                                mRoomList.clear();
                                mSearchRoomAdapter.notifyDataSetChanged();
                            }
                            // 每次筛选，都是从第0条开始请求最新的数据
                            // 因为这相当于完全的重新开始了，所以我们需要将我们已经获得的UserList清空才可以
                            if (sParamsPreference.getRoomRange(mContext) != null) {
                                getRoomList(region, mArgs, price, evaluate, 0, 9);
                            }
                            break;
                        case REQUEST_ROOM_INFO_PRICE_FILTERED:
                            if (!mRoomList.isEmpty()){
                                mRoomList.clear();
                                mSearchRoomAdapter.notifyDataSetChanged();
                            }
                            if (sParamsPreference.getRoomPrice(mContext) != null) {
                                getRoomList(region, range, mArgs, evaluate, 0, 9);
                            }
                            break;
                        case REQUEST_ROOM_INFO_APPRISAL_FILTERED:
                            if (!mRoomList.isEmpty()){
                                mRoomList.clear();
                                mSearchRoomAdapter.notifyDataSetChanged();
                            }
                            if (sParamsPreference.getRoomRange(mContext) != null) {
                                getRoomList(region, range, price, mArgs, 0, 9);
                            }
                            break;
                    }
                    break;
                case DATA_HAS_BEEN_UPDATED:
                    mSearchRoomAdapter.notifyDataSetChanged();
                    break;
                case PublicConstant.REQUEST_ERROR:
                    String errorMsg = (String) msg.obj;
                    if (mRoomList.isEmpty()){
                        setEmptyViewVisible();
                        if (! TextUtils.isEmpty(errorMsg)){
                           mEmptyView.setText(errorMsg);
                        } else {
                            mEmptyView.setText(mContext.getString(R.string.http_request_error));
                        }
                    }else{
                        if (! TextUtils.isEmpty(errorMsg)){
                            Utils.showToast(mContext, errorMsg);
                        } else {
                            Utils.showToast(mContext, mContext.getString(R.string.http_request_error));
                        }
                    }

                    break;
                case PublicConstant.NO_NETWORK:
                    hideProgress();
                    if (mRoomList.isEmpty()){
                        setEmptyViewVisible();
                        mEmptyView.setText(mContext.getString(R.string.network_not_available));
                    }else{
                        Utils.showToast(mContext, mContext.getString(R.string.network_not_available));
                    }
                    break;

                case PublicConstant.NO_RESULT:
                    setEmptyViewGone();
                    if (mRoomList.isEmpty()){
                        setEmptyViewVisible();
                    } else{
                        if (mLoadMore){
                            Utils.showToast(mContext, mContext.getString(R.string.no_more_info, mContext.getString(R.string.nearby_billiard_room_str)));
                        }
                    }
                    hideProgress();
                    break;
                case SET_PULLREFRESH_DISABLE:
                    mRoomListView.setMode(PullToRefreshBase.Mode.DISABLED);
                    break;
            }
            mSearchRoomAdapter.notifyDataSetChanged();
            if(mLoadMore && !mRoomList.isEmpty()){
                mRoomListView.getRefreshableView().setSelection(mCurrentPos - 1 );
            }
        }
    };


    private TextView mEmptyView;
    // 我们通过将disable的值设置为false来进行加载EmptyView
    // 通过将disable的值设置为true来隐藏emptyView
    private void setEmptyViewVisible(){
        mEmptyView = new TextView(mContext);
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mEmptyView.setTextColor(mContext.getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(mContext.getString(R.string.search_activity_roomfragment_empty_tv_str));
        mRoomListView.setEmptyView(mEmptyView);
    }

    private void setEmptyViewGone(){
        if (null != mEmptyView){
            mEmptyView.setVisibility(View.GONE);
            mRoomListView.setEmptyView(null);
        }
    }

    private void showProgress(){
        if(mRoomList.isEmpty()) {
            mPreTextView.setVisibility(View.VISIBLE);
        }
        mPreProgress.setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        mPreProgress.setVisibility(View.GONE);
        mPreTextView.setVisibility(View.GONE);
    }

    private class WorkerHandlerThread extends HandlerThread{
        public WorkerHandlerThread(){
            super(WORKER_HANDLER_THREAD_NAME, Process.THREAD_PRIORITY_BACKGROUND);
        }
        // 参照MateFragment当中的理解
        private Handler mWorkerHandler;
        @Override
        protected void onLooperPrepared(){
            super.onLooperPrepared();

            mWorkerHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            };
            if (Utils.networkAvaiable(mContext)){
                // 我们初始情况下的数据请求都是请求最新的数据
                fetchRoomData();
            }
        }
        public void fetchRoomData(){
            if (null != mWorkerHandler){
                mUIEventsHandler.obtainMessage(GET_LOCATION,REQUEST_ALL_ROOM_INFO,0).sendToTarget();
            }
        }
    }
    /**
     * 实现RoomFragment当中的ListView的下拉刷新的实现逻辑
     * 这个过程是可以抽象出来的
     */
    private PullToRefreshBase.OnRefreshListener2<ListView> onRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>(){
        /**
         * onPullDownToRefresh will be called only when the user has Pulled from
         * the start, and released.
         *
         * @param refreshView
         */
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){
            String label = NearbyFragmentsCommonUtils.getLastedTime(mContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mRefresh = true;
            mLoadMore = false;
            if (Utils.networkAvaiable(mContext)){
                // 我们将我们的page数增加
                // 因为我们向下拉的时候，加载的总是最新的数据，所以我们将这里的page的数目置成1，
                // 因为第一页当中的数据总是最新的
                if (mWorkerThread != null){
                    mWorkerThread.fetchRoomData();
                }
            } else{
                mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
        }

        /**
         * onPullUpToRefresh will be called only when the user has Pulled from
         * the end, and released.
         *
         * @param refreshView
         */
        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
            String label = NearbyFragmentsCommonUtils.getLastedTime(mContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mLoadMore = true;

//            if (mBeforeCount != mAfterCount && !mRefresh)
//                mPage += 1;
            mRefresh = false;
            mCurrentPos = mRoomList.size();
            if (mBeforeCount != mAfterCount && mRefresh){
                mStartNum = mEndNum + (mAfterCount - mBeforeCount);
                mEndNum += 10 + (mAfterCount - mBeforeCount);
            } else{
                mStartNum = mEndNum + 1;
                mEndNum += 10;
            }
            if (Utils.networkAvaiable(mContext)){
                if (null != mWorkerThread){
                    mWorkerThread.fetchRoomData();
                }
            }else{
                mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        mSearchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query){
                //TODO:将搜索结果传到SearResultActivity，在SearchResultActivity中进行搜索
                if(Utils.networkAvaiable(mContext)) {
                    Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                    Bundle args = new Bundle();
                    args.putInt(PublicConstant.SEARCH_TYPE, PublicConstant.SEARCH_NEARBY_ROOM);
                    args.putString(PublicConstant.SEARCH_KEYWORD, query);

                    intent.putExtras(args);
                    startActivity(intent);
                } else{
                    Utils.showToast(mContext,getString(R.string.network_not_available));
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                return false;
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                if(Utils.networkAvaiable(getActivity())) {
                    NearbyFragmentsCommonUtils commonUtils = new NearbyFragmentsCommonUtils(mContext);
                    commonUtils.initViewPager(mContext, mView);
                    if (mRoomList.isEmpty()) {
                        mLoadMore = false;
                        mRefresh = false;
                        if (null != mWorkerThread) {
                            mWorkerThread.fetchRoomData();
                        }
                    }
                }
            }
        }
    };

}

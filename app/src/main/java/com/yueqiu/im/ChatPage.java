package com.yueqiu.im;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeMessageType;
import com.gotye.api.GotyeRoom;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.gotye.api.PathUtil;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.OnKeyboardHideListener;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.ProgressDialogUtil;
import com.yueqiu.util.SendImageMessageTask;
import com.yueqiu.util.Utils;
import com.yueqiu.view.CustomListView;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by doushuqi on 15/2/4.
 */
public class ChatPage extends BaseActivity implements View.OnClickListener,
        EditText.OnEditorActionListener, EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    private static final String TAG = "ChatPage";
    public static final int REALTIMEFROM_OTHER = 2;
    public static final int REALTIMEFROM_SELF = 1;
    public static final int REALTIMEFROM_NO = 0;
    private static final int REQUEST_PIC = 1;
    private static final int REQUEST_CAMERA = 2;

    public static final int VOICE_MAX_TIME = 60 * 1000;
    private CustomListView pullListView;
//    private PullToRefreshListView mPullToRefreshListView;
    private ChatMessageAdapter adapter;
    private GotyeUser user;
    private GotyeRoom room;
    private GotyeGroup group;
    private GotyeUser currentLoginUser;

    //    private ImageView voice_text_chage;
//    private Button pressToVoice;
    private EditText textMessage;//文本消息框
    //    private ImageView showMoreType;
//    private LinearLayout moreTypeLayout;

    private PopupWindow menuWindow;
    private AnimationDrawable anim;
    public int chatType = 0;

    //    private View realTalkView;
//    private TextView realTalkName, stopRealTalk;
    private AnimationDrawable realTimeAnim;
    private boolean moreTypeForSend = true;

    public int onRealTimeTalkFrom = -1; // -1默认状态 ,0表示我在说话,1表示别人在实时语音

    private File cameraFile;
    public static final int IMAGE_MAX_SIZE_LIMIT = 1024 * 1024;
    public static final int Voice_MAX_TIME_LIMIT = 60 * 1000;
    private long playingId;

    //    private TextView title;
    private ActionBar mActionBar;
    private Button mSend;
    private View mMessageMore;
    private boolean mIsDisplayInputMethod;//键盘
//    private boolean mIsShowExtension;//扩展(包括表情和插件)
    private boolean mIsDisplayPlugin;//插件
    private boolean mIsDisplayEmoji;//表情，三者（键盘、插件、表情）只能显示一个或1者都不显示，当键盘消失时需延时加载其他控件
    private static final int DISPLAY_INPUT_METHOD = 0;
    private static final int UNDISPLAY_INPUT_METHOD = 1;
    private ImageView mEmotion, mAssistToggle;
    private View mExtension, mEmotionToggle, mSendFromePic, mSendfromCamera,mRootView;
    private InputMethodManager mInputMethodManager;

    private Fragment emojiconFragment = EmojiconsFragment.newInstance(false);
    private int mKeyboardHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_container);
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        currentLoginUser = api.getCurrentLoginUser();
        api.addListerer(this);
        user = (GotyeUser) getIntent().getSerializableExtra("user");
        room = (GotyeRoom) getIntent().getSerializableExtra("room");
        group = (GotyeGroup) getIntent().getSerializableExtra("group");
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);

        initView();
        setListener();
        if (chatType == 0) {
            api.activeSession(user);
            loadData();
        } else if (chatType == 1) {
            int code = api.enterRoom(room);
            if (code == GotyeStatusCode.CODE_OK) {
                api.activeSession(room);
                loadData();
                api.getLocalMessages(room, true);
                GotyeRoom temp = api.requestRoomInfo(room.Id, true);
                if (temp != null && !TextUtils.isEmpty(temp.getRoomName())) {
                    mActionBar.setTitle("聊天室：" + temp.getRoomName());
                }
            } else {
                ProgressDialogUtil.showProgress(this, "正在进入房间...");
            }
        } else if (chatType == 2) {
            api.activeSession(group);
            loadData();
        }



    }
    private OnKeyboardHideListener mOnKeyHideListener = new OnKeyboardHideListener() {
        @Override
        public void onKeyBoardHide() {
            if(mIsDisplayPlugin){
                mIsDisplayPlugin = true;
                mIsDisplayEmoji = false;
                mExtension.setVisibility(View.VISIBLE);
                mEmotionToggle.setVisibility(View.GONE);
            }else if(mIsDisplayEmoji){
                mIsDisplayEmoji = true;
                mIsDisplayPlugin = false;
                mEmotionToggle.setVisibility(View.VISIBLE);
                mExtension.setVisibility(View.GONE);
            }
        }
    };
    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    private void setListener() {
        mAssistToggle.setOnClickListener(this);
        mEmotion.setOnClickListener(this);
        textMessage.setOnClickListener(this);
        mSend.setOnClickListener(this);
        mMessageMore.setOnClickListener(this);
        mSendFromePic.setOnClickListener(this);
        mSendfromCamera.setOnClickListener(this);
        ViewTreeObserver rootObserver = mRootView.getViewTreeObserver();
        rootObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                mRootView.getRootView().getWindowVisibleDisplayFrame(rect);
                int screenHeight = mRootView.getRootView().getHeight();
                mKeyboardHeight = screenHeight - (rect.bottom - rect.top);
                if(mKeyboardHeight == rect.top){
                    mIsDisplayInputMethod = false;
                }else{
                    mIsDisplayInputMethod = true;
                }
                if(mIsDisplayInputMethod){
                    mExtension.setVisibility(View.GONE);
                    mEmotionToggle.setVisibility(View.GONE);

                    mIsDisplayEmoji = false;
                    mIsDisplayPlugin = false;
                }else{
                    mOnKeyHideListener.onKeyBoardHide();
                }
            }
        });
//        //用于判断当前屏幕是否显示了软键盘
//        pullListView.setOnResizeListener(new CustomListView.OnResizeListener() {
//            @Override
//            public void onResize(int w, int h, int oldw, int oldh) {
////                isDisplayInputMethod = !isShowExtension;
//            }
//        });
    }

    private void initView() {
        mEmotion = $(R.id.chat_container_open_emotion);
        mAssistToggle = $(R.id.chat_container_open_assist_toggle);
        mEmotionToggle = $(R.id.chat_container_emotion);
        mExtension = $(R.id.chat_container_extension_container);

        textMessage = $(R.id.chat_container_text_ed);
        mSend = $(R.id.chat_container_send_btn);
        pullListView = $(R.id.chat_container_list_item);
        mMessageMore = $(R.id.chat_container_message_more);
        mSendFromePic = $(R.id.to_gallery);
        mSendfromCamera = $(R.id.to_camera);
        mRootView = $(R.id.chat_root_view);
//        realTalkView = findViewById(R.id.real_time_talk_layout);
//        realTalkName = (TextView) realTalkView
//                .findViewById(R.id.real_talk_name);
//        Drawable[] anim = realTalkName.getCompoundDrawables();
//        realTimeAnim = (AnimationDrawable) anim[2];
//        stopRealTalk = (TextView) realTalkView
//                .findViewById(R.id.stop_real_talk);
//        stopRealTalk.setOnClickListener(this);

        if (user != null) {
            chatType = 0;
            mActionBar.setTitle("和 " + user.name + " 聊天");
        } else if (room != null) {
            chatType = 1;
            mActionBar.setTitle("聊天室：" + room.getRoomID());
        } else if (group != null) {
            chatType = 2;
            String titleText = null;
            if (!TextUtils.isEmpty(group.getGroupName())) {
                titleText = group.getGroupName();
            } else {
                GotyeGroup temp = api.requestGroupInfo(group.getGroupID(), true);
                if (temp != null && !TextUtils.isEmpty(temp.getGroupName())) {
                    titleText = temp.getGroupName();
                } else {
                    titleText = String.valueOf(group.getGroupID());
                }
            }
            mActionBar.setTitle("群：" + titleText);
        }

//        voice_text_chage = (ImageView) findViewById(R.id.send_voice);
//        pressToVoice = (Button) findViewById(R.id.press_to_voice_chat);
//        textMessage = (EditText) findViewById(R.id.chat_container_text_ed);
//        mSend = (Button) findViewById(R.id.chat_container_send_btn);
//        moreTypeLayout = (LinearLayout) findViewById(R.id.more_type_layout);
//
//        moreTypeLayout.findViewById(R.id.to_gallery).setOnClickListener(this);
//        moreTypeLayout.findViewById(R.id.to_camera).setOnClickListener(this);
//        moreTypeLayout.findViewById(R.id.real_time_voice_chat)
//                .setOnClickListener(this);
//
//        voice_text_chage.setOnClickListener(this);
//        showMoreType.setOnClickListener(this);
        textMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                String text = arg0.getText().toString();
                // GotyeMessage message =new GotyeMessage();
                // GotyeChatManager.getInstance().sendMessage(message);
                sendTextMessage(text);
                textMessage.setText("");
                return true;
            }
        });
        /*pressToVoice.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (onRealTimeTalkFrom == 0) {
                            Utils.showToast(ChatPage.this, "正在实时通话中...");
                            return false;
                        }

                        if (GotyeVoicePlayClickListener.isPlaying) {
                            GotyeVoicePlayClickListener.currentPlayListener
                                    .stopPlayVoice();
                        }

                        if (chatType == 0) {
                            api.startTalk(user, WhineMode.DEFAULT, false,
                                    60 * 1000);
                        } else if (chatType == 1) {
                            api.startTalk(room, WhineMode.DEFAULT, false,
                                    60 * 1000);
                        } else if (chatType == 2) {
                            api.startTalk(group, WhineMode.DEFAULT, false,
                                    60 * 1000);
                        }
                        pressToVoice.setText("松开 发送");
                        break;
                    case MotionEvent.ACTION_UP:
                        if (onRealTimeTalkFrom == 0) {
                            return false;
                        }
                        Log.d("chat_page",
                                "onTouch action=ACTION_UP" + event.getAction());
                        // if (onRealTimeTalkFrom > 0) {
                        // return false;
                        // }
                        api.stopTalk();
                        Log.d("chat_page",
                                "after stopTalk action=" + event.getAction());
                        pressToVoice.setText("按住 说话");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        if (onRealTimeTalkFrom == 0) {
                            return false;
                        }
                        Log.d("chat_page",
                                "onTouch action=ACTION_CANCEL" + event.getAction());
                        // if (onRealTimeTalkFrom > 0) {
                        // return false;
                        // }
                        api.stopTalk();
                        pressToVoice.setText("按住 说话");
                        break;
                    default:
                        Log.d("chat_page",
                                "onTouch action=default" + event.getAction());
                        break;
                }
                return false;
            }
        });*/
        adapter = new ChatMessageAdapter(this, new ArrayList<GotyeMessage>());
        pullListView.setClickable(false);
        pullListView.setAdapter(adapter);
        pullListView.setSelection(adapter.getCount());
        setListViewInfo();
    }

    private void setListViewInfo() {
        // 下拉刷新监听器
        pullListView.setonRefreshListener(new CustomListView.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (chatType == 1) {
                    api.getLocalMessages(room, true);
                } else {
                    List<GotyeMessage> list = null;

                    if (chatType == 0) {
                        list = api.getLocalMessages(user, true);
                    } else if (chatType == 2) {
                        list = api.getLocalMessages(group, true);
                    }
                    if (list != null) {
                        for (GotyeMessage msg : list) {
                            api.downloadMessage(msg);
                        }
                        adapter.refreshData(list);
                    } else {
                        Utils.showToast(ChatPage.this, "没有更多历史消息");
                    }
                }
                adapter.notifyDataSetChanged();
                pullListView.onRefreshComplete();
            }
        });
        pullListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                // TODO Auto-generated method stub
                final GotyeMessage message = adapter.getItem(arg2);
                pullListView.setTag(message);
                if (message.getSender().name.equals(currentLoginUser.getName())) {
                    return false;
                }
                pullListView.showContextMenu();
                return true;
            }
        });
        pullListView
                .setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

                    @Override
                    public void onCreateContextMenu(ContextMenu conMenu,
                                                    View arg1, ContextMenu.ContextMenuInfo arg2) {
                        final GotyeMessage message = (GotyeMessage) pullListView
                                .getTag();
                        if (message.getSender().name
                                .equals(currentLoginUser.name)) {
                            return;
                        }
                        MenuItem m = conMenu.add(0, 0, 0, "举报");
                        m.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                api.report(0, "举报的说明", message);
                                return true;
                            }
                        });
                    }
                });

    }

    private void loadData() {
        List<GotyeMessage> messages = null;
        if (user != null) {
            messages = api.getLocalMessages(user, true);
        } else if (room != null) {
            messages = api.getLocalMessages(room, true);
        } else if (group != null) {
            messages = api.getLocalMessages(group, true);
        }
        if (messages == null) {
            messages = new ArrayList<GotyeMessage>();
        }
        for (GotyeMessage msg : messages) {
            api.downloadMessage(msg);
        }
        adapter.refreshData(messages);
    }

    //发送文本消息
    private void sendTextMessage(String text) {
        if (!TextUtils.isEmpty(text)) {
            GotyeMessage toSend;
            if (chatType == 0) {
                toSend = GotyeMessage.createTextMessage(currentLoginUser, user,
                        text);
            } else if (chatType == 1) {
                toSend = GotyeMessage.createTextMessage(currentLoginUser, room,
                        text);
            } else {
                toSend = GotyeMessage.createTextMessage(currentLoginUser,
                        group, text);
            }
            String extraStr = null;
            if (text.contains("#")) {
                String[] temp = text.split("#");
                if (temp.length > 1) {
                    extraStr = temp[1];
                }

            } else if (text.contains("#")) {
                String[] temp = text.split("#");
                if (temp.length > 1) {
                    extraStr = temp[1];
                }
            }
            if (extraStr != null) {
                toSend.putExtraData(extraStr.getBytes());
            }

            int code = api.sendMessage(toSend);
            adapter.addMsgToBottom(toSend);
            scrollToBottom();
//            sendUserDataMessage("userdata message".getBytes(), "text#text");
        }
    }

    public void sendUserDataMessage(byte[] userData, String text) {
        if (userData != null) {
            GotyeMessage toSend;
            if (chatType == 0) {
                toSend = GotyeMessage.createUserDataMessage(currentLoginUser, user,
                        userData, userData.length);
            } else if (chatType == 1) {
                toSend = GotyeMessage.createUserDataMessage(currentLoginUser, room,
                        userData, userData.length);
            } else {
                toSend = GotyeMessage.createUserDataMessage(currentLoginUser,
                        group, userData, userData.length);
            }
            String extraStr = null;
            if (text.contains("#")) {
                String[] temp = text.split("#");
                if (temp.length > 1) {
                    extraStr = temp[1];
                }

            } else if (text.contains("#")) {
                String[] temp = text.split("#");
                if (temp.length > 1) {
                    extraStr = temp[1];
                }
            }
            if (extraStr != null) {
                toSend.putExtraData(extraStr.getBytes());
            }

            int code = api.sendMessage(toSend);
            adapter.addMsgToBottom(toSend);
            scrollToBottom();
        }
    }


    private void scrollToBottom() {
        pullListView.setSelection(adapter.getCount() - 1);
    }

    public void callBackSendImageMessage(GotyeMessage msg) {
        adapter.addMsgToBottom(msg);
        scrollToBottom();
    }

//    private void showExtension(boolean isShow) {
//        if (isShow) {
//            mHandler.sendEmptyMessage(isDisplayInputMethod ? DISPLAY_INPUT_METHOD : UNDISPLAY_INPUT_METHOD);
//        } else {
//            mExtension.setVisibility(View.GONE);
//            mEmotionToggle.setVisibility(View.GONE);
//        }
//    }
//
//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case DISPLAY_INPUT_METHOD:
//                    mInputMethodManager.hideSoftInputFromWindow(textMessage.getWindowToken(), 0);
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            showExtensionDetail();
//                        }
//                    }, 300);
//                    break;
//                case UNDISPLAY_INPUT_METHOD:
//                    showExtensionDetail();
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

//    private void showExtensionDetail() {
//        if (isDisplayPlugin) {
//            mExtension.setVisibility(View.VISIBLE);
//            mEmotionToggle.setVisibility(View.GONE);
//        }
//        if (isDisplayEmoji) {
//            mEmotionToggle.setVisibility(View.VISIBLE);
//            mExtension.setVisibility(View.GONE);
//        }
//    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        api.removeListener(this);
        if (chatType == 0) {
            api.deactiveSession(user);
        } else if (chatType == 1) {
            api.deactiveSession(room);
            api.leaveRoom(room);
        } else {
            api.deactiveSession(group);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        /*if (GotyeVoicePlayClickListener.isPlaying
                && GotyeVoicePlayClickListener.currentPlayListener != null) {
            // 停止语音播放
            GotyeVoicePlayClickListener.currentPlayListener.stopPlayVoice();
        }*/
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);
        boolean isIMOnline = api.isOnline();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // if (onRealTimeTalkFrom == REALTIMEFROM_SELF) {
        api.stopTalk();
        // return;
        // } else if (onRealTimeTalkFrom == REALTIMEFROM_OTHER) {
        api.stopPlay();
        // }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_container_open_emotion:
//                isDisplayEmoji = true;
//                isDisplayPlugin = false;
//                showExtension(true);
//                isShowExtension = true;
                mIsDisplayPlugin = false;
                if(mIsDisplayEmoji){
                    mEmotionToggle.setVisibility(View.GONE);
                    mIsDisplayEmoji = false;
                }else{
                    mIsDisplayEmoji = true;
                    if(mIsDisplayInputMethod){
                        Utils.dismissInputMethod(this,textMessage);
                    }else{
                        mOnKeyHideListener.onKeyBoardHide();
                    }
                }
                break;
            case R.id.chat_container_open_assist_toggle:
//              mIsDisplayEmoji = false;
                if(mIsDisplayPlugin){
                    mExtension.setVisibility(View.GONE);
                    mIsDisplayPlugin = false;
                }else{
                    mIsDisplayPlugin = true;
                    if(mIsDisplayInputMethod){
                        Utils.dismissInputMethod(this,textMessage);
                    }else{
                        mOnKeyHideListener.onKeyBoardHide();
                    }
                }
//                isDisplayEmoji = false;
//                isDisplayPlugin = !isDisplayPlugin;
//                showExtension(isDisplayPlugin);
//
//                isShowExtension = isDisplayPlugin;
                break;
            case R.id.chat_container_text_ed:
//                isDisplayPlugin = false;
//                isDisplayEmoji = false;
                textMessage.setFocusable(true);
//                showExtension(false);
//                isShowExtension = false;
                break;
            case R.id.chat_container_send_btn:
//                sendMessage();
//                if (isShowExtension) {
//                    showExtension(false);
//                }
                sendTextMessage(textMessage.getText().toString());
                textMessage.setText("");
                break;
            case R.id.chat_container_message_more:
                //查看更多消息
                break;
            case R.id.to_gallery:
                takePic();
                break;
            case R.id.to_camera:
                takePhoto();
                break;
            default:
                break;
        }
    }

    private void takePic() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PIC);
    }

    private void takePhoto() {
        selectPicFromCamera();
    }

    public void selectPicFromCamera() {
        if (!FileUtil.isSDCardReady()) {
            Toast.makeText(getApplicationContext(), "SD卡不存在，不能拍照",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        cameraFile = new File(PathUtil.getAppFIlePath()
                + +System.currentTimeMillis() + ".jpg");
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 选取图片的返回值
        if (requestCode == REQUEST_PIC) {
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    String path = FileUtil.uriToPath(this, selectedImage);
                    sendPicture(path);
                }
            }

        } else if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {

                if (cameraFile != null && cameraFile.exists())
                    sendPicture(cameraFile.getAbsolutePath());
            }
        }
        // TODO 获取图片失败
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendPicture(String path) {
        SendImageMessageTask task;
        if (chatType == 0) {
            task = new SendImageMessageTask(this, user);
        } else if (chatType == 1) {
            task = new SendImageMessageTask(this, room);
        } else {
            task = new SendImageMessageTask(this, group);
        }
        task.execute(path);
    }

    public void setPlayingId(long playingId) {
        this.playingId = playingId;
        adapter.notifyDataSetChanged();
    }

    public long getPlayingId() {
        return playingId;
    }

    @Override
    public void onSendMessage(int code, GotyeMessage message) {
        Log.d("ddd", "code= " + code + "message = " + message);
        // GotyeChatManager.getInstance().insertChatMessage(message);
        adapter.updateMessage(message);
        if (message.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
            api.decodeMessage(message);
        }
        // message.senderUser =
        // DBManager.getInstance().getUser(currentLoginName);
        pullListView.setSelection(adapter.getCount());
    }

    @Override
    public void onReceiveMessage(int code, GotyeMessage message) {
        // GotyeChatManager.getInstance().insertChatMessage(message);
        if (chatType == 0) {
            if (isMyMessage(message)) {
                // msg.senderUser = user;
                adapter.addMsgToBottom(message);
                pullListView.setSelection(adapter.getCount());
            }
        } else if (chatType == 1) {
            if (message.getReceiver().Id == room.getRoomID()) {
                // message.senderUser = user;
                adapter.addMsgToBottom(message);
                pullListView.setSelection(adapter.getCount());
            }
        } else if (chatType == 2) {
            if (message.getReceiver().Id == group.getGroupID()) {
                adapter.addMsgToBottom(message);
                pullListView.setSelection(adapter.getCount());
            }
        }
        //scrollToBottom();
    }

    private boolean isMyMessage(GotyeMessage message) {
        if (message.getSender() != null
                && user.getName().equals(message.getSender().name)
                && currentLoginUser.name.equals(message.getReceiver().name)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDownloadMessage(int code, GotyeMessage message) {
        adapter.downloadDone(message);
    }

    /*@Override
    public void onEnterRoom(int code, long lastMsgID, GotyeRoom room) {
        ProgressDialogUtil.dismiss();
        if (code == 0) {
            api.activeSession(room);
            loadData();
            GotyeRoom temp = api.requestRoomInfo(room.Id, true);
            if (temp != null && !TextUtils.isEmpty(temp.getRoomName())) {
                mActionBar.setTitle("聊天室：" + temp.getRoomName());
            }
        } else {
            Utils.showToast(this, "房间不存在...");
            finish();
        }
    }*/

    @Override
    public void onGetHistoryMessageList(int code, List<GotyeMessage> list) {
        if (chatType == 1) {
            List<GotyeMessage> listmessages = api.getLocalMessages(room,
                    false);
            if (listmessages != null) {
                for (GotyeMessage temp : listmessages) {
                    api.downloadMessage(temp);
                }
                adapter.refreshData(listmessages);
            } else {
                Utils.showToast(this, "没有历史记录");
            }
        }
        adapter.notifyDataSetInvalidated();
        pullListView.onRefreshComplete();
    }
/*
    @Override
    public void onStartTalk(int code, boolean isRealTime, int targetType,
                            GotyeChatTarget target) {
        if (isRealTime) {
            if (code != 0) {
                Utils.showToast(this, "抢麦失败，先听听别人说什么。");
                return;
            }
            if (GotyeVoicePlayClickListener.isPlaying) {
                GotyeVoicePlayClickListener.currentPlayListener.stopPlayVoice();
            }
            onRealTimeTalkFrom = 0;
            realTimeAnim.start();
            realTalkView.setVisibility(View.VISIBLE);
            realTalkName.setText("您正在说话..");
            stopRealTalk.setVisibility(View.VISIBLE);
        }
    }*/

//    /**
//     * 发送语音结束时调用该方法，然后更新adapter
//     *
//     * @param code
//     * @param message
//     * @param isVoiceReal
//     */
  /*  @Override
    public void onStopTalk(int code, GotyeMessage message, boolean isVoiceReal) {
        if (isVoiceReal) {
            onRealTimeTalkFrom = -1;
            realTimeAnim.stop();
            realTalkView.setVisibility(View.GONE);
        } else {
            if (code != 0) {
                Utils.showToast(this, "时间太短...");
                return;
            } else if (message == null) {
                Utils.showToast(this, "时间太短...");
                return;
            }
            api.sendMessage(message);
            message.setStatus(GotyeMessage.STATUS_SENDING);
            adapter.addMsgToBottom(message);
            scrollToBottom();
            api.decodeMessage(message);
        }

    }

    @Override
    public void onPlayStop(int code) {
        onRealTimeTalkFrom = -1;
        realTimeAnim.stop();
        realTalkView.setVisibility(View.GONE);
        setPlayingId(0);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayStartReal(int code, long roomId, String who) {
        if (code == 0 && roomId == this.room.getRoomID()) {
            onRealTimeTalkFrom = 1;
            realTalkView.setVisibility(View.VISIBLE);
            realTalkName.setText(who + "正在说话..");
            realTimeAnim.start();
            stopRealTalk.setVisibility(View.GONE);
            if (GotyeVoicePlayClickListener.isPlaying) {
                GotyeVoicePlayClickListener.currentPlayListener.stopPlayVoice();
            }
        }
    }
*/
    @Override
    public void onRequestUserInfo(int code, GotyeUser user) {
//        this.user = user;
    }

    @Override
    public void onDownloadMedia(int code, String path, String url) {
        // TODO Auto-generated method stub
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onUserDismissGroup(GotyeGroup group, GotyeUser user) {
        // TODO Auto-generated method stub
        if (this.group != null && group.getGroupID() == this.group.getGroupID()) {
//            Intent i = new Intent(this, MainActivity.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Toast.makeText(getBaseContext(), "群主解散了该群,会话结束", Toast.LENGTH_SHORT)
                    .show();
            finish();
//            startActivity(i);
        }
    }

    @Override
    public void onUserKickdFromGroup(GotyeGroup group, GotyeUser kicked,
                                     GotyeUser actor) {
        // TODO Auto-generated method stub
        if (this.group != null && group.getGroupID() == this.group.getGroupID()) {
            if (kicked.getName().equals(currentLoginUser.getName())) {
//                Intent i = new Intent(this, MainActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Toast.makeText(getBaseContext(), "您被踢出了群,会话结束",
                        Toast.LENGTH_SHORT).show();
                finish();
//                startActivity(i);
            }

        }
    }

    @Override
    public void onReport(int code, GotyeMessage message) {
        // TODO Auto-generated method stub
        if (code == GotyeStatusCode.CODE_OK) {
            Utils.showToast(this, "举报成功");
        } else {
            Utils.showToast(this, "举报失败");
        }
        super.onReport(code, message);
    }

    @Override
    public void onRequestRoomInfo(int code, GotyeRoom room) {
        // TODO Auto-generated method stub
        if (this.room != null && this.room.getRoomID() == room.getRoomID()) {
            mActionBar.setTitle("聊天室：" + room.getRoomName());
        }
        super.onRequestRoomInfo(code, room);
    }

    @Override
    public void onRequestGroupInfo(int code, GotyeGroup group) {
        // TODO Auto-generated method stub
        if (this.group != null && this.group.getGroupID() == group.getGroupID()) {
            mActionBar.setTitle("聊天室：" + group.getGroupName());
        }
    }

    @Override
    public void onDecodeMessage(int code, GotyeMessage message) {
        // TODO Auto-generated method stub
        super.onDecodeMessage(code, message);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(textMessage);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(textMessage, emojicon);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(mIsDisplayEmoji){
                    mEmotionToggle.setVisibility(View.GONE);
                    mIsDisplayEmoji = false;
                }else if(mIsDisplayPlugin){
                    mExtension.setVisibility(View.GONE);
                    mIsDisplayInputMethod = false;
                }
                else {
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //加载表情Fragment
        if (getSupportFragmentManager().findFragmentById(R.id.chat_container_emotion) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.chat_container_emotion, emojiconFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
            case R.id.chat_page_friend:
                //查看好友信息
                //TODO:
//                Intent intent = new Intent(this, MyProfileActivity.class);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_page_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}

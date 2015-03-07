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
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
import com.yueqiu.bean.OnKeyboardHideListener;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.ProgressDialogUtil;
import com.yueqiu.util.SendImageMessageTask;
import com.yueqiu.util.Utils;
import com.yueqiu.view.CustomListView;

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
    private CustomListView mPullListView;
    private ChatMessageAdapter mAdapter;
    private GotyeUser mUser;
    private GotyeRoom mRoom;
    private GotyeGroup mGroup;
    private GotyeUser mCurrentLoginUser;
    private EditText mTextMessage;//文本消息框

    private PopupWindow mMenuWindow;
    private AnimationDrawable mAnim;
    public int mChatType = 0;

    private AnimationDrawable mRealTimeAnim;
    private boolean mMoreTypeForSend = true;

    public int mOnRealTimeTalkFrom = -1; // -1默认状态 ,0表示我在说话,1表示别人在实时语音

    private File mCameraFile;
    public static final int IMAGE_MAX_SIZE_LIMIT = 1024 * 1024;
    public static final int Voice_MAX_TIME_LIMIT = 60 * 1000;
    private long mPlayingId;

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
    private View mExtension, mEmotionToggle, mSendFromePic, mSendfromCamera, mRootView;
    private InputMethodManager mInputMethodManager;

    private Fragment emojiconFragment = EmojiconsFragment.newInstance(false);
    private int mKeyboardHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_container);
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mCurrentLoginUser = api.getCurrentLoginUser();
        api.addListerer(this);
        mUser = (GotyeUser) getIntent().getSerializableExtra("user");
        mRoom = (GotyeRoom) getIntent().getSerializableExtra("room");
        mGroup = (GotyeGroup) getIntent().getSerializableExtra("group");
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);

        initView();
        setListener();
        if (mChatType == 0) {
            api.activeSession(mUser);
            loadData();
        } else if (mChatType == 1) {
            int code = api.enterRoom(mRoom);
            if (code == GotyeStatusCode.CODE_OK) {
                api.activeSession(mRoom);
                loadData();
                api.getLocalMessages(mRoom, true);
                GotyeRoom temp = api.requestRoomInfo(mRoom.Id, true);
                if (temp != null && !TextUtils.isEmpty(temp.getRoomName())) {
                    mActionBar.setTitle(getString(R.string.chat_room) + temp.getRoomName());
                }
            } else {
                ProgressDialogUtil.showProgress(this, getString(R.string.entering_room));
            }
        } else if (mChatType == 2) {
            api.activeSession(mGroup);
            loadData();
        }


    }
    private OnKeyboardHideListener mOnKeyHideListener = new OnKeyboardHideListener() {
        @Override
        public void onKeyBoardHide() {
            if (mIsDisplayPlugin) {
                mIsDisplayPlugin = true;
                mIsDisplayEmoji = false;
                mExtension.setVisibility(View.VISIBLE);
                mEmotionToggle.setVisibility(View.GONE);
            } else if (mIsDisplayEmoji) {
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
        mTextMessage.setOnClickListener(this);
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
                if (mKeyboardHeight == rect.top) {
                    mIsDisplayInputMethod = false;
                } else {
                    mIsDisplayInputMethod = true;
                }
                if (mIsDisplayInputMethod) {
                    mExtension.setVisibility(View.GONE);
                    mEmotionToggle.setVisibility(View.GONE);

                    mIsDisplayEmoji = false;
                    mIsDisplayPlugin = false;
                } else {
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
        //TODO:先把表情去了
        mEmotion.setVisibility(View.GONE);

        mAssistToggle = $(R.id.chat_container_open_assist_toggle);
        mEmotionToggle = $(R.id.chat_container_emotion);
        mExtension = $(R.id.chat_container_extension_container);
        mTextMessage = $(R.id.chat_container_text_ed);
        mSend = $(R.id.chat_container_send_btn);
        mPullListView = $(R.id.chat_container_list_item);
        mMessageMore = $(R.id.chat_container_message_more);
        mSendFromePic = $(R.id.to_gallery);
        mSendfromCamera = $(R.id.to_camera);
        mRootView = $(R.id.chat_root_view);

        if (mUser != null) {
            mChatType = 0;
            mActionBar.setTitle(getString(R.string.and) + mUser.name + getString(R.string.chat));
        } else if (mRoom != null) {
            mChatType = 1;
            mActionBar.setTitle(getString(R.string.chat_room) + mRoom.getRoomID());
        } else if (mGroup != null) {
            mChatType = 2;
            String titleText;
            if (!TextUtils.isEmpty(mGroup.getGroupName())) {
                titleText = mGroup.getGroupName();
            } else {
                GotyeGroup temp = api.requestGroupInfo(mGroup.getGroupID(), true);
                if (temp != null && !TextUtils.isEmpty(temp.getGroupName())) {
                    titleText = temp.getGroupName();
                } else {
                    titleText = String.valueOf(mGroup.getGroupID());
                }
            }
            mActionBar.setTitle(getString(R.string.crowed) + titleText);
        }

        mTextMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                String text = arg0.getText().toString();
                // GotyeMessage message =new GotyeMessage();
                // GotyeChatManager.getInstance().sendMessage(message);
                sendTextMessage(text);
                mTextMessage.setText("");
                return true;
            }
        });

        mAdapter = new ChatMessageAdapter(this, new ArrayList<GotyeMessage>());
        mPullListView.setClickable(false);
        mPullListView.setAdapter(mAdapter);
        mPullListView.setSelection(mAdapter.getCount());
        setListViewInfo();
    }

    private void setListViewInfo() {
        // 下拉刷新监听器
        mPullListView.setonRefreshListener(new CustomListView.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (mChatType == 1) {
                    api.getLocalMessages(mRoom, true);
                } else {
                    List<GotyeMessage> list = null;

                    if (mChatType == 0) {
                        list = api.getLocalMessages(mUser, true);
                    } else if (mChatType == 2) {
                        list = api.getLocalMessages(mGroup, true);
                    }
                    if (list != null) {
                        for (GotyeMessage msg : list) {
                            api.downloadMessage(msg);
                        }
                        mAdapter.refreshData(list);
                    } else {
                        Utils.showToast(ChatPage.this, getString(R.string.no_more_history_info));
                    }
                }
                mAdapter.notifyDataSetChanged();
                mPullListView.onRefreshComplete();
            }
        });
        mPullListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                // TODO Auto-generated method stub
                final GotyeMessage message = mAdapter.getItem(arg2);
                mPullListView.setTag(message);
                if (message.getSender().name.equals(mCurrentLoginUser.getName())) {
                    return false;
                }
                mPullListView.showContextMenu();
                return true;
            }
        });
        mPullListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

                @Override
                public void onCreateContextMenu(ContextMenu conMenu,
                                                    View arg1, ContextMenu.ContextMenuInfo arg2) {
                    final GotyeMessage message = (GotyeMessage) mPullListView.getTag();
                    if (message.getSender().name.equals(mCurrentLoginUser.name)) {
                            return;
                        }
                    MenuItem m = conMenu.add(0, 0, 0, getString(R.string.report));
                    m.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                           api.report(0, getString(R.string.report_content), message);
                                return true;
                        }
                    });
                }
        });

    }

    private void loadData() {
        List<GotyeMessage> messages = null;
        if (mUser != null) {
            messages = api.getLocalMessages(mUser, true);
        } else if (mRoom != null) {
            messages = api.getLocalMessages(mRoom, true);
        } else if (mGroup != null) {
            messages = api.getLocalMessages(mGroup, true);
        }
        if (messages == null) {
            messages = new ArrayList<GotyeMessage>();
        }
        for (GotyeMessage msg : messages) {
            api.downloadMessage(msg);
        }
        mAdapter.refreshData(messages);
    }

    //发送文本消息
    private void sendTextMessage(String text) {
        if (!TextUtils.isEmpty(text)) {
            GotyeMessage toSend;
            if (mChatType == 0) {
                toSend = GotyeMessage.createTextMessage(mCurrentLoginUser, mUser,text);
            } else if (mChatType == 1) {
                toSend = GotyeMessage.createTextMessage(mCurrentLoginUser, mRoom,text);
            } else {
                toSend = GotyeMessage.createTextMessage(mCurrentLoginUser,mGroup, text);
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
            mAdapter.addMsgToBottom(toSend);
            scrollToBottom();
//            sendUserDataMessage("userdata message".getBytes(), "text#text");
        }
    }

    public void sendUserDataMessage(byte[] userData, String text) {
        if (userData != null) {
            GotyeMessage toSend;
            if (mChatType == 0) {
                toSend = GotyeMessage.createUserDataMessage(mCurrentLoginUser, mUser,
                        userData, userData.length);
            } else if (mChatType == 1) {
                toSend = GotyeMessage.createUserDataMessage(mCurrentLoginUser, mRoom,
                        userData, userData.length);
            } else {
                toSend = GotyeMessage.createUserDataMessage(mCurrentLoginUser,
                        mGroup, userData, userData.length);
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
            mAdapter.addMsgToBottom(toSend);
            scrollToBottom();
        }
    }


    private void scrollToBottom() {
        mPullListView.setSelection(mAdapter.getCount() - 1);
    }

    public void callBackSendImageMessage(GotyeMessage msg) {
        mAdapter.addMsgToBottom(msg);
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
        if (mChatType == 0) {
            api.deactiveSession(mUser);
        } else if (mChatType == 1) {
            api.deactiveSession(mRoom);
            api.leaveRoom(mRoom);
        } else {
            api.deactiveSession(mGroup);
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
        if (!isIMOnline)
            Utils.showToast(this, getString(R.string.im_user_offline));
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
                if (mIsDisplayEmoji) {
                    mEmotionToggle.setVisibility(View.GONE);
                    mIsDisplayEmoji = false;
                } else {
                    mIsDisplayEmoji = true;
                    if (mIsDisplayInputMethod) {
                        Utils.dismissInputMethod(this, mTextMessage);
                    } else {
                        mOnKeyHideListener.onKeyBoardHide();
                    }
                }
                break;
            case R.id.chat_container_open_assist_toggle:
//              mIsDisplayEmoji = false;
                if (mIsDisplayPlugin) {
                    mExtension.setVisibility(View.GONE);
                    mIsDisplayPlugin = false;
                } else {
                    mIsDisplayPlugin = true;
                    if (mIsDisplayInputMethod) {
                        Utils.dismissInputMethod(this, mTextMessage);
                    } else {
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
                mTextMessage.setFocusable(true);
//                showExtension(false);
//                isShowExtension = false;
                break;
            case R.id.chat_container_send_btn:
//                sendMessage();
//                if (isShowExtension) {
//                    showExtension(false);
//                }
                sendTextMessage(mTextMessage.getText().toString());
                mTextMessage.setText("");
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
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
//        intent.setType("image/*");
        Intent albumIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(albumIntent, REQUEST_PIC);
    }

    private void takePhoto() {
        selectPicFromCamera();
    }

    public void selectPicFromCamera() {
        if (!FileUtil.isSDCardReady()) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_sdcard_cannot_photo),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mCameraFile = new File(PathUtil.getAppFIlePath()
                + +System.currentTimeMillis() + ".jpg");
        mCameraFile.getParentFile().mkdirs();
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraFile)),REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 选取图片的返回值
        if (requestCode == REQUEST_PIC) {
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    String path = FileUtil.uriToPath(this, selectedImage);
                    if (null != path && !"".equals(path))
                        sendPicture(path);
                }
            }

        } else if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {

                if (mCameraFile != null && mCameraFile.exists())
                    sendPicture(mCameraFile.getAbsolutePath());
            }
        }
        // TODO 获取图片失败
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendPicture(String path) {
        SendImageMessageTask task;
        if (mChatType == 0) {
            task = new SendImageMessageTask(this, mUser);
        } else if (mChatType == 1) {
            task = new SendImageMessageTask(this, mRoom);
        } else {
            task = new SendImageMessageTask(this, mGroup);
        }
        task.execute(path);
    }

    public void setPlayingId(long playingId) {
        this.mPlayingId = playingId;
        mAdapter.notifyDataSetChanged();
    }

    public long getPlayingId() {
        return mPlayingId;
    }

    @Override
    public void onSendMessage(int code, GotyeMessage message) {
        Log.d("ddd", "code= " + code + "message = " + message);
        // GotyeChatManager.getInstance().insertChatMessage(message);
        mAdapter.updateMessage(message);
        if (message.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
            api.decodeMessage(message);
        }
        // message.senderUser =
        // DBManager.getInstance().getUser(currentLoginName);
        mPullListView.setSelection(mAdapter.getCount());
    }

    @Override
    public void onReceiveMessage(int code, GotyeMessage message) {
        // GotyeChatManager.getInstance().insertChatMessage(message);
        if (mChatType == 0) {
            if (isMyMessage(message)) {
                // msg.senderUser = user;
                mAdapter.addMsgToBottom(message);
                mPullListView.setSelection(mAdapter.getCount());
            }
        } else if (mChatType == 1) {
            if (message.getReceiver().Id == mRoom.getRoomID()) {
                // message.senderUser = user;
                mAdapter.addMsgToBottom(message);
                mPullListView.setSelection(mAdapter.getCount());
            }
        } else if (mChatType == 2) {
            if (message.getReceiver().Id == mGroup.getGroupID()) {
                mAdapter.addMsgToBottom(message);
                mPullListView.setSelection(mAdapter.getCount());
            }
        }
        //scrollToBottom();
    }

    private boolean isMyMessage(GotyeMessage message) {
        if (message.getSender() != null&& mUser.getName().equals(message.getSender().name)
                && mCurrentLoginUser.name.equals(message.getReceiver().name)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDownloadMessage(int code, GotyeMessage message) {
        mAdapter.downloadDone(message);
    }


    @Override
    public void onGetHistoryMessageList(int code, List<GotyeMessage> list) {
        if (mChatType == 1) {
            List<GotyeMessage> listmessages = api.getLocalMessages(mRoom,false);
            if (listmessages != null) {
                for (GotyeMessage temp : listmessages) {
                    api.downloadMessage(temp);
                }
                mAdapter.refreshData(listmessages);
            } else {
                Utils.showToast(this, getString(R.string.no_history));
            }
        }
        mAdapter.notifyDataSetInvalidated();
        mPullListView.onRefreshComplete();
    }

    @Override
    public void onRequestUserInfo(int code, GotyeUser user) {
//        this.user = user;
    }

    @Override
    public void onDownloadMedia(int code, String path, String url) {
        // TODO Auto-generated method stub
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUserDismissGroup(GotyeGroup group, GotyeUser user) {
        // TODO Auto-generated method stub
        if (this.mGroup != null && group.getGroupID() == this.mGroup.getGroupID()) {
//            Intent i = new Intent(this, MainActivity.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Toast.makeText(getBaseContext(), getString(R.string.group_owner_dismiss_group), Toast.LENGTH_SHORT)
                    .show();
            finish();
//            startActivity(i);
        }
    }

    @Override
    public void onUserKickdFromGroup(GotyeGroup group, GotyeUser kicked,
                                     GotyeUser actor) {
        // TODO Auto-generated method stub
        if (this.mGroup != null && group.getGroupID() == this.mGroup.getGroupID()) {
            if (kicked.getName().equals(mCurrentLoginUser.getName())) {
//                Intent i = new Intent(this, MainActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Toast.makeText(getBaseContext(), getString(R.string.you_are_out_group),
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
            Utils.showToast(this, getString(R.string.report_success));
        } else {
            Utils.showToast(this, getString(R.string.report_fail));
        }
        super.onReport(code, message);
    }

    @Override
    public void onRequestRoomInfo(int code, GotyeRoom room) {
        // TODO Auto-generated method stub
        if (this.mRoom != null && this.mRoom.getRoomID() == room.getRoomID()) {
            mActionBar.setTitle(getString(R.string.chat_room) + room.getRoomName());
        }
        super.onRequestRoomInfo(code, room);
    }

    @Override
    public void onRequestGroupInfo(int code, GotyeGroup group) {
        // TODO Auto-generated method stub
        if (this.mGroup != null && this.mGroup.getGroupID() == group.getGroupID()) {
            mActionBar.setTitle(getString(R.string.chat_room) + group.getGroupName());
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
        EmojiconsFragment.backspace(mTextMessage);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mTextMessage, emojicon);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mIsDisplayEmoji) {
                    mEmotionToggle.setVisibility(View.GONE);
                    mIsDisplayEmoji = false;
                } else if (mIsDisplayPlugin) {
                    mExtension.setVisibility(View.GONE);
                    mIsDisplayPlugin = false;
                } else {
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

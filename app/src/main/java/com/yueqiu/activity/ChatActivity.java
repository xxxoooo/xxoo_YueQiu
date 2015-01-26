package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.yueqiu.R;
import com.yueqiu.adapter.ChatMsgViewAdapter;
import com.yueqiu.bean.ChatMsgEntity;
import com.yueqiu.bean.RecentChatEntity;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.db.DBUtils;
import com.yueqiu.fragment.chatbar.MessageFragment;
import com.yueqiu.view.CustomListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doushuqi on 15/1/10.
 */
public class ChatActivity extends FragmentActivity implements View.OnClickListener,
        EditText.OnFocusChangeListener, EditText.OnEditorActionListener,
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    private ImageView mEmotion, mAssistToggle;
    private View mExtension, mEmotionToggle;
    private EditText mEditText;
    private InputMethodManager mInputMethodManager;
    private Button mSend;
    private ListView mListView;
    private ChatMsgViewAdapter mAdapter;
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();
    private DBUtils mDBUtils;
    private int mFriendUserId;
    private String mUserName;
    private View mMessageMore;
    private boolean isDisplayInputMethod;//键盘
    private boolean isShowExtension;//扩展(包括表情和插件)
    private boolean isDisplayPlugin;//插件
    private boolean isDisplayEmoji;//表情，三者（键盘、插件、表情）只能显示一个或1者都不显示，当键盘消失时需延时加载其他控件
    private static final int DISPLAY_INPUT_METHOD = 0;
    private static final int UNDISPLAY_INPUT_METHOD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_container);
        initView();
        setListener();
        initData();
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mFriendUserId = getIntent().getIntExtra(MessageFragment.FRIEND_USER_ID, 0);
        mUserName = getIntent().getStringExtra(MessageFragment.FRIEND_USER_NAME);
        actionBar.setTitle(mUserName);
    }

    private void initView() {
        mEmotion = $(R.id.chat_container_open_emotion);
        mAssistToggle = $(R.id.chat_container_open_assist_toggle);
        mEmotionToggle = $(R.id.chat_container_emotion);
        mExtension = $(R.id.chat_container_extension_container);
        mEditText = $(R.id.chat_container_text_ed);
        mSend = $(R.id.chat_container_send_btn);
        mListView = $(R.id.chat_container_list_item);
        mMessageMore = $(R.id.chat_container_message_more);
    }

    private void setListener() {
        mAssistToggle.setOnClickListener(this);
        mEmotion.setOnClickListener(this);
        mEditText.setOnFocusChangeListener(this);
        mEditText.setOnClickListener(this);
        mSend.setOnClickListener(this);
        mMessageMore.setOnClickListener(this);
        //用于判断当前屏幕是否显示了软键盘
        ((CustomListView) mListView).setOnResizeListener(new CustomListView.OnResizeListener() {
            @Override
            public void onResize(int w, int h, int oldw, int oldh) {
                isDisplayInputMethod = !isShowExtension;
            }
        });
    }

    private void initData() {
        //从本地读取聊天记录
//        mDataArrays = getLocalData(String.valueOf(mFriendUserId));

        mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mAdapter.getCount() - 1);
    }

    private void sendMessage() {
        String content = mEditText.getText().toString();
        if (content.length() > 0) {
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setMessage(content);
            entity.setComMsg(false);
            //TODO:本地存一份
//            insertLocalData(entity);
            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();
            mEditText.setText("");
            mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项
            //TODO:发送消息到服务器

            RecentChatEntity recentChatEntity = new RecentChatEntity();//TODO:用于加载到消息界面上

        }
    }

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_container_open_emotion:
                isDisplayEmoji = true;
                isDisplayPlugin = false;
                showExtension(true);
                isShowExtension = true;
                break;
            case R.id.chat_container_open_assist_toggle:

                isDisplayEmoji = false;
                isDisplayPlugin = !isDisplayPlugin;
                showExtension(isDisplayPlugin);

                isShowExtension = isDisplayPlugin;
                break;
            case R.id.chat_container_text_ed:
                isDisplayPlugin = false;
                isDisplayEmoji = false;
                mEditText.setFocusable(true);
                showExtension(false);
                isShowExtension = false;
                break;
            case R.id.chat_container_send_btn:
                sendMessage();
                if (isShowExtension) {
                    showExtension(false);
                }
                break;
            case R.id.chat_container_message_more:
                //查看更多消息
                break;
            default:
                break;
        }
    }

    private void showExtension(boolean isShow) {
        if (isShow) {
            mHandler.sendEmptyMessage(isDisplayInputMethod ? DISPLAY_INPUT_METHOD : UNDISPLAY_INPUT_METHOD);
        } else {
            mExtension.setVisibility(View.GONE);
            mEmotionToggle.setVisibility(View.GONE);
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DISPLAY_INPUT_METHOD:
                    mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showExtensionDetail();
                        }
                    }, 300);
                    break;
                case UNDISPLAY_INPUT_METHOD:
                    showExtensionDetail();
                    break;
                default:
                    break;
            }
        }
    };

    private void showExtensionDetail() {
        if (isDisplayPlugin) {
            mExtension.setVisibility(View.VISIBLE);
            mEmotionToggle.setVisibility(View.GONE);
        }
        if (isDisplayEmoji) {
            mEmotionToggle.setVisibility(View.VISIBLE);
            mExtension.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //加载表情Fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_container_emotion, EmojiconsFragment.newInstance(false))
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
//        showExtension(!hasFocus);
        switch (v.getId()) {
            case R.id.chat_container_text_ed:
//                if (!hasFocus)
//                    mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mEditText);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mEditText, emojicon);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (isShowExtension) {
                    isShowExtension = false;
                    showExtension(isShowExtension);
                }else {
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}

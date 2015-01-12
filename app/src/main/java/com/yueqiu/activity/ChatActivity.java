package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.adapter.ChatMsgViewAdapter;
import com.yueqiu.bean.ChatMsgEntity;
import com.yueqiu.bean.RecentChatEntity;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.db.DBUtils;
import com.yueqiu.fragment.chatbar.MessageFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doushuqi on 15/1/10.
 */
public class ChatActivity extends Activity implements View.OnClickListener, EditText.OnFocusChangeListener, EditText.OnEditorActionListener {

    private ImageView mEmotion, mAssistToggle;
    private View mExtension;
    private boolean isShowExtension = false;
    private EditText mEditText;
    private InputMethodManager mInputMethodManager;
    private Button mSend;
    private ListView mListView;
    private ChatMsgViewAdapter mAdapter;
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();
    private DBUtils mDBUtils;
    private int mFriendUserId;
    private String mUserName;

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
        mExtension = $(R.id.chat_container_extension_container);
        mEditText = $(R.id.chat_container_text_ed);
        mSend = $(R.id.chat_container_send_btn);
        mListView = $(R.id.chat_container_list_item);
    }

    private void setListener() {
        mAssistToggle.setOnClickListener(this);
        mEditText.setOnFocusChangeListener(this);
        mEditText.setOnClickListener(this);
        mSend.setOnClickListener(this);
    }

    private void initData() {
        //从本地读取聊天记录
        mDataArrays = getLocalData(String.valueOf(mFriendUserId));

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
            insertLocalData(entity);
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
            case R.id.chat_container_open_assist_toggle:
                showExtension(!isShowExtension);
                break;
            case R.id.chat_container_text_ed:
                mEditText.setFocusable(true);
                if (isShowExtension)
                    showExtension(false);
                break;
            case R.id.chat_container_send_btn:
                sendMessage();
                break;
            default:
                break;
        }
    }

    private void showExtension(boolean isShow) {
        if (isShow) {
            if (mInputMethodManager.isActive())
                mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            mExtension.setVisibility(View.VISIBLE);
            isShowExtension = true;
        } else {
            mExtension.setVisibility(View.GONE);
            isShowExtension = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
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

    private ArrayList<ChatMsgEntity> getLocalData(String userId) {
        mDBUtils = new DBUtils(this, DatabaseConstant.ChatMessageTable.CREATE_SQL);
        Log.e("ddd","==>" + DatabaseConstant.ChatMessageTable.CREATE_SQL);
        ArrayList<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();
        SQLiteDatabase db = mDBUtils.getReadableDatabase();
        String sql = "select * from " + DatabaseConstant.ChatMessageTable.TABLE + " where " + DatabaseConstant.ChatMessageTable.USER_ID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{userId});
        if (cursor != null && cursor.getCount() != 0) {
            for (cursor.moveToFirst(); cursor.isAfterLast(); cursor.moveToNext()) {
                String message = cursor.getString(cursor.getColumnIndex(DatabaseConstant.ChatMessageTable.MESSAGE_CONTENT));
                String date = cursor.getString(cursor.getColumnIndex(DatabaseConstant.ChatMessageTable.DATETIME));
                boolean isCome = !"0".equals(cursor.getString(cursor.getColumnIndex(DatabaseConstant.ChatMessageTable.IS_COME)));
                ChatMsgEntity entity = new ChatMsgEntity(message, date, isCome);
                list.add(entity);
            }
            cursor.close();
        }
        return list;
    }

    private void insertLocalData(ChatMsgEntity data) {
        mDBUtils = new DBUtils(this, DatabaseConstant.ChatMessageTable.CREATE_SQL);
        SQLiteDatabase db = mDBUtils.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DatabaseConstant.ChatMessageTable.USER_ID, mFriendUserId);
        values.put(DatabaseConstant.ChatMessageTable.IMG_URL, "");
        values.put(DatabaseConstant.ChatMessageTable.USERNAME, mUserName);
        values.put(DatabaseConstant.ChatMessageTable.MESSAGE_CONTENT, data.getMessage());
        values.put(DatabaseConstant.ChatMessageTable.DATETIME, "");
        values.put(DatabaseConstant.ChatMessageTable.IS_COME, data.isComMsg());

        db.insert(DatabaseConstant.ChatMessageTable.TABLE, null, values);
    }
}

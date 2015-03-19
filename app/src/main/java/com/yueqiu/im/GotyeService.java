package com.yueqiu.im;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeMessageType;
import com.gotye.api.GotyeNotify;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.gotye.api.listener.NotifyListener;
import com.yueqiu.ChatBarActivity;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.LoginActivity;
import com.yueqiu.util.AppUtil;


public class GotyeService extends Service implements NotifyListener {
    private static final String TAG = "GotyeService";
    public static final String ACTION_INIT = "gotye.action.init";
    public static final String ACTION_LOGIN = "gotye.action.login";
    private GotyeAPI api;

    // public static final String ACTION_RUN_BACKGROUND =
    // "gotye.action.run_in_background";
    // public static final String ACTION_RUN_ON_UI = "gotye.action.run_on_ui";

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        api = GotyeAPI.getInstance();
        api.setNetConfig("", -1);//use default config
        int code = api.init(getBaseContext(), YueQiuApp.APPKEY);
        api.addListener(this);
        api.beginRcvOfflineMessge();
        Log.d(TAG, "onCreate......");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand.....");
        if (intent != null) {
            if (ACTION_LOGIN.equals(intent.getAction())) {
                String name = intent.getStringExtra("name");
                String pwd = intent.getStringExtra("pwd");
                int code = api.login(name, pwd);
            } else if (ACTION_INIT.equals(intent.getAction())) {
                api.setNetConfig("", -1);
                GotyeAPI.getInstance().init(getBaseContext(), YueQiuApp.APPKEY);
            }
        }else {
            String[] user = LoginActivity.getUser(this);
            if (!TextUtils.isEmpty(user[0])) {
                int code = api.login(user[0], user[1]);
            }
        }
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved.....");
        GotyeAPI.getInstance().removeListener(this);
        Intent localIntent = new Intent();
        localIntent.setClass(this, GotyeService.class); // 銷毀時重新啟動Service
        this.startService(localIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy.....");
        GotyeAPI.getInstance().removeListener(this);
        Intent localIntent = new Intent();
        localIntent.setClass(this, GotyeService.class); // 銷毀時重新啟動Service
        this.startService(localIntent);
        super.onDestroy();
    }


    private void notify(String msg) {
        String currentActivityName = AppUtil.getCurrentActivityName(getBaseContext());
        if (currentActivityName.equals("com.yueqiu.im.ChatPage") ||
                currentActivityName.equals("com.yueqiu.ChatBarActivity")) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(true);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(msg);
        builder.setDefaults(Notification.DEFAULT_ALL);

        Intent intent = new Intent(this, ChatBarActivity.class);
        intent.putExtra("notify", 1);
        PendingIntent pendingIntent =TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(intent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        notificationManager.notify(0, builder.build());
    }

    /**
     * 这个回调方法是当接收到消息时的回调方法，主要作用是弹出notification
     *
     * @param code    状态码 参见 {@link GotyeStatusCode}
     * @param message 消息对象
     * @param unRead  是否已读
     */
    @Override
    public void onReceiveMessage(int code, GotyeMessage message, boolean unRead) {
        Log.d("wy"," service mReceive");
        String msg = null;

        if (message.getType() == GotyeMessageType.GotyeMessageTypeText) {
            msg = message.getSender().getName() + ":" + message.getText();
        } else if (message.getType() == GotyeMessageType.GotyeMessageTypeImage) {
            msg = message.getSender().getName() + ":图片消息";
        } else if (message.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
            msg = message.getSender().getName() + ":语音消息";
        } else if (message.getType() == GotyeMessageType.GotyeMessageTypeUserData) {
            msg = message.getSender().getName() + ":自定义消息";
        } else {
            msg = message.getSender().getName() + ":群邀请信息";
        }
        notify(msg);
    }

    @Override
    public void onSendMessage(int code, GotyeMessage message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReceiveNotify(int code, GotyeNotify notify) {
        String msg = notify.getSender().getName() + "邀请您加入群[";
        if (!TextUtils.isEmpty(notify.getFrom().getName())) {
            msg += notify.getFrom().getName() + "]";
        } else {
            msg += notify.getFrom().getId() + "]";
        }
        notify(msg);
    }

    @Override
    public void onRemoveFriend(int code, GotyeUser user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAddFriend(int i, GotyeUser gotyeUser) {

    }

    @Override
    public void onNotifyStateChanged() {
        // TODO Auto-generated method stub
    }
}

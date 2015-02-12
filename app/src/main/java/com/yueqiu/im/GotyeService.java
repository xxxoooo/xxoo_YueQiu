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
import com.yueqiu.util.AppUtil;


public class GotyeService extends Service implements NotifyListener {
    private static final String TAG = "GotyeService";
    public static final String ACTION_INIT = "gotye.action.init";

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_INIT.equals(intent.getAction())) {
                GotyeAPI.getInstance().init(getBaseContext(),
                        YueQiuApp.APPKEY, YueQiuApp.PACKAGENAME);
            }
        }
        GotyeAPI.getInstance().addListerer(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        GotyeAPI.getInstance().removeListener(this);
        GotyeAPI.getInstance().serviceDestoryNotify();
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
        builder.setSmallIcon(R.drawable.logo);
        builder.setAutoCancel(true);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(msg);
        builder.setDefaults(Notification.DEFAULT_ALL);

        Intent intent = new Intent(this, ChatBarActivity.class);
        intent.putExtra("notify", 1);
        PendingIntent pendingIntent =
                TaskStackBuilder.create(this)
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
        String msg = null;

        if (message.getType() == GotyeMessageType.GotyeMessageTypeText) {
            msg = message.getSender().name + ":" + message.getText();
        } else if (message.getType() == GotyeMessageType.GotyeMessageTypeImage) {
            msg = message.getSender().name + ":图片消息";
        } else if (message.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
            msg = message.getSender().name + ":语音消息";
        } else if (message.getType() == GotyeMessageType.GotyeMessageTypeUserData) {
            msg = message.getSender().name + ":自定义消息";
        } else {
            msg = message.getSender().name + ":群邀请信息";
        }
        notify(msg);
    }

    @Override
    public void onSendMessage(int code, GotyeMessage message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReceiveNotify(int code, GotyeNotify notify) {
        String msg = notify.getSender().name + "邀请您加入群[";
        if (!TextUtils.isEmpty(notify.getFrom().name)) {
            msg += notify.getFrom().name + "]";
        } else {
            msg += notify.getFrom().Id + "]";
        }
        notify(msg);
    }

    @Override
    public void onRemoveFriend(int code, GotyeUser user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAddFriend(int code, GotyeUser user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNotifyStateChanged() {
        // TODO Auto-generated method stub

    }
}

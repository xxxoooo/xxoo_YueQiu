package com.yueqiu.im;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeMessageType;
import com.gotye.api.GotyeUser;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;

import com.yueqiu.constant.HttpConstants;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.util.BitmapUtil;
import com.yueqiu.util.ImageCache;
import com.yueqiu.util.TimeUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;

import java.io.File;
import java.util.List;

public class ChatMessageAdapter extends BaseAdapter {

    public static final int TYPE_RECEIVE_TEXT = 0;
    public static final int TYPE_RECEIVE_IMAGE = 1;
    public static final int TYPE_RECEIVE_VOICE = 2;
    public static final int TYPE_RECEIVE_USER_DATA = 3;

    public static final int TYPE_SEND_TEXT = 4;
    public static final int TYPE_SEND_IMAGE = 5;
    public static final int TYPE_SEND_VOICE = 6;
    public static final int TYPE_SEND_USER_DATA = 7;

    public static final int MESSAGE_DIRECT_RECEIVE = 1;
    public static final int MESSAGE_DIRECT_SEND = 0;

    private final ChatPage chatPage;
    private List<GotyeMessage> messageList;

    private LayoutInflater inflater;
    private String currentLoginName;

    private GotyeAPI api;
    private Handler mHandler = new Handler();
    private int checkImgMsgCount;//点击图片消息查看大图,计次


    private String mReceiveImgUrl;

    private ImageLoader mImageLoader;

    public ChatMessageAdapter(ChatPage activity, List<GotyeMessage> messageList) {
        this.chatPage = activity;
        this.messageList = messageList;
        inflater = activity.getLayoutInflater();
        api = GotyeAPI.getInstance();
        currentLoginName = api.getCurrentLoginUser().getName();

        mImageLoader = VolleySingleton.getInstance().getImgLoader();//volley

    }

    public void addMsgToBottom(GotyeMessage msg) {
        messageList.add(msg);
        notifyDataSetChanged();
    }

    public void updateMessage(GotyeMessage msg) {
        int position = messageList.indexOf(msg);
        if (position < 0) {
            return;
        }
        messageList.remove(position);
        messageList.add(position, msg);
        notifyDataSetChanged();
    }

    public void updateChatMessage(GotyeMessage msg) {
        if (messageList.contains(msg)) {
            int index = messageList.indexOf(msg);
            messageList.remove(index);
            messageList.add(index, msg);
            notifyDataSetChanged();
        }
    }

    public void addMessagesToTop(List<GotyeMessage> histMessages) {
        messageList.addAll(0, histMessages);
    }

    public void addMessageToTop(GotyeMessage msg) {
        messageList.add(0, msg);
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public GotyeMessage getItem(int position) {
        return position >= 0 ? messageList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        GotyeMessage message = getItem(position);
        if (message.getType() == GotyeMessageType.GotyeMessageTypeText) {
            return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_TEXT
                    : TYPE_SEND_TEXT;
        }
        if (message.getType() == GotyeMessageType.GotyeMessageTypeImage) {
            return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_IMAGE
                    : TYPE_SEND_IMAGE;

        }
        if (message.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
            return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_VOICE
                    : TYPE_SEND_VOICE;
        }
        if (message.getType() == GotyeMessageType.GotyeMessageTypeUserData) {
            return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_USER_DATA
                    : TYPE_SEND_USER_DATA;
        }
        return -1;// invalid
    }

    public int getViewTypeCount() {
        return 8;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final GotyeMessage message = getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByMessage(message, position);
            if (message.getType() == GotyeMessageType.GotyeMessageTypeImage) {
                holder.iv = ((ImageView) convertView.findViewById(R.id.iv_sendPicture));
                holder.head_iv = (CustomNetWorkImageView) convertView.findViewById(R.id.iv_userhead);
                holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
            } else if (message.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
                holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
                holder.head_iv = (CustomNetWorkImageView) convertView.findViewById(R.id.iv_userhead);
                holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
                holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
            } else {
                holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                holder.head_iv = (CustomNetWorkImageView) convertView.findViewById(R.id.iv_userhead);
                // 这里是文字内容
                holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (holder.tv_userId != null) {
            String name ;
            GotyeUser user = api.requestUserInfo(message.getSender().getName(),true);
            String nick;

            if(user.getNickname() != null){
                String nicknameStr = user.getNickname();
                int splitIndex = nicknameStr.lastIndexOf("|");
                if(splitIndex != -1) {
                    nick = nicknameStr.substring(0, splitIndex);
                    mReceiveImgUrl = nicknameStr.substring(splitIndex + 1);
                }else{
                    nick = nicknameStr;
                }
                if(TextUtils.isEmpty(nick)){
                    name = user.getName();
                }else{
                    name = nick;
                }
            }else{
                name = user.getName();
            }
            holder.tv_userId.setText(name);
        }

        switch (message.getType()) {
            // 根据消息type显示item
            case GotyeMessageTypeImage: // 图片
                handleImageMessage(message, holder, position, convertView);
                break;
            case GotyeMessageTypeAudio: // 语音
                //handleVoiceMessage(message, holder, position, convertView);
                break;
            default:
                handleTextMessage(message, holder, position);
                break;
        }

        TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);

        if (position == 0) {
            timestamp.setText(TimeUtil.dateToMessageTime(message.getDate() * 1000));
            timestamp.setVisibility(View.VISIBLE);
        } else {
            //两条消息时间离得如果稍长，显示时间
            if (TimeUtil.needShowTime(message.getDate(), messageList.get(position- 1).getDate())) {
                timestamp.setText(TimeUtil.toLocalTimeString(message.getDate() * 1000));
                timestamp.setVisibility(View.VISIBLE);
            } else {
                timestamp.setVisibility(View.GONE);
            }
        }

        if(getDirect(message) == MESSAGE_DIRECT_SEND){
            ((CustomNetWorkImageView)holder.head_iv).setDefaultImageResId(R.drawable.default_head);
            ((CustomNetWorkImageView)holder.head_iv).setImageUrl("http://" + YueQiuApp.sUserInfo.getImg_url(),mImageLoader);
        }else {
//            setIcon(holder.head_iv, message.getSender().name);
            ((CustomNetWorkImageView)holder.head_iv).setDefaultImageResId(R.drawable.default_head);
            ((CustomNetWorkImageView)holder.head_iv).setImageUrl(HttpConstants.IMG_BASE_URL +  mReceiveImgUrl,mImageLoader);
            Log.d("cao","message receiver img_url ->" + mReceiveImgUrl);
        }

        return convertView;
    }

//    private void setUserPhoto(ViewHolder holder, String userName) {
//        holder.head_iv.setDefaultImageResId(R.drawable.default_head);
//        holder.head_iv.setErrorImageResId(R.drawable.default_head);
//        String url = getUserPhotoUrl(userName);
//        holder.head_iv.setImageUrl(url, mImageLoader);
//    }

//    private String getUserPhotoUrl(String userName) {
//        if (userName.equals(currentLoginName)) {
//            UserInfo userInfo = DaoFactory.getUser(chatPage)
//                    .getUserByUserId(String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
//            return "http://" + userInfo.getImg_url();
//        } else {
//            String photoName = DaoFactory.getContacts(chatPage).getContactByName(userName).getImg_url();
//            return HttpConstants.IMG_BASE_URL + photoName;
//        }
//    }

    /**
     * 处理图片类型item的方法
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleImageMessage(final GotyeMessage message,
                                    final ViewHolder holder, final int position, View convertView) {
        holder.iv.setImageResource(android.R.drawable.ic_menu_gallery);

        //TODO:主要看这个方法
        setImageMessage(holder.iv, message, holder);
//        setIcon(holder.head_iv, message.gotyeMessage.getSender().name);

        if (getDirect(message) == MESSAGE_DIRECT_SEND) {
            switch (message.getStatus()) {
                case GotyeMessage.STATUS_SENT: // 发送成功
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                case GotyeMessage.STATUS_SENDFAILED: // 发送失败
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.VISIBLE);
                    break;
                case GotyeMessage.STATUS_SENDING: // 发送中
                    holder.pb.setVisibility(View.VISIBLE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                default:
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 处理文本信息的item
     *
     * @param message
     * @param holder
     * @param position
     */
    private void handleTextMessage(GotyeMessage message, ViewHolder holder,final int position) {
        // 设置内容
        String extraData = message.getExtraData() == null ? null : new String(message.getExtraData());
        if (extraData != null) {
            if (message.getType() == GotyeMessageType.GotyeMessageTypeText) {
                holder.tv.setText(message.getText() + "\n额外数据：" + extraData);
            } else {
                holder.tv.setText("自定义消息：" + new String(message.getUserData()) + "\n额外数据：" + extraData);
            }
        } else {
            if (message.getType() == GotyeMessageType.GotyeMessageTypeText) {
                holder.tv.setText(message.getText());
            } else {
                holder.tv.setText("自定义消息：" + new String(message.getUserData()));
            }
        }

        // 设置长按事件监听
        if (getDirect(message) == MESSAGE_DIRECT_SEND) {
            switch (message.getStatus()) {
                case GotyeMessage.STATUS_SENT: // 发送成功
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                case GotyeMessage.STATUS_SENDFAILED: // 发送失败
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.VISIBLE);
                    break;
                case GotyeMessage.STATUS_SENDING: // 发送中
                    holder.pb.setVisibility(View.VISIBLE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                default:
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.GONE);
            }
        }
    }

    private View createViewByMessage(GotyeMessage message, int position) {
        switch (message.getType()) {
            case GotyeMessageTypeImage:
                return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? inflater
                        .inflate(R.layout.layout_row_received_picture, null) : inflater
                        .inflate(R.layout.layout_row_sent_picture, null);

            case GotyeMessageTypeAudio:
                return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? inflater
                        .inflate(R.layout.layout_row_received_voice, null) : inflater
                        .inflate(R.layout.layout_row_sent_voice, null);
            case GotyeMessageTypeUserData:
                return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? inflater
                        .inflate(R.layout.layout_row_received_message, null) : inflater
                        .inflate(R.layout.layout_row_sent_message, null);
            default:
                return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? inflater
                        .inflate(R.layout.layout_row_received_message, null) : inflater
                        .inflate(R.layout.layout_row_sent_message, null);
        }
    }

    private ImageCache cache = ImageCache.getInstance();

//    private void setIcon(NetworkImageView iconView, String name) {
//        Bitmap bmp = cache.get(name);
//        if (bmp != null) {
//            iconView.setImageBitmap(bmp);
//        } else {
//            GotyeUser user = api.requestUserInfo(name, false);
//            if (user != null && user.getIcon() != null) {
//                bmp = cache.get(user.getIcon().path);
//                if (bmp != null) {
//                    iconView.setImageBitmap(bmp);
//                    cache.put(name, bmp);
//                } else {
//                    bmp = BitmapUtil.getBitmap(user.getIcon().getPath());
//                    if (bmp != null) {
//                        iconView.setImageBitmap(bmp);
//                        cache.put(name, bmp);
//                    } else {
//                        iconView.setImageResource(R.drawable.default_head);
//                        int code = api.downloadMedia(user.getIcon().url);
//                    }
//                }
//            } else {
//                iconView.setImageResource(R.drawable.default_head);
//            }
//        }
//    }


    //TODO:在handleImageMessage里调用
    private void setImageMessage(ImageView msgImageView,final GotyeMessage msg, ViewHolder holder) {
        Bitmap cacheBm = cache.get(msg.getMedia().getPath());
        if (cacheBm != null) {
            msgImageView.setImageBitmap(cacheBm);
            holder.pb.setVisibility(View.GONE);
        } else if (msg.getMedia().getPath() != null) {
            Bitmap bm = BitmapUtil.getBitmap(msg.getMedia().getPath());
            if (bm != null) {
                msgImageView.setImageBitmap(bm);
                cache.put(msg.getMedia().getPath(), bm);
            }
            holder.pb.setVisibility(View.GONE);
        }
        msgImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                final Intent intent = new Intent(chatPage, ShowBigImage.class);
                String path = msg.getMedia().getPath_ex();
                if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                    Uri uri = Uri.fromFile(new File(path));
                    intent.putExtra(ShowBigImage.EXTRA_URI, uri);
                    chatPage.startActivity(intent);
                } else {
                    if (!Utils.networkAvaiable(chatPage)) {
                        Utils.showToast(chatPage, chatPage.getString(R.string.network_not_available));
                        return;
                    }
                    int code = api.downloadMessage(msg);
                    chatPage.startActivity(intent);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (checkImgMsgCount > 10) {
                                intent.putExtra(ShowBigImage.EXTRA_TIMEOUT, true);
                                chatPage.startActivity(intent);
                                checkImgMsgCount = 0;
                            } else {
                                checkImgMsgCount += 1;
                                onClick(v);
                            }
                        }
                    }, 200);
                }

            }
        });

    }

    private int getDirect(GotyeMessage message) {
        if (message.getSender().getName().equals(currentLoginName)) {
            return MESSAGE_DIRECT_SEND;
        } else {
            return MESSAGE_DIRECT_RECEIVE;
        }
    }

    public void downloadDone(GotyeMessage msg) {
        if (msg.getType() == GotyeMessageType.GotyeMessageTypeImage) {
            // if (TextUtils.isEmpty(msg.getMedia().getPath_ex())) {
            // ToastUtil.show(chatPage, "图片下载失败");
            // return;
            // }
        }
        if (messageList.contains(msg)) {
            int index = messageList.indexOf(msg);
            messageList.remove(index);
            messageList.add(index, msg);
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        CustomNetWorkImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;

        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;
    }

    public void refreshData(List<GotyeMessage> list) {
        // TODO Auto-generated method stub
        this.messageList = list;
        notifyDataSetChanged();
    }
}

package com.yueqiu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.bean.ChatMsgEntity;

import java.util.List;

/**
 * 消息ListView的Adapter
 * 
 * @author doushuqi
 */
public class ChatMsgViewAdapter extends BaseAdapter {

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;// 收到对方的消息
		int IMVT_TO_MSG = 1;// 自己发送出去的消息
	}

	private static final int ITEM_COUNT = 2;// 消息类型的总数
	private List<ChatMsgEntity> coll;// 消息对象数组
	private LayoutInflater mInflater;

	public ChatMsgViewAdapter(Context context, List<ChatMsgEntity> coll) {
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * 得到Item的类型，是对方发过来的消息，还是自己发送出去的
	 */
	public int getItemViewType(int position) {
		ChatMsgEntity entity = coll.get(position);

		if (entity.isComMsg()) {// 收到的消息
			return IMsgViewType.IMVT_COM_MSG;
		} else {// 自己发送的消息
			return IMsgViewType.IMVT_TO_MSG;
		}
	}

	/**
	 * Item类型的总数
	 */
	public int getViewTypeCount() {
		return ITEM_COUNT;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ChatMsgEntity entity = coll.get(position);
		boolean isComMsg = entity.isComMsg();

		ViewHolder viewHolder = null;
		if (convertView == null) {
			if (isComMsg) {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_left, null);
			} else {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_right, null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.chat_details_receive_time_tv);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.chat_details_receive_chatcontent);
			viewHolder.icon = (ImageView) convertView
					.findViewById(R.id.chat_details_receive_userhead);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tvSendTime.setText(entity.getDate());
//		viewHolder.tvUserName.setText(entity.getName());
		viewHolder.tvContent.setText(entity.getMessage());
//		viewHolder.icon.setImageResource(imgs[entity.getImg()]);
		return convertView;
	}

	static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public ImageView icon;
		public boolean isComMsg = true;
	}

}

package com.yueqiu.adapter;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatTarget;
import com.gotye.api.GotyeChatTargetType;
import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeMessageType;
import com.gotye.api.GotyeRoom;
import com.gotye.api.GotyeUser;
import com.yueqiu.R;
import com.yueqiu.fragment.chatbar.MessageFragment;
import com.yueqiu.util.ImageCache;
import com.yueqiu.util.TimeUtil;

import java.util.List;

public class MessageListAdapter extends BaseAdapter {
	private MessageFragment messageFragment;
	private List<GotyeChatTarget> sessions;
	private GotyeAPI api;

	public MessageListAdapter(MessageFragment messageFragment,
                              List<GotyeChatTarget> sessions) {
		this.messageFragment = messageFragment;
		this.sessions = sessions;
		api = GotyeAPI.getInstance();
	}

	static class ViewHolder {
		ImageView icon;
		TextView title, content, time, count;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return sessions.size();
	}

	@Override
	public GotyeChatTarget getItem(int arg0) {
		// TODO Auto-generated method stub
		return sessions.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		GotyeChatTarget t = sessions.get(position);
		if (t.name.equals(MessageFragment.fixName)) {
			return 0;
		} else {
			return 1;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int arg0, View view, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (view == null) {
			view = LayoutInflater.from(messageFragment.getActivity()).inflate(
					R.layout.item_delete, null);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
			viewHolder.title = (TextView) view.findViewById(R.id.title_tx);
			viewHolder.content = (TextView) view.findViewById(R.id.content_tx);
			viewHolder.time = (TextView) view.findViewById(R.id.time_tx);
			viewHolder.count = (TextView) view.findViewById(R.id.count);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		final GotyeChatTarget session =  getItem(arg0);
		Log.e("MessageListAdapter init view", "session = " + session);
		if (getItemViewType(arg0)==0) {
			viewHolder.title.setText("验证消息");//session.name
			viewHolder.content.setVisibility(View.GONE);
			viewHolder.icon.setImageResource(R.drawable.message);
			viewHolder.time.setVisibility(View.GONE);
			int count = api.getUnreadNotifyCount();
			if (count > 0) {
				viewHolder.count.setVisibility(View.VISIBLE);
				viewHolder.count.setText(String.valueOf(count));
			} else {
				viewHolder.count.setVisibility(View.GONE);
			}

		} else {
			String title = "", content = "";
			viewHolder.content.setVisibility(View.VISIBLE);
			GotyeMessage lastMsg = api.getLastMessage(session);
            Log.e("ddd", "lastMsg  = " + lastMsg );
			String lastMsgTime = TimeUtil
					.dateToMessageTime(lastMsg.getDate() * 1000);
			viewHolder.time.setText(lastMsgTime);
			setIcon(viewHolder.icon, session);
			if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeText) {
				content = "文本消息：" + lastMsg.getText();
			} else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeImage) {
				content = "图片消息";
			} else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
				content = "语音消息";
			} else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeUserData) {
				content = "自定义消息";
			} else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeInviteGroup) {
				content = "邀请消息";
			}

			if (session.type == GotyeChatTargetType.GotyeChatTargetTypeUser) {
				GotyeUser user = api.requestUserInfo(session.name, false);
				if (user != null) {
					if (TextUtils.isEmpty(user.getNickname())) {
						title = "好友：" + user.name;
					} else {
						title = "好友：" + user.getNickname();
					}
				} else {
					title = "好友：" + session.name;
				}
			} else if (session.type == GotyeChatTargetType.GotyeChatTargetTypeRoom) {
				GotyeRoom room = api.requestRoomInfo(session.Id, false);
				if (room != null) {
					if (TextUtils.isEmpty(room.getRoomName())) {
						title = "聊天室：" + room.Id;
					} else {
						title = "聊天室：" + room.getRoomName();
					}
				} else {
					title = "聊天室：" + session.Id;
				}

			} else if (session.type == GotyeChatTargetType.GotyeChatTargetTypeGroup) {
				GotyeGroup group = api.requestGroupInfo(session.Id, false);
				if (group != null) {
					if (TextUtils.isEmpty(group.getGroupName())) {
						title = "群：" + group.Id;
					} else {
						title = "群：" + group.getGroupName();
					}
				} else {
					title = "群：" + session.Id;
				}

			}
			viewHolder.title.setText(title);
			viewHolder.content.setText(content);
			int count = api.getUnreadMsgcounts(session);
			if (count > 0) {
				viewHolder.count.setVisibility(View.VISIBLE);
				viewHolder.count.setText(String.valueOf(count));
			} else {
				viewHolder.count.setVisibility(View.GONE);
			}
		}
		return view;
	}

	private void setIcon(ImageView imgView, GotyeChatTarget target) {
		if (target.type == GotyeChatTargetType.GotyeChatTargetTypeUser) {
			GotyeUser user = api.requestUserInfo(target.name, true);
            Log.e("ddd", "user = " + user);
			if (user == null) {
				return;
			} else if (user.getIcon() != null) {
                Log.e("ddd", " image url = " + user.getIcon().getUrl() + "   image path" + user.getIcon().getPath());
				ImageCache.getInstance().setIcom(imgView,
						user.getIcon().getPath(), user.getIcon().getUrl());
			}
		} else if (target.type == GotyeChatTargetType.GotyeChatTargetTypeRoom) {
			GotyeRoom room = api.requestRoomInfo(target.Id, false);
			if (room != null && room.getIcon() != null) {
				ImageCache.getInstance().setIcom(imgView,
						room.getIcon().getPath(), room.getIcon().getUrl());
			}
		} else {
			GotyeGroup group = api.requestGroupInfo(target.Id, false);
			if (group == null) {
				return;
			} else if (group.getIcon() != null) {
				ImageCache.getInstance().setIcom(imgView,
						group.getIcon().getPath(), group.getIcon().getUrl());
			}
		}

	}

	public void setData(List<GotyeChatTarget> sessions) {
		// TODO Auto-generated method stub
		this.sessions = sessions;
		notifyDataSetChanged();
	}
}

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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatTarget;
import com.gotye.api.GotyeChatTargetType;
import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeMessageType;
import com.gotye.api.GotyeRoom;
import com.gotye.api.GotyeUser;
import com.yueqiu.R;
import com.yueqiu.bean.FriendsApplication;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.chatbar.MessageFragment;
import com.yueqiu.util.ImageCache;
import com.yueqiu.util.TimeUtil;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;


import java.util.List;

public class MessageListAdapter extends BaseAdapter {
	private MessageFragment mMessageFragment;
	private List<GotyeChatTarget> sessions;
	private GotyeAPI api;
    private String mContent;
    private ImageLoader mImageLoader;
    private Verification mVerification;

	public MessageListAdapter(MessageFragment mMessageFragment,List<GotyeChatTarget> sessions
                              ) {
		this.mMessageFragment = mMessageFragment;
		this.sessions = sessions;
		this.api = GotyeAPI.getInstance();

        mImageLoader = VolleySingleton.getInstance().getImgLoader();
	}
    public MessageListAdapter(){}

    public void setVertification(Verification vertification){
        this.mVerification = vertification;
        this.notifyDataSetChanged();
    }

    public Verification getVerification() {
        return mVerification;
    }

    static class ViewHolder {
        CustomNetWorkImageView icon;
		ImageView state_icon;
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
		return sessions.get(arg0 );
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
		if (t.getName().equals(MessageFragment.fixName)) {
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
			view = LayoutInflater.from(mMessageFragment.getActivity()).inflate(
					R.layout.item_delete, null);
			viewHolder = new ViewHolder();
			viewHolder.icon = (CustomNetWorkImageView) view.findViewById(R.id.icon);

            viewHolder.state_icon = (ImageView) view.findViewById(R.id.message_state_icon);
			viewHolder.title = (TextView) view.findViewById(R.id.title_tx);
			viewHolder.content = (TextView) view.findViewById(R.id.content_tx);
			viewHolder.time = (TextView) view.findViewById(R.id.time_tx);
			viewHolder.count = (TextView) view.findViewById(R.id.count);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		final GotyeChatTarget session = getItem(arg0);
		Log.e("MessageListAdapter init view", "session = " + session);
		if (getItemViewType(arg0)==0) {
			viewHolder.title.setText(getVerification().title);//session.name

            if(getVerification().newFriend == null) {
                viewHolder.content.setVisibility(View.GONE);
                viewHolder.time.setVisibility(View.GONE);
            }else{
                viewHolder.content.setVisibility(View.VISIBLE);
                viewHolder.content.setText(mMessageFragment.getActivity().getString(R.string.ask_for_friend,getVerification().newFriend.getUsername()));
                viewHolder.time.setVisibility(View.VISIBLE);
                viewHolder.time.setText(getVerification().newFriend.getCreate_time());

            }
			viewHolder.icon.setDefaultImageResId(R.drawable.message);
            if(getVerification().hasNewMsg) {
                viewHolder.state_icon.setVisibility(View.VISIBLE);
            }else {
                viewHolder.state_icon.setVisibility(View.GONE);
            }

			int count = api.getUnreadNotifyCount();
			if (count > 0) {
				viewHolder.count.setVisibility(View.VISIBLE);
				viewHolder.count.setText(String.valueOf(count));
//                viewHolder.state_icon.setVisibility(View.VISIBLE);
			} else {
				viewHolder.count.setVisibility(View.GONE);
//                viewHolder.state_icon.setVisibility(View.GONE);
			}

		} else {
			String title = "", content = "",img_url="",nicknameStr,nick;
			viewHolder.content.setVisibility(View.VISIBLE);
            viewHolder.state_icon.setVisibility(View.GONE);

			GotyeMessage lastMsg = api.getLastMessage(session);
			String lastMsgTime = TimeUtil.dateToMessageTime(lastMsg.getDate() * 1000);
			viewHolder.time.setText(lastMsgTime);
//			setIcon(viewHolder.icon, session);
			if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeText) {
//				content = mMessageFragment.getString(R.string.text_msg) + lastMsg.getText();
                content = lastMsg.getText();
			} else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeImage) {
				content = mMessageFragment.getString(R.string.image_msg);
			} else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
				content = mMessageFragment.getString(R.string.voice_msg);
			} else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeUserData) {
				content = mMessageFragment.getString(R.string.custom_msg);
			} else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeInviteGroup) {
				content = mMessageFragment.getString(R.string.invent_msg);
			}

			if (session.getType() == GotyeChatTargetType.GotyeChatTargetTypeUser) {
				GotyeUser user = api.requestUserInfo(session.getName(), true);
                Log.d("cao","user is ->"+ user);
                nicknameStr = user.getNickname();
                if(!TextUtils.isEmpty(nicknameStr)) {
                    Log.d("wy", "message adapter nickStr ->" + nicknameStr);
                    int splitIndex = nicknameStr.lastIndexOf("|");
                    if (splitIndex != -1) {
                        nick = nicknameStr.substring(0, splitIndex);
                        img_url = nicknameStr.substring(splitIndex + 1);

                        Log.d("cao", "message adapter nick is -> " + nick);
                    } else {
                        nick = nicknameStr;
                    }

                    if (user != null) {
                        if (TextUtils.isEmpty(nick)) {
                            title = mMessageFragment.getString(R.string.good_friend) + user.getName();
                            Log.d("cao","message adapter user nick is null");
                        } else {
                            title = mMessageFragment.getString(R.string.good_friend) + nick;
                        }
                        Log.d("cao","message adapter img_url ->" + img_url);
                    } else {
                        title = mMessageFragment.getString(R.string.good_friend)+ session.getName();
                    }
                }else{
                    if (user != null) {
                       title = mMessageFragment.getString(R.string.good_friend) + user.getName();
                    } else {
                        title = mMessageFragment.getString(R.string.good_friend)+ session.getName();
                    }
                }


			} else if (session.getType() == GotyeChatTargetType.GotyeChatTargetTypeRoom) {
				GotyeRoom room = api.requestRoomInfo(session.getId(), false);
				if (room != null) {
					if (TextUtils.isEmpty(room.getRoomName())) {
						title = mMessageFragment.getString(R.string.chatbar_room) + room.getId();
					} else {
						title = mMessageFragment.getString(R.string.chatbar_room) + room.getRoomName();
					}
				} else {
					title = mMessageFragment.getString(R.string.chatbar_room) + session.getId();
				}

			} else if (session.getType() == GotyeChatTargetType.GotyeChatTargetTypeGroup) {
				GotyeGroup group = api.requestGroupInfo(session.getId(), false);
				if (group != null) {
					if (TextUtils.isEmpty(group.getGroupName())) {
						title = mMessageFragment.getString(R.string.chatbar_group) + group.getId();
					} else {
						title = mMessageFragment.getString(R.string.chatbar_group) + group.getGroupName();
					}
				} else {
					title = mMessageFragment.getString(R.string.chatbar_group) + session.getId();
				}

			}
			viewHolder.title.setText(title);
			viewHolder.content.setText(content);
            viewHolder.icon.setDefaultImageResId(R.drawable.default_head);
            viewHolder.icon.setImageUrl(HttpConstants.IMG_BASE_URL  + img_url,mImageLoader);
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

//    private void setUserPhoto(NetworkImageView imgView, GotyeChatTarget target) {
//        String user = target.getName();
//        String photoName = DaoFactory.getContacts(mMessageFragment.getActivity()).getContactByName(user).getImg_url();
//        String url = HttpConstants.IMG_BASE_URL + photoName;
//        imgView.setDefaultImageResId(R.drawable.default_head);
//        imgView.setErrorImageResId(R.drawable.default_head);
//        imgView.setImageUrl(url, mImageLoader);
//    }

	private void setIcon(NetworkImageView imgView, GotyeChatTarget target) {
		if (target.getType() == GotyeChatTargetType.GotyeChatTargetTypeUser) {
			GotyeUser user = api.requestUserInfo(target.getName(), true);
			if (user == null) {
				return;
			} else if (user.getIcon() != null) {
				ImageCache.getInstance().setIcom(imgView,
						user.getIcon().getPath(), user.getIcon().getUrl());

			}
		} else if (target.getType() == GotyeChatTargetType.GotyeChatTargetTypeRoom) {
			GotyeRoom room = api.requestRoomInfo(target.getId(), false);
			if (room != null && room.getIcon() != null) {
				ImageCache.getInstance().setIcom(imgView,
						room.getIcon().getPath(), room.getIcon().getUrl());
			}
		} else {
			GotyeGroup group = api.requestGroupInfo(target.getId(), false);
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

    public static class Verification {
        public boolean hasNewMsg;

        public FriendsApplication newFriend;

        public String title;

        public int state;
    }
}

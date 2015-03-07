package com.gotye.api;

import com.yueqiu.bean.FriendsApplication;

import java.io.Serializable;

/**
 * 聊天对象父类
 * @author gotye
 *
 */
public class GotyeChatTarget implements Serializable{
	
	/**
	 * 聊天室ID或群ID
	 */
	public long Id;
	
	/**
	 * 用户名、聊天室名或群名
	 */
	public String name;
	
//	public GotyeChatTargetType type;
	
//	public GotyeTargetable(GotyeChatTargetType t){
//		type = t;
//	}
	/**
	 * 类型 
	 */
	public GotyeChatTargetType type;

    public boolean hasNewMsg;

    public FriendsApplication newFriend;

    public String title;
	
}

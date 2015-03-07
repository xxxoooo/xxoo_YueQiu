package com.yueqiu.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatTarget;
import com.gotye.api.GotyeMessage;
import com.yueqiu.im.ChatPage;

import java.io.File;

public class SendImageMessageTask extends AsyncTask<String, String, String> {

	public static final int IMAGE_MAX_SIZE_LIMIT = 100;
	private GotyeMessage createMessage;
	private GotyeChatTarget target;
	private ChatPage chatPage;

	private String bigImagePath;

	public SendImageMessageTask(ChatPage chatPage, GotyeChatTarget target) {
		this.target = target;
		this.chatPage = chatPage;
	}

	@Override
	protected String doInBackground(String... arg0) {
		File f = new File(arg0[0]);
		if (!f.exists()) {
			return null;
		}
		if (f.length() < 1000) {
			if (BitmapUtil.checkCanSend(f.getAbsolutePath())) {
				return f.getAbsolutePath();
			} else {
				return BitmapUtil.saveBitmapFile(BitmapUtil.getSmallBitmap(
						f.getAbsolutePath(), 500, 500));
			}
		} else {
			Bitmap bmp = BitmapUtil.getSmallBitmap(f.getAbsolutePath(), 500,
					500);
			if (bmp != null) {
				return BitmapUtil.saveBitmapFile(bmp);
			}
		}

		return null;
	}

	private void sendImageMessage(String imagePath) {
		createMessage = GotyeMessage.createImageMessage(GotyeAPI.getInstance()
                .getCurrentLoginUser(), target, imagePath);
		createMessage.getMedia().setPath_ex(imagePath);
        Log.e("ddd", "sending message: createmessage = " + createMessage);
		int code = GotyeAPI.getInstance().sendMessage(createMessage);
        Log.e("ddd", "chat page send a image result: code = " + code);
	}

	@Override
	protected void onPostExecute(String result) {
		Log.e("ddd", "task onPostExecute: result = " + result);
		if (result == null) {
			Utils.showToast(chatPage, "请发送jpg图片");
			return;
		}
		sendImageMessage(result);
		if (createMessage == null) {
            Utils.showToast(chatPage, "图片消息发送失败");
		} else {
            Log.e("ddd", "send a image success:");
			createMessage.getMedia().setPath_ex(bigImagePath);
			chatPage.callBackSendImageMessage(createMessage);
		}
		super.onPostExecute(result);
	}
}

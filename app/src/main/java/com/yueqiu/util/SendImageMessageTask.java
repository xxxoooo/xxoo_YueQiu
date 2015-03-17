package com.yueqiu.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatTarget;
import com.gotye.api.GotyeMessage;
import com.yueqiu.R;
import com.yueqiu.im.ChatPage;

import java.io.File;

public class SendImageMessageTask extends AsyncTask<String, String, String> {

	public static final int IMAGE_MAX_SIZE_LIMIT = 100;
	private GotyeMessage mCreateMessage;
	private GotyeChatTarget mTarget;
	private ChatPage mChatPage;

	private String mBigImagePath;
    private String mSmallImagePath;
    private File mFile;

	public SendImageMessageTask(ChatPage chatPage, GotyeChatTarget target) {
		this.mTarget = target;
		this.mChatPage = chatPage;
	}

	@Override
	protected String doInBackground(String... arg0) {
		mFile = new File(arg0[0]);
		if (!mFile.exists()) {
			return null;
		}
		if (mFile.length() < 1000) {
			if (BitmapUtil.checkCanSend(mFile.getAbsolutePath())) {
				return mFile.getAbsolutePath();
			} else {
				return BitmapUtil.saveBitmapFile(BitmapUtil.getSmallBitmap(
                        mFile.getAbsolutePath(), 500, 500));
			}
		} else {
			Bitmap bmp = BitmapUtil.getSmallBitmap(mFile.getAbsolutePath(), 500,
					500);
			if (bmp != null) {
				return BitmapUtil.saveBitmapFile(bmp);
			}
		}

		return null;
	}

	private void sendImageMessage(String imagePath) {
        mBigImagePath = imagePath;
        Bitmap smallBimap = BitmapUtil.getSmallBitmap(mFile.getAbsolutePath(),50,50);
        mSmallImagePath = BitmapUtil.saveSmallBitmapFile(smallBimap);
		mCreateMessage = GotyeMessage.createImageMessage(GotyeAPI.getInstance()
                .getCurrentLoginUser(), mTarget, imagePath);
        mCreateMessage.getMedia().setPath(mSmallImagePath);
		mCreateMessage.getMedia().setPath_ex(imagePath);
        Log.e("cao", "sending message: mCreateMessage = " + mCreateMessage);
		int code = GotyeAPI.getInstance().sendMessage(mCreateMessage);
        Log.e("cao", "chat page send a image result: code = " + code);
	}

	@Override
	protected void onPostExecute(String result) {
		if (result == null) {
			Utils.showToast(mChatPage, mChatPage.getString(R.string.please_send_jpg));
			return;
		}
		sendImageMessage(result);
		if (mCreateMessage == null) {
            Utils.showToast(mChatPage, mChatPage.getString(R.string.image_send_fail));
		} else {
            mCreateMessage.getMedia().setPath(mSmallImagePath);
			mCreateMessage.getMedia().setPath_ex(mBigImagePath);
			mChatPage.callBackSendImageMessage(mCreateMessage);
		}
		super.onPostExecute(result);
	}
}

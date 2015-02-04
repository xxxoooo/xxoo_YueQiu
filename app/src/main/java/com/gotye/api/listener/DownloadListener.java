package com.gotye.api.listener;

public interface DownloadListener  extends GotyeListener {
	/**
	 * 下载图片回调
	 * @param code 状态码 参见 {@link com.gotye.api.GotyeStatusCode}
	 * @param path 下载完成后保存路径
	 * @param url 对应下载的Url
	 */
	  void onDownloadMedia(int code, String path, String url);
}

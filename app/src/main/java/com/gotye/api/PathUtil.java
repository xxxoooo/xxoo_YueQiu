package com.gotye.api;

import android.os.Environment;

import java.io.File;

/**
 * 路径工具类
 * @author Administrator
 *
 */
public class PathUtil {
	private  static final String ROOT_PATH_NAME="gotye.cache";
  public static String getAppFIlePath(){
	  String path=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+ROOT_PATH_NAME+File.separator;
	  return path;
  }
}

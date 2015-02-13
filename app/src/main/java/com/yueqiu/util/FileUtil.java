package com.yueqiu.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileUtil {

    public static String toFile(byte[] bfile, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            int len = bfile.length;
            file = new File(fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    public static byte[] getBytes(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static String uriToPath(Context context, Uri selectedImage) {
        // String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(selectedImage, null,
                null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex("_data");
            if (columnIndex == -1) {
                Utils.showToast(context, "找不到图片");
                return null;
            }
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;

            if (picturePath == null || picturePath.equals("null")) {
                Utils.showToast(context, "找不到图片");
                return null;
            }
            return picturePath;
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Utils.showToast(context, "找不到图片");
                return null;

            }
            return file.getAbsolutePath();
        }

    }

    private static String path = "";

    static {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory() + "/yueqiu";
        } else {
            path = Environment.getDataDirectory().getAbsolutePath() + "/yueqiu";
        }
    }

    public static String getRecentChatPath() {
        File file = new File(path + "/RecentChat/");
        if (!file.exists()) {
            file.mkdirs();
        }
        return path + "/RecentChat/";
    }

    public static String getWaterPhotoPath() {
        File file = new File(path + "/WaterPhoto/");
        if (!file.exists()) {
            file.mkdirs();
        }
        return path + "/WaterPhoto/";
    }

    /**
     * 得到sd卡真实路径
     *
     * @return
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 判断SD卡是否有效
     */
    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 得到SD卡路径
     */
    public static String getSdDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static class FileNameFilter implements FilenameFilter {

        private String mExtension = ".";

        public FileNameFilter(String fileExtName) {
            mExtension += fileExtName;
        }

        /**
         * Indicates if a specific filename matches this filter.
         *
         * @param dir      the directory in which the {@code filename} was found.
         * @param filename the name of the file in {@code dir} to test.
         * @return {@code true} if the filename matches the filter
         * and can be included in the list, {@code false}
         * otherwise.
         */
        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(mExtension);
        }
    }

//	////////////////////////////////////////////////////////////
//	private static String ANDROID_SECURE = "/mnt/sdcard/.android_secure";
//
//    private static final String LOG_TAG = "Util";
//

//
//    // if path1 contains path2
//    public static boolean containsPath(String path1, String path2) {
//        String path = path2;
//        while (path != null) {
//            if (path.equalsIgnoreCase(path1))
//                return true;
//
//            if (path.equals(GlobalConsts.ROOT_PATH))
//                break;
//            path = new File(path).getParent();
//        }
//
//        return false;
//    }
//
//    public static String makePath(String path1, String path2) {
//        if (path1.endsWith(File.separator))
//            return path1 + path2;
//
//        return path1 + File.separator + path2;
//    }
//

//
//    public static boolean isNormalFile(String fullName) {
//        return !fullName.equals(ANDROID_SECURE);
//    }
//
//    public static FileInfo GetFileInfo(String filePath) {
//        File lFile = new File(filePath);
//        if (!lFile.exists())
//            return null;
//
//        FileInfo lFileInfo = new FileInfo();
//        lFileInfo.canRead = lFile.canRead();
//        lFileInfo.canWrite = lFile.canWrite();
//        lFileInfo.isHidden = lFile.isHidden();
//        lFileInfo.fileName = FileUtil.getNameFromFilepath(filePath);
//        lFileInfo.ModifiedDate = lFile.lastModified();
//        lFileInfo.IsDir = lFile.isDirectory();
//        lFileInfo.filePath = filePath;
//        lFileInfo.fileSize = lFile.length();
//        return lFileInfo;
//    }
//
//    public static FileInfo GetFileInfo(File f, FilenameFilter filter, boolean showHidden) {
//        FileInfo lFileInfo = new FileInfo();
//        String filePath = f.getPath();
//        File lFile = new File(filePath);
//        lFileInfo.canRead = lFile.canRead();
//        lFileInfo.canWrite = lFile.canWrite();
//        lFileInfo.isHidden = lFile.isHidden();
//        lFileInfo.fileName = f.getName();
//        lFileInfo.ModifiedDate = lFile.lastModified();
//        lFileInfo.IsDir = lFile.isDirectory();
//        lFileInfo.filePath = filePath;
//        if (lFileInfo.IsDir) {
//            int lCount = 0;
//            File[] files = lFile.listFiles(filter);
//
//            // null means we cannot access this dir
//            if (files == null) {
//                return null;
//            }
//
//            for (File child : files) {
//                if ((!child.isHidden() || showHidden)
//                        && FileUtil.isNormalFile(child.getAbsolutePath())) {
//                    lCount++;
//                }
//            }
//            lFileInfo.Count = lCount;
//
//        } else {
//
//            lFileInfo.fileSize = lFile.length();
//
//        }
//        return lFileInfo;
//    }
//
//    /*
//     * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
//     * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
//     * http://code.google.com/p/android/issues/detail?id=9151
//     */
//    public static Drawable getApkIcon(Context context, String apkPath) {
//        PackageManager pm = context.getPackageManager();
//        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
//                PackageManager.GET_ACTIVITIES);
//        if (info != null) {
//            ApplicationInfo appInfo = info.applicationInfo;
//            appInfo.sourceDir = apkPath;
//            appInfo.publicSourceDir = apkPath;
//            try {
//                return appInfo.loadIcon(pm);
//            } catch (OutOfMemoryError e) {
//                Log.e(LOG_TAG, e.toString());
//            }
//        }
//        return null;
//    }
//
//    public static String getExtFromFilename(String filename) {
//        int dotPosition = filename.lastIndexOf('.');
//        if (dotPosition != -1) {
//            return filename.substring(dotPosition + 1, filename.length());
//        }
//        return "";
//    }
//
//    public static String getNameFromFilename(String filename) {
//        int dotPosition = filename.lastIndexOf('.');
//        if (dotPosition != -1) {
//            return filename.substring(0, dotPosition);
//        }
//        return "";
//    }
//
//    public static String getPathFromFilepath(String filepath) {
//        int pos = filepath.lastIndexOf('/');
//        if (pos != -1) {
//            return filepath.substring(0, pos);
//        }
//        return "";
//    }
//
//    public static String getNameFromFilepath(String filepath) {
//        int pos = filepath.lastIndexOf('/');
//        if (pos != -1) {
//            return filepath.substring(pos + 1);
//        }
//        return "";
//    }
//
//    // return new file path if successful, or return null
//    public static String copyFile(String src, String dest) {
//        File file = new File(src);
//        if (!file.exists() || file.isDirectory()) {
//            Log.v(LOG_TAG, "copyFile: file not exist or is directory, " + src);
//            return null;
//        }
//        FileInputStream fi = null;
//        FileOutputStream fo = null;
//        try {
//            fi = new FileInputStream(file);
//            File destPlace = new File(dest);
//            if (!destPlace.exists()) {
//                if (!destPlace.mkdirs())
//                    return null;
//            }
//
//            String destPath = FileUtil.makePath(dest, file.getName());
//            File destFile = new File(destPath);
//            int i = 1;
//            while (destFile.exists()) {
//                String destName = FileUtil.getNameFromFilename(file.getName()) + " " + i++ + "."
//                        + FileUtil.getExtFromFilename(file.getName());
//                destPath = FileUtil.makePath(dest, destName);
//                destFile = new File(destPath);
//            }
//
//            if (!destFile.createNewFile())
//                return null;
//
//            fo = new FileOutputStream(destFile);
//            int count = 102400;
//            byte[] buffer = new byte[count];
//            int read = 0;
//            while ((read = fi.read(buffer, 0, count)) != -1) {
//                fo.write(buffer, 0, read);
//            }
//
//            // TODO: set access privilege
//
//            return destPath;
//        } catch (FileNotFoundException e) {
//            Log.e(LOG_TAG, "copyFile: file not found, " + src);
//            e.printStackTrace();
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "copyFile: " + e.toString());
//        } finally {
//            try {
//                if (fi != null)
//                    fi.close();
//                if (fo != null)
//                    fo.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return null;
//    }
//
//    // does not include sd card folder
//    private static String[] SysFileDirs = new String[] {
//        "miren_browser/imagecaches"
//    };
//
//    public static boolean shouldShowFile(String path) {
//        return shouldShowFile(new File(path));
//    }
//
//    public static boolean shouldShowFile(File file) {
//        boolean show = Settings.instance().getShowDotAndHiddenFiles();
//        if (show)
//            return true;
//
//        if (file.isHidden())
//            return false;
//
//        if (file.getName().startsWith("."))
//            return false;
//
//        String sdFolder = getSdDirectory();
//        for (String s : SysFileDirs) {
//            if (file.getPath().startsWith(makePath(sdFolder, s)))
//                return false;
//        }
//
//        return true;
//    }
//
//   /* public static ArrayList<FavoriteItem> getDefaultFavorites(Context context) {
//        ArrayList<FavoriteItem> list = new ArrayList<FavoriteItem>();
//        list.add(new FavoriteItem(context.getString(R.string.favorite_photo), makePath(getSdDirectory(), "DCIM/Camera")));
//        list.add(new FavoriteItem(context.getString(R.string.favorite_sdcard), getSdDirectory()));
//        //list.add(new FavoriteItem(context.getString(R.string.favorite_root), getSdDirectory()));
//        list.add(new FavoriteItem(context.getString(R.string.favorite_screen_cap), makePath(getSdDirectory(), "MIUI/screen_cap")));
//        list.add(new FavoriteItem(context.getString(R.string.favorite_ringtone), makePath(getSdDirectory(), "MIUI/ringtone")));
//        return list;
//    }*/
//
//    public static boolean setText(View view, int id, String text) {
//        TextView textView = (TextView) view.findViewById(id);
//        if (textView == null)
//            return false;
//
//        textView.setText(text);
//        return true;
//    }
//
//    public static boolean setText(View view, int id, int text) {
//        TextView textView = (TextView) view.findViewById(id);
//        if (textView == null)
//            return false;
//
//        textView.setText(text);
//        return true;
//    }
//
//    // comma separated number
//    public static String convertNumber(long number) {
//        return String.format("%,d", number);
//    }
//
//    // storage, G M K B
//    public static String convertStorage(long size) {
//        long kb = 1024;
//        long mb = kb * 1024;
//        long gb = mb * 1024;
//
//        if (size >= gb) {
//            return String.format("%.1f GB", (float) size / gb);
//        } else if (size >= mb) {
//            float f = (float) size / mb;
//            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
//        } else if (size >= kb) {
//            float f = (float) size / kb;
//            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
//        } else
//            return String.format("%d B", size);
//    }
//
//    public static class SDCardInfo {
//        public long total;
//
//        public long free;
//    }
//
//    public static SDCardInfo getSDCardInfo() {
//        String sDcString = android.os.Environment.getExternalStorageState();
//
//        if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
//            File pathFile = android.os.Environment.getExternalStorageDirectory();
//
//            try {
//                android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
//
//                // 获取SDCard上BLOCK总数
//                long nTotalBlocks = statfs.getBlockCount();
//
//                // 获取SDCard上每个block的SIZE
//                long nBlocSize = statfs.getBlockSize();
//
//                // 获取可供程序使用的Block的数量
//                long nAvailaBlock = statfs.getAvailableBlocks();
//
//                // 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
//                long nFreeBlock = statfs.getFreeBlocks();
//
//                SDCardInfo info = new SDCardInfo();
//                // 计算SDCard 总容量大小MB
//                info.total = nTotalBlocks * nBlocSize;
//
//                // 计算 SDCard 剩余大小MB
//                info.free = nAvailaBlock * nBlocSize;
//
//                return info;
//            } catch (IllegalArgumentException e) {
//                Log.e(LOG_TAG, e.toString());
//            }
//        }
//
//        return null;
//    }
//
//   /* public static void showNotification(Context context, Intent intent, String title, String body, int drawableId) {
//        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification notification = new Notification(drawableId, body, System.currentTimeMillis());
//        notification.flags = Notification.FLAG_AUTO_CANCEL;
//        notification.defaults = Notification.DEFAULT_SOUND;
//        if (intent == null) {
//            // FIXEME: category tab is disabled
//            intent = new Intent(context, FileViewActivity.class);
//        }
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        notification.setLatestEventInfo(context, title, body, contentIntent);
//        manager.notify(drawableId, notification);
//    }*/
//
//    public static String formatDateString(Context context, long time) {
//        DateFormat dateFormat = android.text.format.DateFormat
//                .getDateFormat(context);
//        DateFormat timeFormat = android.text.format.DateFormat
//                .getTimeFormat(context);
//        Date date = new Date(time);
//        return dateFormat.format(date) + " " + timeFormat.format(date);
//    }
//
//    /*public static void updateActionModeTitle(ActionMode mode, Context context, int selectedNum) {
//        if (mode != null) {
//            mode.setTitle(context.getString(R.string.multi_select_title,selectedNum));
//            if(selectedNum == 0){
//                mode.finish();
//            }
//        }
//    }
//*/
//    public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
//        {
//            add("text/plain");
//            add("text/plain");
//            add("application/pdf");
//            add("application/msword");
//            add("application/vnd.ms-excel");
//            add("application/vnd.ms-excel");
//        }
//    };

    public static String sZipFileMimeType = "application/zip";

    public static int CATEGORY_TAB_INDEX = 0;
    public static int SDCARD_TAB_INDEX = 1;
}

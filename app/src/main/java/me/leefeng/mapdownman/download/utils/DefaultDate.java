package me.leefeng.mapdownman.download.utils;

import android.os.Environment;

/**
 * Created by limxing on 16/7/20.
 */
public class DefaultDate {
    // 请求网络IP
    public static String DOWNIP = "http://106.120.75.105:92/";
    // 文件下载的目录
    public static String FILE_MAPDATE = Environment.getExternalStorageDirectory().toString()+"/Siwei/MapDate/";
    // 文件下载url
    public static String downUrl = DOWNIP + "download.xml";
    // poi文件下载的目录
    public static String FILE_POIDATE = Environment.getExternalStorageDirectory()+"/Siwei/PoiDate/";
}

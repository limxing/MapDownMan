package me.leefeng.mapdownman.download.utils;

import android.os.Environment;

/**
 * Created by limxing on 16/7/20.
 */
public class DefaultDate {
    // 请求网络IP
    public static String DOWNIP = "http://192.168.100.172:81/";
    // 文件下载的目录
    public static String FILE_MAPDATE = Environment.getExternalStorageDirectory().toString()+"/Siwei/MapDate/";
    // 文件下载url
    public static String downUrl = DOWNIP + "shenzhen/sql/download.xml";
    // poi文件下载的目录
    public static String FILE_POIDATE = Environment.getExternalStorageDirectory()+"/Siwei/PoiDate/";
}

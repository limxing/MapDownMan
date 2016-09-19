package me.leefeng.mapdownman;

import android.app.Application;
import android.content.Intent;

import me.leefeng.mapdownman.download.DownLoadService;

/**
 * Created by limxing on 16/7/21.
 */
public class DownApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, DownLoadService.class));
    }
}

package me.leefeng.mapdownman.download;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 类功能描述：下载器后台服务</br>
 *
 * @author zhuiji7  (470508081@qq.com)
 * @version 1.0
 * </p>
 */

public class DownLoadService extends Service {
    private static DownLoadManager  downLoadManager;
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static DownLoadManager getDownLoadManager(){
        return downLoadManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("leefengg","启动服务onCreat");
        downLoadManager = new DownLoadManager(DownLoadService.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("leefengg","停止服务onCreat");
        //释放downLoadManager
        downLoadManager.stopAllTask();
        downLoadManager = null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if(downLoadManager == null){
            downLoadManager = new DownLoadManager(DownLoadService.this);
        }
    }
    
    
    
    
    

}

package me.leefeng.mapdownman.download.activity;

import android.content.Context;
import android.support.v4.view.LayoutInflaterFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import me.leefeng.mapdownman.R;
import me.leefeng.mapdownman.download.DownLoadListener;
import me.leefeng.mapdownman.download.DownLoadManager;
import me.leefeng.mapdownman.download.DownLoadService;
import me.leefeng.mapdownman.download.TaskInfo;
import me.leefeng.mapdownman.download.dbcontrol.bean.SQLDownLoadInfo;
import me.leefeng.mapdownman.download.utils.DefaultDate;


/**
 * Created by limxing on 16/7/20.
 */
public class DownAdapter extends BaseAdapter implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ArrayList<TaskInfo> listdata;
    private DownLoadManager downLoadManager;
    private Context context;
    private List<DownMap> mapList;
    private int vect;
    private int image;
    private int tran;

    public DownAdapter(Context mContext, DownLoadManager manager) {
        this.context = mContext;
        this.downLoadManager = manager;
        listdata = downLoadManager.getAllTask();
        downLoadManager.setAllTaskListener(new DownloadManagerListener());
        for (TaskInfo taskInfo : listdata) {
            for (DownMap downMap : mapList) {
                if (downMap.getUrl().equals(taskInfo.getTaskID())) {
                    downMap.setFileSize(taskInfo.getFileSize());
                    downMap.setCurrentSize(taskInfo.getDownFileSize());
                    downMap.setDown(taskInfo.isOnDownloading());
                    break;
                }
            }
        }
    }

    /**
     * 获取下载管理器
     *
     * @param mContext
     */
    public DownAdapter(Context mContext) {
        super();
         /*获取下载管理器*/
        downLoadManager = DownLoadService.getDownLoadManager();
        /*断点续传需要服务器的支持，设置该项时要先确保服务器支持断点续传功能*/
        downLoadManager.setSupportBreakpoint(true);
        this.context = mContext;
        listdata = downLoadManager.getAllTask();
        downLoadManager.setAllTaskListener(new DownloadManagerListener());
    }

    @Override
    public int getCount() {
        if (mapList == null) {
            return 0;
        }
        return mapList.size();
    }

    @Override
    public Object getItem(int i) {
        return mapList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        DownMap downMap = mapList.get(i);
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_down_item, null);
            holder.download_item_name = (TextView) convertView.findViewById(R.id.download_item_name);
            holder.download_item_size = (TextView) convertView.findViewById(R.id.download_item_size);
            holder.down_item_top_name = (TextView) convertView.findViewById(R.id.down_item_top_name);
            holder.download_item_jindu = (TextView) convertView.findViewById(R.id.download_item_jindu);
            holder.down_item_download = (CheckBox) convertView.findViewById(R.id.down_item_download);
            holder.down_item_cancle = (Button) convertView.findViewById(R.id.down_item_cancle);
            holder.download_item_progressbar = (ProgressBar) convertView.findViewById(R.id.download_item_progressbar);
            holder.down_item_top = convertView.findViewById(R.id.down_item_top);
            holder.down_item_download.setOnCheckedChangeListener(this);
            holder.down_item_cancle.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        try {
            holder.download_item_name.setText(downMap.getDescribe());
            holder.download_item_size.setText(downMap.getSize());
            //反射获取属性值
            holder.down_item_top_name.setText(downMap.getStringTileType() + "(" +
                    getClass().getDeclaredField(downMap.getTileType()).get(this) + ")");
            if (i > 0 && downMap.getTileType().equals(mapList.get(i - 1).getTileType())) {
                holder.down_item_top.setVisibility(View.GONE);
            } else {
                holder.down_item_top.setVisibility(View.VISIBLE);
            }
            holder.download_item_progressbar.setProgress(downMap.getProgress());
            holder.download_item_jindu.setText(downMap.getProgress() + "%");
            holder.down_item_download.setTag(i);
            holder.down_item_cancle.setTag(i);
            if (downMap.isDown()) {
                holder.down_item_download.setText("暂停");
                holder.download_item_jindu.setVisibility(View.VISIBLE);
                if (downMap.getCurrentSize() == 0) {
                    holder.download_item_jindu.setText("等待");
                }
            } else {
                holder.down_item_download.setText("下载");
                if (downMap.getCurrentSize() > 0) {
                    holder.download_item_jindu.setVisibility(View.VISIBLE);
                    holder.download_item_jindu.setText("已暂停");
                } else {
                    holder.download_item_jindu.setVisibility(View.INVISIBLE);
                }

            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    /**
     * 按钮的点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        DownMap downMap = mapList.get((int) view.getTag());
        //点击取消按钮
        for (TaskInfo taskInfo : listdata) {
            if (downMap.getUrl().equals(taskInfo.getTaskID())) {
                //正在下载
                downLoadManager.stopTask(downMap.getUrl());
                downLoadManager.deleteTask(downMap.getUrl());
                downMap.setDown(false);
                downMap.setCurrentSize(0l);
                listdata.remove(taskInfo);
                notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * 下载按钮的点击事件
     *
     * @param compoundButton
     * @param b
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        DownMap downMap = mapList.get((int) compoundButton.getTag());
        if (!downMap.isDown()) {//打开  开始下载
            for (TaskInfo taskInfo : listdata) {
                if (taskInfo.getTaskID().equals(downMap.getUrl())) {
                    taskInfo.setOnDownloading(true);
                    downMap.setDown(true);
                    downLoadManager.startTask(taskInfo.getTaskID());
                    break;
                }
            }
            if (!downMap.isDown()) {
                Log.i("leefengg", getBlank() + "/" + downMap.getFileSize());
                if (getBlank() < downMap.getFileSize()) {
                    Toast.makeText(context, "内存空间不足", Toast.LENGTH_LONG).show();
                } else {
                    //没有下载
                    TaskInfo info = new TaskInfo();
                    info.setFileName(downMap.getName());
                            /*服务器一般会有个区分不同文件的唯一ID，用以处理文件重名的情况*/
                    info.setTaskID(downMap.getUrl());
                    info.setOnDownloading(true);
                    downMap.setDown(true);
                    downLoadManager.addTask(info.getTaskID(), info.getTaskID(), info.getFileName());
                    listdata.add(info);
                    Log.i("leefengg", info.toString());
                }
            }
        } else {
            //正在下载
            downLoadManager.stopTask(downMap.getUrl());
            for (TaskInfo taskInfo : listdata) {
                if (taskInfo.getTaskID().equals(downMap.getUrl())) {
                    taskInfo.setOnDownloading(false);
                    downMap.setDown(false);
                    break;
                }
            }
        }
        notifyDataSetChanged();

    }

    public void onDestory() {
        downLoadManager = null;
        context = null;
        mapList.clear();
        mapList = null;
        listdata.clear();
        listdata = null;
    }

    static class Holder {
        TextView download_item_name;
        TextView download_item_size;
        TextView down_item_top_name;
        TextView download_item_jindu;
        CheckBox down_item_download;
        Button down_item_cancle;
        View down_item_top;
        ProgressBar download_item_progressbar;
    }

    /**
     * 设置集合的内容,并计算三个分类的总数
     *
     * @param mapList
     */
    public void setMapList(List<DownMap> mapList) {
        //对集合进行排序
        Collections.sort(mapList);
        vect = image = tran = 0;
        for (DownMap downMap : mapList) {
            switch (downMap.getTileType()) {
                case "vect":
                    vect++;
                    break;
                case "image":
                    image++;
                    break;
                case "tran":
                    tran++;
                    break;
            }
            for (TaskInfo taskInfo : listdata) {
                if (downMap.getUrl().equals(taskInfo.getTaskID())) {
                    downMap.setFileSize(taskInfo.getFileSize());
                    downMap.setCurrentSize(taskInfo.getDownFileSize());
                    downMap.setDown(taskInfo.isOnDownloading());
                    break;
                }
            }
        }

        this.mapList = mapList;
        notifyDataSetChanged();
    }

    /**
     * 实现下载监听器
     */
    private class DownloadManagerListener implements DownLoadListener {

        @Override
        public void onStart(SQLDownLoadInfo sqlDownLoadInfo) {

        }

        @Override
        public void onProgress(SQLDownLoadInfo sqlDownLoadInfo, boolean isSupportBreakpoint) {
            if (mapList != null) {
                for (DownMap downMap : mapList) {
                    if (downMap.getUrl().equals(sqlDownLoadInfo.getTaskID())) {
                        downMap.setCurrentSize(sqlDownLoadInfo.getDownloadSize());
                        downMap.setFileSize(sqlDownLoadInfo.getFileSize());
                        break;
                    }
                }
                notifyDataSetChanged();
            }
            for (TaskInfo taskInfo : listdata) {
                if (taskInfo.getTaskID().equals(sqlDownLoadInfo.getTaskID())) {
                    taskInfo.setDownFileSize(sqlDownLoadInfo.getDownloadSize());
                    taskInfo.setFileSize(sqlDownLoadInfo.getFileSize());
                    break;
                }
            }
        }

        @Override
        public void onStop(SQLDownLoadInfo sqlDownLoadInfo, boolean isSupportBreakpoint) {

        }

        @Override
        public void onError(SQLDownLoadInfo sqlDownLoadInfo) {
            for (TaskInfo taskInfo : listdata) {
                if (taskInfo.getTaskID().equals(sqlDownLoadInfo.getTaskID())) {
                    taskInfo.setOnDownloading(false);
                }
            }
            if (mapList != null) {
                for (DownMap downMap : mapList) {
                    if (downMap.getUrl().equals(sqlDownLoadInfo.getTaskID())) {
                        downMap.setDown(false);
                    }
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public void onSuccess(SQLDownLoadInfo sqlDownLoadInfo) {
            for (TaskInfo taskInfo : listdata) {
                if (taskInfo.getTaskID().equals(sqlDownLoadInfo.getTaskID())) {
                    listdata.remove(taskInfo);
                    break;
                }
            }
            if (mapList != null) {
                for (DownMap downMap : mapList) {
                    if (downMap.getUrl().equals(sqlDownLoadInfo.getTaskID())) {
                        mapList.remove(downMap);
                        break;
                    }
                }

                notifyDataSetChanged();
            }
        }
    }

    /**
     * 获取sd卡剩余大小
     *
     * @return
     */
    private long getBlank() {
        File file1 = new File(DefaultDate.FILE_MAPDATE);
        return file1.getUsableSpace();
    }
}

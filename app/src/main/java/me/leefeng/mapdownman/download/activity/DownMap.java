package me.leefeng.mapdownman.download.activity;

import me.leefeng.mapdownman.download.utils.StringUtils;

/**
 * Created by limxing on 16/7/20.
 */
public class DownMap implements Comparable<DownMap> {
    private String url;
    private String fileName;
    private String name;
    private String tileType;
    private String md5;
    private String size;
    private String describe;
    private boolean isDown;

    //数据来自Taskinfo
    private long fileSize;
    private long currentSize;

    public long getCurrentSize() {
        return currentSize;
    }

    public int getProgress() {
        if (fileSize == 0) {
            return 0;
        } else {
            return ((int) (100 * currentSize / fileSize));
        }

    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public boolean isDown() {
        return isDown;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getSize() {
        String string = StringUtils.formatFileSize(Long.parseLong(size), false);
        if (isDown) {
            string = StringUtils.formatFileSize(currentSize, false) + "/" + string;
        }
        return string;
    }

    public void setSize(String size) {
        this.fileSize = Long.parseLong(size);
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        String urlName = url.substring(url.lastIndexOf('/'));
        return name + urlName.substring(urlName.indexOf('.'));
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTileType() {
        return tileType;
    }

    public String getStringTileType() {
        String type = null;
        switch (tileType) {
            case "tran":
                type = "透明地图下载";
                break;
            case "vect":
                type = "矢量地图下载";
                break;
            case "image":
                type = "影像地图下载";
                break;
        }
        return type;
    }

    public void setTileType(String tileType) {
        this.tileType = tileType;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public int compareTo(DownMap downMap) {

        return tileType.compareTo(downMap.getTileType());
    }
}

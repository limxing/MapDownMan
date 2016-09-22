package me.leefeng.mapdownman.download.activity;

import java.util.ArrayList;

/**
 * Created by limxing on 16/7/20.
 */
public interface DownPresenter {
    void refresh();

    void onDestory();

    void search(String s, String key);

    ArrayList<DownMap> getList();
}

package me.leefeng.mapdownman.download.activity;

import java.util.ArrayList;

/**
 * Created by limxing on 16/7/20.
 */
public interface DownView {
    void refreshSuccess(ArrayList<DownMap> list);

    void refreshFail(String error);

    void initComplet();

    void checkSd();
}

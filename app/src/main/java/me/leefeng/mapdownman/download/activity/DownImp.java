package me.leefeng.mapdownman.download.activity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import me.leefeng.mapdownman.download.utils.DefaultDate;
import me.leefeng.mapdownman.download.utils.NetUtil;

/**
 * Created by limxing on 16/7/20.
 */
public class DownImp implements DownPresenter {

    private static final int XML_SUCCESS = 0;
    private static final int XML_FAILT = 1;
    private static final int FIRST_LOAD = 2;
    private static final int CHECK_SD = 3;
    private Timer timer;

    private DownView downView;
    private ArrayList<DownMap> list;
    private Context context;

    public DownImp(DownView downView) {
        this.downView = downView;
        this.context = (DownActivity) downView;
        list = new ArrayList<DownMap>();
        mHandler.sendEmptyMessageDelayed(FIRST_LOAD, 50);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(CHECK_SD);
            }
        }, 1000, 5000);
    }

    @Override
    public void refresh() {
        download();
    }

    @Override
    public void onDestory() {
        timer.cancel();
        timer = null;
        context = null;
        downView = null;
    }

    /**
     * 搜索结果显示
     *
     * @param key  关键字
     * @param type 类型
     */
    @Override
    public void search(String key, String type) {
        synchronized (this) {
            ArrayList<DownMap> newList = new ArrayList<>();
            for (DownMap downMap : list) {
                if ((key.isEmpty() || downMap.getDescribe().contains(key)) && (type.isEmpty() || type.equals(downMap.getTileType()))) {
                    newList.add(downMap);
                }
            }
            downView.refreshSuccess(newList);
        }
    }

    @Override
    public ArrayList<DownMap> getList() {
        return list;
    }

    /**
     * 在UI线程中执行回调Handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<DownImp> mActivity;

        public MyHandler(DownImp activity) {
            mActivity = new WeakReference<DownImp>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DownImp downImp = mActivity.get();
            if (downImp != null && downImp.downView != null) {
                switch (msg.what) {
                    case XML_SUCCESS:
                        downImp.downView.refreshSuccess(downImp.list);
                        break;
                    case XML_FAILT:
                        downImp.downView.refreshFail(msg.obj.toString());
                        break;
                    case FIRST_LOAD:
                        downImp.downView.initComplet();
                        if (downImp.list != null) {
                            downImp.downView.refreshSuccess(downImp.list);
                        }
                        break;
                    case CHECK_SD:
                        downImp.downView.checkSD();
                        break;
                }
            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);


    /**
     * 创建：李利锋
     * <p/>
     * 创建时间：2015-11-11 下午2:58:30
     * <p/>
     * 方法说明：请求xml并解析xml
     */
    private void download() {
        list.clear();
        new Thread() {
            public void run() {
                String error = null;
                URL checkUrl;
                InputStream is = null;
                HttpURLConnection conn = null;
                try {
                    if (NetUtil.isNetworkAvailable(context)) {
                        checkUrl = new URL(DefaultDate.downUrl);
                        conn = (HttpURLConnection) checkUrl.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setRequestMethod("GET");
                        if (conn.getResponseCode() == 200) {
                            is = conn.getInputStream();
                            DocumentBuilderFactory factory = DocumentBuilderFactory
                                    .newInstance();
                            DocumentBuilder builder = factory
                                    .newDocumentBuilder();
                            Document dom = builder.parse(is);
                            Element root = dom.getDocumentElement();
                            Element ditu = (Element) dom.getElementsByTagName(
                                    "ditu").item(0);
                            //获取map节点的数据
                            NodeList maps = ditu.getElementsByTagName("map");
                            for (int i = 0; i < maps.getLength(); i++) {
                                Element mapNode = (Element) maps.item(i);
                                DownMap map = new DownMap();
                                map.setUrl(mapNode.getAttribute("url"));
                                map.setName(mapNode.getAttribute("tiletype")
                                        + "#" + mapNode.getAttribute("name"));
                                map.setDescribe(mapNode.getAttribute("describe"));
                                map.setSize(mapNode.getAttribute("size"));
                                map.setTileType(mapNode.getAttribute("tiletype"));
                                map.setMd5(mapNode.getAttribute("MD5"));
                                list.add(map);
                            }
                            //获取dbs 节点的数据
                            Element dbs = (Element) dom.getElementsByTagName(
                                    "dbs").item(0);
                            if (dbs != null) {
                                NodeList dbList = dbs.getElementsByTagName("db");
                                for (int j = 0; j < dbList.getLength(); j++) {
                                    Element mapNode = (Element) dbList.item(j);
                                    DownMap map = new DownMap();
                                    map.setUrl(mapNode.getAttribute("url"));
                                    map.setName(mapNode.getAttribute("tiletype")
                                            + "#" + mapNode.getAttribute("name"));
                                    map.setDescribe(mapNode.getAttribute("describe"));
                                    map.setSize(mapNode.getAttribute("size"));
                                    map.setTileType(mapNode.getAttribute("tiletype"));
                                    map.setMd5(mapNode.getAttribute("MD5"));
                                    list.add(map);
                                }
                            }

//                            比对数据前奏
                            File file = new File(DefaultDate.FILE_MAPDATE);
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            File[] fileList = file.listFiles(new FileFilter() {
                                @Override
                                public boolean accept(File file) {
                                    return file.getName().endsWith(".db");
                                }
                            });
//                            比对数据去掉已下载的条目
                            if (fileList != null && fileList.length > 0) {
                                Iterator<DownMap> iter = list.iterator();
                                while (iter.hasNext()) {
                                    DownMap downMap = iter.next();
                                    for (File file1 : fileList) {
                                        if (file1.getName().startsWith(downMap.getName())) {
                                            iter.remove();
                                            break;
                                        }
                                    }
                                }
                            }
                            error = null;
                        } else if (conn.getResponseCode() == 403) {
                            error = "请求失败,请重试";
                        } else {
                            error = "请求失败,请重试";
                        }
                    } else {
                        error = "请检查网络";
                    }

                } catch (SocketTimeoutException e) {
                    // 超时
                    e.printStackTrace();
                    error = "请求超时,请重试";
                } catch (MalformedURLException e) {
                    error = "解析失败,请联系世景";
                    e.printStackTrace();
                } catch (IOException e) {
                    error = "解析失败,请联系世景";
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    error = "解析失败,请联系世景";
                    e.printStackTrace();
                } catch (SAXException e) {
                    error = "解析失败,请联系世景";
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Message message = mHandler.obtainMessage();
                    if (error == null) {

                        mHandler.sendEmptyMessage(XML_SUCCESS);
                    } else {

                        message.what = XML_FAILT;
                        message.obj = error;
                        mHandler.sendMessage(message);
                    }
                }
            }
        }.start();
    }
}

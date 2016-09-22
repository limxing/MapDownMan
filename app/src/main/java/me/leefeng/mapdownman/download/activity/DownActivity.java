package me.leefeng.mapdownman.download.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.leefeng.mapdownman.R;
import me.leefeng.mapdownman.download.DownLoadManager;
import me.leefeng.mapdownman.download.DownLoadService;
import me.leefeng.mapdownman.download.utils.DefaultDate;
import me.leefeng.mapdownman.download.utils.StringUtils;
import me.leefeng.mapdownman.download.utils.SystemBarTintManager;
import me.leefeng.mapdownman.download.utils.XListView.XListView;

/**
 * Created by limxing on 16/7/20.
 */
public class DownActivity extends AppCompatActivity implements DownView, View.OnClickListener,
        XListView.IXListViewListener, TextWatcher, View.OnTouchListener, AdapterView.OnItemSelectedListener {
    private static final int PERMISSTION_W = 0;
    private XListView down_listview;
    private Context mContext;
    private DownAdapter adapter;
    private DownImp presenter;
    private TextView down_left;
    private View down_search_et;
    private EditText down_search_text;
    private Spinner down_spinner;
    private String sraechType = "";
    private String key = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBarTintManager.initSystemBar(this, R.color.titlebac);
        setContentView(R.layout.activity_down);
        initView();
        presenter = new DownImp(this);
        checkPermissions();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        down_left = (TextView) findViewById(R.id.down_left);
        down_listview = (XListView) findViewById(R.id.down_listview);
        down_listview.setOnTouchListener(this);
        findViewById(R.id.down_back).setOnClickListener(this);
        findViewById(R.id.down_search).setOnClickListener(this);
        findViewById(R.id.dowm_close).setOnClickListener(this);
        down_spinner = (Spinner) findViewById(R.id.down_spinner);
        down_spinner.setOnItemSelectedListener(this);
        down_search_text = (EditText) findViewById(R.id.down_search_text);
        down_search_text.addTextChangedListener(this);
        down_search_et = findViewById(R.id.down_search_et);
        down_listview.setXListViewListener(this);
        down_listview.hideTimeView();
        mContext = DownActivity.this;
        //启动服务
        if (!serviceIsExist("me.leefeng.mapdownman.download.DownLoadService")) {
            Intent intent = new Intent(mContext, DownLoadService.class);
            startService(intent);
        }
    }

    /**
     * 延时回调获取下载管理器
     */
    @Override
    public void initComplet() {
        adapter = new DownAdapter(mContext);
        down_listview.setAdapter(adapter);
    }

    @Override
    public void checkSD() {
        down_left.setText(getSD());
    }

    /**
     * 销毁Activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestory();
        presenter = null;
        adapter.onDestory();
    }

    /**
     * 点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.down_back:
                finish();
                break;
            case R.id.down_search:
                down_search_et.setVisibility(View.VISIBLE);
                down_listview.setPullRefreshEnable(false);
                down_search_text.requestFocus();
                showInput(true);
                break;
            case R.id.dowm_close:
                down_search_et.setVisibility(View.GONE);
                down_listview.setPullRefreshEnable(true);
                refreshSuccess(presenter.getList());
                down_search_text.setText("");
                showInput(false);
                break;
        }
    }

    /**
     * 刷新xListView
     */
    @Override
    public void onRefresh() {
        presenter.refresh();
    }

    /**
     * 加载更多xlistview
     */
    @Override
    public void onLoadMore() {

    }

    /**
     * 检测服务是否存在
     *
     * @param className
     * @return
     */
    private boolean serviceIsExist(String className) {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> mServiceList = mActivityManager
                .getRunningServices(30);
        for (int i = 0; i < mServiceList.size(); i++) {
            if (className.equals(mServiceList.get(i).service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 刷新成功
     *
     * @param list
     */
    @Override
    public void refreshSuccess(ArrayList<DownMap> list) {
        if (adapter != null) {
            down_listview.stopRefresh(true);
            adapter.setMapList(new ArrayList<DownMap>(list));
        }
    }

    /**
     * 刷新失败
     *
     * @param error
     */
    @Override
    public void refreshFail(String error) {
        Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
        down_listview.stopRefresh(false);
    }


    /**
     * 检查6.0权限
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSTION_W);
        } else {
            onRefresh();
        }
    }

    /**
     * 权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onRefresh();
        } else {
            Toast.makeText(this, "下载需要获取写入权限", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 获取sd卡内存信息
     *
     * @return
     */
    private String getSD() {
        File file1 = new File(DefaultDate.FILE_MAPDATE);
        String blank = StringUtils.formatFileSize(file1.getUsableSpace());
        String total = StringUtils.formatFileSize(file1.getTotalSpace());
        return blank + "/" + total;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        key = down_search_text.getText().toString().trim();
        presenter.search(key, sraechType);
    }

    /**
     * 是否关闭键盘
     *
     * @param b
     */
    protected void showInput(boolean b) {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (b) {
                inputmanger.showSoftInput(down_search_text, 0);
            } else {
                inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            showInput(false);

        }
//        if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return false;
    }

    /**
     * Spinner的监听
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                sraechType = "";
                break;
            case 1:
                sraechType = "vect";
                break;
            case 3:
                sraechType = "tran";
                break;
            case 2:
                sraechType = "image";
                break;
        }
        TextView tv = (TextView) view;
        tv.setTextColor(Color.WHITE);    //设置颜色
        tv.setTextSize(16.0f);    //设置大小
        tv.setGravity(Gravity.CENTER_VERTICAL);   //设置居中
        presenter.search(key, sraechType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

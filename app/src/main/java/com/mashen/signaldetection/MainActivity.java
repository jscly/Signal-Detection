package com.mashen.signaldetection;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = getContext().getClass().getName();
    private LocationManager locationManager;
    private Intent intent;
    private Location location;
    private EditText et;
    ImageView ivCancel;
    private String imgSaveDirPath = "/sdcard/SignalDetectionPrtSc";
    Handler handler;
    Runnable runnable;

    private com.mashen.signaldetection.SignalView signalView1;
    private com.mashen.signaldetection.SignalView signalView2;
    private com.mashen.signaldetection.SignalView signalView3;
    private com.mashen.signaldetection.SignalView signalView4;
    private com.mashen.signaldetection.SignalView signalView5;
    private com.mashen.signaldetection.SignalView signalView6;
    private com.mashen.signaldetection.SignalView signalView7;
    private com.mashen.signaldetection.SignalView signalViewdianxin3g;
    private com.mashen.signaldetection.SignalView signalView8;

    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    private final int REQUEST_ACCESS_FINE_LOCATION = 300;
    private final int REQUEST_READ_EXTERNAL_STORAGE = 200;
    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 201;
    private final int REQUEST_READ_PHONE_STATE = 301;

    private final int REQUEST_READ_EXTERNAL_STORAGE_PRTSC = 400;
    private final int REQUEST_WRITE_EXTERNAL_STORAGE_PRTSC = 401;

    SimpleDateFormat sdf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        //获取信号的ProgressBar和view
        getDataView();
        //截屏保存按钮
        findViewById(R.id.imgSave).setOnClickListener(this);
        //查看记录按钮
        findViewById(R.id.imgDiary).setOnClickListener(this);
        //实时监测按钮
        findViewById(R.id.imgTest).setOnClickListener(this);
        //定位图标
        findViewById(R.id.imgLocation).setOnClickListener(this);

        ivCancel = (ImageView) findViewById(R.id.ivCancel);
        et = (EditText) findViewById(R.id.et);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (et.isFocusable()){
                    ivCancel.setVisibility(View.VISIBLE);
                    return false;
                }
                return false;
            }
        });

        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                ivCancel.setVisibility(View.VISIBLE);
                if (EditorInfo.IME_ACTION_DONE == actionId ||EditorInfo.IME_ACTION_GO ==
                        actionId || EditorInfo.IME_ACTION_SEND == actionId ||
                        (event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    if (imm.isActive()){

                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                        ivCancel.setVisibility(View.INVISIBLE);
                        return true;
                    }
                }
                return false;
            }
        });

        //删除et的内容
        findViewById(R.id.ivCancel).setOnClickListener(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

    }

    private void getDataView() {
        signalView1 = (SignalView)findViewById(R.id.signalView1);
        signalView2 = (SignalView)findViewById(R.id.signalView2);
        signalView3 = (SignalView)findViewById(R.id.signalView3);
        signalView4 = (SignalView)findViewById(R.id.signalView4);
        signalView5 = (SignalView)findViewById(R.id.signalView5);
        signalView6 = (SignalView)findViewById(R.id.signalView6);
        signalView7 = (SignalView)findViewById(R.id.signalView7);
        signalViewdianxin3g = (SignalView)findViewById(R.id.signalViewdianxin3g);
        signalView8 = (SignalView)findViewById(R.id.signalView8);
        signalView1.setOnClickListener(this);
        signalView2.setOnClickListener(this);
        signalView3.setOnClickListener(this);
        signalView4.setOnClickListener(this);
        signalView5.setOnClickListener(this);
        signalView6.setOnClickListener(this);
        signalView7.setOnClickListener(this);
        signalView8.setOnClickListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem updateText = menu.add(0, 100, 0, "自动检测");
        updateText.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        final Switch update = (Switch) findViewById(R.id.swiUpdate);
        MenuItem updateSwitch = menu.add(0, 101, 0, "自动检测");
        updateSwitch.setActionView(update);
        updateSwitch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        //自动检测事件
        update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "开始自动检测！", Toast.LENGTH_SHORT).show();

                    if(Build.VERSION.SDK_INT >= 23){
                        int checkReadPhoneStatePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE);
                        if (checkReadPhoneStatePermission != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_READ_PHONE_STATE);
                            return;
                        }else {
                            doDecect();
                        }
                    }else {
                        doDecect();
                    }
                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            doDecect();
                            handler.postDelayed(this, 5000);
                        }
                    };
                    handler.postDelayed(runnable, 5000);

                }else{
                    Toast.makeText(MainActivity.this, "取消自动检测", Toast.LENGTH_SHORT).show();
                    handler.removeCallbacks(runnable);
                }

            }
        });
        return true;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            //截屏保存
            case R.id.imgSave:
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (!et.getText().toString().equals(new String(""))) {

                    if(Build.VERSION.SDK_INT >= 23){
                        int checkReadExternalStoragePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
                        int checkWriteExternalStoragePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (checkReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_READ_EXTERNAL_STORAGE_PRTSC);
                            return;
                        }
                        if (checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXTERNAL_STORAGE_PRTSC);
                            return;
                        }
                        if (saveToSDCard(screenShot(), imgSaveDirPath, et.getText().toString() + "-" + System.currentTimeMillis() + ".png"))
                            Toast.makeText(getContext(),"截屏成功！",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(),"截屏失败！",Toast.LENGTH_SHORT).show();
                    }else {
                        if (saveToSDCard(screenShot(), imgSaveDirPath, et.getText().toString() + "-" + System.currentTimeMillis() + ".png"))
                            Toast.makeText(getContext(),"截屏成功！",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(),"截屏失败！",Toast.LENGTH_SHORT).show();
                    }

                } else {
                    if(Build.VERSION.SDK_INT >= 23){
                        int checkReadExternalStoragePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
                        int checkWriteExternalStoragePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (checkReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_READ_EXTERNAL_STORAGE_PRTSC);
                            return;
                        }
                        if (checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXTERNAL_STORAGE_PRTSC);
                            return;
                        }
                        if (saveToSDCard(screenShot(), imgSaveDirPath, sdf.format(new Date()) + "-" + System.currentTimeMillis() + ".png"))
                            Toast.makeText(getContext(),"截屏成功！",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(),"截屏失败！",Toast.LENGTH_SHORT).show();
                    }else {
                        if (saveToSDCard(screenShot(), imgSaveDirPath, sdf.format(new Date()) + "-" + System.currentTimeMillis() + ".png"))
                            Toast.makeText(getContext(),"截屏成功！",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(),"截屏失败！",Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            //查看记录
            case R.id.imgDiary:
                if(Build.VERSION.SDK_INT >= 23){
                    int checkReadExternalStoragePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
                    int checkWriteExternalStoragePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (checkReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_READ_EXTERNAL_STORAGE);
                        return;
                    }
                    if (checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXTERNAL_STORAGE);
                        return;
                    }
                    openDiary();
                }else {
                    openDiary();
                }
                break;

            //定位
            case R.id.imgLocation:
                if (Build.VERSION.SDK_INT >= 23){
                    int checkAccessFineLocationPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                    if (checkAccessFineLocationPermission != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_ACCESS_FINE_LOCATION);
                        return;
                    }
                    else {
                        doPosition();
                    }
                }else {
                    doPosition();
                }
                break;

            //执行全部检测
            case R.id.imgTest:
                if(Build.VERSION.SDK_INT >= 23){
                    int checkReadPhoneStatePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE);
                    if (checkReadPhoneStatePermission != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_READ_PHONE_STATE);
                        return;
                    }else {
                        doDecect();
                    }
                }else {
                    doDecect();
                }
                break;


            //移动2G信号设置
            case R.id.signalView1:
                signalView1.updateSignal(getDbm(2));
                break;
            //移动4G信号设置
            case R.id.signalView3:
                signalView3.updateSignal(getDbm(4));
                break;
            //联通2G信号设置
            case R.id.signalView4:
                signalView4.updateSignal(getDbm(2));
                break;
            //联通3G信号设置
            case R.id.signalView5:
                signalView5.updateSignal(getDbm(3));
                break;
            //联通4G信号设置
            case R.id.signalView6:
                signalView6.updateSignal(getDbm(4));
                break;
            //电信2G信号设置
            case R.id.signalView7:
                signalView7.updateSignal(getDbm(2));
                break;
            //电信3G信号设置
            case R.id.signalViewdianxin3g:
                signalViewdianxin3g.updateSignal(getDbm(3));
                break;
            //电信4G信号设置
            case R.id.signalView8:
                signalView8.updateSignal(getDbm(4));
                break;
            //et右侧的取消图标
            case R.id.ivCancel:
                et.getText().clear();
                ivCancel.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private int getDbm(int type) {
        List<CellInfo> infoLists = telephonyManager.getAllCellInfo();
        Log.e("getAllCellInfo。。。。",""+infoLists.toArray());
        for (CellInfo info : infoLists) {
            if (!info.isRegistered()) {
                continue;
            }
            switch (type) {
                case 4:
                    if (info instanceof CellInfoLte) {
                        return ((CellInfoLte) info).getCellSignalStrength().getDbm();
                    }
                    break;
                case 3:
                    if (info instanceof CellInfoCdma) {
                        return ((CellInfoCdma) info).getCellSignalStrength().getDbm();
                    }
                    break;
                case 2:
                    if (info instanceof CellInfoGsm) {
                        return ((CellInfoGsm) info).getCellSignalStrength().getDbm();
                    }
                    break;
            }
        }
        return -1;
    }


    //执行定位
    private void doPosition() {
        Toast.makeText(MainActivity.this, "开始定位...", Toast.LENGTH_SHORT).show();

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(getContext(), "请打开网络或GPS定位功能！", Toast.LENGTH_SHORT).show();
            intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }
        try {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                Log.d(TAG, "location为空！");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            Log.d(TAG, "location = " + location);
            updateView(location);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 5, locationListener);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    //执行检测
    private void doDecect() {
        if (phoneStateListener == null) {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    super.onSignalStrengthsChanged(signalStrength);
                    //http://www.cnblogs.com/lr393993507/p/5542673.html
                    Log.d(TAG, "signal --> : " + telephonyManager.getNetworkType());
                    switch (telephonyManager.getNetworkType()) {
                        //4G
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            switch (getProvider()) {
                                case "中国移动":
                                    Log.d("test.......","signalStrength.toString() =---> " + signalStrength.toString());
                                    Log.d("test.......","signalStrength.toString() =---> " + signalStrength.toString().split(" ")[11]);
                                    setDBM(signalView3, Integer.parseInt(signalStrength.toString().split(" ")[9]));
                                    break;
                                case "中国联通":
                                    setDBM(signalView6, Integer.parseInt(signalStrength.toString().split(" ")[9]));
                                    break;
                                case "中国电信":
                                    setDBM(signalView8, Integer.parseInt(signalStrength.toString().split(" ")[9]));
                                    break;
                            }
                            break;
                        //3G
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            switch (getProvider()) {
                                case "中国移动":
                                    //http://ask.csdn.net/questions/177471
                                    break;
                                case "中国联通":
                                    setDBM(signalView5, signalStrength.getCdmaDbm());
                                    break;
                                case "中国电信":
                                    setDBM(signalViewdianxin3g, signalStrength.getEvdoDbm());
                                    break;
                            }
                            break;
                        //2G
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            int asu = signalStrength.getGsmSignalStrength();
                            int dbm = -113 + 2 * asu;
                            Log.d("test;sa;dadad.......","asu=" + asu + " dbm = " + dbm);
                            switch (getProvider()) {
                                case "中国移动":
                                    setDBM(signalView1, dbm);
                                    break;
                                case "中国联通":
                                    setDBM(signalView4, dbm);
                                    break;
                                case "中国电信":
                                    setDBM(signalView7, dbm);
                                    break;
                            }
                            break;
                    }
                }
            };
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
        /*signalView1.updateSignal(-30);
        signalView2.updateSignal(-40);
        signalView3.updateSignal(-50);
        signalView4.updateSignal(-60);
        signalView5.updateSignal(-70);
        signalView6.updateSignal(-80);
        signalView7.updateSignal(-90);
        signalView8.updateSignal(-100);*/

    }

    private String getProvider() {
        String provider = "未知";
        try {
            String IMSI = telephonyManager.getSubscriberId();
            Log.d("test.......","IMSI---->"+IMSI);
            if (IMSI != null) {
                if (IMSI.startsWith("46000") || IMSI.startsWith("46002")
                        || IMSI.startsWith("46007")) {
                    provider = "中国移动";
                } else if (IMSI.startsWith("46001")) {
                    provider = "中国联通";
                } else if (IMSI.startsWith("46003")) {
                    provider = "中国电信";
                }
            } else {
                if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                    String operator = telephonyManager.getSimOperator();
                    Log.e("test.......","运营商"+operator);
                    if (operator != null) {
                        switch (operator) {
                            case "46000":
                            case "46002":
                            case "46007":
                                provider = "中国移动";
                                break;
                            case "46001":
                                provider = "中国联通";
                                break;
                            case "46003":
                                provider = "中国电信";
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return provider;
    }

    private void setDBM(SignalView view, int dbm) {
        view.updateSignal(dbm);
    }

    //查看记录
    private void openDiary() {
        intent = new Intent(MainActivity.this, DiaryActivity.class);
        startActivity(intent);
    }

    //截屏保存
    private boolean saveToSDCard(Bitmap bitmap, String dirName, String fileName) {
        //Toast.makeText(MainActivity.this, "正在截屏...", Toast.LENGTH_SHORT).show();
//        Toast.makeText(MainActivity.this, "正在截屏...", Toast.LENGTH_SHORT).show();
        //检查sdcard是否可用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(dirName);
            //目录不存在则创建
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(dirName + "/" + fileName);
            try {
                //文件不存在则创建
                if (!file.exists()) {
                    file.createNewFile();
                }

                //文件输出流
                FileOutputStream fos = new FileOutputStream(file);
                if (fos != null) {
                    //第一参数是图片格式，2：图片质量，3：输出流
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "存储卡不可用！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        try {
            locationManager.removeUpdates(locationListener);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "LocationListener里面的onLocationChanged的位置:" + location);
            updateView(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "LocationListener的onStatusChanged中，provider = " + provider
                    + ",status = " + status + ",extras = " + extras);
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d(TAG, "LocationProvider.AVAILABLE!");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d(TAG, "LocationProvider.OUT_OF_SERVICE!");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d(TAG, "LocationProvider.TEMPORARILY_UNAVAILABLE!");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "LocationListener的onProviderEnabled中，provider = " + provider);
            try {
                location = locationManager.getLastKnownLocation(provider);
                Log.d(TAG, "onProviderEnabled中，location = " + location);
                updateView(location);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "LocationListener的onProviderDisabled中，provider = " + provider);

        }
    };

    private void updateView(Location location) {
        Geocoder gc = new Geocoder(getContext());
        List<Address> addresses;
        String msg = "";
        Log.d(TAG, "updateView()的位置" + location);
        if (location != null) {
            try {
                addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Log.d(TAG, "地址：" + addresses);
                System.out.print("地址：" + addresses);
                if (addresses.size() > 0) {
                    msg = addresses.get(0).getAddressLine(0);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            et.setText(msg);
            ivCancel.setVisibility(View.VISIBLE);
        } else {
            et.getText().clear();
            et.setHint("定位中...");
        }
    }
    //截屏保存
    private Bitmap screenShot() {
        //获取屏幕中最顶层的View
        View view = getWindow().getDecorView();
        //强制构建绘图缓存
        view.buildDrawingCache();

        //获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
//        Log.d("状态栏高度：", "" + statusBarHeight);

        //获取屏幕高度和宽度
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        //允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        //去掉状态栏并转成Bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeight, width, height - statusBarHeight);
        //销毁缓存信息
        view.destroyDrawingCache();
        if (bitmap == null) return null;
        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    doPosition();
                }
                else {
                    Toast.makeText(getContext(),"无法获取定位权限！",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openDiary();
                }
                else {
                    Toast.makeText(getContext(),"无法访问SD卡！",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openDiary();
                }
                else {
                    Toast.makeText(getContext(),"无法获取修改SD卡内容权限！",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    doDecect();
                }
                else {
                    Toast.makeText(getContext(),"无法获取手机状态信息！",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_READ_EXTERNAL_STORAGE_PRTSC:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (!et.getText().toString().equals(new String(""))) {
                        if (saveToSDCard(screenShot(), imgSaveDirPath, et.getText().toString() + "-" + System.currentTimeMillis() + ".png"))
                            Toast.makeText(getContext(),"截屏成功！",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(),"截屏失败！",Toast.LENGTH_SHORT).show();
                    } else {
                        if (saveToSDCard(screenShot(), imgSaveDirPath, sdf.format(new Date()) + "-" + System.currentTimeMillis() + ".png"))
                            Toast.makeText(getContext(),"截屏成功！",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(),"截屏失败！",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(),"无法访问SD卡！",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE_PRTSC:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (!et.getText().toString().equals(new String(""))) {
                        if (saveToSDCard(screenShot(), imgSaveDirPath, et.getText().toString() + "-" + System.currentTimeMillis() + ".png"))
                            Toast.makeText(getContext(),"截屏成功！",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(),"截屏失败！",Toast.LENGTH_SHORT).show();
                    } else {
                        if (saveToSDCard(screenShot(), imgSaveDirPath, sdf.format(new Date()) + "-" + System.currentTimeMillis() + ".png"))
                            Toast.makeText(getContext(),"截屏成功！",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(),"截屏失败！",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(),"无法获取修改SD卡内容权限！",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Context getContext() {
        return this;
    }


}


package com.qianbajin.locationdemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity {

    private static final String[] PER = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    private AMapLocationClient mLocationClient;
    private LocationClient mBaiduClient;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvAmap = findViewById(R.id.tv_amap);
        TextView tvDu = findViewById(R.id.tv_du);
        if (checkPer()) {
//            gaoDe();
//            baiDu();
        }
        tvAmap.setOnClickListener(v -> gaoDe());
        tvDu.setOnClickListener(v -> baiDu());
        result = findViewById(R.id.tv_result);
    }

    /**
     * https://lbsyun.baidu.com/index.php?title=android-locsdk/guide/create-project/android-studio
     * https://blog.csdn.net/ssh159/article/details/78609690
     * 百度定位
     */
    private void baiDu() {
        if (mBaiduClient != null) {
            mBaiduClient.start();
            return;
        }
        //请在主线程中声明LocationClient类对象，该对象初始化需传入Context类型参数
        mBaiduClient = new LocationClient(getApplicationContext());
        //回调
        mBaiduClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();
        // 设置返回值的坐标类型
        option.setCoorType("bd09ll");
        // 是否打开GPS
        option.setOpenGps(true);
        // 设置定时定位的时间间隔。单位毫秒
        option.setScanSpan(20000);
        // 可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(true);

        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        //具体设置下面说明
        mBaiduClient.setLocOption(option);

        //定位的方式分为单次定位和定时定位，在LocationClientOption配置
//        mBaiduClient.stop();  //结束定位,可以在定位返回的时候暂停定位服务
        mBaiduClient.start();  //开始定位
    }

    private void toa(String s) {
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 200);
        toast.show();
    }

    /**
     * 高德定位
     * 定位成功，定位回调将按照定位结果返回如下几种响应码，用于区分本次定位的来源：
     * 0 定位失败 请通过AMapLocation.getErrorCode()方法获取错误码，并参考错误码对照表进行问题排查。
     * 响应码 说明 介绍 getLocationType()
     * 1 GPS定位结果 通过设备GPS定位模块返回的定位结果，精度较高，在10米－100米左右
     * 2 前次定位结果 网络定位请求低于1秒、或两次定位之间设备位置变化非常小时返回，设备位移通过传感器感知。
     * 4 缓存定位结果 返回一段时间前设备在同样的位置缓存下来的网络定位结果
     * 5 Wifi定位结果 属于网络定位，定位精度相对基站定位会更好，定位精度较高，在5米－200米之间。
     * 6 基站定位结果纯粹依赖移动、联通、电信等移动网络定位，定位精度在500米-5000米之间。
     * 8 离线定位结果
     * 9 最后位置缓存
     */
    private void gaoDe() {
        Log.d("MainActivity", "gaoDe");
        if (mLocationClient != null) {
            mLocationClient.startLocation();
            return;
        }
        // https://lbs.amap.com/api/android-location-sdk/guide/android-location/getlocation
        //声明定位回调监听器
        AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                String address = aMapLocation.toStr();
                Log.d("MainActivity", "aMapLocation:" + address);
                try {
                    result.setText(new JSONObject(address).toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                toa("amap");
            }
        };
        //初始化定位
        //声明AMapLocationClient类对象
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        AMapLocationClientOption option = new AMapLocationClientOption();

        // 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        //设置定位模式为AMapLocationMode.Device_Sensors，仅设备模式。
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        option.setOnceLocationLatest(true);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        option.setInterval(12000);
        //设置是否返回地址信息（默认返回地址信息）
        option.setNeedAddress(true);
        mLocationClient.setLocationOption(option);

        //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
        mLocationClient.stopLocation();
        mLocationClient.startLocation();
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f
            String s = "baidu latitude:" + latitude
                    + " longitude:" + longitude + " radius:" + radius;
            Log.d("MainActivity", s);
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            String coorType = location.getCoorType();

            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            int errorCode = location.getLocType();
            String s1 = "baidu errorCode:" + errorCode + " " + location.getLocTypeDescription() + " " + location.getAddrStr();
            Log.d("MainActivity", s1);
            result.setText(s + "  " + s1);
            toa("baidu");
        }

    }

    private boolean checkPer() {
        String[] supportedAbis = Build.SUPPORTED_ABIS;
        Log.d("MainActivity", "supportedAbis:" + Arrays.toString(supportedAbis));
        List<String> list = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String s : PER) {
                int i = checkSelfPermission(s);
                if (i != PackageManager.PERMISSION_GRANTED) {
                    list.add(s);
                }
            }
        }
        if (!list.isEmpty()) {
            ActivityCompat.requestPermissions(this, list.toArray(new String[0]), 100);
        }
        return list.isEmpty();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("MainActivity", "permissions:" + Arrays.toString(permissions) + " " + Arrays.toString(grantResults));
//        gaoDe();
//        baiDu();
    }

    @Override
    protected void onDestroy() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
        if (mBaiduClient != null) {
            mBaiduClient.stop();
        }

        super.onDestroy();
    }
}
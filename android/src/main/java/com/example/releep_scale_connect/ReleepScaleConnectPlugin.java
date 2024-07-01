package com.example.releep_scale_connect;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.example.releep_scale_connect.scan.DeviceDialog;
import com.example.releep_scale_connect.utils.T;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import aicare.net.cn.iweightlibrary.AiFitSDK;
import aicare.net.cn.iweightlibrary.bleprofile.BleProfileService;
import aicare.net.cn.iweightlibrary.bleprofile.BleProfileServiceReadyActivity;
import aicare.net.cn.iweightlibrary.entity.AlgorithmInfo;
import aicare.net.cn.iweightlibrary.entity.BM09Data;
import aicare.net.cn.iweightlibrary.entity.BM15Data;
import aicare.net.cn.iweightlibrary.entity.BodyFatData;
import aicare.net.cn.iweightlibrary.entity.BroadData;
import aicare.net.cn.iweightlibrary.entity.DecimalInfo;
import aicare.net.cn.iweightlibrary.entity.User;
import aicare.net.cn.iweightlibrary.entity.WeightData;
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig;
import aicare.net.cn.iweightlibrary.utils.L;
import aicare.net.cn.iweightlibrary.utils.ParseData;
import aicare.net.cn.iweightlibrary.wby.WBYService;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** ReleepScaleConnectPlugin */
public class ReleepScaleConnectPlugin implements FlutterPlugin, EventChannel.StreamHandler, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private FlutterActivity activity;
  private MethodChannel channel;
  private EventChannel stream_chanel;
  private EventChannel stream_chanel2;
  private DeviceDialog devicesDialog;
  private BinaryMessenger binaryMessenger;

  private WBYService.WBYBinder binder;
  private Context context;

  private boolean isNewBM15TestData;
  private boolean mGetVersion = false;
  private long mOldBM15DataTime = 0;
  private String mOldData = "";

  private List<User> userList = new ArrayList<>();
  private User user = null;

  private String[] permissionArray = new String[]{
          Manifest.permission.BLUETOOTH,
          Manifest.permission.BLUETOOTH_ADMIN,
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.INTERNET,
          Manifest.permission.ACCESS_NETWORK_STATE,
          Manifest.permission.BLUETOOTH_SCAN,
          Manifest.permission.BLUETOOTH_CONNECT
  };


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ///
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private static final String TAG = "BleProfileServiceReadyActivity";
  protected static final int REQUEST_ENABLE_BT = 2;
  private WBYService.WBYBinder mService;
  private boolean mIsScanning = false;
  private BluetoothAdapter adapter = null;

  private BroadcastReceiver mCommonBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      int did;
      if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
        did = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
        bluetoothStateChanged(did);
      } else {
        String result;
        if ("aicare.net.cn.fatscale.action.CONNECT_STATE_CHANGED".equals(action)) {
          did = intent.getIntExtra("aicare.net.cn.fatscale.extra.CONNECT_STATE", -1);
          result = intent.getStringExtra("aicare.net.cn.fatscale.extra.DEVICE_ADDRESS");
          onStateChanged(result, did);
        } else {
          String cmd;
          if ("aicare.net.cn.fatscale.action.CONNECT_ERROR".equals(action)) {
            cmd = intent.getStringExtra("aicare.net.cn.fatscale.extra.ERROR_MSG");
            int errCode = intent.getIntExtra("aicare.net.cn.fatscale.extra.ERROR_CODE", -1);
            onError(cmd, errCode);
          } else if ("aicare.net.cn.fatscale.action.WEIGHT_DATA".equals(action)) {
            WeightData weightData = (WeightData) intent.getSerializableExtra("aicare.net.cn.fatscale.extra.WEIGHT_DATA");
            onGetWeightData(weightData);
          } else if ("aicare.net.cn.fatscale.action.SETTING_STATUS_CHANGED".equals(action)) {
            did = intent.getIntExtra("aicare.net.cn.fatscale.extra.SETTING_STATUS", -1);
            onGetSettingStatus(did);
          } else if ("aicare.net.cn.fatscale.action.RESULT_CHANGED".equals(action)) {
            did = intent.getIntExtra("aicare.net.cn.fatscale.extra.RESULT_INDEX", -1);
            result = intent.getStringExtra("aicare.net.cn.fatscale.extra.RESULT");
            onGetResult(did, result);
          } else {
            boolean status;
            if ("aicare.net.cn.fatscale.action.FAT_DATA".equals(action)) {
              status = intent.getBooleanExtra("aicare.net.cn.fatscale.extra.IS_HISTORY", false);
              BodyFatData bodyFatData = (BodyFatData) intent.getSerializableExtra("aicare.net.cn.fatscale.extra.FAT_DATA");
              onGetFatData(status, bodyFatData);
            } else if ("aicare.net.cn.fatscale.action.AUTH_DATA".equals(action)) {
              byte[] sources = intent.getByteArrayExtra("aicare.net.cn.fatscale.extra.SOURCE_DATA");
              byte[] bleReturn = intent.getByteArrayExtra("aicare.net.cn.fatscale.extra.BLE_DATA");
              byte[] encrypt = intent.getByteArrayExtra("aicare.net.cn.fatscale.extra.ENCRYPT_DATA");
              boolean isEquals = intent.getBooleanExtra("aicare.net.cn.fatscale.extra.IS_EQUALS", false);
              onGetAuthData(sources, bleReturn, encrypt, isEquals);
            } else if ("aicare.net.cn.fatscale.action.DID".equals(action)) {
              did = intent.getIntExtra("aicare.net.cn.fatscale.extra.DID", -1);
              onGetDID(did);
            } else if ("aicare.net.cn.fatscale.action.DECIMAL_INFO".equals(action)) {
              DecimalInfo decimalInfo = (DecimalInfo) intent.getSerializableExtra("aicare.net.cn.fatscale.extra.DECIMAL_INFO");
              onGetDecimalInfo(decimalInfo);
            } else if ("aicare.net.cn.fatscale.action.CMD".equals(action)) {
              cmd = intent.getStringExtra("aicare.net.cn.fatscale.extra.CMD");
              onGetCMD(cmd);
            } else if ("aicare.net.cn.fatscale.action.ALGORITHM_INFO".equals(action)) {
              AlgorithmInfo algorithmInfo = (AlgorithmInfo) intent.getSerializableExtra("aicare.net.cn.fatscale.extra.ALGORITHM_INFO");
              onGetAlgorithmInfo(algorithmInfo);
            } else if ("aicare.net.cn.fatscale.action.ACTION_SET_MODE".equals(action)) {
              status = intent.getBooleanExtra("aicare.net.cn.fatscale.action.EXTRA_SET_MODE", false);
              onGetMode(status);
            }
          }
        }
      }

    }
  };
  private ServiceConnection mServiceConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName name, IBinder service) {
      WBYService.WBYBinder bleService = mService = (WBYService.WBYBinder) service;
      onServiceBinded(bleService);
      if (bleService.isConnected()) {
        onStateChanged(bleService.getDeviceAddress(), 1);
      }

    }

    public void onServiceDisconnected(ComponentName name) {
      mService = null;
      onServiceUnbinded();
    }
  };
  private Handler handler = new Handler();
  private static final int SCAN_DURATION = 60000;
  private Runnable startScanRunnable = new Runnable() {
    public void run() {
//      startScan();
    }
  };


  private Runnable stopScanRunnable = new Runnable() {
    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    public void run() {
      stopScan();
      handler.post(startScanRunnable);
    }
  };


  private BluetoothAdapter.LeScanCallback mLEScanCallback;


  @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
  private void bluetoothStateChanged(int state) {
    switch (state) {
      case 13:
        if (this.mService != null) {
          this.mService.disconnect();
        }
        if (ActivityCompat.checkSelfPermission(activity.getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
          // TODO: Consider calling
          //    ActivityCompat#requestPermissions
          // here to request the missing permissions, and then overriding
          //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
          //                                          int[] grantResults)
          // to handle the case where the user grants the permission. See the documentation
          // for ActivityCompat#requestPermissions for more details.
          return;
        }
        this.stopScan();
      default:
    }
  }

  private void onStateChanged(String deviceAddress, int state) {
    switch(state) {
      case 0:
        unbindService();
      default:
    }
  }

  private void unbindService() {
    try {
      if (mServiceConnection != null) {
       activity.unbindService(mServiceConnection);
      }

      this.mService = null;
      this.onServiceUnbinded();
    } catch (IllegalArgumentException var2) {
    }

  }

  private void onError(String var1, int var2) {

  }

  private void onGetWeightData(WeightData var1) {

  }

  protected void onGetSettingStatus(int var1) {

  }

  private void onGetResult(int var1, String var2) {

  }

  private void onGetFatData(boolean var1, BodyFatData var2) {

  }

  private void onGetAuthData(byte[] sources, byte[] bleReturn, byte[] encrypt, boolean isEquals) {
  }

  private void onGetDID(int did) {
  }

  private void onGetCMD(String cmd) {
  }

  private void onGetDecimalInfo(DecimalInfo var1) {

  }

  private void onGetAlgorithmInfo(AlgorithmInfo var1) {

  }

  protected void onGetMode(boolean status) {
  }

  private void bindService(String address) {
    Intent service = new Intent(activity.getContext(), WBYService.class);
    if (!TextUtils.isEmpty(address)) {
      service.putExtra("aicare.net.cn.fatscale.extra.DEVICE_ADDRESS", address);
      activity.startService(service);
    }

    activity.bindService(service, mServiceConnection, 0);
  }

  private void onInitialize() {
    BluetoothManager bluetoothManager = (BluetoothManager)activity.getSystemService(Context.BLUETOOTH_SERVICE);
    this.adapter = bluetoothManager.getAdapter();

  }

  private void initData() {
    user  = new User(1, 2, 28, 170, 768, 551);
    userList.add(user);
  }


  private static IntentFilter makeIntentFilter() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
    intentFilter.addAction("aicare.net.cn.fatscale.action.CONNECT_STATE_CHANGED");
    intentFilter.addAction("aicare.net.cn.fatscale.action.CONNECT_ERROR");
    intentFilter.addAction("aicare.net.cn.fatscale.action.WEIGHT_DATA");
    intentFilter.addAction("aicare.net.cn.fatscale.action.SETTING_STATUS_CHANGED");
    intentFilter.addAction("aicare.net.cn.fatscale.action.RESULT_CHANGED");
    intentFilter.addAction("aicare.net.cn.fatscale.action.FAT_DATA");
    intentFilter.addAction("aicare.net.cn.fatscale.action.AUTH_DATA");
    intentFilter.addAction("aicare.net.cn.fatscale.action.DID");
    intentFilter.addAction("aicare.net.cn.fatscale.action.DECIMAL_INFO");
    intentFilter.addAction("aicare.net.cn.fatscale.action.CMD");
    intentFilter.addAction("aicare.net.cn.fatscale.action.ALGORITHM_INFO");
    intentFilter.addAction("aicare.net.cn.fatscale.action.ACTION_SET_MODE");
    return intentFilter;
  }

  private void onServiceBinded(WBYService.WBYBinder var1) {
    binder = var1;
    L.e("2017-11-20", TAG + ", onServiceBinded: binder = " + binder);
  }

  private void onServiceUnbinded() {
    this.binder = null;
    L.e("2017-11-20", TAG + ", onServiceUnbinded");
  }

  @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
  public void startConnect(String address) {
    this.stopScan();
    this.bindService(address);
  }

  protected boolean isDeviceConnected() {
    return this.mService != null && this.mService.isConnected();
  }

  protected boolean ensureBLESupported() {
    return activity.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
  }

  protected boolean isBLEEnabled() {
    BluetoothManager bluetoothManager = (BluetoothManager)activity.getSystemService(Context.BLUETOOTH_SERVICE);
    BluetoothAdapter adapter = bluetoothManager.getAdapter();
    return adapter != null && adapter.isEnabled();
  }

  protected void showBLEDialog() {
    Intent enableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
    activity.startActivityForResult(enableIntent, 2);
  }

  public boolean isScanning() {
    return this.mIsScanning;
  }

  @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
  protected void startScan() {
    if (!AiFitSDK.getInstance().isInitOk()) {
      Log.e("AiFitSDK", "请先调用AiFitSDK.getInstance().init()");
      throw new SecurityException("请先调用AiFitSDK.getInstance().init().(Please call AiFitSDK.getInstance().init() first.)");
    } else {
      if (this.isBLEEnabled()) {
        if (!this.mIsScanning) {
          this.adapter.startLeScan(this.mLEScanCallback);
          this.mIsScanning = true;
          this.handler.postDelayed(this.stopScanRunnable, 60000L);
        }
      } else {
        this.showBLEDialog();
      }

    }
  }

  @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
  protected void stopScan() {
//    this.handler.removeCallbacks(this.startScanRunnable);
//    this.handler.removeCallbacks(this.stopScanRunnable);
    if (this.mIsScanning) {
      if (this.adapter != null) {
        this.adapter.stopLeScan(this.mLEScanCallback);
      }

      this.mIsScanning = false;
    }

  }

  protected void getAicareDevice(BroadData broadData) {
    if (broadData != null) {
      L.e(TAG, broadData.toString());
    }
  }


  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
//    YCBTClient.initClient(flutterPluginBinding.getApplicationContext(), true);
    AiFitSDK.getInstance().init (flutterPluginBinding.getApplicationContext());
    binaryMessenger = flutterPluginBinding.getBinaryMessenger();

  }

  @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("initStreamChannel")) {
     // startScan();
      initStreamChannel();
      result.success("initialStreamChanel ");
    }else if (call.method.equals("stopScan")) {
      stopScan();
//       this.adapter.stopLeScan();
      result.success("stopScan " + android.os.Build.VERSION.RELEASE);
    }else if (call.method.equals("connectReleepScale")) {
      String macAddress = call.argument("releepScaleMac");
      startConnect(macAddress);
      result.success(0);
    }
    else if (call.method.equals("disconnect")) {
    //  binder.disconnect();
//       this.adapter.stopLeScan();
      initStreamChannel();
      if (isDeviceConnected()) {
        this.binder.disconnect();
      }

      try {
        if (this.mCommonBroadcastReceiver != null) {
          activity.getApplication().unregisterReceiver(this.mCommonBroadcastReceiver);
        }

        this.unbindService();
      } catch (Exception var2) {
        var2.printStackTrace();
      }
      result.success("disconnected ");
    }else {
      result.notImplemented();
    }
  }

  private void initStreamChannel(){
    stream_chanel = new EventChannel(binaryMessenger, "scan_releep_scale");
    stream_chanel.setStreamHandler(this);

    stream_chanel2 = new EventChannel(binaryMessenger, "listen_releep_scale");
    stream_chanel2.setStreamHandler(new EventChannel.StreamHandler() {

      @Override
      public void onListen(Object arguments, EventChannel.EventSink events) {
        if (arguments.equals("listeningdata")) {
          activity.getApplication().registerReceiver(mCommonBroadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              String action = intent.getAction();
              int did;
              if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
                did = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                bluetoothStateChanged(did);
              } else {
                String result;
                if ("aicare.net.cn.fatscale.action.CONNECT_STATE_CHANGED".equals(action)) {
                  did = intent.getIntExtra("aicare.net.cn.fatscale.extra.CONNECT_STATE", -1);
                  result = intent.getStringExtra("aicare.net.cn.fatscale.extra.DEVICE_ADDRESS");
                  onStateChanged(result, did);
                } else {
                  String cmd;
                  if ("aicare.net.cn.fatscale.action.CONNECT_ERROR".equals(action)) {
                    cmd = intent.getStringExtra("aicare.net.cn.fatscale.extra.ERROR_MSG");
                    int errCode = intent.getIntExtra("aicare.net.cn.fatscale.extra.ERROR_CODE", -1);
                    onError(cmd, errCode);
                  } else if ("aicare.net.cn.fatscale.action.WEIGHT_DATA".equals(action)) {
                    WeightData weightData = (WeightData) intent.getSerializableExtra("aicare.net.cn.fatscale.extra.WEIGHT_DATA");
//                onGetWeightData(weightData);
                    L.e("onGetWeightData", "WEIGHT_DATA: " + weightData.getWeight());
                    if (weightData == null)
                      return;
                    L.i(TAG,"WeightData:"+weightData.toString());
                    String json = gson.toJson(weightData);
                    events.success(json);
                    if (weightData.getDeviceType() == AicareBleConfig.BM_15) {
                      if (weightData.getCmdType() != 3) {
                        long time = System.currentTimeMillis();
                        isNewBM15TestData = true;
                        if (weightData.toString().equalsIgnoreCase(mOldData)) {
                          if (time - mOldBM15DataTime > 1000) {
                            mOldBM15DataTime = time;
//                            showInfo(weightData.toString(), false);
                          }
                        } else {
                          mOldBM15DataTime = time;
//                          showInfo(weightData.toString(), false);
                        }
                        mOldData = weightData.toString();

                      }
                      if (weightData.getCmdType() == 3 && weightData.getAdc() > 0 && isNewBM15TestData) {
                        isNewBM15TestData = false;
                        BodyFatData bm15BodyFatData = AicareBleConfig.getBM15BodyFatData(weightData, user.getSex(), user.getAge(), user.getHeight());
//                        showInfo(bm15BodyFatData.toString(), true);
                      }
                    } else if (weightData.getCmdType() == 2 && binder != null && user != null) {
                      binder.syncUser(user);
                    }


                  } else if ("aicare.net.cn.fatscale.action.SETTING_STATUS_CHANGED".equals(action)) {
                    did = intent.getIntExtra("aicare.net.cn.fatscale.extra.SETTING_STATUS", -1);
                    onGetSettingStatus(did);
                  } else if ("aicare.net.cn.fatscale.action.RESULT_CHANGED".equals(action)) {
                    did = intent.getIntExtra("aicare.net.cn.fatscale.extra.RESULT_INDEX", -1);
                    result = intent.getStringExtra("aicare.net.cn.fatscale.extra.RESULT");
                    onGetResult(did, result);
                  } else {
                    boolean status;
                    if ("aicare.net.cn.fatscale.action.FAT_DATA".equals(action)) {
                      status = intent.getBooleanExtra("aicare.net.cn.fatscale.extra.IS_HISTORY", false);
                      BodyFatData bodyFatData = (BodyFatData) intent.getSerializableExtra("aicare.net.cn.fatscale.extra.FAT_DATA");
//                  onGetFatData(status, bodyFatData);
                      L.e("onGetFatData", "FAT_DATA: " + bodyFatData.getWeight());
                      boolean deviceConnected = isDeviceConnected();
                      if (deviceConnected) {
                        if (binder != null && bodyFatData.getAdc() != 0) {
                          binder.updateUser(user);
                        }
                      } else {
                        L.i(TAG, "SDK判断到连接断开");
                      }
                      String json = gson.toJson(bodyFatData);
                      events.success(json);
                    } else if ("aicare.net.cn.fatscale.action.AUTH_DATA".equals(action)) {
                      byte[] sources = intent.getByteArrayExtra("aicare.net.cn.fatscale.extra.SOURCE_DATA");
                      byte[] bleReturn = intent.getByteArrayExtra("aicare.net.cn.fatscale.extra.BLE_DATA");
                      byte[] encrypt = intent.getByteArrayExtra("aicare.net.cn.fatscale.extra.ENCRYPT_DATA");
                      boolean isEquals = intent.getBooleanExtra("aicare.net.cn.fatscale.extra.IS_EQUALS", false);
                      onGetAuthData(sources, bleReturn, encrypt, isEquals);
                    } else if ("aicare.net.cn.fatscale.action.DID".equals(action)) {
                      did = intent.getIntExtra("aicare.net.cn.fatscale.extra.DID", -1);
                      onGetDID(did);
                    } else if ("aicare.net.cn.fatscale.action.DECIMAL_INFO".equals(action)) {
                      DecimalInfo decimalInfo = (DecimalInfo) intent.getSerializableExtra("aicare.net.cn.fatscale.extra.DECIMAL_INFO");
                      onGetDecimalInfo(decimalInfo);
                    } else if ("aicare.net.cn.fatscale.action.CMD".equals(action)) {
                      cmd = intent.getStringExtra("aicare.net.cn.fatscale.extra.CMD");
                      onGetCMD(cmd);
                    } else if ("aicare.net.cn.fatscale.action.ALGORITHM_INFO".equals(action)) {
                      AlgorithmInfo algorithmInfo = (AlgorithmInfo) intent.getSerializableExtra("aicare.net.cn.fatscale.extra.ALGORITHM_INFO");
                      onGetAlgorithmInfo(algorithmInfo);
                    } else if ("aicare.net.cn.fatscale.action.ACTION_SET_MODE".equals(action)) {
                      status = intent.getBooleanExtra("aicare.net.cn.fatscale.action.EXTRA_SET_MODE", false);
                      onGetMode(status);
                    }
                  }
                }
              }
            }
          }, makeIntentFilter());
        }
      }

      @Override
      public void onCancel(Object arguments) {
        //  stream_chanel2.setStreamHandler(null);

      }
    });

  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);


  }

  ArrayList listVal = new ArrayList();
  private Gson gson = new Gson();

  @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {

    if (arguments.equals("scan")) {

//      stopScan();
      if (!AiFitSDK.getInstance().isInitOk()) {
        Log.e("AiFitSDK", "请先调用AiFitSDK.getInstance().init()");
        throw new SecurityException("请先调用AiFitSDK.getInstance().init().(Please call AiFitSDK.getInstance().init() first.)");
      } else {
        if (this.isBLEEnabled()) {
          if (!this.mIsScanning) {
            this.adapter.startLeScan(this.mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
              @Override
              public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//                L.e("BleProfileServiceReadyActivity", "onLeScan");
                if (device != null) {
//                  L.e("BleProfileServiceReadyActivity", "address: " + device.getAddress() + "; name: " + device.getName());
                  final BroadData broadData = AicareBleConfig.getBroadData(device, rssi, scanRecord);
                  if (broadData != null) {
                    activity.runOnUiThread(new Runnable() {
                      public void run() {
                        if(!listVal.contains(broadData)) {
                          listVal.add(broadData);
                        }
                        String json = gson.toJson(listVal);
                        events.success(json);
//              getAicareDevice(broadData);
                      }
                    });
                  }
                }
              }
            });
            this.mIsScanning = true;
            this.handler.postDelayed(this.stopScanRunnable, 60000L);
          }
        } else {
          this.showBLEDialog();
        }

      }

    }


  }

  @Override
  public void onCancel(Object arguments) {

  //  stream_chanel.setStreamHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull @NotNull ActivityPluginBinding binding) {
    L.e("TAG", "onAttachedToActivity");

    activity = (FlutterActivity) binding.getActivity();

    onInitialize();
    initData();
    bindService((String)null);


    channel = new MethodChannel(binaryMessenger, "releep_scale_connect");
    channel.setMethodCallHandler(this);

    boolean backBoolean = PermissionUtils.checkPermissionArray(activity.getContext(), permissionArray, 3);

    activity.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    Intent enableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
    activity.startActivityForResult(enableIntent, 2);
    boolean isInitOk = AiFitSDK.getInstance().isInitOk();
    Log.d("TAG", "isInitOk: ");
//    startScan();
  }

  private void initPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      ActivityCompat.requestPermissions(activity.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    L.e("TAG", "onDetachedFromActivityForConfigChanges");
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull @NotNull ActivityPluginBinding binding) {
    L.e("TAG", "onReattachedToActivityForConfigChanges");
  }


  @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
  @Override
  public void onDetachedFromActivity() {
    L.e("TAG", "onDetachedFromActivity");
    stopScan();
    if (isDeviceConnected()) {
      this.binder.disconnect();
    }

    try {
      if (this.mCommonBroadcastReceiver != null) {
        activity.getApplication().unregisterReceiver(this.mCommonBroadcastReceiver);
      }

      this.unbindService();
    } catch (Exception var2) {
      var2.printStackTrace();
    }

  }

}

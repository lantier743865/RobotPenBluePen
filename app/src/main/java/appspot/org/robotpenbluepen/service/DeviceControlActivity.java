package appspot.org.robotpenbluepen.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import appspot.org.robotpenbluepen.BluetoothLeService;
import appspot.org.robotpenbluepen.R;
import appspot.org.robotpenbluepen.model.DrawView;
import appspot.org.robotpenbluepen.model.PointObject;
import appspot.org.robotpenbluepen.model.entity.PenDataUtil;
import appspot.org.robotpenbluepen.model.entity.symbol.SceneType;

/**
 * 对于一个BLE设备，该activity向用户提供设备连接，显示数据，显示GATT服务和设备的字符串支持等界面，
 * 另外这个activity还与BluetoothLeService通讯，反过来与Bluetooth LE API进行通讯
 */

public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    //连接状态
    private TextView mConnectionState;  //连接状态
    private String mDeviceAddress;  //设备的地址
    private RelativeLayout drawRelative;  //画布
    private Button mClearDraw;      //清空画布

    private BluetoothLeService mBluetoothLeService;

    // 管理服务的生命周期
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            Log.e(TAG, "Unable to initialize Bluetooth");
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);  //设备连接
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;  //设备断开连接
        }
    };

    // 处理服务所激发的各种事件
    // ACTION_GATT_CONNECTED: 连接一个GATT服务
    // ACTION_GATT_DISCONNECTED:  从GATT服务中断开连接
    // ACTION_GATT_SERVICES_DISCOVERED: 查找GATT服务
    // ACTION_DATA_AVAILABLE: This can be a result of read
    //                        or notification operations.从服务中接受数据
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            }
            //发现有可支持的服务
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mBluetoothLeService.getSupportedGattServices(SampleGattAttributes.SERVICE_UUID); //写数据的服务和characteristic

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //显示数据
                //将数据显示在mDataField上
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                checkReceiveData(data);
            }
        }
    };

    //  画笔的数据处理
    public void checkReceiveData(byte[] data) {
        if (data == null || data.length <= 0) {
            return;
        }
        switch (data[1]) {
            case -127:
                byte[] penData = new byte[data[2] & 255];
                System.arraycopy(data, 3, penData, 0, penData.length);
                this.putPenDataBuffer(penData);
                break;
            case -128:
                break;
        }
    }

    public void putPenDataBuffer(final byte[] data) {
        PointObject[] list = PenDataUtil.getPointList(data);
        if (list == null || list.length == 0) {
            return;
        }
        for (int i = 0; i < list.length; i++) {
            PointObject object = list[i];
            if (object != null && object.originalX != 0f) {
                float orgX = (float) ((double) object.originalX * aDouble);
                float orgY = (float) ((double) object.originalY * aDouble);

                if (object.isRoute) {
                    DrawView.LINE_TYPE = true;
                    Log.e("object.isRoute", "onDescriptorRead: " + object.toJsonString());
                    drawView.setDrawLine(orgX, orgY, object.pressureValue);
                } else {
                    DrawView.LINE_TYPE = false;
                    Log.e("object.isRoute = false", "onDescriptorRead: ");
                    drawView.setTouchDown(orgX, orgY);
                }
                //更新绘制
                drawView.invalidate();
            }
        }
    }

    //手机的宽高
    private float screenHeight = 0;  //  屏幕的高度
    private double aDouble;

    //蓝牙笔设备的宽高
    int height = (int) PointObject.getHeight(SceneType.ELITE);  //屏幕的高度
    private DrawView drawView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        drawView = new DrawView(DeviceControlActivity.this);
        //手机的宽高
        screenHeight = drawView.getWm().getDefaultDisplay().getHeight();  //  屏幕的高度

        //这个为什么要成7／4 因为屏幕的比例等比例缩放
        aDouble = ((screenHeight * 7) / 4) / height;

        final Intent intent = getIntent();
//        mDeviceAddress = "5D:C8:A4:F6:DB:41";
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        drawRelative = (RelativeLayout) findViewById(R.id.draw_view); //设置画布
        drawRelative.addView(drawView); //添加画布
        mClearDraw = (Button) findViewById(R.id.clear_draw);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //清空画布
        mClearDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setClearCanvas();
                //更新绘制
                drawView.invalidate();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    //当用手画布时调用这个方法
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawView.touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                drawView.touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                drawView.touchDown(event);
        }
        //更新绘制
        drawView.invalidate();
        return true;
    }

}


package appspot.org.robotpenbluepen;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.UUID;

import appspot.org.robotpenbluepen.model.PointObject;
import appspot.org.robotpenbluepen.model.entity.PenDataUtil;
import appspot.org.robotpenbluepen.service.SampleGattAttributes;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";  // 状态正在连接
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED"; //
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "EXTRA_DATA";


    private BluetoothGattCharacteristic mPenDataCharacteristic;  //数据特征
    private BluetoothGattCharacteristic mPenWriteCharacteristic;  //笔的特征

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            //收到设备notify值 （设备上报值）
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            Iterator e = gatt.getServices().iterator();
            while (e.hasNext()) {
                BluetoothGattService service = (BluetoothGattService) e.next();
                if (service.getUuid().equals(SampleGattAttributes.SERVICE_UUID)) {
                    BluetoothLeService.this.mPenDataCharacteristic = service.getCharacteristic(SampleGattAttributes.PEN_DATA_UUID);
                    BluetoothLeService.this.mPenWriteCharacteristic = service.getCharacteristic(SampleGattAttributes.PEN_WRITE_UUID);
                    break;
                }
            }
            setCharacteristicNotification(mPenDataCharacteristic, true);
            //setCharacteristicNotification(mPenWriteCharacteristic, true);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                System.out.println("onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] mValue = characteristic.getValue();
            Log.e(TAG, "onServicesDiscovered received: " + mValue.toString());
            //读取到值，在这里读数据
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.e(TAG, "onDescriptorRead: " + status);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // Intent 传递数据到 Activity 并显示
        if (SampleGattAttributes.PEN_DATA_UUID.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.e(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // 使用一个给定的设备后，你应该确保bluetoothgatt()调用。这样资源才能得到适当的清理。在这个特定的例子中，close()是当UI从服务断开时调用。
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.e(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e(TAG, "Trying to create a new connection.");

        if (mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                Log.e(TAG, "Connect succeed.");
            } else {
                Log.e(TAG, "Connect error.");
            }
        }
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        Log.e("device.getBondState==", device.getBondState() + "");
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * 使用一个给定的BLE设备后，应用程序必须调用这个方法来确保资源
     * 正确释放。
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    /**
     * 要求在一个给定的{ @代码bluetoothgattcharacteristic读}。读取结果报告
     * 异步通过{ @代码bluetoothgattcallback # oncharacteristicread（android.bluetooth.bluetoothgatt，android.bluetooth.bluetoothgattcharacteristic，int）}
     * 回调。
     *
     * @param characteristic 特征
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    //获取相关UUID服务
    public BluetoothGattService getSupportedGattServices(UUID uuid) {
        BluetoothGattService mBluetoothGattService;
        if (mBluetoothGatt == null) return null;
        mBluetoothGattService = mBluetoothGatt.getService(uuid);
        return mBluetoothGattService;
    }

    /**
     * @param characteristic 根据服务所需要监听的特征
     * @param enabled        状态
     * @return 发送通知
     */
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        boolean result = false;
        this.mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(SampleGattAttributes.NOTIFICATION_DESCRIPTOR_UUID);
        if (descriptor == null) {
            for (int step = 0; descriptor == null && step < 20; descriptor = characteristic.getDescriptor(SampleGattAttributes.NOTIFICATION_DESCRIPTOR_UUID)) {
                ++step;
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException var7) {
                    var7.printStackTrace();
                }
            }
        }
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            result = this.mBluetoothGatt.writeDescriptor(descriptor);
        } else {
            Log.e(TAG, "setCharacteristicNotification descriptor is null");
        }
        return result;
    }



    public  void putPenDataBuffer(final byte[] data) {
        PointObject[] list = PenDataUtil.getPointList(data);

        String string = "";
        for (int i = 0; i < data.length; i++) {
            string = string + "    " + data[i];
        }

        if (data[0] == 2 && data[1] == 0) {
            if (data[2] == -1 && data[3] == 33 && data[4] == -1 && data[5] == 20) {
                return;
            }
            Log.e(TAG, "笔离开板子");
        }
        if (this.initPenDataBuffer() && this.mPenBuffer.position() + this.DATA_IN_LEN <= 2048) {
            this.mPenBuffer.put(data);
        }
    }

    protected int DATA_IN_LEN = 8;

    private ByteBuffer mPenBuffer;

    protected boolean initPenDataBuffer() {
        boolean result = false;
        if (this.mPenBuffer == null) {
            try {
                this.mPenBuffer = ByteBuffer.allocate(2048);
                this.mPenBuffer.order(ByteOrder.nativeOrder());
                result = true;
            } catch (IllegalArgumentException var3) {
                var3.printStackTrace();
            }
        } else {
            result = true;
        }
        return result;
    }

}

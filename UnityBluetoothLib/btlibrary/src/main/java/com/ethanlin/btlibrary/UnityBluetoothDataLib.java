package com.ethanlin.btlibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.ethanlin.config.GlobalConfig;
import com.ethanlin.utils.Utils;
import com.unity3d.player.UnityPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UnityBluetoothDataLib {

    /**
     * 實體
     */
    private static UnityBluetoothDataLib mInstance;
    /**
     * 實體
     */
    public static UnityBluetoothDataLib getInstance() {
        if (mInstance == null) {
            mInstance = new UnityBluetoothDataLib();
        }
        return mInstance;
    }

    private Context mContext;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private ImuDataHandler mImuDataHandler = null;

    /**
     * 收到的RawData
     */
    private byte[] mReceivedDta;
    public static ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    public static HashMap<String, BluetoothDevice> deviceMap = new HashMap<String, BluetoothDevice>();
    private static HashMap<String, BluetoothGatt> mDeviceGattMap = new HashMap<String, BluetoothGatt>();

    /**
     * 是否正在掃瞄
     */
    private boolean mIsScanning = false;

    /**
     * 當前已連接的裝置位址
     */
    private String mCurrentConnectedDeviceAddress;

    public void initNativeLibAndBluetoothManager(Context aContext) {
        mContext = aContext;

        mBluetoothManager = mContext.getSystemService(BluetoothManager.class);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mImuDataHandler = new ImuDataHandler();

        mIsScanning = false;

        if (isBluetoothAdapterEnable()) {
            android.util.Log.d(GlobalConfig.DEBUG_TAG, "棒棒，藍牙已開啟了！");
        } else {
            android.util.Log.d(GlobalConfig.DEBUG_TAG, "藍牙沒開，趕快去開啦！");
            enableBluetoothEnable();
        }
    }

    /**
     * 檢查是否有External storage permission
     */
    public boolean getIsExternalStorageManager() { return Environment.isExternalStorageManager(); }

    /**
     * request external storage permission
     */
    public void requestManageExternalStoragePermission() { PermissionHelper.requestManageExternalStoragePermission(); }

    /**
     * is bluetooth adapter enable
     * @return true: enable, false: disable
     */
    public boolean isBluetoothAdapterEnable() { return mBluetoothAdapter.isEnabled(); }

    /**
     * enable bluetooth adapter
     */
    public void enableBluetoothEnable() {
        if (!isBluetoothAdapterEnable()) {
            if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission();
                return;
            }

            // 打開系統藍牙設定頁
            UnityPlayer.currentActivity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), GlobalConfig.PERMISSIONS_REQUEST_BLUETOOTH);
        }
    }
    /**
     * 要求藍牙權限
     */
    public void requestBluetoothPermission() { PermissionHelper.requestBluetoothPermissions(); }

    /**
     * start scan
     */
    public void scanBluetoothDevices() {
        deviceList.clear();
        deviceMap.clear();

        if (isBluetoothAdapterEnable()) {
            if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission();
                return;
            }
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
            mIsScanning = true;
            android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("is scanning: %b", true));
            // mBluetoothAdapter.getBluetoothLeScanner().startScan(createScanFilters(), createScanSettings(), mLeScanCallback);
        } else {
            enableBluetoothEnable();
        }
    }
    /**
     * stop scan
     */
    public void stopScan() {
        if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermission();
            return;
        }
        mIsScanning = false;
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("stop scan, is scanning: %b", mIsScanning));
        if (deviceList.isEmpty()) {
            UnityPlayer.UnitySendMessage(GlobalConfig.UNITY_GAME_OBJECT_NAME, "whenStopScan", "0");
        }
    }

    /**
     * 掃瞄藍牙的Callback
     */
    private final ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice btDevice = result.getDevice();
            if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission();
                return;
            }

            if (btDevice.getName() == null) return;

            if (!TextUtils.isEmpty(btDevice.getName())) {
                if (btDevice.getName().contains(GlobalConfig.SC_BLE_NAME) || btDevice.getName().contains(GlobalConfig.SC_BLE_NAME1) || btDevice.getName().contains(GlobalConfig.SC_BLE_NAME2)) {

                    while (!deviceMap.containsKey(btDevice.getAddress()) && mIsScanning) {
                        deviceList.add(btDevice);
                        deviceMap.put(btDevice.getAddress(), btDevice);
                        // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("onScanResult: %s, %s", btDevice.getName(), btDevice.getAddress()));
                    }

                    if (!deviceList.isEmpty()) {
                        for (int i = 0; i < deviceList.size(); ++i) {
                            @SuppressLint("DefaultLocale") String deviceInfo = String.format("%d#%s#%s", deviceList.size(), deviceList.get(i).getName(), deviceList.get(i).getAddress());
                            UnityPlayer.UnitySendMessage(GlobalConfig.UNITY_GAME_OBJECT_NAME, "detectedDevices", deviceInfo);
                        }
                    }

                    // 跟Unity報告有掃瞄到裝置
                    // Utils.unitySendMessage("onScanResult");
                    // 掃瞄時間由Unity直接實作
                    // stopScan();
                }
            } else {
                android.util.Log.e(GlobalConfig.DEBUG_TAG, "Device name is empty!!!");
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            android.util.Log.e(GlobalConfig.DEBUG_TAG, "onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            android.util.Log.e(GlobalConfig.DEBUG_TAG, String.format("onScanFailed Error Code: %d", errorCode));
            // 跟Unity報告找不到裝置
            UnityPlayer.UnitySendMessage(GlobalConfig.UNITY_GAME_OBJECT_NAME, "detectedDevices", "0#null#null");
        }
    };

    /**
     * 連接藍牙裝置 with device address
     */
    public void connectBluetoothDevice(String aDeviceAddress) {
        android.util.Log.d(GlobalConfig.DEBUG_TAG, "connectBluetoothDevice with device address");
        BluetoothGatt gatt = null;
        if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermission();
            return;
        }

        mCurrentConnectedDeviceAddress = aDeviceAddress;
        gatt = Objects.requireNonNull(deviceMap.get(aDeviceAddress)).connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        if (gatt != null) {
            // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("connectBluetoothDevice gatt is %s", gatt));
            mDeviceGattMap.clear();
            mDeviceGattMap.put(aDeviceAddress, gatt);
            // Utils.unitySendMessage("devicesConnected");
        } else {
            android.util.Log.e(GlobalConfig.DEBUG_TAG, "oops, gatt is null.");
        }
    }
    /**
     * 連結藍牙的Callback
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("onConnectionStateChange newState: %d", newState));

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                android.util.Log.d(GlobalConfig.DEBUG_TAG, "onConnectionStateChange STATE: CONNECTED 已連接");
                UnityPlayer.UnitySendMessage(GlobalConfig.UNITY_GAME_OBJECT_NAME, "receiveMessageFromNative", "devicesConnected");
                if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothPermission();
                    return;
                }

                if (gatt != null) {
                    // String deviceAddress = gatt.getDevice().getAddress();
                    gatt.discoverServices();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                android.util.Log.d(GlobalConfig.DEBUG_TAG, "onConnectionStateChange STATE: DISCONNECTED 已斷開連線");
                UnityPlayer.UnitySendMessage(GlobalConfig.UNITY_GAME_OBJECT_NAME, "receiveMessageFromNative", "devicesDisconnected");
                if (gatt != null) {
                    // String deviceAddress = gatt.getDevice().getAddress();
                    gatt.close();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission();
                return;
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (gatt != null) {
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                    List<BluetoothGattService> services = gatt.getServices();
                    if (services != null) {
                        String deviceAddress = gatt.getDevice().getAddress();
                        for (BluetoothGattService service : services) {
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("onServicesDiscovered DiscoveredService address: %s, uuid: %s, characteristic: %s", deviceAddress, service.getUuid(), characteristic.getUuid()));
                            }
                        }

                        onServiceDiscoveredFinished();
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        /*
        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);
        }
        */

        @SuppressLint("DefaultLocale")
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if (characteristic != null) {
                mReceivedDta = characteristic.getValue();

                if (mReceivedDta != null && mReceivedDta.length == 36) {
                    if (mImuDataHandler != null) {
                        mImuDataHandler.setByteArrayData(mReceivedDta);
                    } else {
                        android.util.Log.e(GlobalConfig.DEBUG_TAG, "ImuDataHandler is null...");
                    }
                }
            }
        }

        /*
        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            super.onCharacteristicChanged(gatt, characteristic, value);
        }
        */

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("onDescriptorRead status: %d", status));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("onCharacteristicWrite gatt address: %s, characteristic uuid: %s, status: %d", gatt.getDevice().getAddress(), characteristic.getUuid(), status));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("onDescriptorWrite gatt address: %s, descriptor uuid: %s, status: %d", gatt.getDevice().getAddress(), descriptor.getUuid(), status));
        }
    };

    private void onServiceDiscoveredFinished() {
        mImuDataHandler.initQuaternionMapping();

        // 開始訂閱
        // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("onServiceDiscoveredFinished 掃瞄完畢, 開始訂閱=> %s", mCurrentConnectedDeviceAddress));
        setCharacteristicNotification(mCurrentConnectedDeviceAddress, GlobalConfig.SUBSCRIBED_SERVICE, GlobalConfig.SUBSCRIBED_CHARACTERISTIC, true);
    }
    private void setCharacteristicNotification(String aDeviceAddress, String aService, String aCharacteristic, boolean aEnable) {
        BluetoothGatt gatt = mDeviceGattMap.get(aDeviceAddress);
        // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("setCharacteristicNotification gatt is %s", gatt));
        if (gatt != null) {
            UUID serviceUUID = Utils.getUUID(aService);
            if (serviceUUID != null) {
                BluetoothGattService service = gatt.getService(serviceUUID);
                if (service != null) {
                    // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("service uuid is %s", service.getUuid()));
                    UUID characteristicUUID = Utils.getUUID(aCharacteristic);
                    // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("characteristicUUID is %s", characteristicUUID));
                    if (characteristicUUID != null) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
                        if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            requestBluetoothPermission();
                            return;
                        }
                        if (characteristic != null) {
                            if (gatt.setCharacteristicNotification(characteristic, true)) {
                                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GlobalConfig.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                                if (descriptor != null) {
                                    int characteristicProperties = characteristic.getProperties();
                                    byte[] valueToSend = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                                    if (aEnable) {
                                        if ((characteristicProperties & 0x10) == 16) {
                                            valueToSend = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                                        } else if ((characteristicProperties & 0x20) == 32) {
                                            valueToSend = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
                                        }
                                    }

                                    // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("setCharacteristicNotification %s", Arrays.toString(valueToSend)));

                                    if (valueToSend.length > 0) {
                                        // android.util.Log.d(GlobalConfig.DEBUG_TAG, String.format("Notification Description: %d", valueToSend.length));
                                        descriptor.setValue(valueToSend);
                                        if (!gatt.writeDescriptor(descriptor)) {
                                            android.util.Log.e(GlobalConfig.DEBUG_TAG, "Error~Failed to write characteristic descriptor");
                                        } else {
                                            android.util.Log.d(GlobalConfig.DEBUG_TAG, "Notification setup succeeded");
                                        }
                                    } else {
                                        android.util.Log.e(GlobalConfig.DEBUG_TAG, "valueToSend is empty");
                                    }

                                } else {
                                    android.util.Log.e(GlobalConfig.DEBUG_TAG, "descriptor is null");
                                }
                            }
                        } else {
                            android.util.Log.e(GlobalConfig.DEBUG_TAG, "characteristic is null");
                        }
                    } else {
                        android.util.Log.e(GlobalConfig.DEBUG_TAG, "characteristicUUID is null");
                    }
                } else {
                    android.util.Log.e(GlobalConfig.DEBUG_TAG, "service is null");
                }
            } else {
                android.util.Log.e(GlobalConfig.DEBUG_TAG, "serviceUUID is null");
            }
        } else {
            android.util.Log.e(GlobalConfig.DEBUG_TAG, "setCharacteristicNotification gatt is null");
        }
    }
}

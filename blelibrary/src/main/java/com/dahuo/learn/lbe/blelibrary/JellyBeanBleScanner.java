package com.dahuo.learn.lbe.blelibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.dahuo.learn.lbe.blelibrary.constant.BleScanState;
import com.dahuo.learn.lbe.blelibrary.utils.BleLog;

/**
 * @author YanLu
 * @since 15/9/14
 *
 * Android4.3 和 4.4 的蓝牙扫描
 */

public class JellyBeanBleScanner extends BaseBleScanner {
    private final static String TAG = JellyBeanBleScanner.class.getName();

    public BluetoothAdapter mBluetooth = null;
    private SimpleScanCallback mScanCallback = null;
    public JellyBeanBleScanner(Context context,SimpleScanCallback callback) {
        mScanCallback = callback;
        BluetoothManager bluetoothMgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetooth = bluetoothMgr.getAdapter();
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mScanCallback.onBleScan(device, rssi, scanRecord);
        }
    };



    @SuppressWarnings(value={"deprecation"})
    @Override
    public void onStartBleScan(long timeoutMillis) {
        long delay = timeoutMillis == 0 ? defaultTimeout : timeoutMillis;
        if(mBluetooth != null) {
            isScanning = mBluetooth.startLeScan(leScanCallback);
            timeoutHandler.postDelayed(timeoutRunnable, delay);
            BleLog.i(TAG, "mBluetooth.startLeScan() " + isScanning);
        } else {
            mScanCallback.onBleScanFailed(BleScanState.BLUETOOTH_OFF);//蓝牙 未开启
        }
    }

    @SuppressWarnings(value={"deprecation"})
    @Override
    public void onStartBleScan( ) {//一直扫描
        if (mBluetooth != null) {
            isScanning = mBluetooth.startLeScan(leScanCallback);
            BleLog.i(TAG, "mBluetooth.startLeScan() " + isScanning);
        } else {
            mScanCallback.onBleScanFailed(BleScanState.BLUETOOTH_OFF);//蓝牙 未开启
        }
    }

    @SuppressWarnings(value={"deprecation"})
    @Override
    public void onStopBleScan() {
        isScanning = false;
        if (mBluetooth != null) {
            mBluetooth.stopLeScan(leScanCallback);
        }
    }

    @Override
    public void onBleScanFailed(BleScanState scanState) {
        mScanCallback.onBleScanFailed(scanState);//扫描设备失败~
    }
}

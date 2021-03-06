package com.dahuo.learn.lbe.blelibrary;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;

import com.dahuo.learn.lbe.blelibrary.constant.BleScanState;
import com.dahuo.learn.lbe.blelibrary.utils.BleLog;

import java.util.List;

/**
 * @author YanLu
 * @since 15/9/14
 *
 * Android5.0以后 的蓝牙扫描
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopBleScanner extends BaseBleScanner {
    private final static String TAG = LollipopBleScanner.class.getName();


    private BluetoothLeScanner mBluetoothScanner = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private SimpleScanCallback mScanCallback = null;

    public LollipopBleScanner(SimpleScanCallback callback) {
        mScanCallback = callback;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
    }

    @SuppressWarnings(value = {"deprecation"})
    @Override
    public void onStartBleScan(long timeoutMillis) {
        long delay = timeoutMillis == 0 ? defaultTimeout : timeoutMillis;
        if (mBluetoothScanner != null && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            try {
                mBluetoothScanner.startScan(scanCallback);
                isScanning = true;
            } catch (Exception e){
                isScanning = false;
                BleLog.e(TAG, e.toString());
            }
            timeoutHandler.postDelayed(timeoutRunnable, delay);
        } else {
            mScanCallback.onBleScanFailed(BleScanState.BLUETOOTH_OFF);//蓝牙 未开启
        }
        BleLog.i(TAG, "mBluetoothScanner.startScan()");
    }

    @SuppressWarnings(value = {"deprecation"})
    @Override
    public void onStartBleScan() {
        if (mBluetoothScanner != null && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            try {
                mBluetoothScanner.startScan(scanCallback);
                isScanning = true;
            } catch (Exception e){
                isScanning = false;
                mScanCallback.onBleScanFailed(BleScanState.BLUETOOTH_OFF);//蓝牙 未开启
                BleLog.e(TAG, e.toString());
            }
        } else {
            mScanCallback.onBleScanFailed(BleScanState.BLUETOOTH_OFF);//蓝牙 未开启
        }
        BleLog.i(TAG, "mBluetoothScanner.startScan()");
    }

    @SuppressWarnings(value = {"deprecation"})
    @Override
    public void onStopBleScan() {
        isScanning = false;
        if (mBluetoothScanner != null && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            try {
                mBluetoothScanner.stopScan(scanCallback);
            } catch (Exception e) {
                BleLog.e(TAG, e.toString());
            }
        }
    }


    @Override
    public void onBleScanFailed(BleScanState scanState) {
        mScanCallback.onBleScanFailed(scanState);//扫描设备超时~
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BleLog.i(TAG, "onScanResult: " + callbackType + " ScanResult:" + result);
            if (result.getScanRecord() != null) {
                mScanCallback.onBleScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            BleLog.i(TAG, "onBatchScanResults()");
        }

        @Override
        public void onScanFailed(int errorCode) {
            //会返回 错误码 3 忽略
            if(errorCode != 3 && errorCode != 1 ) {
                BleLog.i(TAG, "onScanFailed: " + errorCode);
                mScanCallback.onBleScanFailed(BleScanState.newInstance(errorCode));
            }
        }
    };
}

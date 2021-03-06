package com.dahuo.learn.lbe.bluetoothletutorial.fragment;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dahuo.learn.lbe.blelibrary.BleScanner;
import com.dahuo.learn.lbe.blelibrary.SimpleScanCallback;
import com.dahuo.learn.lbe.blelibrary.constant.BleScanState;
import com.dahuo.learn.lbe.blelibrary.utils.BleLog;
import com.dahuo.learn.lbe.blelibrary.utils.HexUtil;
import com.dahuo.learn.lbe.bluetoothletutorial.R;
import com.dahuo.learn.lbe.bluetoothletutorial.adapter.BleDeviceAdapter;
import com.dahuo.learn.lbe.bluetoothletutorial.constant.AppConstants;
import com.dahuo.learn.lbe.bluetoothletutorial.model.BleDevice;
import com.dahuo.learn.lbe.bluetoothletutorial.model.FavouriteInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Yan Lu
 * @since 2015-07-23
 */

public class BleDevicesFragment extends BaseFragment implements SimpleScanCallback {
    private static final String TAG = BleDevicesFragment.class.getSimpleName();

    private String mTitle;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private List<BleDevice> mDataList = new ArrayList<>();
    private BleDeviceAdapter mAdapter;
    private BleScanner mBleScanner;

    public BleDevicesFragment() {
    }

    public static BleDevicesFragment newInstance(String title) {
        BleDevicesFragment f = new BleDevicesFragment();

        Bundle args = new Bundle();

        args.putString(AppConstants.KEY_TITLE, title);
        f.setArguments(args);

        return (f);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mTitle = getArguments().getString(AppConstants.KEY_TITLE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frg_swipe_refresh_list, null);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        mRecyclerView =  (RecyclerView) rootView.findViewById(R.id.recycler_view);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //设置加载圈圈的颜色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.line_color_run_speed_13);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mBleScanner != null) {
                    mBleScanner.bleScanner.onStopBleScan();
                } else {
                    mBleScanner = new BleScanner(getContext(), BleDevicesFragment.this);
                }
                bleDeviceHashMap.clear();
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                mBleScanner.startBleScan();
                //
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);//1秒
            }
        });

        //mDataList.add(mTitle);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new BleDeviceAdapter(getActivity(), mRecyclerView, mDataList);
        mAdapter.setHasMoreData(false);
        mAdapter.setHasFooter(false);
        mRecyclerView.setAdapter(mAdapter);
        mBleScanner = new BleScanner(getContext(), BleDevicesFragment.this);
        mBleScanner.startBleScan();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(mBleScanner != null && mBleScanner.isScanning()){
            mBleScanner.bleScanner.onStartBleScan();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mBleScanner != null){
            mBleScanner.bleScanner.onStopBleScan();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.ble_scan_action, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ble_action_start:
                if (mBleScanner.isScanning()) {
                    item.setTitle(R.string.app_ble_scan_start);
                    mBleScanner.stopBleScan();
                } else {
                    item.setTitle(R.string.app_ble_scan_stop);
                    bleDeviceHashMap.clear();
                    mAdapter.clear();
                    mAdapter.notifyDataSetChanged();
                    mBleScanner.startBleScan();
                }
                break;
            }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBleScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BleLog.i(TAG, device.toString() + " rssi: " + rssi);
                String address = device.getAddress();
                if (bleDeviceHashMap.get(address) == null) {
                    bleDeviceHashMap.put(address, device);
                    FavouriteInfo favourite = FavouriteInfo.getFavourite(address);
                    BleDevice bleDevice = new BleDevice(address, device.getAddress(),
                            rssi, HexUtil.encodeHexStr(scanRecord), favourite.isFavourite);
                    bleDevice.aliasName = (TextUtils.isEmpty(favourite.name) ? "" : (favourite.name));

                    mAdapter.append(bleDevice);
                    try {
                        mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    //更新rssi
                    List<BleDevice> mList = mAdapter.getList();
                    int i = 0;
                    for (BleDevice bleDevice : mList) {

                        if (bleDevice.address.equalsIgnoreCase(device.getAddress())) {
                            //控制刷新
                            long now = System.currentTimeMillis();
                            if(rssi != bleDevice.rssi && (now - bleDevice.updateTime > 1000)) {
                                bleDevice.updateTime = now;
                                bleDevice.rssi = rssi;
                                bleDevice.broadcast = HexUtil.encodeHexStr(scanRecord);
                                try {
                                    mAdapter.notifyItemChanged(i);
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                                //mAdapter.notifyDataSetChanged();
                            }
                            break;
                        }
                        i++;
                    }
                }
            }
        });
    }

    @Override
    public void onBleScanFailed(BleScanState scanState) {
        Toast.makeText(getContext(), scanState.getMessage(), Toast.LENGTH_LONG).show();
    }


    private HashMap<String, BluetoothDevice> bleDeviceHashMap = new HashMap<>();

}

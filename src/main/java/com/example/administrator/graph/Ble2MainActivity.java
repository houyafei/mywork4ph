package com.example.administrator.graph;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Ble2MainActivity extends Activity implements OnClickListener, OnItemClickListener {

    private  String TAG = "houyafei" ;
	private Button mBtnSearch ;
	private ListView mlist ;
	
    private List<String> mDevicesNames = new ArrayList<String>();
    private List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();
    private  ArrayAdapter<String> mListAdapter ; 
	
	private BluetoothAdapter mBluetoothAdapter ;
	
    private boolean mScanning;
    private Handler mHandler;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_ble_layout);
		
		//创建Handler
		mHandler = new Handler() ;
		
		//按钮点击事件
        mBtnSearch = (Button) findViewById(R.id.id_btnSearchDevices);
        mBtnSearch.setOnClickListener( this);
		
		//搜索到的设备列表
		mlist = (ListView) findViewById(R.id.id_device_list);
		mlist.setOnItemClickListener(this);
		mListAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.mytext_layout4list, mDevicesNames);
		mlist.setAdapter(mListAdapter);
		
		// 得到蓝牙适配器
		final BluetoothManager bluetoothManager =
		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		//判断系统是否支持蓝牙
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
		    Toast.makeText(this, "Sorry", Toast.LENGTH_SHORT).show();
		    finish();
		}else{
			Toast.makeText(this, "Support Ble", Toast.LENGTH_SHORT).show();
		}
		
		//蓝牙若没有打开，则打开
		if(!mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.enable();
		}
	}

	@Override
	public void onClick(View v) {
		scanLeDevice(true) ;
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {
        final BluetoothDevice device = mDevices.get(position);
        if (device == null) return;
        final Intent intent = new Intent(this, ControlDevicesActivity.class);
        intent.putExtra(ControlDevicesActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(ControlDevicesActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
		
	}

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;
  
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
     
    }
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                byte[] scanRecord) {
            runOnUiThread(new Runnable() {
			@Override
               public void run() {
				if(!mDevicesNames.contains(device.getName()+"-"+device.getAddress())){
					mDevicesNames.add(device.getName()+"-"+device.getAddress());
	            	mDevices.add(device);
	            	Log.i(TAG, "搜索到的设备信息：" + device.getAddress() + device.getName());
	            	mListAdapter.notifyDataSetChanged();
				}
            	   
            	   
               }
           });
       }


    };

}

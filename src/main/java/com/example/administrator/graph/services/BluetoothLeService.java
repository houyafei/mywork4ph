package com.example.administrator.graph.services;/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.graph.utils.ImageDataSaveUtil;
import com.example.administrator.graph.utils.SampleGattAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;



/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = "houyafei";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "my.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "my.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "my.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "my.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "my.bluetooth.le.EXTRA_DATA";
    public static final UUID SERVIE_UUID = UUID
			.fromString("0000FFF0-0000-1000-8000-00805f9b34fb");
	public static final UUID RED_LIGHT_CONTROL_UUID = UUID
			.fromString("0000FFF4-0000-1000-8000-00805f9b34fb");
	public static final UUID RED_LIGHT_CONTROL_UUID_TWO = UUID
			.fromString("0000FFF1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    
    //有人服务，自己蓝牙服务
    public final static UUID UUID_USR_SERVICE =
            UUID.fromString(SampleGattAttributes.USR_SERVICE);

    //为每次存储数据的文件夹命名
	SimpleDateFormat f = new SimpleDateFormat("yyyyMMddhhmmss") ;
	Date date = new Date() ;
    private String mpathMsgs =  f.format(date);
    
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                //发送已连接设备的广播，由DeviceControlActivity 中的BrocastReceiver来处理
                broadcastUpdate(intentAction);  
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
              //发送未设备的广播，由DeviceControlActivity 中的BrocastReceiver来处理
                broadcastUpdate(intentAction);
            }
        }

        /**
         * 发现硬件支持的服务，返回所有的服务列表（由DeviceControlActivity 中的BrocastReceiver来处理）
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	//获取所有的Services，发送广播，由DeviceControlActivity 中的BrocastReceiver来处理
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.i(TAG, "onServicesDiscovered(BluetoothGatt gatt, int status)");
            } else {
                Log.i(TAG, "onServicesDiscovered received: " + status);

            }
        }

        /**
         * 发现硬件支持的属性，可读属性
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
        	 Log.i(TAG, "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	//获取所有的属性，发送广播，由DeviceControlActivity 中的BrocastReceiver来处理
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
        /**
         * 属性发生变化
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        	//属性发生变化，发送广播，由DeviceControlActivity 中的BrocastReceiver来处理
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            
            Log.i("onCharacteristicChanged", "属性发生变化----onCharacteristicChanged()");
        }

	
        /**
         * 发现硬件支持的属性，可写属性
         */
        @Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
       	 Log.i(TAG, "------------onCharacteristicWrite()");
         if (status == BluetoothGatt.GATT_SUCCESS) {
         	//获取所有的属性，发送广播，由DeviceControlActivity 中的BrocastReceiver来处理
            // broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
         }
		}



        
    
    };

    /**
     * 发送广播
     *@Aurthor 写有态度的程序---> Hou Yafei <--- 揍是他 ^_^
     *@Time  2016-5-17下午2:27:55 
     *@param action
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);


        	//这里获取蓝牙设备的数据 ，然后通过广播发送给广播接受者
            final byte[] data = characteristic.getValue();
            //转换成16进制数据
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for(byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            }
            String str = new String(data) ;
            //保存到 文件中
            ImageDataSaveUtil.saveLogData(str, mpathMsgs) ;
            //显示到列表中
            intent.putExtra(EXTRA_DATA, new String(data));
        
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
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
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
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.i(TAG, "---------connect(final String address)--BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "---------connect(final String address)Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.i(TAG, "---------connect(final String address)--\nDevice not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.i(TAG, "--------Tconnect(final String address)------\nrying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        Log.i(TAG, "--------Tconnect(final String address)--device.getBondState=="+device.getBondState());
        
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
            Log.i(TAG, "---------disconnect()---BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "---------readCharacteristic(BluetoothGattCharacteristic characteristic)---\nBluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "----------setCharacteristicNotification()----BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
 
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    
    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}.
     *
     * @param characteristic
     * @param byteArray
     */
    public  void writeCharacteristicGattDb(
            BluetoothGattCharacteristic characteristic, byte[] byteArray) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        } else {
            byte[] valueByte = byteArray;
            if(valueByte.length>20){
            	Toast.makeText(getApplicationContext(), "数据超过20", Toast.LENGTH_SHORT).show();
            }else{
            	characteristic.setValue(valueByte);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            
        }
    }
    public void writeLlsAlertLevel(int iAlertLevel, byte[] bb) {

		// Log.i("iDevice", iDevice);
		BluetoothGattService linkLossService = mBluetoothGatt
				.getService(SERVIE_UUID);
		if (linkLossService == null) {
			showMessage("link loss Alert service not found!");
			return;
		}
		// enableBattNoti(iDevice);
		BluetoothGattCharacteristic alertLevel = null;
		switch (iAlertLevel) {
		case 1: // red
			alertLevel = linkLossService.getCharacteristic(RED_LIGHT_CONTROL_UUID);
			break;
		case 2:
			alertLevel = linkLossService.getCharacteristic(RED_LIGHT_CONTROL_UUID_TWO);
			break;
		}
		
		
		if (alertLevel == null) {
			showMessage("link loss Alert Level charateristic not found!");
			return;
		}
		
		boolean status = false;
		int storedLevel = alertLevel.getWriteType();
		Log.d(TAG, "storedLevel() - storedLevel=" + storedLevel);

		
		alertLevel.setValue(bb);
		

		alertLevel.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		status = mBluetoothGatt.writeCharacteristic(alertLevel);
		Log.d(TAG, "writeLlsAlertLevel() - status=" + status);
	}
    private void showMessage(String msg) {
		Log.i(TAG, msg);
	}
}

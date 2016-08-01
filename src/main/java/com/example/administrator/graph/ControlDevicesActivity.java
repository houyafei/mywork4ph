package com.example.administrator.graph;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.graph.services.BluetoothLeService;
import com.example.administrator.graph.utils.SampleGattAttributes;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ControlDevicesActivity extends Activity {

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	
	public static final String  TAG= "houyafei";
	
	//ѡ��ǰ�����¶�Ӧ������
	private RadioButton mNotify,
						mWrite ;
	private RadioGroup mRadioGroup ;
	private Boolean mIsNotify = false ; //��ʾRadio��״̬
	//������Ϣ
	private Button btnSendMsg ;
	private EditText mEdt ;
	
	//��Ϣ�б�
	private ListView mListView ;
	private List<String> mMsgs = new ArrayList<String>();
	private ArrayAdapter<String> mAdapter ;
	
	//Ŀ���豸������ַ
	private String mDeviceName ;
	private String mDeviceAddress ;
	
	//��������
	private BluetoothLeService mBluetoothLeService;
	
	//������������
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	
	//���������Ҫ�����������ض�UUID����������
	private BluetoothGattService usrService;
	
	//��������״̬
	private boolean mConnected = false ;
	
	//��ʾ�����յ���������Լ�ʱ����
	private TextView mTextShowDataSum ;
	//��������ݴ���
	private int mRecvSum = 0 ;
	//������ݴ���
	private int mSendSum = 0 ;
	//���յ����ֽ���
	private int mRecvbytes = 0 ;
	//��ʼ������ݵ�ʱ��
	private Date mStartDate = new Date() ;
	//
	private Date mEndDate = new Date();
	
	//����б��
	private Button mBtnClearList ;
	
	//��ʾ����״̬�İ�ť���������ԶϿ����ӵİ�ť
	private Button mBtnConnState ;
	
	
	// �����������
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// �����ʼ�����Զ����������豸
			mBluetoothLeService.connect(mDeviceAddress);
		}

		//����Ͽ�
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	//
	// Handles various events fired by the Service.
		// ACTION_GATT_CONNECTED: connected to a GATT server.
		// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
		// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
		// ACTION_DATA_AVAILABLE: received data from the device. This can be a
		// result of read
		// or notification operations.
		private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
					mConnected = true;
					//updateConnectionState(R.string.connected);
					Log.i(TAG, "----------�Ѿ�����") ;
					//invalidateOptionsMenu();
					updateUI();
				} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
						.equals(action)) {
					mConnected = false;
					Log.i(TAG, "----------�Ѿ��Ͽ�") ;
					//updateConnectionState(R.string.disconnected);
					//invalidateOptionsMenu();
					updateUI();
				} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
						.equals(action)) {
					// �������еķ���������б�
					// user interface.
					displayGattServices(mBluetoothLeService
							.getSupportedGattServices());
				} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
					//�������
					displayData(intent
							.getStringExtra(BluetoothLeService.EXTRA_DATA));
				}
			}
		};

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control_activity_ble2_main);
		
		//��ñ����ӵ��豸����ƺ͵�ַ
		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
		
		//��ʾ�շ������
		mTextShowDataSum = (TextView) findViewById(R.id.id_text);
		//���list�е����
		mBtnClearList  =(Button) findViewById(R.id.id_btnClearList);
		//������е����
		mBtnClearList.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mMsgs!=null){
					mMsgs.removeAll(mMsgs);
				}
				//ˢ���б�
				mMsgs.add("--------��ʾ��Ϣ-----------");
				mAdapter.notifyDataSetChanged();
				//�ÿ��ı�
				mTextShowDataSum.setText("Recv: "+(mRecvSum=0)+" times\n"+"Send: "+(mSendSum=0)+" times");
				mRecvbytes = 0 ;
			}
		});
		
		//��ʼ���б�
		mListView = (ListView) findViewById(R.id.id_msg_list);
		mMsgs.add("--------��ʾ��Ϣ-----------");
		mAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.mytext_layout4list, mMsgs);
		mListView.setAdapter(mAdapter);

		//��ʼ��Radiogroup,��ʱ���أ��������Ҫ֪ͨ onCreate()�е� �߳�ȡ��
		mRadioGroup = (RadioGroup) findViewById(R.id.id_radiogroup);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				mIsNotify = !mIsNotify  ;
				if(mIsNotify){
					btnSendMsg.setVisibility(View.GONE);
					mEdt.setVisibility(View.GONE);
					//�ȴ�BLE�豸��������Ϣ
					notifyBleMsgs();
					
				}else{
					btnSendMsg.setVisibility(View.VISIBLE);
					mEdt.setVisibility(View.VISIBLE);
				}
			}
		}) ;
		mRadioGroup.setVisibility(View.GONE);
		//------------------���ϲ���ʹ��----------------------
		
		//������Ϣ
		btnSendMsg =(Button) findViewById(R.id.id_btnsend);
		mEdt  = (EditText)findViewById(R.id.id_etd);
		
		btnSendMsg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str = mEdt.getText().toString();
				if(TextUtils.isEmpty(str)){
					Toast.makeText(getApplicationContext(), "���Ͳ���Ϊ��", Toast.LENGTH_SHORT).show();
				}else{
					//�����ݣ�ˢ���б�,�ÿձ༭��,������ݵ�BLE�豸
					mMsgs.add("Android:"+str);
					mAdapter.notifyDataSetChanged();
	                mListView.setSelection(mMsgs.size()-1) ;
					mEdt.setText("");
					sendData2BleDevices(str);
				}
				
			}
		});
		
		btnSendMsg.setVisibility(View.VISIBLE);
		mEdt.setVisibility(View.VISIBLE);

		//
		mBtnConnState  = (Button) findViewById(R.id.id_btnConState);
		mBtnConnState.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//������ӱ��Ͽ�
				if(!mConnected){
					mConnected = true ;
					mBluetoothLeService.connect(mDeviceAddress);
				}else{
					mConnected = false ;
					mBluetoothLeService.disconnect();
				}
				
			}
		});
		
		//�󶨷���
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		
		//�ȴ�BLE�豸��������Ϣ����֤����򿪺���Ի�����
		//notifyBleMsgs(); 
		//ע��֪ͨ���ӳ�300ms����У����� service ��û������
		new Thread(){
			@Override
			public void run() {
				try {
					Thread.sleep(300) ;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				notifyBleMsgs();
			}
		}.start();
	}
	
	/**
	 * ��������״̬��ť�� ����״̬
	 *@Aurthor д��̬�ȵĳ���---> Hou Yafei <--- ������ ^_^
	 *@Time  2016-5-17����2:10:35
	 */
	protected void updateUI() {
		if(mConnected){
			mBtnConnState.setText("Connected") ;
			Toast.makeText(getApplicationContext(), "Has connected the BLE", Toast.LENGTH_SHORT).show();
		}else{
			mBtnConnState.setText("DisConnected") ;
			Toast.makeText(getApplicationContext(), "Has disconnected the BLE", Toast.LENGTH_SHORT).show();
		}
		
	}
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}


	/**
	 * ������ݣ�US-ascII
	 *@Aurthor д��̬�ȵĳ���---> Hou Yafei <--- ������ ^_^
	 *@Time  2016-5-12����8:04:34 
	 *@param str
	 */
	protected void sendData2BleDevices(String str) {
		
		if(mBluetoothLeService==null){
			return ;
		}else{
			//��ȡ����֪ͨ������ Characteritic
			final BluetoothGattCharacteristic characteristic = usrService.
					getCharacteristic(UUID.fromString(SampleGattAttributes.WRITE)) ;
			byte[] array;
			try {
				//������ݼ���
				array = str.getBytes("us-ascII");
				if(array.length>20){
					int times = array.length/20 + 1 ;
					List<byte[]> listbytes = new ArrayList<byte[]>() ;
					//�ֳ� times�������
					for (int i = 0; i <times; i++) {
						byte[] tempbytes = new byte[20] ;
						for (int j = 0; j<20&&(j+i*20)<array.length; j++) {
							tempbytes[j] = array[j+i*20] ;
							
						}
						listbytes.add(tempbytes);
						
						
					}
					for (final byte[] bs : listbytes) {
						new Thread(){
							@Override
							public void run() {
								mBluetoothLeService.writeCharacteristicGattDb(characteristic,bs);
								Log.i(TAG, "-----------ok------") ;
							}
						}.start();
					}
				}else{
					mBluetoothLeService.writeCharacteristicGattDb(characteristic,array);
				}
				
				//�������
				mSendSum += 1 ;
				mTextShowDataSum.setText("Recv: "+mRecvSum+" times\n"+"Send: "+mSendSum+" times");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	/**
	 * ֪ͨ Ble�豸�������
	 */
	protected void notifyBleMsgs() {
		if(usrService==null){
			Log.i(TAG, "��������Ҫ�ķ���");
			return ;
		}
		//��ȡ����֪ͨ������ Characteritic
		final BluetoothGattCharacteristic characteristic = usrService.
				getCharacteristic(UUID.fromString(SampleGattAttributes.NOTIFY)) ;
		final int charaProp = characteristic.getProperties();
		
		//ʹ���пɶ�����
		if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
			// If there is an active notification on a characteristic,
			// clear
			// it first so it doesn't update the data field on the user
			// interface.
			if (mNotifyCharacteristic != null) {
				mBluetoothLeService.setCharacteristicNotification(
						mNotifyCharacteristic, false);
				mNotifyCharacteristic = null;
			}
			mBluetoothLeService.readCharacteristic(characteristic);
		}
		//����֪ͨ
		if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
			mNotifyCharacteristic = characteristic;
			mBluetoothLeService.setCharacteristicNotification(
					characteristic, true);
		}
		Log.i(TAG, "����֪ͨ");
		
		
	}

	/**
	 * ע��㲥������
	 */
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
		//����֪ͨ------2016-5-17
		//notifyBleMsgs();
	}
	
	/**
	 * ע��㲥��ȡ�����
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}
	
	/**
	 * ˢ���б���ʾ���
	 *@Aurthor д��̬�ȵĳ���---> Hou Yafei <--- ������ ^_^
	 *@Time  2016-5-12����2:03:01 
	 *@param stringExtra
	 */
	protected void displayData(String stringExtra) {
		Log.i(TAG, "displayData(String stringExtra)--:"+stringExtra);
		if(stringExtra==null){
			return ;
		}
		int bytes = stringExtra.getBytes().length;
		Log.i(TAG, "displayData(String stringExtra)2--:"+stringExtra);
		mRecvbytes += bytes ;
		//ÿ����һ����ݣ����մ���+1 ��
		mRecvSum += 1 ;
		//�����ݣ�ˢ���б�
		mMsgs.add("BLE Device:"+stringExtra);
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(mMsgs.size()-1);
		if(mRecvSum==1){
			mStartDate = new Date() ;
			mEndDate = new Date();
		}else{
			mEndDate = new Date();
		}
		mTextShowDataSum.setText("Recv: " + mRecvSum + " times" +
					"--startTime"+mStartDate.toString()+"\n"+
					
					"Send: "+ mSendSum + " times" +
					"--endTime"+mEndDate.toString()+"\n" + "bytes:" + mRecvbytes);
	}


	/**
	 * ����������Ҫ��SerVice�������� Usr service
	 *@Aurthor д��̬�ȵĳ���---> Hou Yafei <--- ������ ^_^
	 *@Time  2016-5-12����2:11:11 
	 *@param supportedGattServices
	 */
	protected void displayGattServices(
			List<BluetoothGattService> supportedGattServices) {
		for (BluetoothGattService bluetoothGattService : supportedGattServices) {
			
			Log.i(TAG, "name:"+SampleGattAttributes.lookup(bluetoothGattService.getUuid().toString(), "δ֪"));
			Log.i(TAG, "UUID:"+bluetoothGattService.getUuid().toString());
			//bluetoothGattService.getUuid().toString());
			if(bluetoothGattService.getUuid().equals(UUID.fromString(SampleGattAttributes.USR_SERVICE))){
				usrService = bluetoothGattService ; 
				Log.i(TAG, "usrService--UUID:"+usrService.getUuid().toString());
			}
		}
		
		
	}
	
	/**
	 * ��ӹ�����
	 *@Aurthor д��̬�ȵĳ���---> Hou Yafei <--- ������ ^_^
	 *@Time  2016-5-12����2:03:49 
	 *@return
	 */
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
	
}

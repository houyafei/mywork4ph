package com.example.administrator.graph;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

public class SensorOpen implements SensorEventListener {

	
	private Context context = null ;
	
	private ArrayList<Integer> list = new ArrayList<>();
	
	SensorManager sensorManager = null ;

	/*
	 * 
	 */
	public SensorOpen(Context context) {
		super();
		this.context = context;
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	}
	
	/*
	 * 
	 */
	public void OpenAccelerate(){
		
	}
	
	/*
	 * 
	 */
	public void resumeSensor(){
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/*
	 * 
	 */
	public void stopSensor(){
		sensorManager.unregisterListener(this);
		
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] values= event.values;
		//x��
		list.add((int)(values[0]*100));
		
		System.out.print("sensor---"+values[0]);
	}
	
	/*
	 * �������
	 */
	public ArrayList<Integer> getList(){
		return list ;
	}

}

package com.example.administrator.graph.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageDataSaveUtil {


	String ImagePath;

 
	private static int count = 0 ;
	
	

	
	//字符串
	public static void saveLogData(String str){
		
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddhhmmss") ;
		Date date = new Date() ;
		String strDate = f.format(date);
		
		//1.解析数据
		//暂不需要
		
		//2.创建文件File
		File file = new File("/sdcard/philLog01/"+ strDate +".txt");
		//3.创建输出流，并且在后面添加数据
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file,true);
			String msg = str;
			//4.1,构造一个字符串
			if(count%11==0){
				msg = String.valueOf(System.currentTimeMillis())+"\n" +msg+"\t";
			}else{
				msg = msg+"\t" ;
			}
			count++  ;
			
			
			try {
				fos.write(msg.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				ImageDataSaveUtil.printExceptionMsg(e);
			}
		}
		
		
		
	
		
		//提示数据已经保存↑↑↑
		//Toast.makeText(context, "OK", 100).show();
	}
	
	//字符串
	public static void saveLogData(String str, String filename){
		
		//1.创建文件夹
		File folder = null ;
		try {
			folder = new File("/sdcard/philLog01/");
			if(!folder.exists()){
				folder.mkdir();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//2.创建文件File
		File file = new File("/sdcard/philLog01/"+ filename +".txt");
		//3.创建输出流，并且在后面添加数据
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file,true);
			String msg = str;
			//4.1,构造一个字符串
			if(count%10==0){
				msg = "\n" +msg+"\t";
			}else{
				msg = msg+"\t" ;
			}
			count++  ;
			
			
			try {
				fos.write(msg.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	
		
		//提示数据已经保存
		//Toast.makeText(context, "OK", 100).show();
	}
	
	
	/**
	 * 打印异常信息到指定文件中。
	 */
	public static void  printExceptionMsg(Exception e){
		
		PrintStream newOut = null ;
		try {
			newOut = new PrintStream(new BufferedOutputStream(new FileOutputStream("/sdcard/ExceptionLog.txt")));
			//-----------------☝↑-------------------☝↑---------------------☝↑
			e.printStackTrace(newOut) ;
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}finally{
			newOut.close();
		}
	}

}

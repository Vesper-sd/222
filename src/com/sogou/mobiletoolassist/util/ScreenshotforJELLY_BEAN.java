package com.sogou.mobiletoolassist.util;

import android.annotation.SuppressLint;


public class ScreenshotforJELLY_BEAN {
	@SuppressLint("SdCardPath")
	private static String imagepath = "/sdcard/";//Environment.getExternalStorageDirectory()+File.separator;
	//����һ�û���������ȡ�õ�����storage/emulated/0/xxx.pngȻ�����������֪��Ϊɶ����ʱ���о�һ��
	public static String shoot(){
		
		long ti = System.currentTimeMillis();
		String path = imagepath + String.valueOf(ti)+".png";
		String cmd = "screencap "+path;
		if(UsefulClass.processCmd(cmd) != StateValue.success){			
			return "";
		}	
		return path;
	}
}

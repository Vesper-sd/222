package com.sogou.mobiletoolassist.util;

/**
 * ScreenShotFb.java
 * ��Ȩ����(C) 2014
 * ������:cuiran 2014-4-3 ����4:55:23
 */

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;


/**
 * FrameBuffer�л�ȡAndroid��Ļ��ͼ
 * 
 * @author cuiran
 * @version 1.0.0
 */
public class ScreenshotforGINGERBREAD_MR1 {

	public static final String TAG = "ScreenShotFb";
	private final static String FB0FILE1 = "/dev/graphics/fb0";
	private static File fbFile;
	private static DataInputStream dStream = null;
	private static byte[] piex = null;
	private static int[] colors = null;
	private static int deepth = 0;
	private static int screenWidth = 0;
	private static int screenHeight = 0;
	private static boolean isInitialized = false;

	public static boolean isInitialized() {
		return isInitialized;
	}

	// �������
	public static String shoot() {
		try {
			/************ ���������� ************/
			String cmd = "chmod 777 /dev/graphics/fb0";

			if (UsefulClass.processCmd(cmd) != StateValue.success){
				
				return "";
			}
				

			piex = new byte[screenHeight * screenWidth * deepth];// ����
			colors = new int[screenHeight * screenWidth];
			long timestamp = System.currentTimeMillis();
			String filePath = "/sdcard/"+String.valueOf(timestamp) + ".png";
					
			final Object lock = new Object();
			synchronized (lock) {
				Bitmap bitmap = getScreenShotBitmap();
				if (bitmap == null) {
					Log.e(TAG, "û����fb0");
					return "";
				}
				
				ScreenshotforGINGERBREAD_MR1.savePic(bitmap, filePath);
				piex = null;
				colors = null;
				
				return filePath;
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception error", e);
			return "";
		}
		

	}

	// ���浽sdcard
	public static void savePic(Bitmap b, String strFileName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(strFileName);
			if (null != fos) {
				b.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException error", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException error", e);
		}

		Log.i(TAG, "savePic success");
	}

	

	public static void init(Activity act) {
		DisplayMetrics dm = new DisplayMetrics();
		Display display = act.getWindowManager().getDefaultDisplay();
		display.getMetrics(dm);
		screenWidth = dm.widthPixels; // ��Ļ�����أ��磺480px��
		screenHeight = dm.heightPixels; // ��Ļ�ߣ����أ��磺800p��
		@SuppressWarnings("deprecation")
		int pixelformat = display.getPixelFormat();
		PixelFormat localPixelFormat1 = new PixelFormat();
		PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
		deepth = localPixelFormat1.bytesPerPixel;// λ��

		isInitialized = screenWidth != 0 && screenHeight != 0 && deepth != 0;

	}

	public static synchronized Bitmap getScreenShotBitmap() {
		FileInputStream buf = null;
		try {
			fbFile = new File(FB0FILE1);
			if (!fbFile.exists()) {
				return null;
			}
			buf = new FileInputStream(fbFile);// ��ȡ�ļ�����
			dStream = new DataInputStream(buf);
			dStream.readFully(piex);
			dStream.close();
			// ��argbתΪɫ
			for (int m = 0; m < piex.length; m += 1) {
				if (m % 4 == 0) {
					int r = (piex[m] & 0xFF);
					int g = (piex[m + 1] & 0xFF);
					int b = (piex[m + 2] & 0xFF);
					int a = (piex[m + 3] & 0xFF);
					colors[m / 4] = (a << 24) + (r << 16) + (g << 8) + b;
				}
			}

			// �õ���Ļbitmap
			return Bitmap.createBitmap(colors, screenWidth, screenHeight,
					Bitmap.Config.ARGB_8888);

		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException error", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException error", e);
		} catch (Exception e) {
			Log.e(TAG, "Exception error", e);
		} finally {
			if (buf != null) {
				try {
					buf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}

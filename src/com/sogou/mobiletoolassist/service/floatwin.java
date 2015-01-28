package com.sogou.mobiletoolassist.service;


import java.io.File;


import com.sogou.mobiletoolassist.AssistActivity;
import com.sogou.mobiletoolassist.R;
import com.sogou.mobiletoolassist.assistApplication;
import com.sogou.mobiletoolassist.util.MailSender;
import com.sogou.mobiletoolassist.util.ScreenshotforGINGERBREAD_MR1;
import com.sogou.mobiletoolassist.util.ScreenshotforJELLY_BEAN;
import com.sogou.mobiletoolassist.util.UsefulClass;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class floatwin extends Service {
	private static WindowManager wm = null;  
	private static WindowManager.LayoutParams params = null; 
	private View btn_floatView = null;  
	private Button clearBtn = null;
	private Button screenshotBtn = null;
	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
	
	@Override
	public void onCreate(){
		//createFloatView();
	}
	@Override
	public void onStart(Intent intent, int startId){
		Log.d("study", "onstart");
	}
	/**
	 * ����������
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d("study", "onStartCommand");
		createFloatView();
		return Service.START_STICKY;//��ʾ��ϵͳɱ������Ҫ����
	}
	@SuppressLint("InflateParams")
	private void createFloatView() {
        btn_floatView = LayoutInflater.from(this).inflate(R.layout.floatwin, null);
        clearBtn = (Button)btn_floatView.findViewById(R.id.cleardatabtn);
		clearBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				
				floatwin.onClearBtn();
			}
			
		});
		screenshotBtn = (Button)btn_floatView.findViewById(R.id.screenshotbtn);
		screenshotBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						floatwin.ScreenShot();			
					}
					
				}).start();
			}
			
		});
        wm = (WindowManager) getApplicationContext()
        	.getSystemService(Context.WINDOW_SERVICE);
        
        params = new WindowManager.LayoutParams();
        
        // ����window type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        /*
         * �������Ϊparams.type = WindowManager.LayoutParams.TYPE_PHONE;
         * ��ô���ȼ��ή��һЩ, ������֪ͨ�����ɼ�
         */
        
        params.format = PixelFormat.RGBA_8888; 
        
        // ����Window flag
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                              | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        /*
         * �����flags���Ե�Ч����ͬ����������
         * ���������ɴ������������κ��¼�,ͬʱ��Ӱ�������¼���Ӧ��
        wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL
                               | LayoutParams.FLAG_NOT_FOCUSABLE
                               | LayoutParams.FLAG_NOT_TOUCHABLE;
         */
        
        // �����������ĳ��ÿ�
        

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        
      //��Ҫ��������system.alert_windowȨ��
        wm.addView(btn_floatView, params);
        // ������������Touch����
        screenshotBtn.setOnTouchListener(new OnTouchListener() {
        	int lastX, lastY;
        	int paramX, paramY;
        	
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {	
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = params.x;
					paramY = params.y;
					

					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					if(Math.abs(dx) < 5 && Math.abs(dy) <5){
						//����ô��̫�����ˣ����������ڵ�����ƶ���ë�߰�
						
						break;	
					}
					params.x = paramX + dx;
					params.y = paramY + dy;
					
					// ����������λ��
			        wm.updateViewLayout(btn_floatView, params);
			        

					break;
				case MotionEvent.ACTION_UP:
					int dx1 = (int) event.getRawX() - lastX;
					int dy1 = (int) event.getRawY() - lastY;
					if(Math.abs(dx1) < 5 && Math.abs(dy1) <5){
						screenshotBtn.performClick();
					}
								
				}
				return true;
			}
		});      
        
        clearBtn.setOnTouchListener(new OnTouchListener() {
        	int lastX, lastY;
        	int paramX, paramY;
        	
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {	
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = params.x;
					paramY = params.y;
					

					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					if(Math.abs(dx) < 5 && Math.abs(dy) <5){
						//����ô��̫�����ˣ����������ڵ�����ƶ���ë�߰�
						
						break;	
					}
					params.x = paramX + dx;
					params.y = paramY + dy;
					
					// ����������λ��
			        wm.updateViewLayout(btn_floatView, params);
			        

					break;
				case MotionEvent.ACTION_UP:
					int dx1 = (int) event.getRawX() - lastX;
					int dy1 = (int) event.getRawY() - lastY;
					if(Math.abs(dx1) < 5 && Math.abs(dy1) <5){
						clearBtn.performClick();
					}
							
				}
				return true;
			}
		});      
        
	}
	public static void onClearBtn(){
		Context ctx = assistApplication.getContext();
		if(ctx == null){
			Log.e(AssistActivity.myTag, "ctx �ǿյ�");
			return;
		}
		if(!UsefulClass.hasappnamedxxx(ctx, "com.sogou.androidtool")){
			Toast.makeText(ctx, "û�а�װ����", Toast.LENGTH_SHORT).show();
			return;
		}
		String cmd = "pm clear com.sogou.androidtool";
		Toast.makeText(ctx, "׼����������~", Toast.LENGTH_SHORT).show();
		if(UsefulClass.processCmd(cmd)){
			Toast.makeText(ctx, "�����������~", Toast.LENGTH_SHORT).show();
		}
	}
	
	public static boolean ScreenShot(){
		String path = "";
		
		if(Build.VERSION.SDK_INT > 13){
			path = ScreenshotforJELLY_BEAN.shoot();
		}else{
			if(ScreenshotforGINGERBREAD_MR1.isInitialized())
				path = ScreenshotforGINGERBREAD_MR1.shoot();
		}
		
		if(path == "")
			return false;
		
		String info = UsefulClass.getDeviceInfo();
		String title = info+"����ͼ��";
		info += "</br>";
		info += UsefulClass.getZSPkgInfo();
		
		if(MailSender.sendTextMail(title, info, path,new String[]{"pdatest@sogou-inc.com"})){
			Log.i(AssistActivity.myTag,"��ͼ�ʼ����ͳɹ�");
			File tmp = new File(path);
			if(tmp.exists())
				tmp.delete();
		}
		return true;
	}
}


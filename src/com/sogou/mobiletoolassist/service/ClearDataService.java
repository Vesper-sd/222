package com.sogou.mobiletoolassist.service;

import java.io.IOException;
import java.io.OutputStream;

import com.sogou.mobiletoolassist.AssistActivity;
import com.sogou.mobiletoolassist.R;
import com.sogou.mobiletoolassist.StreamReader;
import com.sogou.mobiletoolassist.util.UsefulClass;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class ClearDataService extends Service implements OnClickListener{
	
	private final int mNotifitionId = 1;
	public ButtonBroadcastReceiver bReceiver;
	/** ֪ͨ����ť����¼���Ӧ��ACTION */
	public final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";
	public final static String INTENT_BUTTONID_TAG = "ButtonId";
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate(){
		super.onCreate();
		Log.e(AssistActivity.myTag, "create clear service");
		bReceiver = new ButtonBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_BUTTON);
		registerReceiver(bReceiver, intentFilter);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		
		RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_custom_button);  
		
		mRemoteViews.setImageViewResource(R.id.custom_song_icon, R.drawable.sing_icon);  
        //API3.0 ���ϵ�ʱ����ʾ��ť��������ʧ  
        mRemoteViews.setTextViewText(R.id.tv_custom_song_singer, "���ֲ��Թ���");  
        mRemoteViews.setTextViewText(R.id.tv_custom_song_name, "zhangshuai203407");  
       
        //������¼�����  
        Intent buttonIntent = new Intent(ACTION_BUTTON);  
        /* ��һ�װ�ť */  
//        buttonIntent.putExtra(INTENT_BUTTONID_TAG, 1);  
//        //������˹㲥������INTENT�ı�����getBroadcast����  
//        PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
//        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_prev, intent_prev);  
//        /* ����/��ͣ  ��ť */  
//        buttonIntent.putExtra(INTENT_BUTTONID_TAG, 2);  
//        PendingIntent intent_paly = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
//        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_play, intent_paly);  
        /* ��һ�� ��ť  */  
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, 3);  
        PendingIntent intent_next = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_next, intent_next);  
        
        
		NotificationManager mNotifyMgr = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		Log.i("assist", "start command service");
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContent(mRemoteViews);
	
		
		builder.setOngoing(true);
		Notification nf = builder.build();
		nf.icon = R.drawable.ic_launcher;
		
		mNotifyMgr.notify(mNotifitionId, nf);
		
		return Service.START_STICKY;
	}
	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public class ButtonBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action.equals(ACTION_BUTTON)){
				//ͨ�����ݹ�����ID�жϰ�ť������Ի���ͨ��getResultCode()�����Ӧ����¼�
				int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
				switch (buttonId) {
				case 1:
//					Log.d("assist" , "��һ��");
//					Toast.makeText(getApplicationContext(), "��һ��", Toast.LENGTH_SHORT).show();
//					break;
				case 2:
//					String play_status = "";
//					
//					//showButtonNotify();
//					Log.d("assist" , play_status);
//					Toast.makeText(getApplicationContext(), play_status, Toast.LENGTH_SHORT).show();
//					break;
				case 3:
					Log.d("assist" , "��������");
					onClearBtn();
					
					break;
				default:
					break;
				}
			}
		}
	}
	public void onClearBtn(){
		if(!UsefulClass.hasappnamedxxx(this, "com.sogou.androidtool")){
			Toast.makeText(getApplicationContext(), "û�а�װ����", Toast.LENGTH_SHORT).show();
			return;
		}
		String cmd = "pm clear com.sogou.androidtool";

		ProcessBuilder pb = new ProcessBuilder().redirectErrorStream(true).command("su");
		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(p == null){
			Toast.makeText(getApplicationContext(), "�����ֻ���δroot��������", Toast.LENGTH_SHORT).show();
		}
		// We must handle the result stream in another Thread first
		StreamReader stdoutReader = new StreamReader(p.getInputStream(), "utf-8");
		stdoutReader.start();
		
		OutputStream out = p.getOutputStream();
		try {
			out.write((cmd + "\n").getBytes("utf-8"));
			out.write(("exit" + "\n").getBytes("utf-8"));
			out.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Toast.makeText(getApplicationContext(), "����Ϊ������", Toast.LENGTH_SHORT).show();
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = stdoutReader.getResult();
	}
}

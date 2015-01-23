package com.sogou.mobiletoolassist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.sogou.mobiletoolassist.service.ClearDataService;
import com.sogou.mobiletoolassist.service.FileObserverService;
import com.sogou.mobiletoolassist.util.ScreenshotforGINGERBREAD_MR1;
import com.sogou.mobiletoolassist.util.UsefulClass;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class AssistActivity extends Activity {
	public static String myTag = "Assist";
	public static String obPath = Environment.getExternalStorageDirectory().getPath()+File.separator+"MobileTool/CrashReport";
	public static int selectedidx = 0;
	private Stack<String> dirs = new Stack<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_assist);
		
//		if (savedInstanceState == null) {
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
		setPathView();//����Ĭ����ʾ·��
//		if(!UsefulClass.isServiceRunning(this,ClearDataService.class.getName())){
//			Intent it = new Intent(this,ClearDataService.class);			
//			this.startService(it);
//			Log.d(myTag, "cleardataservice start");
//		}
		
	}
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_BACK) {  
            moveTaskToBack(false);  
            return true;  
        }  
        return super.onKeyDown(keyCode, event);  
    } 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.assist, menu);
		return true;
	}

	
	
	private void setPathView(){
		TextView v = (TextView)this.findViewById(R.id.textView1);
		v.setText(obPath);
	}
	
	private void ShowDialog(final String path){
		
		final File tmp = new File(path);
		File files[] = tmp.listFiles();
		selectedidx = 0;//ÿ�ν��붼����
		ArrayList<String> paths = new ArrayList<String>(); 
		if(files != null){
			for(File apath :files){
				if(apath.isDirectory()){
					paths.add(apath.toString());
				}
			}
		}
		final String spaths[] = (String[])paths.toArray(new String[paths.size()]);
		AlertDialog ad = new AlertDialog.Builder(this)
		.setTitle("ѡ��һ��Ҫ��ص��ļ���")
		.setSingleChoiceItems(spaths, 0,new DialogInterface.OnClickListener(){
			@Override
		     public void onClick(DialogInterface dialog, int which) {
				selectedidx = which;//��֪��Ϊʲô�õ�ѡ�б��������ʱwhich����Чֵ
		     }
		})
		.setNegativeButton("��һ��", new DialogInterface.OnClickListener(){
			@Override
		     public void onClick(DialogInterface dialog, int which) {
				if(dirs.isEmpty()){
					Toast.makeText(getApplicationContext(), "�Ѿ������Ŀ¼��", Toast.LENGTH_LONG).show();
					ShowDialog(tmp.getPath());
					return;
				}
				ShowDialog(dirs.pop());
		     }
		})
		.setNeutralButton("����", new DialogInterface.OnClickListener(){
			@Override
		     public void onClick(DialogInterface dialog, int which) {
				dirs.add(path);
				ShowDialog(spaths[selectedidx]);
		     }
		})
		.setPositiveButton("ѡ��", new DialogInterface.OnClickListener(){
			@Override
		     public void onClick(DialogInterface dialog, int which) {
				if(spaths != null && spaths.length > selectedidx){
					obPath = spaths[selectedidx];
					setPathView();
				}
		     }
		})
		.show();
		
	}

	public void onSelectClick(View arg0) {		
		File file = Environment.getExternalStorageDirectory();
		ShowDialog(file.getPath());
		
	}
	public void onStartObserve(View v){
		if(!new File(obPath).exists()){
			Log.e(myTag, obPath + " does not exist");
			File p = new File(obPath);
			p.mkdirs();
		}
		Intent intent = new Intent(this, FileObserverService.class);
		intent.putExtra("observerpath", obPath);
		this.startService(intent);
		
		//v.setClickable(false);���ɵ���ǲ��ܴ�������¼������ǰ�ť�����������ܵ��
		v.setEnabled(false);
		this.findViewById(R.id.SelectPath).setEnabled(false);

	}
	
	public void onTestbtn(View v){
		ScreenshotforGINGERBREAD_MR1.init(this);
		ScreenshotforGINGERBREAD_MR1.shoot();
	}
	
}

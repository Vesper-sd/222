package com.sogou.mobiletoolassist.fileobserver;


import java.io.File;

import com.sogou.mobiletoolassist.AssistActivity;
import com.sogou.mobiletoolassist.util.MailSender;

import android.os.FileObserver;
import android.util.Log;
import android.widget.Toast;

public class FileObserverThread extends FileObserver {
	private String observerpath = null;
	private String tmp = null;
	public FileObserverThread(String path) {  
        /* 
         * ���ֹ��췽����Ĭ�ϼ��������¼���,���ʹ��super(String,int)���ֹ��췽���� 
         * ��int������Ҫ�������¼�����. 
         */  
        super(path);  
        observerpath = path;
    }  
  
    @Override  
    public void onEvent(int event, String path) {         
        switch(event) {  
        case FileObserver.ALL_EVENTS:  
            
        	System.out.println("everything");
            break;  
        case FileObserver.CREATE:  
        	tmp = observerpath+File.separator+path;
        	new Thread(new Runnable(){
        		@Override
        		public void run(){
        			try {
        				Thread.sleep(10000);
        			} catch (InterruptedException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
	            	//String screenshot = observerpath+File.separator+path;
	            	MailSender.sendTextMail("pdatest0123@163.com", "0123pdatest", "smtp.163.com", 
	    					"�ֻ�sd�����", "��ϸ���ݼ�����", tmp, 
	    					new String[]{"26304484@163.com"});
	            	
	            	
	            	File shot = new File(tmp);
	            	if(shot.exists()){
	            		shot.delete();
	            	}

        		}
        	}).start();
        	
//        	String screenshot = observerpath+File.separator+path;
//        	MailSender.sendTextMail("pdatest0123@163.com", "0123pdatest", "smtp.163.com", 
//					"�ֻ�sd�����", "��ϸ���ݼ�����", screenshot, 
//					new String[]{"zhangshuai203407@sogou-inc.com"});
//        	Log.i(FileSelectorActivity.myTag, "send mail over");


            break;  
        }  
    }  
}

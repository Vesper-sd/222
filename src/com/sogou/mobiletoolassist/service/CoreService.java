package com.sogou.mobiletoolassist.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.sogou.mobiletoolassist.AssistActivity;
import com.sogou.mobiletoolassist.R;
import com.sogou.mobiletoolassist.AssistApplication;
import com.sogou.mobiletoolassist.fileobserver.FileObserverThread;
import com.sogou.mobiletoolassist.receiver.SimuBroadcastRec;
import com.sogou.mobiletoolassist.util.FetchNewestMTApk;
import com.sogou.mobiletoolassist.util.MailSender;
import com.sogou.mobiletoolassist.util.ScreenshotforGINGERBREAD_MR1;
import com.sogou.mobiletoolassist.util.ScreenshotforJELLY_BEAN;
import com.sogou.mobiletoolassist.util.StateValue;
import com.sogou.mobiletoolassist.util.UsefulClass;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class CoreService extends Service implements OnClickListener {
	private static WindowManager wm = null;
	private static WindowManager.LayoutParams params = null;
	private View btn_floatView = null;
	private ImageButton clearBtn = null;
	private ImageButton screenshotBtn = null;
	private ImageButton nexthour = null;
	private ImageButton nextday = null;
	private ImageButton smallview = null;
	private ImageButton wifisetview = null;
	private ImageButton appmagBtn = null;
	private final IBinder binder = new MyBinder();
	private final static int hide = 0x0000001;
	public final static int screenshot = 0x0000002;
	private final static int visiable = 0x0000003;
	private final static int installmt = 0x0000004;
	private final static int downloadfailed = 0x0000005;
	public final static int sendBroadcast = 0x0000006;
	private static boolean isUninstalling = false;
	public static String mtpathString = Environment
			.getExternalStorageDirectory().getPath() + File.separator;
	private FileObserverThread listener = null;
	private String observerpath = null;
	private String emailReceiver = null;
	public boolean isInstalling = false;
	private Thread sendBroadcastThd = null;
	public Handler fltwinhandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CoreService.hide:
				wm.removeView(btn_floatView);
				break;
			case CoreService.screenshot:
				Uri uri = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);// ϵͳ�Դ���ʾ��
				Ringtone rt = RingtoneManager.getRingtone(
						getApplicationContext(), uri);
				if (rt != null)
					rt.play();
				CoreService.ScreenShot();
				Message message = new Message();
				message.what = CoreService.visiable;
				fltwinhandler.sendMessage(message);
				break;
			case CoreService.visiable:
				wm.addView(btn_floatView, params);
				break;
			case CoreService.installmt:
				// Intent installIntent = new Intent(Intent.ACTION_MAIN);
				// installIntent.setAction(AssistActivity.installedaction);
				// installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// startActivity(installIntent);
				String pathString = msg.getData().getString("path");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.fromFile(new File(pathString)),
						"application/vnd.android.package-archive");
				startActivity(intent);
				isInstalling = false;
				// stopForeground(true);// ȡ��ǰ̨����
				NotificationManager mNotifyMgr = (NotificationManager) AssistApplication
						.getContext().getSystemService(NOTIFICATION_SERVICE);
				mNotifyMgr.cancel(2048);
				break;
			case CoreService.downloadfailed:
				Toast.makeText(AssistApplication.getContext(), "����ʧ�ܣ�",
						Toast.LENGTH_LONG).show();
				isInstalling = false;
				// stopForeground(true);// ȡ��ǰ̨����
				NotificationManager motifyMgr = (NotificationManager) AssistApplication
						.getContext().getSystemService(NOTIFICATION_SERVICE);
				motifyMgr.cancel(2048);
				break;
			case CoreService.sendBroadcast:
				sendBroadcastSimulation();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {

		return binder;
	}

	public class MyBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}

	public native String memcreate(int mem);

	public native String memfree();

	static {
		System.loadLibrary("memCtrl");
	}
	public ButtonBroadcastReceiver bReceiver;

	@Override
	public void onCreate() {
		if (intentStrings != null && intentStrings.isEmpty()) {
			try {
				InputStream inputStream = getAssets().open("build_in_actions");
				int cnt = inputStream.available();
				byte buf[] = new byte[cnt];
				inputStream.read(buf);
				String actions = new String(buf);
				String actionsArrString[] = actions.split("\r\n");// note it may
																	// be
																	// different
																	// in linux
																	// system
				intentStrings.addAll(Arrays.asList(actionsArrString));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		bReceiver = new ButtonBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_BUTTON);
		registerReceiver(bReceiver, intentFilter);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("study", "core service onstart");
	}

	private void init(String path) {
		SharedPreferences appdata = this.getSharedPreferences("AppData",
				MODE_PRIVATE);
		appdata.edit().putString("obPath", path).commit(); // ��ֹ����������path���浽����

	}

	public void startWatching() {
		SharedPreferences appdata = this.getSharedPreferences("AppData",
				MODE_PRIVATE);
		String deafultpath = Environment.getExternalStorageDirectory()
				.getPath();
		deafultpath += File.separator + "MobileTool/CrashReport";
		observerpath = appdata.getString("obPath", deafultpath);
		emailReceiver = appdata.getString("mailReceiver",
				"pdatest@sogou-inc.com");
		if (listener != null) {
			listener.stopWatching();
			listener = null;
		}
		listener = new FileObserverThread(observerpath, emailReceiver);
		listener.startWatching();
	}

	public void stopWatching() {
		listener.stopWatching();
		listener = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("study", "core service onStartCommand");
		if (intent != null && intent.getBooleanExtra("setalarm", false)) {
			int cnt = getSharedPreferences("broadcastcnt", MODE_PRIVATE)
					.getInt("bdcnt", 0);
			if (cnt > 0) {
				setNextAlarm(cnt);
			}
			return Service.START_STICKY;
		}
		createFloatView();
		SharedPreferences appdata = this.getSharedPreferences("AppData",
				MODE_PRIVATE);

		int state = appdata.getInt("isWatching", AssistActivity.neverWatching);
		if (state == AssistActivity.isWatching) {
			startWatching();
		}

		setToolNotify();
		return Service.START_STICKY;// ��ʾ��ϵͳɱ������Ҫ����
	}

	@Override
	public void onDestroy() {
		wm.removeView(btn_floatView);
		super.onDestroy();
	}

	/**
	 * ����������
	 */
	@SuppressLint("InflateParams")
	private void createFloatView() {
		SharedPreferences appdata = getSharedPreferences("AppData",
				MODE_PRIVATE);
		if (!appdata.getBoolean("isFloatWinOn", true)) {
			return;
		}
		if (btn_floatView != null) {
			wm.addView(btn_floatView, params);
			return;
		}
		btn_floatView = LayoutInflater.from(this).inflate(R.layout.floatwin,
				null);
		smallview = (ImageButton) btn_floatView
				.findViewById(R.id.fwinsmallview);
		smallview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!clearBtn.isShown() || !screenshotBtn.isShown()) {
					clearBtn.setVisibility(Button.VISIBLE);
					screenshotBtn.setVisibility(Button.VISIBLE);
					nexthour.setVisibility(Button.VISIBLE);
					nextday.setVisibility(Button.VISIBLE);
					wifisetview.setVisibility(Button.VISIBLE);
					appmagBtn.setVisibility(Button.VISIBLE);
					smallview.setImageResource(R.drawable.floatwin);
				} else {
					wifisetview.setVisibility(Button.GONE);
					nextday.setVisibility(Button.GONE);
					nexthour.setVisibility(Button.GONE);
					clearBtn.setVisibility(Button.GONE);
					screenshotBtn.setVisibility(Button.GONE);
					appmagBtn.setVisibility(Button.GONE);
					smallview.setImageResource(R.drawable.floatwin_collapsed);
				}
			}

		});
		appmagBtn = (ImageButton) btn_floatView.findViewById(R.id.appmag);
		appmagBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent it = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);

			}

		});
		nexthour = (ImageButton) btn_floatView.findViewById(R.id.nexthour);
		nexthour.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd",
						Locale.CHINA);
				Calendar ca = Calendar.getInstance();
				ca.add(Calendar.HOUR_OF_DAY, 1);
				String nowDate = format.format(ca.getTime());
				int hour = ca.get(Calendar.HOUR_OF_DAY);
				String nextTime = String.valueOf(hour) + "5500";
				String cmd = "date -s  " + nowDate + "." + nextTime;
				UsefulClass.processCmd(cmd);

			}

		});
		nextday = (ImageButton) btn_floatView.findViewById(R.id.nextday);
		nextday.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Calendar ca = Calendar.getInstance();
				ca.add(Calendar.DAY_OF_MONTH, 1);
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd",
						Locale.CHINA);
				String nowTime = format.format(ca.getTime());
				UsefulClass.processCmd("date -s " + nowTime + ".000500");
			}
		});
		wifisetview = (ImageButton) btn_floatView.findViewById(R.id.wifiset);
		wifisetview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent it = new Intent(Settings.ACTION_DATE_SETTINGS);

				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
			}

		});
		clearBtn = (ImageButton) btn_floatView.findViewById(R.id.cleardatabtn);
		clearBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CoreService.onClearBtn();
			}

		});
		screenshotBtn = (ImageButton) btn_floatView
				.findViewById(R.id.screenshotbtn);
		screenshotBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Message message = new Message();
						message.what = CoreService.hide;
						Message message1 = new Message();
						message1.what = CoreService.screenshot;
						fltwinhandler.sendMessage(message);
						fltwinhandler.sendMessageDelayed(message1, 500);
					}

				}).start();
			}

		});
		wm = (WindowManager) getApplicationContext().getSystemService(
				Context.WINDOW_SERVICE);

		params = new WindowManager.LayoutParams();

		// ����window type
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		/*
		 * �������Ϊparams.type = WindowManager.LayoutParams.TYPE_PHONE; ��ô���ȼ��ή��һЩ,
		 * ������֪ͨ�����ɼ�
		 */

		params.format = PixelFormat.RGBA_8888;

		// ����Window flag
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		/*
		 * �����flags���Ե�Ч����ͬ���������� ���������ɴ������������κ��¼�,ͬʱ��Ӱ�������¼���Ӧ��
		 * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
		 * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		 */

		// �����������ĳ��ÿ�

		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.gravity = Gravity.LEFT; // �����������������Ͻ�
		params.x = 0;
		params.y = 0;
		// ��Ҫ��������system.alert_windowȨ��
		wm.addView(btn_floatView, params);
		appdata.edit().putBoolean("isFloatWinOn", true).commit();
		smallview.setOnTouchListener(new OnTouchListener() {
			int lastX, lastY;
			int paramX, paramY;

			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = params.x;
					paramY = params.y;

					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
						// ����ô��̫�����ˣ����������ڵ�����ƶ���ë�߰�

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
					if (Math.abs(dx1) < 5 && Math.abs(dy1) < 5) {
						smallview.performClick();
					}

				}
				return true;
			}
		});

	}

	public static void onClearBtn() {
		Context ctx = AssistApplication.getContext();
		if (ctx == null) {
			Log.e(AssistActivity.myTag, "ctx �ǿյ�");
			return;
		}
		if (!UsefulClass.hasappnamedxxx(ctx, "com.sogou.androidtool")) {
			Toast.makeText(ctx, "û�а�װ����", Toast.LENGTH_SHORT).show();
			return;
		}
		String cmd = "pm clear com.sogou.androidtool";
		Toast.makeText(ctx, "׼����������~", Toast.LENGTH_SHORT).show();
		if (UsefulClass.processCmd(cmd) == StateValue.success) {
			Toast.makeText(ctx, "�����������~", Toast.LENGTH_SHORT).show();
		}
	}

	public static boolean ScreenShot() {

		String path = "";
		if (Build.VERSION.SDK_INT > 13) {
			path = ScreenshotforJELLY_BEAN.shoot();
		} else {
			if (ScreenshotforGINGERBREAD_MR1.isInitialized())
				path = ScreenshotforGINGERBREAD_MR1.shoot();
		}

		File testpath = new File(path);
		if (!testpath.exists()) {
			Toast.makeText(AssistApplication.getContext(), "��ͼ�ļ����ڣ�����sd���Ƿ�����",
					Toast.LENGTH_LONG).show();

			return false;
		}
		String info = UsefulClass.getDeviceInfo();
		String title = info + "����ͼ��";
		info += "</br>";
		info += UsefulClass.getZSPkgInfo();
		SharedPreferences appdata = AssistApplication.getContext()
				.getSharedPreferences("AppData", MODE_PRIVATE);
		String emailReceiver = appdata.getString("mailReceiver",
				"pdatest@sogou-inc.com");
		if (AssistApplication.getContext() != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) AssistApplication
					.getContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo == null || !mNetworkInfo.isConnected()) {
				Toast.makeText(AssistApplication.getContext(),
						"����ò��������Ŷ���ʼ�������ȥ", Toast.LENGTH_LONG).show();

				return false;
			}
		}
		if (MailSender.sendTextMail(title, info, path,
				new String[] { emailReceiver })) {
			String emailReceivername = appdata.getString("name", "pdatest");
			Toast.makeText(AssistApplication.getContext(),
					"��ͼ��ϣ�" + emailReceivername + "ͬѧ�뾲���ʼ�~", Toast.LENGTH_LONG)
					.show();
			File tmp = new File(path);
			if (tmp.exists())
				tmp.delete();
		} else {
			Toast.makeText(AssistApplication.getContext(), "�����ʼ��쳣�������Ƕ���ͼʧ����",
					Toast.LENGTH_LONG).show();
		}

		return true;
	}

	private Runnable uinstallrun = new Runnable() {

		@Override
		public void run() {
			CoreService.isUninstalling = true;
			Context ctx = AssistApplication.getContext();
			PackageManager pkgmgr = (PackageManager) ctx.getPackageManager();
			List<PackageInfo> allapps = pkgmgr.getInstalledPackages(0);
			List<PackageInfo> alluserapps = new ArrayList<PackageInfo>();
			for (PackageInfo app : allapps) {
				if ((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					alluserapps.add(app);// �������з�ϵͳӦ��
				}
			}
			if (alluserapps.size() > 30) {
				Toast.makeText(ctx, "app̫�࣬�����������ȥ�ȱ�ˮ��~", Toast.LENGTH_SHORT)
						.show();
			}
			allapps = null;
			if (alluserapps == null || alluserapps.isEmpty()) {
				Toast.makeText(ctx, "����ֻ�û���Ѱ�װӦ��~", Toast.LENGTH_SHORT).show();
				CoreService.isUninstalling = false;
				return;
			}
			Toast.makeText(ctx, "��ʼ����app", Toast.LENGTH_SHORT).show();

			String cmd = "pm uninstall ";
			for (PackageInfo app : alluserapps) {
				if (app.packageName != null
						&& app.packageName.length() != 0
						&& !app.packageName.equals("com.sogou.androidtool")
						&& !app.packageName
								.equals("com.sogou.mobiletoolassist")
						&& !app.packageName
								.equals("com.sohu.inputmethod.sogou")
						&& !app.packageName
								.equals("com.speedsoftware.rootexplorer")) {

					if (StateValue.unroot == UsefulClass.processCmd(cmd
							+ app.packageName)) {
						Toast.makeText(ctx, "��ȡrootȨ��ʧ�ܣ�����", Toast.LENGTH_SHORT)
								.show();
						break;
					}
				}
			}
			Toast.makeText(ctx, "��ж������app", Toast.LENGTH_LONG).show();
			CoreService.isUninstalling = false;
		}
	};

	public void uninstallAPPs() {
		if (isUninstalling)
			return;
		fltwinhandler.post(uinstallrun);
	}

	public void floatwinswitch(boolean set) {
		if (wm == null || btn_floatView == null || params == null) {
			createFloatView();
			return;
		}
		if (set) {
			wm.addView(btn_floatView, params);
		} else {
			wm.removeView(btn_floatView);
		}
	}

	public void installmt() {
		if (isInstalling) {
			Toast.makeText(this, "�������أ��벻Ҫ�ظ����", Toast.LENGTH_LONG).show();
			return;
		}

		Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		notificationIntent.setClass(AssistApplication.getContext(),
				AssistActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent contentIntent = PendingIntent.getActivity(
				AssistApplication.getContext(), 0, notificationIntent, 0);
		// notif.setLatestEventInfo(context, contentTitle, contentText,
		// contentIntent);
		Builder builder = new NotificationCompat.Builder(
				AssistApplication.getContext());
		builder.setContentIntent(contentIntent).setAutoCancel(false)
				.setSmallIcon(R.drawable.ic_launcher).setOngoing(true)
				.setContentTitle("�����������°����ֲ��԰�");
		// startForeground(1024, builder.build());
		NotificationManager mNotifyMgr = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		mNotifyMgr.notify(2048, builder.build());
		new Thread(new Runnable() {

			@Override
			public void run() {

				isInstalling = true;

				String root_urlString = getResources().getString(
						R.string.mt_download_dir_url);
				String downloadurlString = null;
				try {
					downloadurlString = FetchNewestMTApk
							.getDownloadUrl(root_urlString);
				} catch (IOException e) {
					// TODO Auto-generated catch block

					e.printStackTrace();
				}
				if (downloadurlString == null) {
					Message msgMessage = new Message();
					msgMessage.what = downloadfailed;
					fltwinhandler.sendMessage(msgMessage);
					return;
				}

				int idx = downloadurlString.lastIndexOf("/");
				String filenameString = downloadurlString.substring(idx + 1);
				File file = new File(mtpathString + filenameString);

				if (file.exists()) {
					file.delete();
					file = null;
				}
				UsefulClass.Download(downloadurlString, mtpathString
						+ filenameString);
				Message msg = new Message();

				msg.what = CoreService.installmt;
				Bundle bundle = new Bundle();
				bundle.putString("path", mtpathString + filenameString);
				msg.setData(bundle);
				fltwinhandler.sendMessage(msg);

			}
		}).start();
	}

	private static ArrayList<String> intentStrings = new ArrayList<String>() {
	};
	private static int intentCnt = 0;

	public void sendBroadcastSimulation() {
		int cnt = getSharedPreferences("broadcastcnt", MODE_PRIVATE).getInt(
				"bdcnt", 0);
		if (cnt > 0) {
			return;
		}
		setNextAlarm(cnt);
	}

	public void setNextAlarm(int cnt) {
		PowerManager.WakeLock wakeLock = null;

		final PowerManager pm = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);

		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"send test broadcast");
		wakeLock.acquire();
		
		
		String bString = intentStrings.get(cnt);
		if (cnt == intentStrings.size()) {
			getSharedPreferences("broadcastcnt", MODE_PRIVATE).edit()
					.putInt("bdcnt", 0).commit();
			return;
		}
		cnt++;
		getSharedPreferences("broadcastcnt", MODE_PRIVATE).edit()
				.putInt("bdcnt", cnt).commit();
		AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		if (alarms == null) {
			return;
		}

		Intent intent = new Intent(SimuBroadcastRec.broadcastAction);
		intent.setClassName("com.sogou.mobiletoolassist",
				SimuBroadcastRec.class.getName());

		intent.putExtra("broadcastname", bString);

		PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent,
				PendingIntent.FLAG_ONE_SHOT);
		alarms.set(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + 15 * 60 * 1000, pIntent);
		wakeLock.release();
		wakeLock = null;
		Log.i("broadcast", "fale 1 tiao");
	}

	public final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";
	public final static String INTENT_BUTTONID_TAG = "ButtonId";

	private void setToolNotify() {
		if (Build.VERSION.SDK_INT > 13) {

			RemoteViews mRemoteViews = new RemoteViews(getPackageName(),
					R.layout.view_custom_button);

			mRemoteViews.setImageViewResource(R.id.custom_song_icon,
					R.drawable.sing_icon);
			// API3.0 ���ϵ�ʱ����ʾ��ť��������ʧ
			mRemoteViews.setTextViewText(R.id.notifyTitle, "���ֲ��Թ���");
			mRemoteViews
					.setTextViewText(R.id.notifyContent, "zhangshuai203407");

			// ������¼�����
			Intent buttonIntent = new Intent(ACTION_BUTTON);
			buttonIntent.putExtra(INTENT_BUTTONID_TAG, 3);
			PendingIntent intent_next = PendingIntent.getBroadcast(this, 3,
					buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_next,
					intent_next);
			Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
			notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			notificationIntent.setClass(this, AssistActivity.class);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			PendingIntent enterAssist = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			mRemoteViews.setOnClickPendingIntent(R.id.toolsNotif, enterAssist);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					this);
			builder.setContent(mRemoteViews);
			builder.setOngoing(true);
			Notification nf = builder.build();
			nf.icon = R.drawable.ic_launcher;
			startForeground(1024, nf);
		} else {
			Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
			notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			notificationIntent.setClass(this, AssistActivity.class);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			Builder builder = new NotificationCompat.Builder(this);
			builder.setContentIntent(contentIntent).setAutoCancel(false)
					.setSmallIcon(R.drawable.ic_launcher).setOngoing(true)
					.setContentTitle("��������������").setContentText("zs");
			startForeground(1024, builder.build());
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	public class ButtonBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//
			String action = intent.getAction();
			if (action.equals(ACTION_BUTTON)) {
				// ͨ�����ݹ�����ID�жϰ�ť������Ի���ͨ��getResultCode()�����Ӧ����¼�
				int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
				switch (buttonId) {
				case 1:
					// Log.d("assist" , "��һ��");
					// Toast.makeText(getApplicationContext(), "��һ��",
					// Toast.LENGTH_SHORT).show();
					// break;
				case 2:
					// String play_status = "";
					//
					// //showButtonNotify();
					// Log.d("assist" , play_status);
					// Toast.makeText(getApplicationContext(), play_status,
					// Toast.LENGTH_SHORT).show();
					// break;
				case 3:
					new Thread(new Runnable() {
						@Override
						public void run() {
							Uri uri = RingtoneManager
									.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);// ϵͳ�Դ���ʾ��
							Ringtone rt = RingtoneManager.getRingtone(
									getApplicationContext(), uri);
							if (rt != null)
								rt.play();
							// TODO ��û���߳�ʱ�����intent���յ�android
							// runtime����ʱ���һ�£������߳̽��
							Looper.prepare();
							CoreService.ScreenShot();
							Looper.loop();

						}

					}).start();

					break;
				default:
					break;
				}
			}
		}
	}
}

package com.sogou.mobiletoolassist.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;


import android.R.integer;
import android.os.Environment;

public class JsonTestResultHandle {
	public static void sendJsonTestResult(String receiver) {
		String pathString = Environment.getExternalStorageDirectory()
				+ File.separator + "TestFramework" + File.separator
				+ "TestResult";
		File file = new File(pathString);
		if (file.exists() && file.isDirectory()) {
			String[] filesString = file.list();
			long recent = 0;
			File lastFile = null;
			for (int i = 0; i < filesString.length; i++) {
				File tmpfileFile = new File(pathString + File.separator+filesString[i]);
				long tm = tmpfileFile.lastModified();
				if (tm > recent) {
					recent = tm;
					lastFile = tmpfileFile;
				}
			}
			if (lastFile != null) {
				ArrayList<String> errStrings = findExceptInLog(lastFile);
				if (errStrings == null || errStrings.isEmpty()) {
					return;
				}
				MailSender.sendTextMail("Json�Զ������Խ��", errStrings.toString(),
						lastFile.getPath(), new String[] { receiver });
			}else{
				MailSender.sendTextMail("��Something is wrong��Json�Զ������Խ��", "û���ҵ����Խ���ļ������ֹ��鿴һ�£�������뱨����bug",
						null, new String[] { receiver });
			}
			
		} else {
			// TODO
		}
	}

	private static ArrayList<String> findExceptInLog(File file) {
		if (file == null) {
			return null;
		}
		ArrayList<String> errCaseIdStrings = new ArrayList<String>();
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(file));

			String tmp = bReader.readLine();
			while (tmp != null) {
				if (tmp.contains("��ʼ����")) {
					int ididx = tmp.indexOf(":");
					int caseidx = tmp.indexOf("caseid");
					String id = tmp.substring(caseidx, ididx);
					while (tmp != null && !tmp.contains(id + ":���Խ���")) {						
						if (tmp.contains("except")) {
							errCaseIdStrings.add(id);
							break;
						}
						tmp = bReader.readLine();
					}
				}
				tmp = bReader.readLine();
			}
			bReader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return errCaseIdStrings;
	}
}

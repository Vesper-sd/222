package com.sogou.mobiletoolassist.util;


import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


/**
 * �����ʼ�����������ߡ������ʼ�
 * 
 * @author andrewleo
 */
public class MailSender {

	private static final int PORT = 25;
	private static final String sender = "pdatest0123@126.com";
	private static final String encryptPassword = "0123pdatest";
	private static final String smtp = "portal.sys.sogou-op.org";
	
	/**
	 * ���ı���ʽ�����ʼ�
	 * 
	 *            �����͵��ʼ�����Ϣ
	 */
	public static boolean sendTextMail(String subject, String content, String file,
			String[] maillists) {
		if (maillists == null || maillists.length == 0
				|| ("".equals(maillists[0].trim()))) {
			return false;
		} else {
			// Get system properties
			Properties props = new Properties();

			// Setup mail server
			props.put("mail.smtp.host", smtp);
			props.put("mail.smtp.port", PORT);
			// Get session
			props.put("mail.smtp.auth", "true"); // �����Ҫ������֤���������false�ĳ�true

			// �ж��Ƿ���Ҫ�����֤
			CustomizedAuthenticator authenticator = null;
			if (true) {
				// �����Ҫ�����֤���򴴽�һ��������֤��
				authenticator = new CustomizedAuthenticator(sender,
						encryptPassword);
			}
			// �����ʼ��Ự���Ժ�������֤������һ�������ʼ���session
			Session sendMailSession = Session.getInstance(props,
					authenticator);
			try {
				// ����session����һ���ʼ���Ϣ
				Message mailMessage = new MimeMessage(sendMailSession);
				// �����ʼ������ߵ�ַ
				Address from = new InternetAddress(sender);
				// �����ʼ���Ϣ�ķ�����
				mailMessage.setFrom(from);
				// �����ʼ��Ľ����ߵ�ַ�������õ��ʼ���Ϣ��
				Address[] tos = null;

				tos = new InternetAddress[maillists.length];
				for (int i = 0; i < maillists.length; i++) {
					tos[i] = new InternetAddress(maillists[i]);
				}

				// Message.RecipientType.TO���Ա�ʾ�����ߵ�����ΪTO
				mailMessage.setRecipients(Message.RecipientType.TO, tos);
				// �����ʼ���Ϣ������
				mailMessage.setSubject(subject);
				// �����ʼ���Ϣ���͵�ʱ��
				mailMessage.setSentDate(new Date());

				BodyPart bodyPart = new MimeBodyPart();
				
				StringBuffer header = new StringBuffer();
				header.append("<html>");
				header.append("<head>");
				header.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=GBK\"/>");
				header.append("<style type=\"text/css\">");
				header.append("table{border:1px #E1E1E1 solid;}");
				header.append("td{border:1px #E1E1E1 solid;height:35px;}");
				header.append("th{border:1px #E1E1E1 solid;}");
				header.append("tr {font-size: 15px; COLOR:#000000; background-color:#FFFFFF; font-family: Tahoma; text-align:center;}");
				header.append("</style>");
				header.append("</head>");
				header.append("<body>");
				StringBuffer table = new StringBuffer(content);
				StringBuffer footer = new StringBuffer();
				footer.append("</body>");
				footer.append("</html>");
				bodyPart.setContent(header.append(table).append(footer).toString(), "text/html; charset=utf-8");
				
				Multipart multipart = new MimeMultipart();
				MimeBodyPart attachPart = new MimeBodyPart();
				File resultFile = new File(file);
				if(resultFile.isFile() && resultFile.exists()){
					DataSource source = new FileDataSource(file);
					attachPart.setDataHandler(new DataHandler(source));
					attachPart.setFileName(new File(file).getName());
					multipart.addBodyPart(attachPart);
				}
				
				multipart.addBodyPart(bodyPart);
				mailMessage.setContent(multipart);
				
				MailcapCommandMap mc = (MailcapCommandMap) CommandMap
						.getDefaultCommandMap();
				mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
				mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
				mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
				mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
				mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
				CommandMap.setDefaultCommandMap(mc);
				
				/*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()  
		        	.detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());  
				StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()  
						.detectLeakedClosableObjects().penaltyDeath().build());*/
				
				Transport.send(mailMessage);
				return true;
			} catch (MessagingException ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}
}


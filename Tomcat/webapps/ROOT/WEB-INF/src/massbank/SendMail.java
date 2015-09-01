/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *******************************************************************************
 *
 * メール送信共通クラス
 *
 * ver 1.0.1 2013.11.11
 *
 ******************************************************************************/
package massbank;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;


/**
 * メール送信共通クラス
 */
public class SendMail {

	private static String host = "";
	private static String port = "";
	private static String user = "";
	private static String pass = "";

	/**
	 * メール送信関数
	 * @param info メール送信情報オブジェクト
	 * @return 結果
	 */
	public static boolean send(SendMailInfo info) {
		
		// メール送信情報チェック
		if (!info.isCheck()) {
			Logger.global.severe( "The mail sending failed.");
			return false;
		}

		String[] smtpItems = info.getSmtp().split(",");
		if ( smtpItems.length >= 1 ) {
			host = smtpItems[0].trim();
		}
		if ( smtpItems.length >= 2 ) {
			port = smtpItems[1].trim();
		}
		if ( smtpItems.length >= 3 ) {
			user = smtpItems[2].trim();
		}
		if ( smtpItems.length >= 4 ) {
			pass = smtpItems[3].trim();
		}
		if ( port.equals("") ) {
			port = "25";
		}
		System.out.println( host + "/" + port + "/" + user + "/" + pass);

		try {
			// SMTPサーバーのアドレスを設定
			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);

			if ( port.equals("465") || port.equals("587") ) {
				props.put("mail.smtp.ssl.trust", host);
				if ( port.equals("465") ) {
					props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
					props.put("mail.smtp.socketFactory.fallback", "false");
				}
				else if ( port.equals("587") ) {
					props.put("mail.smtp.starttls.enable", "true");
				}
			}
			Session session = null;
			if ( user.equals("") || pass.equals("") ) {
				props.put("mail.smtp.auth", "false");
				session = Session.getDefaultInstance(props, null);
			}
			else {
				props.put("mail.smtp.auth", "true");
				session = Session.getInstance(props, new Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(user, pass);
						}
					}
				);
			}

			MimeMessage mimeMsg = new MimeMessage(session);

			// 送信元メールアドレスと送信者名を設定
			mimeMsg.setFrom(new InternetAddress(info.getFrom(), info.getFromName(), "utf-8"));
			
			// 送信先メールアドレスを設定
			mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(info.getTo()));
			if (!info.getCc().equals("")) {
				mimeMsg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(info.getCc()));
			}
			if (!info.getBcc().equals("")) {
				mimeMsg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(info.getBcc()));
			}
			
			// メールタイトルを設定
			mimeMsg.setSubject(info.getSubject(), "utf-8");
			
			// メールボディ用マルチパートオブジェクト生成
			MimeMultipart mp = new MimeMultipart();
			MimeBodyPart mbp = new MimeBodyPart();
			mbp.setText(info.getContents(), "utf-8" );	// 本文
			mp.addBodyPart(mbp);
			File[] files = info.getFiles();
			if (files != null) {
				for(int i=0; i<files.length; i++){		// 添付ファイル
					mbp = new MimeBodyPart();
					FileDataSource fds = new FileDataSource(files[i]);
					mbp.setDataHandler(new DataHandler(fds));
					mbp.setFileName(MimeUtility.encodeWord(fds.getName()));
					mp.addBodyPart(mbp);
				}
			}
			
			// メール内容にマルチパートオブジェクトと送信日付を設定して送信
			mimeMsg.setContent(mp);
			mimeMsg.setSentDate(new Date());
			Transport.send(mimeMsg);
		}
		catch (Exception e) {
			Logger.global.severe( "The mail sending failed.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

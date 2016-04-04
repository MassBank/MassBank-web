/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
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
 * メール送信クラス
 *
 * ver 1.0.1 2009.02.04
 *
 ******************************************************************************/
package massbank.pserver;

import java.util.Properties;
import java.util.Date;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.Message;
import javax.mail.Transport;

public class SendMail {

	public static void send(String subject, String contents) {
		try {
			Properties props = System.getProperties();

			// SMTPサーバーのアドレスを設定
			props.put( "mail.smtp.host", "mail.iab.keio.ac.jp" );

			Session session = Session.getDefaultInstance( props, null );
			MimeMessage mimeMsg = new MimeMessage(session);

			// 送信元メールアドレスと送信者名を設定
			mimeMsg.setFrom( new InternetAddress("massbank@iab.keio.ac.jp", "MassBank Administrator", "iso-2022-jp") );

			// 送信先メールアドレスを設定
			mimeMsg.setRecipients( Message.RecipientType.TO, "massbank@iab.keio.ac.jp" );

			// メールのタイトルを設定
			mimeMsg.setSubject( subject, "iso-2022-jp" );

			// メールの内容を設定
			mimeMsg.setText( contents, "iso-2022-jp" );

			// 送信日付を設定
			mimeMsg.setSentDate( new Date() );

			// 送信
			Transport.send( mimeMsg );
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

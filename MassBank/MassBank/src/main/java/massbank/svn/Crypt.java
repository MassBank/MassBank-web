/*******************************************************************************
 *
 * Copyright (C) 2012 MassBank project
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
 * Crypt.java
 *
 * ver 1.0.0 2012.08.30
 *
 ******************************************************************************/
package massbank.svn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.mail.internet.MimeUtility;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class Crypt {

	/*
	 * decrypt Blowfish,CBC,PKCS5Padding
	 */
	public static String decrypt(String key, String encrypted) throws Exception {
		byte[] code = decodeBase64(encrypted);
		SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), "Blowfish");
		Cipher cipher = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
		IvParameterSpec ips = new IvParameterSpec("tsuruoka".getBytes());
		cipher.init(Cipher.DECRYPT_MODE, sksSpec, ips);
		return new String(cipher.doFinal(code)); 
	}

	/*
	 * base64 decode
	 */
	public static byte[] decodeBase64(String base64) throws Exception {
		InputStream fromBase64 = MimeUtility.decode(
			new ByteArrayInputStream(base64.getBytes()), "base64");
		byte[] buf = new byte[1024];
		ByteArrayOutputStream toByteArray = new ByteArrayOutputStream();
		for ( int len = -1;(len = fromBase64.read(buf)) != -1;)  {
			toByteArray.write(buf, 0, len);
		}
		return toByteArray.toByteArray();
	}
}


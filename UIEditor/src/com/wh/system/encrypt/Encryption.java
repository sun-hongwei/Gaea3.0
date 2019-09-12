package com.wh.system.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.wh.system.tools.BytesHelp;

import java.security.SecureRandom;  
import javax.crypto.KeyGenerator;  
import javax.crypto.SecretKey;  

public class Encryption {

	  
	public static class DESUtil {

		Cipher cipher;

		String key;

		public DESUtil() {

		}

		public DESUtil(String str) {
			this.key = str;
		}

		public static void genKeyFile(File keyFile) throws Exception {  
			String algorithm = "DES";  
			  
			SecureRandom sr = new SecureRandom();  
			KeyGenerator kg = KeyGenerator.getInstance(algorithm);  
			kg.init(sr);  
			SecretKey key = kg.generateKey();  
			  
			BytesHelp.saveFile(key.getEncoded(), keyFile);  
		}  

		public void setKey(boolean isencrypt) throws InvalidKeyException,
				UnsupportedEncodingException, NoSuchPaddingException,
				NoSuchAlgorithmException {
			if (key.length() < 8) {
				int start = key.length();
				for (int i = start; i < 8; i++)
					key += " ";
			}
			byte[] raw = key.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			cipher = Cipher.getInstance("AES");
			if (isencrypt)
				cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			else
				cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		}

		public String encryptStr(String strMing) {
			byte[] byteMi = null;
			byte[] byteMing = null;
			String strMi = "";
			BASE64Encoder base64en = new BASE64Encoder();
			try {
				byteMing = strMing.getBytes("UTF8");
				byteMi = this.encryptByte(byteMing);
				strMi = base64en.encode(byteMi);
			} catch (Exception e) {
				throw new RuntimeException(
						"Error initializing SqlMap class. Cause: " + e);
			} finally {
				base64en = null;
				byteMing = null;
				byteMi = null;
			}
			return strMi;
		}

		public String decryptStr(String strMi) {
			BASE64Decoder base64De = new BASE64Decoder();
			byte[] byteMing = null;
			byte[] byteMi = null;
			String strMing = "";
			try {
				byteMi = base64De.decodeBuffer(strMi);
				byteMing = this.decryptByte(byteMi);
				strMing = new String(byteMing, "UTF8");
			} catch (Exception e) {
				throw new RuntimeException(
						"Error initializing SqlMap class. Cause: " + e);
			} finally {
				base64De = null;
				byteMing = null;
				byteMi = null;
			}
			return strMing;
		}

		private byte[] encryptByte(byte[] byteS) {
			byte[] byteFina = null;
			try {
				setKey(true);
				byteFina = cipher.doFinal(byteS);
			} catch (Exception e) {
				throw new RuntimeException(
						"Error initializing SqlMap class. Cause: " + e);
			}
			cipher = null;
			return byteFina;
		}

		private byte[] decryptByte(byte[] byteD) {
			byte[] byteFina = null;
			try {
				setKey(false);
				byteFina = cipher.doFinal(byteD);
			} catch (Exception e) {
				throw new RuntimeException(
						"Error initializing SqlMap class. Cause: " + e);
			}
			cipher = null;
			return byteFina;
		}

		public void encryptFile(String file, String destFile) throws Exception {
			InputStream is = new FileInputStream(file);
			OutputStream out = new FileOutputStream(destFile);
			encryptStream(is, out);
			is.close();
			out.close();
		}

		public void decryptFile(String file, String dest) throws Exception {
			InputStream is = new FileInputStream(file);
			OutputStream out = new FileOutputStream(dest);
			decryptStream(is, out);
			out.close();
			is.close();
		}

		public void encryptStream(InputStream is, OutputStream out)
				throws Exception {
			setKey(true);
			CipherInputStream cis = new CipherInputStream(is, cipher);
			byte[] buffer = new byte[1024];
			int r;
			while ((r = cis.read(buffer)) > 0) {
				out.write(buffer, 0, r);
			}
			cis.close();
		}

		public void decryptStream(InputStream is, OutputStream out)
				throws Exception {
			setKey(false);
			CipherOutputStream cos = new CipherOutputStream(out, cipher);
			byte[] buffer = new byte[1024];
			int r;
			while ((r = is.read(buffer)) >= 0) {
				cos.write(buffer, 0, r);
			}
			cos.close();
		}

		public static String decryptString(String password, String content) {
			DESUtil des = new DESUtil(password);
			String deStr = des.decryptStr(content);
			return deStr;
		}

		public static String encryptString(String password, String content) {
			DESUtil des = new DESUtil(password);
			String str = des.encryptStr(content);
			return str;
		}

		public static void encryptFile(String password, String sourceFileName,
				String destFileName) throws Exception {
			DESUtil des = new DESUtil(password);
			des.encryptFile(sourceFileName, destFileName);
		}

		public static void decryptFile(String password, String sourceFileName,
				String destFileName) throws Exception {
			DESUtil des = new DESUtil(password);
			des.decryptFile(sourceFileName, destFileName);
		}

		public static void encryptStream(String password, InputStream in,
				OutputStream out) throws Exception {
			DESUtil des = new DESUtil(password);
			des.encryptStream(in, out);
		}

		public static void decryptStream(String password, InputStream in,
				OutputStream out) throws Exception {
			DESUtil des = new DESUtil(password);
			des.decryptStream(in, out);
		}
	}

	public static class MD5Util {
		public static String MD5(String s) {
			char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
					'9', 'A', 'B', 'C', 'D', 'E', 'F' };

			try {
				byte[] btInput = s.getBytes();

				MessageDigest mdInst = MessageDigest.getInstance("MD5");

				mdInst.update(btInput);

				byte[] md = mdInst.digest();

				int j = md.length;
				char str[] = new char[j * 2];
				int k = 0;
				for (int i = 0; i < j; i++) {
					byte byte0 = md[i];
					str[k++] = hexDigits[byte0 >>> 4 & 0xf];
					str[k++] = hexDigits[byte0 & 0xf];
				}
				return new String(str);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static String MD5_128(String value) {
			return null;
		}
	}
}

package org.ietf.tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

public class App {

	public static String getRandomSecretKey() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[20];
		random.nextBytes(bytes);
		Base32 base32 = new Base32();
		String secretKey = base32.encodeToString(bytes);
		// make the secret key more human-readable by lower-casing and
		// inserting spaces between each group of 4 characters
		return secretKey.toLowerCase().replaceAll("(.{4})(?=.{4})", "$1 ");
	}

	public static String getTOTPCode(String secretKey) {
		String normalizedBase32Key = secretKey.replace(" ", "").toUpperCase();
		Base32 base32 = new Base32();
		byte[] bytes = base32.decode(normalizedBase32Key);
		String hexKey = Hex.encodeHexString(bytes);
		long time = (System.currentTimeMillis() / 1000) / 30;
		String hexTime = Long.toHexString(time);
		return TOTP.generateTOTP(hexKey, hexTime, "6");
	}

	public static String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
		String normalizedBase32Key = secretKey.replace(" ", "").toUpperCase();
		try {
			return "otpauth://totp/" + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
					+ "?secret=" + URLEncoder.encode(normalizedBase32Key, "UTF-8").replace("+", "%20") + "&issuer="
					+ URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void createQRCode(String barCodeData, String filePath, int height, int width)
			throws WriterException, IOException {
		BitMatrix matrix = new MultiFormatWriter().encode(barCodeData, BarcodeFormat.QR_CODE, width, height);
//		BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			MatrixToImageWriter.writeToStream(matrix, "png", out);
		}
	}

	public static void main(String[] args) throws WriterException, IOException {
		String secretKey = "acc2 2j4k vgrk uv4u oq5p c2sb 4s5b p3wx";
		String barCodeUrl = getGoogleAuthenticatorBarCode(secretKey, "emrah.soytekin@krontech.com", "Single Connect");
		createQRCode(barCodeUrl, "D:/Development/tutorial/file.png", 166, 166);
		/*
		 * String lastCode = null; while (true) { String code =
		 * getTOTPCode(secretKey); if (!code.equals(lastCode)) { // output a new
		 * 6 digit code System.out.println(code); } lastCode = code; try {
		 * Thread.sleep(1000); } catch (InterruptedException e) {}; }
		 */
	}

}

package org.komparator.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

public class CryptoUtil {

    public static byte[] asymCipher(byte[] data, Key key){
    	Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Transformation does not exist.");
			return null;
		} catch (NoSuchPaddingException e) {
			System.out.println("Padding does not exist.");
			return null;
		} catch (InvalidKeyException e) {
			System.out.println("Key is not valid.");
			return null;
		} catch (IllegalBlockSizeException e) {
			System.out.println("The block size is not legal.");
			return null;
		} catch (BadPaddingException e) {
			System.out.println("BAD PADDING!");
			return null;
		}
    }

    public static byte[] asymDecipher(byte[] data, Key key){
    	Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Transformation does not exist.");
			return null;
		} catch (NoSuchPaddingException e) {
			System.out.println("Padding does not exist.");
			return null;
		} catch (InvalidKeyException e) {
			System.out.println("Key is not valid.");
			return null;
		} catch (IllegalBlockSizeException e) {
			System.out.println("The block size is not legal.");
			return null;
		} catch (BadPaddingException e) {
			System.out.println("BAD PADDING!");
			return null;
		}
    }

    public static String createSignature(String msg, PrivateKey key){
    	try {
			Signature s = Signature.getInstance("SHA256withRSA");
			s.initSign(key);
			s.update(DatatypeConverter.parseBase64Binary(msg));
			return DatatypeConverter.printBase64Binary(s.sign());
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Algorithm does not exist.");
			return null;
		} catch (SignatureException e) {
			System.out.println("Signature object has not been properly initialized.");
			return null;
		} catch (InvalidKeyException e) {
			System.out.println("Key is not valid.");
			return null;
		}
    }

    public static boolean verifySignature(String sig, String msg, PublicKey key){
    	try {
			Signature s = Signature.getInstance("SHA256withRSA");
			s.initVerify(key);
			s.update(DatatypeConverter.parseBase64Binary(msg));
			return s.verify(DatatypeConverter.parseBase64Binary(sig));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Given algorithm is not available.");
			return false;
		} catch (SignatureException e) {
			System.out.println("Signature object has not been correctly initialized.");
			return false;
		} catch (InvalidKeyException e) {
			System.out.println("The given key is invalid");
			return false;
		}
    }

}

package org.komparator.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CryptoUtilTest {

    // static members
	
	/** Plain text to digest. */
	private final String plainText = "This is the plain text!";
	
    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
        // runs once before all tests in the suite
    }

    @AfterClass
    public static void oneTimeTearDown() {
        // runs once after all tests in the suite
    }

    // members

    // initialization and clean-up for each test
    @Before
    public void setUp() {
        // runs before each test
    }

    @After
    public void tearDown() {
        // runs after each test
    }

    // tests
    @Test
    public void test() {
        // do something ...

        // assertEquals(expected, actual);
        // if the assert fails, the test fails
    }
    
    
    // Public key cryptography test. Cipher with public key, decipher with private key.
    @Test
	public void testCipherPublicDecipherPrivate() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/example.cer");
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		Certificate cert = certFactory.generateCertificate(is);
		PublicKey publicKey = cert.getPublicKey();
		
		byte [] encMessageBytes = CryptoUtil.asymCipher(plainText.getBytes(), publicKey);
		
		is = this.getClass().getResourceAsStream("/example.jks");
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(is, "1nsecure".toCharArray());
		PrivateKey privateKey = (PrivateKey) keystore.getKey("example", "ins3cur3".toCharArray());

		byte[] decodMessageBytes = CryptoUtil.asymDecipher(encMessageBytes, privateKey);
		String decodMessage = new String(decodMessageBytes);
		
		assertEquals(plainText, decodMessage);
	}
    
    
    //Public key cryptography test. Cipher with private key, decipher with public key.
    @Test
	public void testCipherPrivateDecipherPublic() throws Exception {
    	InputStream is = this.getClass().getResourceAsStream("/example.jks");
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(is, "1nsecure".toCharArray());
		PrivateKey privateKey = (PrivateKey) keystore.getKey("example", "ins3cur3".toCharArray());

		byte [] encMessageBytes = CryptoUtil.asymCipher(plainText.getBytes(), privateKey);
		
    	is = this.getClass().getResourceAsStream("/example.cer");
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		Certificate cert = certFactory.generateCertificate(is);
		PublicKey publicKey = cert.getPublicKey();
		
		byte[] decodMessageBytes = CryptoUtil.asymDecipher(encMessageBytes, publicKey);
		String decodMessage = new String(decodMessageBytes);
		
		assertEquals(plainText, decodMessage);
	}
    
    @Test
	public void testCipherPublicDecipherPublic() throws Exception {
    	InputStream is = this.getClass().getResourceAsStream("/example.cer");
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		Certificate cert = certFactory.generateCertificate(is);
		PublicKey publicKey = cert.getPublicKey();

		byte [] encMessageBytes = CryptoUtil.asymCipher(plainText.getBytes(), publicKey);
		
		byte[] decodMessageBytes = CryptoUtil.asymDecipher(encMessageBytes, publicKey);
		assertNull(decodMessageBytes);
	}
    
    @Test
	public void testCipherPrivateDecipherPrivate() throws Exception {
    	InputStream is = this.getClass().getResourceAsStream("/example.jks");
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(is, "1nsecure".toCharArray());
		PrivateKey privateKey = (PrivateKey) keystore.getKey("example", "ins3cur3".toCharArray());

		byte [] encMessageBytes = CryptoUtil.asymCipher(plainText.getBytes(), privateKey);
		
		byte[] decodMessageBytes = CryptoUtil.asymDecipher(encMessageBytes, privateKey);
		assertNull(decodMessageBytes);
	}
}

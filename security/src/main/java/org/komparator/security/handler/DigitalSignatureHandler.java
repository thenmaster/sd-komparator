package org.komparator.security.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

public class DigitalSignatureHandler implements SOAPHandler<SOAPMessageContext> {

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		if (outbound){
			if (System.getProperty("ServiceGet") != null){
				System.out.println("Outbound messages to server are not signed.");
				return true;
			}
			SOAPMessage msg = context.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			try {
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				Name name = se.createName("Signature", "s", "http://sig");
				SOAPHeaderElement element = sh.addHeaderElement(name);

				InputStream is = this.getClass().getResourceAsStream("/" + System.getProperty("Service") + ".jks");
				PrivateKey key = null;

				try {
					KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
					keystore.load(is, "f19Ho2MJ".toCharArray());
					key = (PrivateKey) keystore.getKey(System.getProperty("Service").toLowerCase(), "f19Ho2MJ".toCharArray());
				} catch (KeyStoreException | NoSuchAlgorithmException e) {
					System.out.println("Could not access keystore.");
					return false;
				} catch (CertificateException e) {
					System.out.println("Could not load keystore certificates.");
					return false;
				} catch (IOException e) {
					System.out.println("Invalid keystore filepath.");
					return false;
				} catch (UnrecoverableKeyException e) {
					System.out.println("Key does not exist.");
					return false;
				}

				String sigString = CryptoUtil.createSignature(msg.getSOAPBody().getTextContent(),key);
				if (sigString == null)
					return false;
				element.addTextNode(sigString);

			} catch (SOAPException e) {
				System.out.println("Problem with SOAP message.");
				return false;
			}
		}else{
			if (System.getProperty("ServiceGet") == null){
				System.out.println("Inbound messages from client are not validated.");
				return true;
			}
			SOAPMessage msg = context.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			try {
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.out.println("Header not found.");
					return false;
				}

				Name name = se.createName("Signature", "s", "http://sig");
				@SuppressWarnings("rawtypes")
				Iterator it =  sh.getChildElements(name);

				if (!it.hasNext()) {
					System.out.println("Header element not found.");
					return false;
				}

				SOAPElement element = (SOAPElement) it.next();
				String signatureString = element.getValue();
				CAClient ca = null;
				try {
					ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
				} catch (CAClientException e) {
					System.out.println("Could not connect to certificate authority.");
					return false;
				}

				String certString = ca.getCertificate(System.getProperty("ServiceGet"));

				byte[] bytes = certString.getBytes(StandardCharsets.UTF_8);
				InputStream in = new ByteArrayInputStream(bytes);
				CertificateFactory certFactory = null;
				Certificate cert = null;
				try {
					certFactory = CertificateFactory.getInstance("X.509");
					cert = certFactory.generateCertificate(in);
				} catch (CertificateException e) {
					System.out.println("Could not generate certificate.");
					return false;
				}

				//verify the certificate with CA help
				InputStream is = this.getClass().getResourceAsStream("/ca.cer");
				Certificate caCert = null;
				try {
					caCert = certFactory.generateCertificate(is);
					cert.verify(caCert.getPublicKey());
				} catch (CertificateException e) {
					System.out.println("Could not generate certificate from authority.");
					return false;
				} catch (InvalidKeyException | NoSuchAlgorithmException e) {
					System.out.println("Could not get authority public key.");
					return false;
				} catch (NoSuchProviderException e) {
					System.out.println("Provider does not exist.");
					return false;
				} catch (SignatureException e) {
					System.out.println("Certificate verification failed.");
					return false;
				}

				if (!(CryptoUtil.verifySignature(signatureString, msg.getSOAPBody().getTextContent(), cert.getPublicKey()))){
					System.out.println("Message signature is incorrect.");
					return false;
				}

			} catch (SOAPException e) {
				System.out.println("Problem with SOAP message.");
				return false;
			}
		}
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}

}

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
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

/**
 * This SOAPHandler encrypts the cc.
 */
public class CypherHandler implements SOAPHandler<SOAPMessageContext> {

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
			SOAPMessage msg = context.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			try {
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = se.getBody();
				NodeList nl = sb.getFirstChild().getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					if (n.getNodeName().equals("creditCardNr")){
						String ccString = n.getTextContent();
						CAClient ca = null;
						try {
							ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
						} catch (CAClientException e) {
							System.out.println("Could not connect to certification authority");
							return false;
						}

						String certString = ca.getCertificate("A24_Mediator");
						byte[] bytes = certString.getBytes(StandardCharsets.UTF_8);
						InputStream in = new ByteArrayInputStream(bytes);
						CertificateFactory certFactory = null;
						Certificate cert = null;

						try {
							certFactory = CertificateFactory.getInstance("X.509");
							cert = certFactory.generateCertificate(in);
						} catch (CertificateException e) {
							System.out.println("Could not generate certificate");
							return false;
						}

						//verify the certificate with CA help
						InputStream is = this.getClass().getResourceAsStream("/ca.cer");
						Certificate caCert = null;
						try {
							caCert = certFactory.generateCertificate(is);
							cert.verify(caCert.getPublicKey());
						} catch (CertificateException | NoSuchAlgorithmException e) {
							System.out.println("Could not generate certificate.");
							return false;
						} catch (InvalidKeyException e) {
							System.out.println("Could not get authority public key.");
							return false;
						} catch (NoSuchProviderException e) {
							System.out.println("Provider not found.");
							return false;
						} catch (SignatureException e) {
							System.out.println("Certificate verification failed.");
							return false;
						}

						byte[] encCC = CryptoUtil.asymCipher(DatatypeConverter.parseBase64Binary(ccString), cert.getPublicKey());
						if (encCC == null)
							return false;
						String encCCString = DatatypeConverter.printBase64Binary(encCC);
						n.setTextContent(encCCString);
					}
				}
			} catch (SOAPException e) {
				System.out.println("Problem with SOAP message.");
				return false;
			}
		}else{
			SOAPMessage msg = context.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			try {
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = se.getBody();
				NodeList nl = sb.getFirstChild().getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					if (n.getNodeName().equals("creditCardNr")){
						String ccEnc = n.getTextContent();
						InputStream is = this.getClass().getResourceAsStream("/A24_Mediator.jks");
						PrivateKey key = null;

						try {
							KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
							keystore.load(is, "f19Ho2MJ".toCharArray());
							key = (PrivateKey) keystore.getKey("a24_mediator", "f19Ho2MJ".toCharArray());
						} catch (KeyStoreException e) {
							System.out.println("Failed to load keystore.");
							return false;
						} catch (CertificateException | NoSuchAlgorithmException e) {
							System.out.println("Could not load keystore certificates");
							return false;
						} catch (IOException e) {
							System.out.println("Failed to load keystore");
							return false;
						} catch (UnrecoverableKeyException e) {
							System.out.println("Could not recover private key from keystore");
							return false;
						}

						byte [] cc = CryptoUtil.asymDecipher(DatatypeConverter.parseBase64Binary(ccEnc), key);
						if (cc == null)
							return false;
						String ccString = DatatypeConverter.printBase64Binary(cc);
						n.setTextContent(ccString);
					}
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

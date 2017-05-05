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
			SOAPMessage soapMessage = context.getMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			try {
				SOAPEnvelope soapEnvelop = soapPart.getEnvelope();
				SOAPBody soapBody = soapEnvelop.getBody();
				NodeList nodeList = soapBody.getFirstChild().getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node n = nodeList.item(i);
					if (n.getNodeName().equals("creditCardNr")){
						String ccString = n.getTextContent();
						CAClient ca = null;
						try {
							ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
						} catch (CAClientException e) {
							throw new RuntimeException("Could not connect to certification authority");
						}

						String certificateString = ca.getCertificate("A24_Mediator");
						byte[] bytes = certificateString.getBytes(StandardCharsets.UTF_8);
						InputStream in = new ByteArrayInputStream(bytes);
						CertificateFactory certFactory = null;
						Certificate certificate = null;

						try {
							certFactory = CertificateFactory.getInstance("X.509");
							certificate = certFactory.generateCertificate(in);
						} catch (CertificateException e) {
							throw new RuntimeException("Could not generate certificate");
						}

						//verify the certificate with CA help
						InputStream inputStream = this.getClass().getResourceAsStream("/ca.cer");
						Certificate caCertificate = null;
						try {
							caCertificate = certFactory.generateCertificate(inputStream);
							certificate.verify(caCertificate.getPublicKey());
						} catch (CertificateException | NoSuchAlgorithmException e) {
							throw new RuntimeException("Could not generate certificate.");
						} catch (InvalidKeyException e) {
							throw new RuntimeException("Could not get authority public key.");
						} catch (NoSuchProviderException e) {
							throw new RuntimeException("Provider not found.");
						} catch (SignatureException e) {
							throw new RuntimeException("Certificate verification failed.");
						}

						byte[] encryptedCC = CryptoUtil.asymCipher(DatatypeConverter.parseBase64Binary(ccString), certificate.getPublicKey());
						if (encryptedCC == null)
							throw new RuntimeException("Could not encrypt the credit card.");
						n.setTextContent(DatatypeConverter.printBase64Binary(encryptedCC));
					}
				}
			} catch (SOAPException e) {
				throw new RuntimeException("Problem with SOAP message.");
			}
		}else{
			SOAPMessage soapMessage = context.getMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			try {
				SOAPEnvelope soapEnvelop = soapPart.getEnvelope();
				SOAPBody soapBody = soapEnvelop.getBody();
				NodeList nodeList = soapBody.getFirstChild().getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node n = nodeList.item(i);
					if (n.getNodeName().equals("creditCardNr")){
						String ccEnc = n.getTextContent();
						InputStream inputStream = this.getClass().getResourceAsStream("/A24_Mediator.jks");
						PrivateKey key = null;

						try {
							KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
							keystore.load(inputStream, "f19Ho2MJ".toCharArray());
							key = (PrivateKey) keystore.getKey("a24_mediator", "f19Ho2MJ".toCharArray());
						} catch (KeyStoreException e) {
							throw new RuntimeException("Failed to load keystore.");
						} catch (CertificateException | NoSuchAlgorithmException e) {
							throw new RuntimeException("Could not load keystore certificates");
						} catch (IOException e) {
							throw new RuntimeException("Failed to load keystore");
						} catch (UnrecoverableKeyException e) {
							throw new RuntimeException("Could not recover private key from keystore");
						}

						byte [] cc = CryptoUtil.asymDecipher(DatatypeConverter.parseBase64Binary(ccEnc), key);
						if (cc == null)
							throw new RuntimeException("Could not decrypt the credit card.");

						n.setTextContent(DatatypeConverter.printBase64Binary(cc));
					}
				}
			} catch (SOAPException e) {
				throw new RuntimeException("Problem with SOAP message.");
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

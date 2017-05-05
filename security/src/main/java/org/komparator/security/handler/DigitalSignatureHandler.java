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
			SOAPMessage soapMessage = context.getMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			try {
				SOAPEnvelope soapEnvelop = soapPart.getEnvelope();
				SOAPHeader soapHeader = soapEnvelop.getHeader();
				
				if (soapHeader == null) soapHeader = soapEnvelop.addHeader();

				Name name = soapEnvelop.createName("Signature", "s", "http://sig");
				SOAPHeaderElement element = soapHeader.addHeaderElement(name);
				
				String filename = null;
				if (System.getProperty("Service") == null)
					filename = "A24_Mediator";
				else
					filename = System.getProperty("Service");

				InputStream inputStream = this.getClass().getResourceAsStream("/" + filename + ".jks");
				PrivateKey privateKey = null;

				try {
					KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
					keystore.load(inputStream, "f19Ho2MJ".toCharArray());
					privateKey = (PrivateKey) keystore.getKey(filename.toLowerCase(), "f19Ho2MJ".toCharArray());
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
				String signatureString = CryptoUtil.createSignature(soapMessage.getSOAPBody().getTextContent(), privateKey);
				if (signatureString == null)
					return false;
				element.addTextNode(signatureString);

			} catch (SOAPException e) {
				System.out.println("Problem with SOAP message.");
				return false;
			}
		}else{
			SOAPMessage soapMessage = context.getMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			try {
				SOAPEnvelope soapEnvelop = soapPart.getEnvelope();
				SOAPHeader soapHeader = soapEnvelop.getHeader();

				// check header
				if (soapHeader == null) {
					System.out.println("Header not found.");
					return false;
				}

				Name name = soapEnvelop.createName("Signature", "s", "http://sig");

				if (!soapHeader.getChildElements(name).hasNext()) {
					System.out.println("Header element not found.");
					return false;
				}

				SOAPElement element = (SOAPElement) soapHeader.getChildElements(name).next();
				String signatureString = element.getValue();
				CAClient ca = null;
				
				try {
					ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
				} catch (CAClientException e) {
					System.out.println("Could not connect to certificate authority.");
					return false;
				}
				
				String filename = null;
				if (System.getProperty("Service") != null)
					filename = "A24_Mediator";
				else
					filename = System.getProperty("ServiceGet");

				String certString = ca.getCertificate(filename);
				byte[] bytes = certString.getBytes(StandardCharsets.UTF_8);
				CertificateFactory certFactory = null;
				Certificate certificate = null;
				
				try {
					certFactory = CertificateFactory.getInstance("X.509");
					certificate = certFactory.generateCertificate(new ByteArrayInputStream(bytes));
				} catch (CertificateException e) {
					System.out.println("Could not generate certificate.");
					return false;
				}

				//verify the certificate with CA help
				InputStream is = this.getClass().getResourceAsStream("/ca.cer");
				Certificate caCert = null;
				try {
					caCert = certFactory.generateCertificate(is);
					certificate.verify(caCert.getPublicKey());
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

				if (!(CryptoUtil.verifySignature(signatureString, soapMessage.getSOAPBody().getTextContent(), certificate.getPublicKey()))){
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

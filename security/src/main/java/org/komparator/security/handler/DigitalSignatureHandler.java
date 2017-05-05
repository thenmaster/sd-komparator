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
					throw new RuntimeException("Could not access keystore.");
				} catch (CertificateException e) {
					throw new RuntimeException("Could not load keystore certificates.");
				} catch (IOException e) {
					throw new RuntimeException("Invalid keystore filepath.");
				} catch (UnrecoverableKeyException e) {
					throw new RuntimeException("Key does not exist.");
				}
				String signatureString = CryptoUtil.createSignature(soapMessage.getSOAPBody().getTextContent(), privateKey);
				if (signatureString == null)
					throw new RuntimeException("Signature could not be created.");
				element.addTextNode(signatureString);

			} catch (SOAPException e) {
				throw new RuntimeException("Problem with SOAP message.");
			}
		}else{
			SOAPMessage soapMessage = context.getMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			try {
				SOAPEnvelope soapEnvelop = soapPart.getEnvelope();
				SOAPHeader soapHeader = soapEnvelop.getHeader();

				// check header
				if (soapHeader == null) {
					throw new RuntimeException("Header not found.");
				}

				Name name = soapEnvelop.createName("Signature", "s", "http://sig");

				if (!soapHeader.getChildElements(name).hasNext()) {
					throw new RuntimeException("Header element not found.");
				}

				SOAPElement element = (SOAPElement) soapHeader.getChildElements(name).next();
				String signatureString = element.getValue();
				CAClient ca = null;
				
				try {
					ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
				} catch (CAClientException e) {
					throw new RuntimeException("Could not connect to certificate authority.");
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
					throw new RuntimeException("Could not generate certificate.");
				}

				//verify the certificate with CA help
				InputStream is = this.getClass().getResourceAsStream("/ca.cer");
				Certificate caCert = null;
				try {
					caCert = certFactory.generateCertificate(is);
					certificate.verify(caCert.getPublicKey());
				} catch (CertificateException e) {
					throw new RuntimeException("Could not generate certificate from authority.");
				} catch (InvalidKeyException | NoSuchAlgorithmException e) {
					throw new RuntimeException("Could not get authority public key.");
				} catch (NoSuchProviderException e) {
					throw new RuntimeException("Provider does not exist.");
				} catch (SignatureException e) {
					throw new RuntimeException("Certificate verification failed.");
				}

				if (!(CryptoUtil.verifySignature(signatureString, soapMessage.getSOAPBody().getTextContent(), certificate.getPublicKey()))){
					throw new RuntimeException("Message signature is incorrect.");
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

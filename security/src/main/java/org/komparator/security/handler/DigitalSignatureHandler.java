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
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class DigitalSignatureHandler implements SOAPHandler<SOAPMessageContext> {

	//
	// Handler interface implementation
	//

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
			if (context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY) != null){
				System.out.println("Get out!");
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
				Map<String, List<String>> headers = (Map<String, List<String>>)context.get(MessageContext.HTTP_REQUEST_HEADERS);
				String host = headers.get("Host").get(0);

				String filename = "/A24_Supplier";
				String alias = "a24_supplier";

				if(host.contains("8081")){
					filename += "1";
					alias += "1";
				}
				else if(host.contains("8082")){
					filename += "2";
					alias += "2";
				}
				else if(host.contains("8083")){
					filename += "3";
					alias += "3";
				}
				else // supplier without any keys
					return false;

				filename += ".jks";

				InputStream is = this.getClass().getResourceAsStream(filename);

				PrivateKey key = null;

				try {
					KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
					keystore.load(is, "f19Ho2MJ".toCharArray());
					key = (PrivateKey) keystore.getKey(alias, "f19Ho2MJ".toCharArray());
				} catch (KeyStoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CertificateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnrecoverableKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String sigString = CryptoUtil.createSignature(msg.getSOAPBody().getTextContent(),key);
				element.addTextNode(sigString);

			} catch (SOAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			if (context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY) == null){
				System.out.println("Get out!");
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
					return true;
				}


			Name name = se.createName("Signature", "s", "http://sig");

			Iterator it =  sh.getChildElements(name);

			if (!it.hasNext()) {
				System.out.println("Header element not found.");
				return false;
			}

			SOAPElement element = (SOAPElement) it.next();

			String sigString = element.getValue();

			CAClient ca = null;
			try {
				ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
			} catch (CAClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*Map<String, List<String>> headers = (Map<String, List<String>>)context.get(MessageContext.HTTP_REQUEST_HEADERS);
			for (String s : headers.keySet()) {
				System.out.println(s);
				for (String i : headers.get(s)) {
					System.out.println("	" + i);
				}
			}
			*/
			String host = (String) context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

			String certname = "A24_Supplier";

			if(host.contains("8081")){
				certname += "1";
			}
			else if(host.contains("8082")){
				certname += "2";
			}
			else if(host.contains("8083")){
				certname += "3";
			}
			else // supplier without any keys
				return false;

			String certString = ca.getCertificate(certname);

			byte[] bytes = certString.getBytes(StandardCharsets.UTF_8);
			InputStream in = new ByteArrayInputStream(bytes);
			CertificateFactory certFactory = null;
			Certificate cert = null;
			try {
				certFactory = CertificateFactory.getInstance("X.509");
				cert = certFactory.generateCertificate(in);
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//verify the certificate with CA help
			InputStream is = this.getClass().getResourceAsStream("/ca.cer");
			Certificate caCert = null;
			try {
				caCert = certFactory.generateCertificate(is);
				cert.verify(caCert.getPublicKey());
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SignatureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			if (!(CryptoUtil.verifySignature(sigString, msg.getSOAPBody().getTextContent(), cert.getPublicKey()))){
				return false;
			}

			} catch (SOAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

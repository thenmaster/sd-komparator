package org.komparator.security.handler;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
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

import com.sun.xml.ws.developer.JAXWSProperties;

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
			SOAPMessage msg = context.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			try {
				SOAPEnvelope se = sp.getEnvelope();

				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				Name name = se.createName("Signature", "s", "http://sig");
				SOAPHeaderElement element = sh.addHeaderElement(name);

				String svcn = (String) context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

				String filename = "/A24_Supplier";
				String alias = "a24_supplier";

				System.out.println(msg.getSOAPBody().getTextContent());
				System.out.println(context.get(JAXWSProperties.HTTP_REQUEST_URL));
				System.out.println(context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));

				if(svcn.contains("8081")){
					filename += "1";
					alias += "1";
				}
				else if(svcn.contains("8082")){
					filename += "2";
					alias += "2";
				}
				else if(svcn.contains("8083")){
					filename += "3";
					alias += "3";
				}
				else // supplier without any keys
					return false;

				filename += ".jks";

				System.out.println(filename + "   " + alias);

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

				/*
			Name name = se.createName("Signature", "s", "http://sig");

			Iterator it =  sh.getChildElements(name);

			if (!it.hasNext()) {
				System.out.println("Header element not found.");
				return true;
			}

			SOAPElement element = (SOAPElement) it.next();

			String sigString = element.getValue();

			//TODO Verificar sig string
			*/
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
		logToSystemOut(smc);
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

	/** Date formatter used for outputting timestamps in ISO 8601 format */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	/**
	 * Check the MESSAGE_OUTBOUND_PROPERTY in the context to see if this is an
	 * outgoing or incoming message. Write a brief message to the print stream
	 * and output the message. The writeTo() method can throw SOAPException or
	 * IOException
	 */
	private void logToSystemOut(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		// print current timestamp
		System.out.print("[");
		System.out.print(dateFormatter.format(new Date()));
		System.out.print("] ");

		System.out.print("intercepted ");
		if (outbound)
			System.out.print("OUTbound");
		else
			System.out.print(" INbound");
		System.out.println(" SOAP message:");

		SOAPMessage message = smc.getMessage();
		try {
			message.writeTo(System.out);
			System.out.println(); // add a newline after message

		} catch (SOAPException se) {
			System.out.print("Ignoring SOAPException in handler: ");
			System.out.println(se);
		} catch (IOException ioe) {
			System.out.print("Ignoring IOException in handler: ");
			System.out.println(ioe);
		}
	}

}

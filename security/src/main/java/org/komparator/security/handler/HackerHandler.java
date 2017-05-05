package org.komparator.security.handler;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HackerHandler implements SOAPHandler<SOAPMessageContext> {

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

		if (outbound && context.get(MessageContext.WSDL_OPERATION).toString().contains("buyProduct")){
			SOAPMessage soapMessage = context.getMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			try {
				SOAPEnvelope soapEnvelop = soapPart.getEnvelope();
				SOAPBody soapBody = soapEnvelop.getBody();
				NodeList nodeList = soapBody.getFirstChild().getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node n = nodeList.item(i);
					if (n.getNodeName().equals("productId") && n.getTextContent().equals("Z5")){
						for (int i2 = 0; i2 < nodeList.getLength(); i2++) {
							Node n2 = nodeList.item(i2);
							if(n2.getNodeName().equals("quantity")){
								n2.setTextContent("1000");
							}
						}
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

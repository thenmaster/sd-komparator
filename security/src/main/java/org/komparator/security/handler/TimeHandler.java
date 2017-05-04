package org.komparator.security.handler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

/**
 * This SOAPHandler adds a header with a time value to outbound messages and checks if time difference is good in inbound messages
 */
public class TimeHandler implements SOAPHandler<SOAPMessageContext> {

	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		System.out.print("TimeHandler: ");
		if (outbound){
			System.out.println("Writing time header in outbound SOAP message...");

			SOAPMessage msg = context.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			try {
				SOAPEnvelope se = sp.getEnvelope();

				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				Name name = se.createName("timeHeader", "t", "http://time");
				SOAPHeaderElement element = sh.addHeaderElement(name);

				LocalDateTime now = LocalDateTime.now();
				//now = now.plusSeconds(-10); // uncomment to test
				String timeString = dateFormatter.format(now);
				element.addTextNode(timeString);
			} catch (SOAPException e) {
				System.out.println("Problem with SOAP message.");
				return false;
			}
		}
		else{
			System.out.println("Verifying time header in inbound SOAP message...");

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

			Name name = se.createName("timeHeader", "t", "http://time");

			@SuppressWarnings("rawtypes")
			Iterator it =  sh.getChildElements(name);

			if (!it.hasNext()) {
				System.out.println("Header element not found.");
				return true;
			}

			SOAPElement element = (SOAPElement) it.next();

			String timeString = element.getValue();

			LocalDateTime intime = LocalDateTime.parse(timeString);

			LocalDateTime now = LocalDateTime.now();

			//check if time difference between message send time and current time is less than 3 seconds
			if(Duration.between(intime, now).getSeconds() > 3 ){
				return false;
			}

			} catch (SOAPException e) {
				System.out.println("Problem with SOAP message.");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}

	@Override
	public void close(MessageContext context) {
		//nothing to clean up
	}

	@Override
	public Set<QName> getHeaders() {
		return null;
	}

}

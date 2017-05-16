package org.komparator.mediator.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.CartView;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.InvalidText_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.ItemView;
import org.komparator.mediator.ws.MediatorPortType;
import org.komparator.mediator.ws.MediatorService;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.mediator.ws.ShoppingResultView;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;


/**
 * Client.
 *
 * Adds easier endpoint address configuration and
 * UDDI lookup capability to the PortType generated by wsimport.
 */
public class MediatorClient implements MediatorPortType {
	
	 int messageID = 1;

     /** WS service */
     MediatorService service = null;

     /** WS port (port type is the interface, port is the implementation) */
     MediatorPortType port = null;

    /** UDDI server URL */
    private String uddiURL = null;

    /** WS name */
    private String wsName = null;

    /** WS endpoint address */
    private String wsURL = null; // default value is defined inside WSDL

    public String getWsURL() {
        return wsURL;
    }

    public String getUddiURL() {
        return uddiURL;
    }

    /** output option **/
    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** constructor with provided web service URL */
    public MediatorClient(String wsURL) throws MediatorClientException {
        this.wsURL = wsURL;
        createStub();
        initializeTimeOut();
    }

    /** constructor with provided UDDI location and name */
    public MediatorClient(String uddiURL, String wsName) throws MediatorClientException {
        this.uddiURL = uddiURL;
        this.wsName = wsName;
        uddiLookup();
        createStub();
        initializeTimeOut();
    }

    private void initializeTimeOut() {
    	int connectionTimeout = 10000, receiveTimeout = 20000;
        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        final List<String> CONN_TIME_PROPS = new ArrayList<String>();
        CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
        CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
        CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");

        for (String propName : CONN_TIME_PROPS)
            requestContext.put(propName, connectionTimeout);
        System.out.printf("Set connection timeout to %d milliseconds%n", connectionTimeout);

        final List<String> RECV_TIME_PROPS = new ArrayList<String>();
        RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
        RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
        RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");

        for (String propName : RECV_TIME_PROPS)
            requestContext.put(propName, receiveTimeout);
	}
    
    private void addIdToMessage(){
    	BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider
                .getRequestContext();
        requestContext.put("my.request.id", Integer.toString(messageID));
        this.messageID++;
    }

	/** UDDI lookup */
    private void uddiLookup() throws MediatorClientException {
        try {
            if (verbose)
                System.out.printf("Contacting UDDI at %s%n", uddiURL);
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            if (verbose)
                System.out.printf("Looking for '%s'%n", wsName);
            
            wsURL = uddiNaming.lookup(wsName);

        } catch (Exception e) {
            String msg = String.format("Client failed lookup on UDDI at %s!",
                    uddiURL);
            throw new MediatorClientException(msg, e);
        }

        if (wsURL == null) {
            String msg = String.format(
                    "Service with name %s not found on UDDI at %s", wsName,
                    uddiURL);
            throw new MediatorClientException(msg);
        }
    }

    /** Stub creation and configuration */
    private void createStub() {
        if (verbose)
            System.out.println("Creating stub ...");
        service = new MediatorService();
        port = service.getMediatorPort();

        if (wsURL != null) {
            if (verbose)
                System.out.println("Setting endpoint address ...");
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider
                    .getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
        }
    }


    // remote invocation methods ----------------------------------------------

     @Override
	public void clear() {
        try {
    		port.clear();
        } catch(WebServiceException wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return;
			}
    		port.clear();
        }
	}

     @Override
	public String ping(String arg0) {
        try {
    		return port.ping(arg0);
        } catch(WebServiceException wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return null;
			}
    		return port.ping(arg0);
        }
	}

     @Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
        try {
    		return port.searchItems(descText);
        } catch(WebServiceException wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return null;
			}
    		return port.searchItems(descText);
        }
	}

     @Override
	public List<CartView> listCarts() {
        try {
    		return port.listCarts();
        } catch(WebServiceException wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return null;
			}
    		return port.listCarts();
        }
	}

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
        try {
    		return port.getItems(productId);
        } catch(WebServiceException wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return null;
			}
    		return port.getItems(productId);
        }
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        try {
        	addIdToMessage();
    		return port.buyCart(cartId, creditCardNr);
        } catch(Exception wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return null;
			}
    		return port.buyCart(cartId, creditCardNr);
        }
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        try {
        	addIdToMessage();
			port.addToCart(cartId, itemId, itemQty);
        } catch(WebServiceException wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return;
			}
		port.addToCart(cartId, itemId, itemQty);
        }


	}

	@Override
	public List<ShoppingResultView> shopHistory() {
        try {
        	return port.shopHistory();
        } catch(WebServiceException wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return null;
			}
            return port.shopHistory();
        }
	}

	@Override
	public void imAlive() {
    	port.imAlive();
	}

	@Override
	public void updateShopHistory(ShoppingResultView shopResult, String cartId) {
        try {
        	port.updateShopHistory(shopResult, cartId);
        } catch(WebServiceException wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return;
			}
            port.updateShopHistory(shopResult, cartId);
        }
	}

	@Override
	public void updateCart(String cartId, CartItemView itemId) {
        try {
        	port.updateCart(cartId, itemId);
        } catch(WebServiceException wse) {
            try {
				uddiLookup();
				createStub();
			} catch (MediatorClientException e) {
				return;
			}
            port.updateCart(cartId, itemId);
        }
	}
}
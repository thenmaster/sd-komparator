package org.komparator.supplier.ws;

/** Main class that starts the Supplier Web Service. */
public class SupplierApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + SupplierApp.class.getName() + " wsURL");
			return;
		}

		// Create server implementation object
		SupplierEndpointManager endpoint = null;
		if (args.length == 1){ // not using UDDI
			endpoint = new SupplierEndpointManager(args[0]);
		}
		else if (args.length >= 3) {
			System.setProperty("Service", args[1]);
			endpoint = new SupplierEndpointManager(args[0],args[1],args[2]);
		}

		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}

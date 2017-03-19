package org.komparator.supplier.ws.cli;

/** Main class that starts the Supplier Web Service client. */
public class SupplierClientApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + SupplierClientApp.class.getName() + " wsURL");
			return;
		}
		String wsURL = args[0];

		// Create client
		System.out.printf("Creating client for server at %s%n", wsURL);
		SupplierClient client = new SupplierClient(wsURL);

		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit

		System.out.println("Invoke ping()...");
		String result = client.ping("client");
		/*
		ProductView prod = new ProductView();
		prod.setId("123");
		prod.setDesc("coisa");
		prod.setQuantity(2);
		prod.setPrice(5);
		client.createProduct(prod);
		ProductView data = client.getProduct("123");
		String purchaseId = client.buyProduct("123", 2);
		*/
		System.out.print("Result: ");
		System.out.println(result);
		/*
		System.out.println(data.getDesc());
		System.out.println(purchaseId);
		*/
	}

}

package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.PurchaseView;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {

		client.clear(); // clear existing changes to client

		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}

	}

	@AfterClass
	public static void oneTimeTearDown() {
		client.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {

		client.clear(); // clear existing changes to client

		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
	}

	@After
	public void tearDown() {
		client.clear();
	}

	// tests
	// assertEquals(expected, actual);

	// public String buyProduct(String productId, int quantity)
	// throws BadProductId_Exception, BadQuantity_Exception,
	// InsufficientQuantity_Exception {

	// bad input tests

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNullIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct(null, 2);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductEmptyIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("", 2);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductBlankIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("      ", 2);
	}

	@Test(expected = BadQuantity_Exception.class)
	public void buyProductNegativeQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("X1", -5);
	}

	@Test(expected = BadQuantity_Exception.class)
	public void buyProductZeroQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("X1", 0);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductEOLidTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("\n", 2);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductTabIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("\t", 2);
	}



	// main tests

	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductOverPurchaseTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("Z3",31);
	}

	@Test
	public void buyProductSucessPurchaseTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		 String purchaseId = client.buyProduct("Y2",15);
		 assertEquals(client.getProduct("Y2").getQuantity(), 5);
		 PurchaseView v = null;
		 for(PurchaseView pv : client.listPurchases()){
			if (pv.getId().equals(purchaseId))
				v = pv;
		 }
		 assertNotNull(v);
		 assertEquals("Y2",v.getProductId());
		 assertEquals(15, v.getQuantity());
	}

}

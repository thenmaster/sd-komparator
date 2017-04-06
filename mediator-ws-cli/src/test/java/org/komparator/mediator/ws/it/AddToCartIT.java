package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.InvalidText_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

public class AddToCartIT extends BaseIT{
	private static final int ITEM_QUANTITY = 5;
	private static final String ITEM_ID_1 = "ItemId1";
	private static final String ITEM_ID_2 = "ItemId2";
	private static final String ITEM_ID_3 = "ItemId3";
	private static final String CART_ID_1 = "CartId1";
	private static final String SUPPLIER_ID_1 = "A24_Supplier1";
	private static final String SUPPLIER_ID_2 = "A24_Supplier2";
	private static final String CREDIT_CARD_NR = "ValidCreditCardNr";
	protected static SupplierClient client;
	protected static SupplierClient client2;
	private static ItemIdView view1;
	private static ItemIdView view2;
	private static ItemIdView view3;

	@Before
	public void SetUp() throws BadProductId_Exception, BadProduct_Exception {
		try {
			client = new SupplierClient(mediatorClient.getUddiURL(), "A24_Supplier1");
			client2 = new SupplierClient(mediatorClient.getUddiURL(), "A24_Supplier2");
		} catch (UDDINamingException e) {
			return;
		}
		
		view1 = new ItemIdView();
		view1.setProductId(ITEM_ID_1);
		view1.setSupplierId(SUPPLIER_ID_1);
		view2 = new ItemIdView();
		view2.setProductId(ITEM_ID_2);
		view2.setSupplierId(SUPPLIER_ID_1);
		view3 = new ItemIdView();
		view3.setProductId(ITEM_ID_3);
		view3.setSupplierId(SUPPLIER_ID_2);

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
		{
			ProductView product = new ProductView();
			product.setId("A1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client2.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(10);
			product.setQuantity(20);
			client2.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client2.createProduct(product);
		}	
	}
	
	@After
	public void TearDown() {
		client.clear();
		client2.clear();
	}
	
	
	@Test(expected = InvalidCartId_Exception.class)
    public void NullCartIdTest() throws InvalidCartId_Exception {
        mediatorClient.addToCart(null, iiv, 5);
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void EmptyCartIdTest() throws InvalidCartId_Exception {
        mediatorClient.addToCart("", iiv, 5);
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void BlankCartIdTest() throws InvalidCartId_Exception {
        mediatorClient.addToCart("     ", iiv, 5);
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void NewLineCartIdTest() throws InvalidCartId_Exception {
        mediatorClient.addToCart("\n", iiv, 5);
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void TabCartIdTest() throws InvalidCartId_Exception {
        mediatorClient.addToCart("\t", iiv, 5);
    }
	
	@Test(expected = InvalidQuantity_Exception.class)
    public void ZeroQuantityTest() throws InvalidQuantity_Exception {
        mediatorClient.addToCart("Valid CartId", iiv, 0);
    }
	
	@Test(expected = InvalidQuantity_Exception.class)
    public void NegativeQuantityTest() throws InvalidQuantity_Exception {
        mediatorClient.addToCart("Valid CartId", iiv, -1);
    }
}

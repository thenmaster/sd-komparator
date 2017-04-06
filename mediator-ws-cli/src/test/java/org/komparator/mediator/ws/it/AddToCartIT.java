package org.komparator.mediator.ws.it;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
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
	protected static SupplierClient client1;
	protected static SupplierClient client2;
	private static ItemIdView view1;
	private static ItemIdView view2;
	private static ItemIdView view3;

	@Before
	public void SetUp() throws BadProductId_Exception, BadProduct_Exception {
		try {
			client1 = new SupplierClient(mediatorClient.getUddiURL(), SUPPLIER_ID_1);
			client2 = new SupplierClient(mediatorClient.getUddiURL(), SUPPLIER_ID_2);
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
			product.setQuantity(ITEM_QUANTITY);
			client1.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(ITEM_QUANTITY);
			client1.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(ITEM_QUANTITY);
			client1.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("A1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(ITEM_QUANTITY);
			client2.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(10);
			product.setQuantity(ITEM_QUANTITY);
			client2.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(ITEM_QUANTITY);
			client2.createProduct(product);
		}	
	}	
	
	@Test(expected = InvalidCartId_Exception.class)
    public void NullCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(null, view1, 5);
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void EmptyCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("", view1, 5);
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void BlankCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("     ", view1, 5);
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void NewLineCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("\n", view1, 5);
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void TabCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("\t", view1, 5);
    }
	
	@Test(expected = InvalidQuantity_Exception.class)
    public void ZeroQuantityTest() throws InvalidQuantity_Exception, InvalidCartId_Exception, InvalidItemId_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(CART_ID_1, view1, 0);
    }
	
	@Test(expected = InvalidQuantity_Exception.class)
    public void NegativeQuantityTest() throws InvalidQuantity_Exception, InvalidCartId_Exception, InvalidItemId_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(CART_ID_1, view1, -1);
    }
	
	@After
	public void TearDown() {
		client1.clear();
		client2.clear();
	}
}

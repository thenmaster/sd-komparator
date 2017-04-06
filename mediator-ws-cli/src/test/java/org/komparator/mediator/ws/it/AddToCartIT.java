package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.CartView;
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
	private static final String ITEM_ID_1 = "X1";
	private static final String ITEM_ID_2 = "Y2";
	private static final String ITEM_ID_3 = "Z3";
	private static final String CART_ID_1 = "cart";
	private static final String SUPPLIER_ID_1 = "A24_Supplier1";
	private static final String SUPPLIER_ID_2 = "A24_Supplier2";
	private static final String CREDIT_CARD_NR = "ValidCreditCardNr";
	protected static SupplierClient client;
	protected static SupplierClient client2;
	private static ItemIdView view1;
	private static ItemIdView view2;
	private static ItemIdView view3;
	private static ItemIdView badView;

	@Before
	public void SetUp() throws BadProductId_Exception, BadProduct_Exception {
		try {
			client = new SupplierClient(mediatorClient.getUddiURL(), SUPPLIER_ID_1);
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
		badView = new ItemIdView();
		badView.setProductId(null);
		badView.setSupplierId(SUPPLIER_ID_1);

		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_1);
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_2);
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_3);
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
			product.setId(ITEM_ID_2);
			product.setDesc("Baseball");
			product.setPrice(10);
			product.setQuantity(20);
			client2.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_3);
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client2.createProduct(product);
		}
	}

	@After
	public void TearDown() {
		mediatorClient.clear();
	}


	@Test(expected = InvalidCartId_Exception.class)
    public void NullCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(null, view1, ITEM_QUANTITY);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void EmptyCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("", view1, ITEM_QUANTITY);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void BlankCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("     ", view1, ITEM_QUANTITY);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void NewLineCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("\n", view1, ITEM_QUANTITY);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void TabCartIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("\t", view1, ITEM_QUANTITY);
    }

	@Test(expected = InvalidItemId_Exception.class)
	 public void nullItemIdViewTest() throws InvalidQuantity_Exception, InvalidCartId_Exception, InvalidItemId_Exception, NotEnoughItems_Exception {
       mediatorClient.addToCart(CART_ID_1, null, ITEM_QUANTITY);
   }

	@Test(expected = InvalidItemId_Exception.class)
	 public void badProductIdItemIdViewTest() throws InvalidQuantity_Exception, InvalidCartId_Exception, InvalidItemId_Exception, NotEnoughItems_Exception {
      mediatorClient.addToCart(CART_ID_1, badView, ITEM_QUANTITY);
	}

	@Test(expected = InvalidQuantity_Exception.class)
    public void ZeroQuantityTest() throws InvalidQuantity_Exception, InvalidCartId_Exception, InvalidItemId_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(CART_ID_1, view1, 0);
    }

	@Test(expected = InvalidQuantity_Exception.class)
    public void NegativeQuantityTest() throws InvalidQuantity_Exception, InvalidCartId_Exception, InvalidItemId_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(CART_ID_1, view1, -1);
    }

	@Test
	public void successTest() throws InvalidQuantity_Exception, InvalidCartId_Exception, InvalidItemId_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(CART_ID_1, view1, ITEM_QUANTITY);
        List<CartView> l = mediatorClient.listCarts();
        List<CartItemView> li = l.get(0).getItems();
        assertEquals(CART_ID_1, l.get(0).getCartId());
        assertTrue(li.size() == 1);
        assertEquals(ITEM_QUANTITY,li.get(0).getQuantity());
        assertEquals(10,li.get(0).getItem().getPrice());
        assertEquals("Basketball",li.get(0).getItem().getDesc());
        assertEquals(ITEM_ID_1,li.get(0).getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,li.get(0).getItem().getItemId().getSupplierId());
    }
}

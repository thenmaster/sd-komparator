package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.CartView;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.mediator.ws.ShoppingResultView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

public class BuyCartIT extends BaseIT{
	private static final int ITEM_QUANTITY = 5;
	private static final int ITEM_PRICE_1 = 10;
	private static final int ITEM_PRICE_2 = 20;
	private static final int ITEM_PRICE_3 = 30;
	private static final String ITEM_ID_1 = "X1";
	private static final String ITEM_ID_2 = "Y2";
	private static final String ITEM_ID_3 = "Z3";
	private static final String CART_ID_1 = "Cart_Id_1";
	private static final String CART_ID_2 = "Cart_Id_2";
	private static final String SUPPLIER_ID_1 = "A24_Supplier1";
	private static final String SUPPLIER_ID_2 = "A24_Supplier2";
	private static final String ITEM_DESC_1 = "Basketball";
	private static final String ITEM_DESC_2 = "Baseball";
	private static final String ITEM_DESC_3 = "Soccer ball";
	protected static SupplierClient client1;
	protected static SupplierClient client2;
	private static ItemIdView view1;
	private static ItemIdView view2;
	private static ItemIdView view3;
	private static final String CREDIT_CARD_NR = "4024007102923926";

	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		view1 = new ItemIdView();
		view1.setProductId(ITEM_ID_1);
		view1.setSupplierId(SUPPLIER_ID_1);
		view2 = new ItemIdView();
		view2.setProductId(ITEM_ID_2);
		view2.setSupplierId(SUPPLIER_ID_1);
		view3 = new ItemIdView();
		view3.setProductId(ITEM_ID_3);
		view3.setSupplierId(SUPPLIER_ID_2);

		try {
			client1 = new SupplierClient(mediatorClient.getUddiURL(), SUPPLIER_ID_1);
			client2 = new SupplierClient(mediatorClient.getUddiURL(), SUPPLIER_ID_2);
		} catch (UDDINamingException e) {
			return;
		}
		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_1);
			product.setDesc(ITEM_DESC_1);
			product.setPrice(ITEM_PRICE_1);
			product.setQuantity(ITEM_QUANTITY);
			client1.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_2);
			product.setDesc(ITEM_DESC_2);
			product.setPrice(ITEM_PRICE_2);
			product.setQuantity(ITEM_QUANTITY);
			client1.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_3);
			product.setDesc(ITEM_DESC_3);
			product.setPrice(ITEM_PRICE_3);
			product.setQuantity(ITEM_QUANTITY);
			client2.createProduct(product);
		}

		mediatorClient.addToCart(CART_ID_1, view1, ITEM_QUANTITY);
	}

	@Test(expected = InvalidCartId_Exception.class)
    public void NullCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(null, CREDIT_CARD_NR);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void EmptyCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("", CREDIT_CARD_NR);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void BlankCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("    ", CREDIT_CARD_NR);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void NewLineCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("\n", CREDIT_CARD_NR);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void TabCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("\t", CREDIT_CARD_NR);
    }

	@Test(expected = InvalidCreditCard_Exception.class)
    public void NullCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(CART_ID_1, null);
    }

	@Test(expected = InvalidCreditCard_Exception.class)
    public void EmptyCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(CART_ID_1,"");
    }

	@Test(expected = InvalidCreditCard_Exception.class)
    public void BlankCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(CART_ID_1, "     ");
    }

	@Test(expected = InvalidCreditCard_Exception.class)
    public void NewLineCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(CART_ID_1, "\n");
    }

	@Test(expected = InvalidCreditCard_Exception.class)
    public void TabCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(CART_ID_1, "\t");
    }

	CartItemView swap(CartItemView a, CartItemView b) {  // usage: y = swap(x, x=y);
		return a;
	}

	@Test
    public void oneItemFromOneSupplier() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception{
		ShoppingResultView sr = mediatorClient.buyCart(CART_ID_1, CREDIT_CARD_NR);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(0, l.size());
        List<CartItemView> li = sr.getPurchasedItems();
        assertEquals(1, li.size());
        CartItemView cv0 = li.get(0);
        assertEquals(ITEM_QUANTITY,cv0.getQuantity());
        assertEquals(ITEM_PRICE_1,cv0.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv0.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv0.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv0.getItem().getItemId().getSupplierId());
		Assert.assertEquals(ITEM_PRICE_1*ITEM_QUANTITY, sr.getTotalPrice());
		Assert.assertEquals(0, sr.getDroppedItems().size());
    }

	@Test
    public void twoItemsFromOneSupplier() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception{
		mediatorClient.addToCart(CART_ID_1, view2, ITEM_QUANTITY);
		ShoppingResultView sr = mediatorClient.buyCart(CART_ID_1, CREDIT_CARD_NR);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(0, l.size());
        List<CartItemView> li = sr.getPurchasedItems();
        assertEquals(2, li.size());
        CartItemView cv0 = li.get(0);
        CartItemView cv1 = li.get(1);

        if(ITEM_ID_1 != cv0.getItem().getItemId().getProductId()){
			cv0 = swap(cv1, cv1=cv0);
        }
        assertEquals(ITEM_QUANTITY,cv0.getQuantity());
        assertEquals(ITEM_PRICE_1,cv0.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv0.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv0.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv0.getItem().getItemId().getSupplierId());

        assertEquals(ITEM_QUANTITY,cv1.getQuantity());
        assertEquals(ITEM_PRICE_2,cv1.getItem().getPrice());
        assertEquals(ITEM_DESC_2,cv1.getItem().getDesc());
        assertEquals(ITEM_ID_2,cv1.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv1.getItem().getItemId().getSupplierId());

		Assert.assertEquals(ITEM_PRICE_1*ITEM_QUANTITY + ITEM_PRICE_2*ITEM_QUANTITY, sr.getTotalPrice());
		Assert.assertEquals(0, sr.getDroppedItems().size());
    }

	@Test
    public void oneItemFromEachSupplier() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception{
		mediatorClient.addToCart(CART_ID_1, view3, ITEM_QUANTITY);
		ShoppingResultView sr = mediatorClient.buyCart(CART_ID_1, CREDIT_CARD_NR);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(0, l.size());
        List<CartItemView> li = sr.getPurchasedItems();
        assertEquals(2, li.size());
        CartItemView cv0 = li.get(0);
        CartItemView cv1 = li.get(1);

        if(ITEM_ID_1 != cv0.getItem().getItemId().getProductId()){
			cv0 = swap(cv1, cv1=cv0);
        }
        assertEquals(ITEM_QUANTITY,cv0.getQuantity());
        assertEquals(ITEM_PRICE_1,cv0.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv0.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv0.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv0.getItem().getItemId().getSupplierId());

        assertEquals(ITEM_QUANTITY,cv1.getQuantity());
        assertEquals(ITEM_PRICE_3,cv1.getItem().getPrice());
        assertEquals(ITEM_DESC_3,cv1.getItem().getDesc());
        assertEquals(ITEM_ID_3,cv1.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_2,cv1.getItem().getItemId().getSupplierId());

		Assert.assertEquals(ITEM_PRICE_1*ITEM_QUANTITY + ITEM_PRICE_3*ITEM_QUANTITY, sr.getTotalPrice());
		Assert.assertEquals(0, sr.getDroppedItems().size());
    }

	@Test
    public void oneItemWithTooMuchQuantity() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception{
    	mediatorClient.addToCart(CART_ID_2, view1, ITEM_QUANTITY);
		mediatorClient.buyCart(CART_ID_1, CREDIT_CARD_NR);
		ShoppingResultView sr = mediatorClient.buyCart(CART_ID_2, CREDIT_CARD_NR);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(0, l.size());
        List<CartItemView> li = sr.getDroppedItems();
        assertEquals(1, li.size());
        CartItemView cv0 = li.get(0);
        assertEquals(ITEM_QUANTITY,cv0.getQuantity());
        assertEquals(ITEM_PRICE_1,cv0.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv0.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv0.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv0.getItem().getItemId().getSupplierId());
		Assert.assertEquals(ITEM_PRICE_1*ITEM_QUANTITY, sr.getTotalPrice());
		Assert.assertEquals(0, sr.getPurchasedItems().size());
    }

	@Test
    public void oneItemWithTooMuchQuantityOneOk() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception{
    	mediatorClient.addToCart(CART_ID_2, view1, ITEM_QUANTITY);
    	mediatorClient.addToCart(CART_ID_2, view2, ITEM_QUANTITY);
		mediatorClient.buyCart(CART_ID_1, CREDIT_CARD_NR);
		ShoppingResultView sr = mediatorClient.buyCart(CART_ID_2, CREDIT_CARD_NR);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(0, l.size());
        List<CartItemView> li = sr.getDroppedItems();
        assertEquals(1, li.size());
        CartItemView cv0 = li.get(0);
        assertEquals(ITEM_QUANTITY,cv0.getQuantity());
        assertEquals(ITEM_PRICE_1,cv0.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv0.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv0.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv0.getItem().getItemId().getSupplierId());
		Assert.assertEquals(ITEM_PRICE_1*ITEM_QUANTITY, sr.getTotalPrice());
        li = sr.getPurchasedItems();
        assertEquals(1, li.size());
        cv0 = li.get(0);
        assertEquals(ITEM_QUANTITY,cv0.getQuantity());
        assertEquals(ITEM_PRICE_2,cv0.getItem().getPrice());
        assertEquals(ITEM_DESC_2,cv0.getItem().getDesc());
        assertEquals(ITEM_ID_2,cv0.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv0.getItem().getItemId().getSupplierId());
		Assert.assertEquals(ITEM_PRICE_2*ITEM_QUANTITY, sr.getTotalPrice());
    }

	@After
	public void TearDown() {
		mediatorClient.clear();
	}
}

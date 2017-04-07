package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;

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
	protected static SupplierClient client;
	protected static SupplierClient client2;
	private static ItemIdView view1;
	private static ItemIdView view2;
	private static ItemIdView view3;
	private static ItemIdView badView1;
	private static ItemIdView badView2;

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
		badView1 = new ItemIdView();
		badView1.setProductId(null);
		badView1.setSupplierId(SUPPLIER_ID_1);
		badView2 = new ItemIdView();
		badView2.setProductId(ITEM_ID_1);
		badView2.setSupplierId(null);

		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_1);
			product.setDesc(ITEM_DESC_1);
			product.setPrice(ITEM_PRICE_1);
			product.setQuantity(ITEM_QUANTITY);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_2);
			product.setDesc(ITEM_DESC_2);
			product.setPrice(ITEM_PRICE_2);
			product.setQuantity(ITEM_QUANTITY);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId(ITEM_ID_3);
			product.setDesc(ITEM_DESC_3);
			product.setPrice(ITEM_PRICE_3);
			product.setQuantity(ITEM_QUANTITY);
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
	 public void nullItemIdViewTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
       mediatorClient.addToCart(CART_ID_1, null, ITEM_QUANTITY);
   }

	@Test(expected = InvalidItemId_Exception.class)
	 public void badProductIdItemIdViewTest1() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
      mediatorClient.addToCart(CART_ID_1, badView1, ITEM_QUANTITY);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	 public void badProductIdItemIdViewTest2() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
     mediatorClient.addToCart(CART_ID_1, badView2, ITEM_QUANTITY);
	}

	@Test(expected = InvalidQuantity_Exception.class)
    public void ZeroQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(CART_ID_1, view1, 0);
    }

	@Test(expected = InvalidQuantity_Exception.class)
    public void NegativeQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(CART_ID_1, view1, -1);
    }
	
	@Test(expected = NotEnoughItems_Exception.class)
    public void TooMuchQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
    	mediatorClient.addToCart(CART_ID_1, view1, ITEM_QUANTITY+1);
    }

	@Test
	public void oneItemFromOneSupplier() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart(CART_ID_1, view1, ITEM_QUANTITY);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(1, l.size());
        assertEquals(CART_ID_1, l.get(0).getCartId());
        List<CartItemView> li = l.get(0).getItems();
        assertEquals(1, li.size());
        CartItemView cv0 = li.get(0);
        assertEquals(ITEM_QUANTITY,cv0.getQuantity());
        assertEquals(ITEM_PRICE_1,cv0.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv0.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv0.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv0.getItem().getItemId().getSupplierId());
    }
	
	@Test
    public void twoItemsFromOneSupplier() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(CART_ID_1, view1, ITEM_QUANTITY);
		mediatorClient.addToCart(CART_ID_1, view2, ITEM_QUANTITY);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(1, l.size());
        assertEquals(CART_ID_1, l.get(0).getCartId());
        List<CartItemView> li = l.get(0).getItems();
        assertEquals(2, li.size());
        CartItemView cv1 = li.get(0);
        CartItemView cv0 = li.get(1);
        assertEquals(ITEM_QUANTITY,cv1.getQuantity());
        assertEquals(ITEM_PRICE_1,cv1.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv1.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv1.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv1.getItem().getItemId().getSupplierId());
        assertEquals(ITEM_QUANTITY,cv0.getQuantity());
        assertEquals(ITEM_PRICE_2,cv0.getItem().getPrice());
        assertEquals(ITEM_DESC_2,cv0.getItem().getDesc());
        assertEquals(ITEM_ID_2,cv0.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv0.getItem().getItemId().getSupplierId());


    }
	
	@Test
    public void oneItemFromEachSupplier() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(CART_ID_1, view1, ITEM_QUANTITY);
		mediatorClient.addToCart(CART_ID_1, view3, ITEM_QUANTITY);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(1, l.size());
        assertEquals(CART_ID_1, l.get(0).getCartId());
        List<CartItemView> li = l.get(0).getItems();
        assertEquals(2, li.size());
        CartItemView cv1 = li.get(1);
        CartItemView cv0 = li.get(0);
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
    }
	
	@Test
    public void twoCartsDifferentItems() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(CART_ID_1, view1, ITEM_QUANTITY);
		mediatorClient.addToCart(CART_ID_2, view2, ITEM_QUANTITY);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(2, l.size());
        CartView l0 = l.get(0);
        CartView l1 = l.get(1);

        if(CART_ID_1 != l0.getCartId()){
        	CartView temp = l1;
        	l1 = l0;
        	l0 = temp;
        }
        
        assertEquals(CART_ID_2, l1.getCartId());
        assertEquals(CART_ID_1, l0.getCartId());
        
        assertEquals(1, l0.getItems().size());
        CartItemView cv1 = l0.getItems().get(0);
        assertEquals(ITEM_QUANTITY,cv1.getQuantity());
        assertEquals(ITEM_PRICE_1,cv1.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv1.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv1.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv1.getItem().getItemId().getSupplierId());


        assertEquals(1, l1.getItems().size());
        cv1 = l1.getItems().get(0);
        assertEquals(ITEM_QUANTITY,cv1.getQuantity());
        assertEquals(ITEM_PRICE_2,cv1.getItem().getPrice());
        assertEquals(ITEM_DESC_2,cv1.getItem().getDesc());
        assertEquals(ITEM_ID_2,cv1.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv1.getItem().getItemId().getSupplierId());
    }
	
	@Test
    public void twoCartsSameItem() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(CART_ID_1, view1, ITEM_QUANTITY);
		mediatorClient.addToCart(CART_ID_2, view1, ITEM_QUANTITY);
        List<CartView> l = mediatorClient.listCarts();
        assertEquals(2, l.size());
        List<CartItemView> li = l.get(1).getItems();
        assertEquals(1, li.size());
        CartItemView cv1 = li.get(0);
        assertEquals(ITEM_QUANTITY,cv1.getQuantity());
        assertEquals(ITEM_PRICE_1,cv1.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv1.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv1.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv1.getItem().getItemId().getSupplierId());
        assertEquals(CART_ID_2, l.get(0).getCartId());
        assertEquals(CART_ID_1, l.get(1).getCartId());
        li = l.get(1).getItems();
        assertEquals(1, li.size());
        cv1 = li.get(0);
        assertEquals(ITEM_QUANTITY,cv1.getQuantity());
        assertEquals(ITEM_PRICE_1,cv1.getItem().getPrice());
        assertEquals(ITEM_DESC_1,cv1.getItem().getDesc());
        assertEquals(ITEM_ID_1,cv1.getItem().getItemId().getProductId());
        assertEquals(SUPPLIER_ID_1,cv1.getItem().getItemId().getSupplierId());
    }
}

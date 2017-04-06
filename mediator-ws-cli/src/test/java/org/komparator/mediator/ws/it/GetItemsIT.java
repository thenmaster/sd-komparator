package org.komparator.mediator.ws.it;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetItemsIT extends BaseIT{
	
	protected static SupplierClient client;
	protected static SupplierClient client2;

	@Before
	public void SetUp() throws BadProductId_Exception, BadProduct_Exception {
		
		try {
			client = new SupplierClient(mediatorClient.getUddiURL(), "A24_Supplier1");
			client2 = new SupplierClient(mediatorClient.getUddiURL(), "A24_Supplier2");
		} catch (UDDINamingException e) {
			return;
		}

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
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(9);
			product.setQuantity(10);
			client2.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
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


	@Test(expected = InvalidItemId_Exception.class)
    public void NullProductIdTest() throws InvalidItemId_Exception {
        mediatorClient.getItems(null);
    }

	@Test(expected = InvalidItemId_Exception.class)
    public void EmptyProductIdTest() throws InvalidItemId_Exception {
        mediatorClient.getItems("");
    }

	@Test(expected = InvalidItemId_Exception.class)
    public void BlankProductIdTest() throws InvalidItemId_Exception {
        mediatorClient.getItems("     ");
    }

	@Test(expected = InvalidItemId_Exception.class)
    public void newLineProductIdTest() throws InvalidItemId_Exception {
        mediatorClient.getItems("\n");
    }

	@Test(expected = InvalidItemId_Exception.class)
    public void TabProductIdTest() throws InvalidItemId_Exception {
        mediatorClient.getItems("\t");
    }
	
	@Test
    public void NoProductFound() throws InvalidItemId_Exception {
        List<ItemView> items = mediatorClient.getItems("x1");
        assertEquals(items.size(),0);
    }

	@Test
    public void sucessTest() throws InvalidItemId_Exception {
        List<ItemView> items = mediatorClient.getItems("X1");
        assertEquals(items.size(),2);
		for (ItemView iv : items){
			assertTrue(iv.getItemId().getProductId().equals("X1"));
			assertTrue(iv.getDesc().equals("Basketball"));
		}
		assertEquals(9, items.get(0).getPrice());
		assertEquals(10, items.get(1).getPrice());
		assertTrue(items.get(0).getItemId().getSupplierId().equals("A24_Supplier2"));
		assertTrue(items.get(1).getItemId().getSupplierId().equals("A24_Supplier1"));
	}
}

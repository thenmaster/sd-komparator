package org.komparator.mediator.ws.it;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.InvalidText_Exception;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SearchItemsIT extends BaseIT{

	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
		SupplierClient client;
		SupplierClient client2;
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
	}
	
	
	@Test(expected = InvalidText_Exception.class)
    public void NulldescTextTest() throws InvalidText_Exception {
        mediatorClient.searchItems(null);
    }

	@Test(expected = InvalidText_Exception.class)
    public void EmptydescTextTest() throws InvalidText_Exception {
        mediatorClient.searchItems("");
    }

	@Test(expected = InvalidText_Exception.class)
    public void BlankdescTextTest() throws InvalidText_Exception {
        mediatorClient.searchItems("     ");
    }

	@Test(expected = InvalidText_Exception.class)
    public void newLinedescTextTest() throws InvalidText_Exception {
        mediatorClient.searchItems("\n");
    }

	@Test(expected = InvalidText_Exception.class)
    public void tabDescTextTest() throws InvalidText_Exception {
        mediatorClient.searchItems("\t");
    }


    public void sucessTest() throws InvalidText_Exception {
        List<ItemView> items = mediatorClient.searchItems("Bas");
        assertEquals(items.size(),2);
		for (ItemView iv : items)
			assertTrue(iv.getDesc().contains("Bas"));
    }
}

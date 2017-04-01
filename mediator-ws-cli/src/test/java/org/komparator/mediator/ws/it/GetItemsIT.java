package org.komparator.mediator.ws.it;

import org.junit.Test;
import org.komparator.mediator.ws.InvalidItemId_Exception;

public class GetItemsIT extends BaseIT{
	
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
	

    public void sucessTest() throws InvalidItemId_Exception {
        mediatorClient.getItems("Valid productId here");       //need to insert a valid productId
    }
}

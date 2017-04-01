package org.komparator.mediator.ws.it;

import org.junit.Test;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;

public class BuyCartIT extends BaseIT{
	
	//IMPORTANT: put valid credit card numbers and cart ids in arguments
	
	@Test(expected = InvalidCartId_Exception.class)
    public void NullCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(null,"ValidCreditCardNr");
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void EmptyCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("","ValidCreditCardNr");
    }
	
	@Test(expected = InvalidCartId_Exception.class)
    public void BlankCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("    ","ValidCreditCardNr");
    }
	
	@Test(expected = InvalidCreditCard_Exception.class)
    public void NullCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("ValidCartId",null);
    }
	
	@Test(expected = InvalidCreditCard_Exception.class)
    public void EmptyCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("ValidCartId","");
    }
	
	@Test(expected = InvalidCreditCard_Exception.class)
    public void BlankCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("ValidCartId","     ");
    }
	
    public void sucessTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("ValidCartId","ValidCreditCardNr");
    }
}

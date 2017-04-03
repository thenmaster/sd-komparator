package org.komparator.mediator.ws.it;

import org.junit.Test;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;

public class BuyCartIT extends BaseIT{

	//TODO use real values
	private static final String CART_ID = "ValidCartId";
	private static final String CREDIT_CARD_NR = "ValidCreditCardNr";

	//IMPORTANT: put valid credit card numbers and cart ids in arguments

	@Test(expected = InvalidCartId_Exception.class)
    public void NullCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(null,CREDIT_CARD_NR);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void EmptyCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("",CREDIT_CARD_NR);
    }

	@Test(expected = InvalidCartId_Exception.class)
    public void BlankCartIdTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("    ",CREDIT_CARD_NR);
    }

	@Test(expected = InvalidCreditCard_Exception.class)
    public void NullCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(CART_ID,null);
    }

	@Test(expected = InvalidCreditCard_Exception.class)
    public void EmptyCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(CART_ID,"");
    }

	@Test(expected = InvalidCreditCard_Exception.class)
    public void BlankCreditCardNrTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(CART_ID,"     ");
    }

    public void sucessTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(CART_ID,CREDIT_CARD_NR);
    }
}

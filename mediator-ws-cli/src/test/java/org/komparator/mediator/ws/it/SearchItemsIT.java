package org.komparator.mediator.ws.it;

import org.junit.Test;
import org.komparator.mediator.ws.InvalidText_Exception;

public class SearchItemsIT extends BaseIT{

	private static final String PRODUCT_DESCRIPTION = "Valid product description here"; //TODO need to insert a valid product description


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
        mediatorClient.searchItems(PRODUCT_DESCRIPTION);
    }
}

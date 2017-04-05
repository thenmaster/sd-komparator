package org.komparator.mediator.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jws.WebService;

import org.komparator.mediator.domain.Cart;
import org.komparator.mediator.domain.CartItem;
import org.komparator.mediator.domain.Mediator;
import org.komparator.mediator.domain.ShoppingResult;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;

import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType",
		wsdlLocation = "mediator.wsdl",
		name = "MediatorWebService",
		portName = "MediatorPort",
		targetNamespace = "http://ws.mediator.komparator.org/",
		serviceName = "MediatorService"
)

public class MediatorPortImpl implements MediatorPortType{

	// end point manager
	private MediatorEndpointManager endpointManager;

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		List<ItemView> list = new ArrayList<ItemView>();
		try{
			Collection<UDDIRecord> records = this.endpointManager.getUddiNaming().listRecords("A24_Supplier%");
			for(UDDIRecord r : records){
				SupplierClient sc = new SupplierClient(this.endpointManager.getUddiURL(), r.getOrgName());
				ProductView p = sc.getProduct(productId);
				ItemIdView iiv = new ItemIdView();
				iiv.setProductId(p.getId());
				iiv.setSupplierId(r.getOrgName());
				ItemView iv = new ItemView();
				iv.setDesc(p.getDesc());
				iv.setItemId(iiv);
				iv.setPrice(p.getPrice());
				list.add(iv);
			}
			Collections.sort(list, (o1, o2) -> Integer.compare(o1.getPrice(), o2.getPrice()));
		}catch(UDDINamingException e){

		}catch(BadProductId_Exception e){
			throw new InvalidItemId_Exception(productId, null);
		}
		return list;
	}

	@Override
	public List<CartView> listCarts() {
		Mediator m = Mediator.getInstance();
		List<CartView> l = new ArrayList<CartView>();
		for (String ref : m.getCartKeys()) {
			Cart c = m.getCart(ref);
			l.add(this.newCartView(c));
		}
		return l;
	}

	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		List<ItemView> list = new ArrayList<ItemView>();
		try{
			Collection<UDDIRecord> records = this.endpointManager.getUddiNaming().listRecords("A24_Supplier%");
			for(UDDIRecord r : records){
				SupplierClient sc = new SupplierClient(this.endpointManager.getUddiURL(), r.getOrgName());
				List<ProductView> l = sc.searchProducts(descText);
				for (ProductView p : l){
					ItemIdView iiv = new ItemIdView();
					iiv.setProductId(p.getId());
					iiv.setSupplierId(r.getOrgName());
					ItemView iv = new ItemView();
					iv.setDesc(p.getDesc());
					iv.setItemId(iiv);
					iv.setPrice(p.getPrice());
					list.add(iv);
				}
			}
			Collections.sort(list, (o1, o2) -> Integer.compare(o1.getPrice(), o2.getPrice()));
		}catch(UDDINamingException e){

		}catch(BadText_Exception e){
			throw new InvalidText_Exception(descText, null);
		}
		return list;
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		Mediator m = Mediator.getInstance();
		if(m.getCart(cartId) == null){
			throw new InvalidCartId_Exception(cartId, null);
		}
		CreditCardClient cc = null;
		try {
			cc = new CreditCardClient(this.endpointManager.getUddiURL(),"CreditCard");
		} catch (CreditCardClientException e) {
			throw new InvalidCreditCard_Exception("Could not connect to service", null); //null for now but we should do functions to aid in exceptions
		}

		if (!cc.validateNumber(creditCardNr))
			throw new InvalidCreditCard_Exception("Invalid credit card number", null); //null for now but we should do functions to aid in exceptions

		ShoppingResult sr = m.buyCart(this.endpointManager.getUddiURL(), cartId);


		return this.newShoppingResultView(sr);
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		try{
			UDDIRecord record = this.endpointManager.getUddiNaming().lookupRecord(itemId.getSupplierId());
			SupplierClient sc = new SupplierClient(this.endpointManager.getUddiURL(), record.getOrgName());
			ProductView p = sc.getProduct(itemId.getProductId());
			if(p.getQuantity() <= itemQty){
				Mediator m = Mediator.getInstance();
				if(!m.cartExists(cartId)){
					m.addCart(cartId);
				}
				m.getCart(cartId).addItem(new CartItem(p.getId(), itemId.getSupplierId(), p.getDesc(), p.getPrice(), itemQty));
			}

		}
		catch(UDDINamingException e){

		} catch (BadProductId_Exception e) {
			throw new InvalidItemId_Exception(itemId.getProductId(),null);
		}

	}

	@Override
	public String ping(String msg) {
		try{
			Collection<UDDIRecord> records = this.endpointManager.getUddiNaming().listRecords("A24_Supplier%");
			String result = "";
			for (UDDIRecord r : records){
				SupplierClient sc = new SupplierClient(this.endpointManager.getUddiURL(), r.getOrgName());
				result += sc.ping(msg) + r.getOrgName() + "\n";
			}
			return result;
		} catch(UDDINamingException e){
			return null;
		}
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	private CartItemView newCartItemView(CartItem i){
		ItemIdView id = new ItemIdView();
		ItemView iv = new ItemView();
		CartItemView ci = new CartItemView();
		id.setProductId(i.getProductId());
		id.setSupplierId(i.getSupplierId());
		iv.setItemId(id);
		iv.setDesc(i.getDesc());
		iv.setPrice(i.getPrice());
		ci.setItem(iv);
		ci.setQuantity(i.getQuantity());
		return ci;
	}

	private CartView newCartView(Cart c){
		CartView cv = new CartView();
		cv.setCartId(c.getRefrence());
		for (CartItem ci : c.getItems()) {
			cv.getItems().add(this.newCartItemView(ci));
		}
		return cv;
	}

	private ShoppingResultView newShoppingResultView(ShoppingResult sr){
		ShoppingResultView srv = new ShoppingResultView();
		srv.setId(sr.getId());
		srv.setTotalPrice(sr.getPrice());
		for (CartItem i : sr.getPurchased()) {
			srv.getPurchasedItems().add(this.newCartItemView(i));
		}
		for (CartItem i : sr.getNotPurchased()) {
			srv.getDroppedItems().add(this.newCartItemView(i));
		}
		if (srv.getDroppedItems().isEmpty())
			srv.setResult(Result.COMPLETE);
		else
			srv.setResult((srv.getPurchasedItems().isEmpty() ? Result.EMPTY : Result.PARTIAL));
		return srv;
	}

	// Main operations -------------------------------------------------------

    // TODO


	// Auxiliary operations --------------------------------------------------

    // TODO


	// View helpers -----------------------------------------------------

    // TODO


	// Exception helpers -----------------------------------------------------

    // TODO

}

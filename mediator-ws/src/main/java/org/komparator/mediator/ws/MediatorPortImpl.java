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

	// Main operations -------------------------------------------------------

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		List<ItemView> list = new ArrayList<ItemView>();
		try{
			Collection<UDDIRecord> records = this.endpointManager.getUddiNaming().listRecords("A24_Supplier%");
			for(UDDIRecord r : records){
				SupplierClient sc = new SupplierClient(this.endpointManager.getUddiURL(), r.getOrgName());
				ProductView p = sc.getProduct(productId);
				if (p != null){
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
			this.invalidItemIdExceptionHelper("Failed to connect to suplier!");
		}catch(BadProductId_Exception e){
			this.invalidItemIdExceptionHelper("Invalid item id!");
		}
		return list;
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
			Collections.sort(list, (o1, o2) -> o1.getItemId().getProductId().compareTo(o2.getItemId().getProductId()) != 0 ?
											   o1.getItemId().getProductId().compareTo(o2.getItemId().getProductId()) :
											   Integer.compare(o1.getPrice(), o2.getPrice()));

		}catch(UDDINamingException e){
			this.invalidTextExceptionHelper("Failed to connect to suplier!");
		}catch(BadText_Exception e){
			this.invalidTextExceptionHelper("Invalid item description!");
		}
		return list;
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		Mediator m = Mediator.getInstance();
		CreditCardClient cc = null;
		if(cartId == null)
			this.invalidCartIdExceptionHelper("Null cart id!");
		cartId = cartId.trim();
		if (cartId.length() == 0)
			this.invalidCartIdExceptionHelper("Empty cart id!");
		if(creditCardNr == null)
			this.invalidCreditCardExceptionHelper("Null credit card number!");
		creditCardNr  = creditCardNr .trim();
		if (creditCardNr.length() == 0)
			this.invalidCreditCardExceptionHelper("Empty credit card number!");
		try {
			cc = new CreditCardClient("http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc");
		} catch (CreditCardClientException e) {
			this.invalidCreditCardExceptionHelper("Cannot connect to credit card client!");
		}

		if (!cc.validateNumber(creditCardNr)){
			this.invalidCreditCardExceptionHelper("Invalid credit card number!");
		}

		ShoppingResult sr = m.buyCart(this.endpointManager.getUddiURL(), cartId);


		return this.newShoppingResultView(sr);
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		Mediator m = Mediator.getInstance();
		if(cartId == null)
			this.invalidCartIdExceptionHelper("Null cart id!");
		cartId = cartId.trim();
		if (cartId.length() == 0)
			this.invalidCartIdExceptionHelper("Empty cart id!");
		if (itemId == null)
			this.invalidItemIdExceptionHelper("Null item id!");
		if (itemId.getProductId() == null || itemId.getSupplierId() == null)
			this.invalidItemIdExceptionHelper("One or more itemId atributes are null!");
		if (itemQty <= 0)
			this.invalidQuantityExceptionHelper("Invalid quantity!");
		try{
			SupplierClient sc = new SupplierClient(this.endpointManager.getUddiURL(), itemId.getSupplierId());
			ProductView p = sc.getProduct(itemId.getProductId());
			if (p == null){
				this.invalidItemIdExceptionHelper("Unknown Item!");
			}
			int initialQuantity = 0;
			if(m.cartExists(cartId) && m.getCart(cartId).getItem(itemId.getProductId()) != null && m.getCart(cartId).getItem(itemId.getProductId()).getSupplierId() == itemId.getSupplierId())
				initialQuantity = m.getCart(cartId).getItem(itemId.getProductId()).getQuantity();
			if(itemQty+initialQuantity <= p.getQuantity()){
				m.addItem(cartId,new CartItem(p.getId(), itemId.getSupplierId(), p.getDesc(), p.getPrice(), itemQty));
				return;
			}
			this.notEnoughItemsExceptionHelper("Too much quantity asked!");
		}
		catch(UDDINamingException e){
			this.invalidItemIdExceptionHelper("Falied to connect to supplier!");
		} catch (BadProductId_Exception e) {
			this.invalidItemIdExceptionHelper("Invalid product id!");
		}

	}

	// Auxiliary operations --------------------------------------------------

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
	public void clear() {
		try {
			Collection<UDDIRecord> records = this.endpointManager.getUddiNaming().listRecords("A24_Supplier%");
			for (UDDIRecord uddiRecord : records) {
				SupplierClient s = new SupplierClient(this.endpointManager.getUddiURL(),uddiRecord.getOrgName());
				s.clear();
			}
		} catch (UDDINamingException e) {
			//do nothing
		}
		Mediator.getInstance().reset();

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
			return "Falied to connect to service";
		}
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		Mediator m = Mediator.getInstance();
		List<ShoppingResultView> l = new ArrayList<ShoppingResultView>();
		for (String s : m.getShoppingResultKeys()) {
			ShoppingResult sr = m.getShoppingResult(s);
			l.add(this.newShoppingResultView(sr));
		}
		return l;
	}

	// View helpers -----------------------------------------------------

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

	// Exception helpers -----------------------------------------------------

	private void invalidCartIdExceptionHelper(final String string) throws InvalidCartId_Exception {
		InvalidCartId i = new InvalidCartId();
		i.setMessage(string);
		throw new InvalidCartId_Exception(string, i);
	}

	private void invalidItemIdExceptionHelper(final String string) throws InvalidItemId_Exception {
		InvalidItemId i = new InvalidItemId();
		i.setMessage(string);
		throw new InvalidItemId_Exception(string, i);
	}

	private void invalidQuantityExceptionHelper(final String string) throws InvalidQuantity_Exception {
		InvalidQuantity i = new InvalidQuantity();
		i.setMessage(string);
		throw new InvalidQuantity_Exception(string, i);
	}

	private void invalidCreditCardExceptionHelper(final String string) throws InvalidCreditCard_Exception {
		InvalidCreditCard i = new InvalidCreditCard();
		i.setMessage(string);
		throw new InvalidCreditCard_Exception(string, i);
	}

	private void notEnoughItemsExceptionHelper(final String string) throws NotEnoughItems_Exception {
		NotEnoughItems i = new NotEnoughItems();
		i.setMessage(string);
		throw new NotEnoughItems_Exception(string, i);
	}

	private void invalidTextExceptionHelper(final String string) throws InvalidText_Exception {
		InvalidText i = new InvalidText();
		i.setMessage(string);
		throw new InvalidText_Exception(string, i);
	}

}

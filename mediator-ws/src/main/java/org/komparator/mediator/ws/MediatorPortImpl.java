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

		}catch(BadProductId_Exception e){
			throw new InvalidItemId_Exception(productId, null);
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

		}catch(BadText_Exception e){
			throw new InvalidText_Exception(descText, null);
		}
		return list;
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		Mediator m = Mediator.getInstance();
		CreditCardClient cc = null;
		if(cartId == null)
			this.invalidCartIdExcpetionHelper("Null cart id!");
		cartId = cartId.trim();
		if (cartId.length() == 0)
			this.invalidCartIdExcpetionHelper("Empty cart id!");
		if(creditCardNr == null)
			this.invalidCreditCardExcpetionHelper("Null credit card number!");
		creditCardNr  = creditCardNr .trim();
		if (creditCardNr.length() == 0)
			this.invalidCreditCardExcpetionHelper("Empty credit card number!");
		try {
			cc = new CreditCardClient("http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc");
		} catch (CreditCardClientException e) {
			this.invalidCreditCardExcpetionHelper("Cannot connect to credit card client!");
		}

		if (!cc.validateNumber(creditCardNr)){
			this.invalidCreditCardExcpetionHelper("Invalid credit card number!");
		}

		ShoppingResult sr = m.buyCart(this.endpointManager.getUddiURL(), cartId);


		return this.newShoppingResultView(sr);
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		if(cartId == null)
			this.invalidCartIdExcpetionHelper("Null cart id!");
		cartId = cartId.trim();
		if (cartId.length() == 0)
			this.invalidCartIdExcpetionHelper("Empty cart id!");
		if (itemId == null)
			this.invalidItemIdExcpetionHelper("Null item id!");
		if (itemQty <= 0)
			this.invalidQuantityExcpetionHelper("Invalid quantity!");
		try{
			SupplierClient sc = new SupplierClient(this.endpointManager.getUddiURL(), itemId.getSupplierId());
			ProductView p = sc.getProduct(itemId.getProductId());
			if (p == null)
				this.invalidItemIdExcpetionHelper("Unknown Item!");
			if(itemQty <= p.getQuantity()){
				Mediator m = Mediator.getInstance();
				m.addItem(cartId,new CartItem(p.getId(), itemId.getSupplierId(), p.getDesc(), p.getPrice(), itemQty));
				return;
			}
			this.invalidQuantityExcpetionHelper("Too much quantity asked!");
		}
		catch(UDDINamingException e){

		} catch (BadProductId_Exception e) {
			this.invalidItemIdExcpetionHelper("Invalid product id!");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			return null;
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

	private void invalidCartIdExcpetionHelper(String string) throws InvalidCartId_Exception {
		InvalidCartId i = new InvalidCartId();
		i.setMessage(string);
		throw new InvalidCartId_Exception(string, i);
	}

	private void invalidItemIdExcpetionHelper(String string) throws InvalidItemId_Exception {
		InvalidItemId i = new InvalidItemId();
		i.setMessage(string);
		throw new InvalidItemId_Exception(string, i);
	}

	private void invalidQuantityExcpetionHelper(String string) throws InvalidQuantity_Exception {
		InvalidQuantity i = new InvalidQuantity();
		i.setMessage(string);
		throw new InvalidQuantity_Exception(string, i);
	}

	private void invalidCreditCardExcpetionHelper(String string) throws InvalidCreditCard_Exception {
		InvalidCreditCard i = new InvalidCreditCard();
		i.setMessage(string);
		throw new InvalidCreditCard_Exception(string, i);
	}

}

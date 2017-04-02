package org.komparator.mediator.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jws.WebService;

import org.komparator.mediator.domain.Cart;
import org.komparator.mediator.domain.CartItem;
import org.komparator.mediator.domain.Mediator;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;

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
			CartView cv = this.newCartView(c);
			l.add(cv);
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
		ShoppingResultView srv = new ShoppingResultView();
		CartView cv = null;
		for(CartView c : this.listCarts()){
			if(c.getCartId().equals(cartId)){
				cv=c;
				break;
			}
		}
		if(cv == null){
			throw new InvalidCartId_Exception(creditCardNr, null);
		}
		for(CartItemView civ : cv.getItems()){

		}

		return srv;
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// TODO Auto-generated method stub

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

	private CartView newCartView(Cart c){
		CartView cv = new CartView();
		cv.setCartId(c.getRefrence());
		for (CartItem i : c.getItems()) {
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
			cv.getItems().add(ci);
		}
		return cv;
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

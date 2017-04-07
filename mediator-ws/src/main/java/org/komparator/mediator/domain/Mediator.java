package org.komparator.mediator.domain;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.cli.SupplierClient;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

public class Mediator {

	private Map<String, Cart> carts = new ConcurrentHashMap<>();

	private Map<String, ShoppingResult> purchases = new ConcurrentHashMap<>();

	private AtomicInteger counter = new AtomicInteger(1); // counter for unique shopping Id's

	private Mediator() {
	}

	private static class SingletonHolder {
		private static final Mediator INSTANCE = new Mediator();
	}

	public static synchronized Mediator getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public boolean cartExists(String reference){
		return carts.containsKey(reference);
	}

	public void addCart(String reference){
		carts.put(reference, new Cart(reference));
	}

	public Set<String> getCartKeys(){
		return carts.keySet();
	}

	public Cart getCart(String reference){
		return carts.get(reference);
	}

	public Map<String, Cart> getCarts(){
		return carts;
	}

	public void addItem(String cartId,CartItem c){
		if(!this.carts.containsKey(cartId))
			this.addCart(cartId);
		this.carts.get(cartId).addItem(c);
	}

	public ShoppingResult buyCart(String uddiUrl,String cartId) throws InvalidCartId_Exception{
		Cart c = this.carts.remove(cartId);

		ShoppingResult sr = new ShoppingResult(Integer.toString(this.counter.getAndIncrement()));
		for (CartItem ci : c.getItems()) {
			try {
				SupplierClient sc = new SupplierClient(uddiUrl,ci.getSupplierId());
				sc.buyProduct(ci.getProductId(), ci.getQuantity());
				sr.addPurchased(ci);
				sr.incPrice(ci.getPrice() * ci.getQuantity());
			} catch (UDDINamingException | BadProductId_Exception | BadQuantity_Exception | InsufficientQuantity_Exception e)  {
				sr.addNotPurchased(ci);
			}
		}

		this.purchases.put(sr.getId(), sr);
		return sr;
	}

	public ShoppingResult getShoppingResult(String id){
		return purchases.get(id);
	}

	public Set<String> getShoppingResultKeys(){
		return purchases.keySet();
	}

	public void reset(){
		this.carts.clear();
		this.purchases.clear();
		this.counter.set(1);
	}

}

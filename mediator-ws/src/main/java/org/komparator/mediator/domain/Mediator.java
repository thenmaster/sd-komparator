package org.komparator.mediator.domain;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Mediator {

	private Map<String, Cart> carts = new ConcurrentHashMap<>();

	private Mediator() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final Mediator INSTANCE = new Mediator();
	}

	public static synchronized Mediator getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public boolean cartExists(String reference){
		return carts.containsKey(reference);
	}

	public void addCart(String refrence){
		carts.put(refrence, new Cart(refrence));
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


}

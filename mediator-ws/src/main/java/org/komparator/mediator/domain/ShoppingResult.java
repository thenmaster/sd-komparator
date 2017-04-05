package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.List;

public class ShoppingResult {

	private String id;

	private int price = 0;

	private List<CartItem> purchased = new ArrayList<CartItem>();

	private List<CartItem> notPurchased  = new ArrayList<CartItem>();

	public ShoppingResult(String id){
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public int getPrice() {
		return price;
	}

	public void incPrice(int price) {
		this.price += price;
	}

	public List<CartItem> getPurchased() {
		return purchased;
	}

	public void addPurchased(CartItem purchased) {
		this.purchased.add(purchased);
	}

	public List<CartItem> getNotPurchased() {
		return notPurchased;
	}

	public void addNotPurchased(CartItem notPurchased) {
		this.purchased.add(notPurchased);
	}

}

package org.komparator.mediator.domain;

import java.util.List;

public class Cart {

	private String refrence;

	private List<CartItem> items;

	public Cart(String reference){
		this.refrence = reference;
	}

	public String getRefrence() {
		return refrence;
	}

	public void setRefrence(String refrence) {
		this.refrence = refrence;
	}

	public List<CartItem> getItems() {
		return items;
	}

	public void addItem(CartItem item){
		items.add(item);
	}

	public CartItem getItem(String reference){
		for (CartItem cartItem : items) {
			if(cartItem.getProductId().equals(reference));
			return cartItem;
		}
		return null;
	}

}

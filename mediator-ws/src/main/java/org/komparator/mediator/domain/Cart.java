package org.komparator.mediator.domain;

import java.util.List;

public class Cart {

	private String reference;

	private List<CartItem> items;

	public Cart(String reference){
		this.reference = reference;
	}

	public String getRefrence() {
		return reference;
	}

	public void setRefrence(String refrence) {
		this.reference = refrence;
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

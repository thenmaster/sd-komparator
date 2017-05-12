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

	public ShoppingResult(){}

	public void setId(String id){
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getPrice() {
		return price;
	}

	public void incPrice(int price) {
		this.price += price;
	}

	public void setPurchased(List<CartItem> purchased) {
		this.purchased = purchased;
	}

	public List<CartItem> getPurchased() {
		return purchased;
	}

	public void addPurchased(CartItem purchased) {
		this.purchased.add(purchased);
	}

	public void setNotPurchased(List<CartItem> notPurchased) {
		this.notPurchased = notPurchased;
	}

	public List<CartItem> getNotPurchased() {
		return notPurchased;
	}

	public void addNotPurchased(CartItem notPurchased) {
		this.notPurchased.add(notPurchased);
	}

}

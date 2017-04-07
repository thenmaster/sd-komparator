package org.komparator.mediator.domain;

public class CartItem {

	private String productId;

	private String supplierId;

	private String desc;

	private int price;

	private int quantity;

	public CartItem(String productId, String supplierId, String desc, int price, int quantity) {
		this.setProductId(productId);
		this.setSupplierId(supplierId);
		this.setDesc(desc);
		this.setPrice(price);
		this.setQuantity(quantity);
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public synchronized int getQuantity() {
		return quantity;
	}

	public synchronized void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}

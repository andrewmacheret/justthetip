package com.justthetip.data;


public class ReceiptManager {
	private static ReceiptManager INSTANCE = new ReceiptManager();
	
	private Receipt receipt;
	
	public static ReceiptManager getInstance() {
		return INSTANCE;
	}

	public Receipt getReceipt() {
		return receipt;
	}

	public void setReceipt(Receipt receipt) {
		this.receipt = receipt;
	}
}

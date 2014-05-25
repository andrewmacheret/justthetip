package com.justthetip.data;


public class TaxCalculation {
	private SubtotalCalculation calc;
	private int tax = 0;

	public TaxCalculation(SubtotalCalculation calc, double taxPercent) {
		if (calc == null) throw new IllegalArgumentException("subtotal calculation must not be null");

		this.calc = calc;
		
		initTax(taxPercent);
	}
	
	private void initTax(double taxPercent) {
		tax = (int) Math.round(calc.getSubtotal() * taxPercent);
	}

	public int getUndiscountedSubtotal() {
		return calc.getUndiscountedSubtotal();
	}
	
	public int getSubtotal() {
		return calc.getSubtotal();
	}

	public int getTax() {
		return tax;
	}

	public int getTaxedTotal() {
		return calc.getSubtotal() + tax;
	}
}

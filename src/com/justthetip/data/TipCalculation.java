package com.justthetip.data;


public class TipCalculation {
	private TaxCalculation calc;
	protected int tip = 0;

	public TipCalculation(TaxCalculation calc, double tipPercent) {
		if (calc == null) throw new IllegalArgumentException("tax calculation must not be null");

		this.calc = calc;
		
		initTip(tipPercent);
	}

	private void initTip(double tipPercent) {
		tip = (int) Math.round(getPretipTotal() * tipPercent);
	}
	
	public int getUndiscountedSubtotal() {
		return calc.getUndiscountedSubtotal();
	}
	
	public int getPretipTotal() {
		return calc.getUndiscountedSubtotal() + calc.getTax();
	}
	
	public int getSubtotal() {
		return calc.getSubtotal();
	}

	public int getTax() {
		return calc.getTax();
	}

	public int getTaxedTotal() {
		return calc.getTaxedTotal();
	}
	
	public int getTip() {
		return tip;
	}
	
	public int getTippedTotal() {
		return calc.getSubtotal() + calc.getTax() + tip;
	}
}

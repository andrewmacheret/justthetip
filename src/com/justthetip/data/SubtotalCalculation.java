package com.justthetip.data;

import java.util.Collection;

public class SubtotalCalculation {
	private int subtotal = 0;
	private int undiscountedSubtotal = 0;

	public SubtotalCalculation(Collection<Integer> costs) {
		if (costs == null) throw new IllegalArgumentException("costs must not be null");

		initSubtotals(costs);
	}
	
	private void initSubtotals(Collection<Integer> costs) {
		subtotal = undiscountedSubtotal = 0;
		for (int cost : costs) {
			subtotal += cost;
			if (cost > 0) {
				undiscountedSubtotal += cost;
			}
		}
	}

	public int getUndiscountedSubtotal() {
		return undiscountedSubtotal;
	}

	public int getSubtotal() {
		return subtotal;
	}
}

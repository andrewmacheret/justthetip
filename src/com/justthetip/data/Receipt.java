package com.justthetip.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Receipt {
	private List<String> payerNames;
	private List<Item> items;
	private double taxPercent;
	
	private Map<String, SubtotalCalculation> subtotalCalculations;
	private SubtotalCalculation globalSubtotalCalculation;

	private Map<String, TaxCalculation> taxCalculations;
	private TaxCalculation globalTaxCalculation;

	public Receipt(List<String> payerNames) {
		this.payerNames = Collections.unmodifiableList(payerNames);
	}

	public void setItems(List<Item> items) {
		this.items = Collections.unmodifiableList(items);

		initItems();
	}

	public void setTaxPercent(double taxPercent) {
		this.taxPercent = taxPercent;

		initTaxPercent();
	}

	public TipCalculation getTipCalculation(String payerName, double tipPercent) {
		return new TipCalculation(taxCalculations.get(payerName), tipPercent);
	}

	private void initItems() {
		// create a map of costs
		Map<String, List<Integer>> costs = new HashMap<String, List<Integer>>();
		for (String payerName : payerNames) {
			costs.put(payerName, new ArrayList<Integer>(items.size()));
		}
		List<Integer> allCosts = new ArrayList<Integer>(items.size());

		// for each item, split each cost
		for (Item item : items) {
			ArrayList<String> payerNames = new ArrayList<String>(item.getPayerNames());
			int numPayers = payerNames.size();
			int fullCost = item.getCost();
			int splitCost = fullCost / numPayers;
			int remainder = fullCost % numPayers;

			// split the remainder-less cost among the payers
			// randomly distribute the remainder across all payers:
			// if remainder is 3, 3 payers will pay 1 extra.
			int i = 0;
			Iterator<String> iter = payerNames.iterator();
			if (remainder > 0) {
				Collections.shuffle(payerNames);
				do {
					// pay splitCost + 1 (unlucky)
					costs.get(iter.next()).add(splitCost + 1);
				} while (++i < remainder);
			}
			do {
				// pay splitCost (lucky)
				costs.get(iter.next()).add(splitCost);
			} while (++i < numPayers);

			// add the full cost to the total
			allCosts.add(fullCost);
		}

		// init the calculations
		subtotalCalculations = new HashMap<String, SubtotalCalculation>();
		for (Map.Entry<String, List<Integer>> entry : costs.entrySet()) {
			String payerName = entry.getKey();
			List<Integer> payerCosts = entry.getValue();
			subtotalCalculations.put(payerName, new SubtotalCalculation(payerCosts));
		}

		// init the global calculation
		globalSubtotalCalculation = new SubtotalCalculation(allCosts);
	}

	private void initTaxPercent() {
		// init the tax in the calculations
		taxCalculations = new HashMap<String, TaxCalculation>();
		for (Map.Entry<String, SubtotalCalculation> entry : subtotalCalculations.entrySet()) {
			String payerName = entry.getKey();
			SubtotalCalculation calc = entry.getValue();
			taxCalculations.put(payerName, new TaxCalculation(calc, taxPercent));
		}

		// init the tax in the global calculation
		globalTaxCalculation = new TaxCalculation(globalSubtotalCalculation, taxPercent);
	}

	public List<String> getPayerNames() {
		return payerNames;
	}

	public List<Item> getItems() {
		return items;
	}

	public double getTaxPercent() {
		return taxPercent;
	}

	public SubtotalCalculation getGlobalSubtotalCalculation() {
		if (globalSubtotalCalculation == null) throw new IllegalArgumentException("setItems must be called before calling getGlobalSubtotalCalculation");
		
		return globalSubtotalCalculation;
	}
	
	public TaxCalculation getTaxCalculation(String payerName) {
		if (taxCalculations == null) throw new IllegalArgumentException("setTaxPercent must be called before calling getTaxCalculation");
		
		return taxCalculations.get(payerName);
	}

	public TaxCalculation getGlobalTaxCalculation() {
		if (globalTaxCalculation == null) throw new IllegalArgumentException("setTaxPercent must be called before calling getGlobalTaxCalculation");
		
		return globalTaxCalculation;
	}

}

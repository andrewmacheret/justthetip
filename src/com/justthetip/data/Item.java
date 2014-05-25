package com.justthetip.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Item {
	private int cost;
	private String description;
	private Set<String> payerNames;

	public Item(int cost, String description, Collection<String> payerNames) {
		if (payerNames.size() < 1) throw new IllegalArgumentException("Someone has to pay for '" + description + "'");
		this.cost = cost;
		this.description = description;
		this.payerNames = Collections.unmodifiableSet(new HashSet<String>(payerNames));
	}

	public int getCost() {
		return cost;
	}

	public String getDescription() {
		return description;
	}

	public Set<String> getPayerNames() {
		return payerNames;
	}
}
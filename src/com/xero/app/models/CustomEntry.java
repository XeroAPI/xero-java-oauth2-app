package com.xero.app.models;

import java.util.ArrayList;
import java.util.List;

public class CustomEntry {

	List<CustomEntryItem> items = new ArrayList<CustomEntryItem>();

	public List<CustomEntryItem> getCustomEntryItems() {
		return items;
	}

	public void setCustomEntryItems(List<CustomEntryItem> customEntryItems) {
		this.items = customEntryItems;
	}
	
	public void addItem(CustomEntryItem customEntryItem) {
		this.items.add(customEntryItem);
	}
	
}

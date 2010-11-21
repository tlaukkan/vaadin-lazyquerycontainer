/**
 * Copyright 2010 Tommi S.E. Laukkanen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.addons.lazyquerycontainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;

/**
 * CompositeItem enables joining multiple items as single item. CompositeItem
 * contains PropertysetItem as default item to support adding and removing
 * of properties.
 * 
 * @author Tommi Laukkanen
 */
public class CompositeItem implements Item {
	private static final long serialVersionUID = 1L;
	
	public static final String DEFAULT_ITEM_KEY="default-item"; 
	private List<String> itemKeys;
	private Map<String,Item> items;
	private Item defaultItem;
	
	public CompositeItem() {
		itemKeys=new ArrayList<String>();
		items=new HashMap<String,Item>();
		defaultItem=new PropertysetItem();
		addItem(DEFAULT_ITEM_KEY,defaultItem);
	}
	
	public void addItem(String key, Item item) {
		itemKeys.add(key);
		items.put(key, item);
	}
	
	public void removeItem(String key) {
		itemKeys.remove(key);
		items.remove(key);
	}
		
	public List<String> getItemKeys() {
		return Collections.unmodifiableList(itemKeys);
	}
	
	public Item getItem(String key) {
		return items.get(key);
	}

	public Collection<?> getItemPropertyIds() {
		List<Object> itemPropertyIds=new ArrayList<Object>();
		for(String itemKey : itemKeys) {
			Item item=items.get(itemKey);
			for(Object propertyId : item.getItemPropertyIds()) {
				itemPropertyIds.add(propertyId);
			}
		}
		return itemPropertyIds;
	}

	public Property getItemProperty(Object id) {
		for(String itemKey : itemKeys) {
			Item item=items.get(itemKey);
			Property property=item.getItemProperty(id);
			if(property!=null) {
				return property;
			}
		}
		return null;
	}

	public boolean addItemProperty(Object id, Property property)
			throws UnsupportedOperationException {
		return defaultItem.addItemProperty(id, property);
	}

	public boolean removeItemProperty(Object id)
			throws UnsupportedOperationException {
		return defaultItem.removeItemProperty(id);
	}

}

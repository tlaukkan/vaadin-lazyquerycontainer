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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;

/**
 * Lazy loading implementation of QueryView. This implementation supports
 * lazy loading, batch loading, caching and sorting. LazyQueryView supports
 * debug properties which will be filled with debug information when they
 * exist in query definition. The debug property IDs are defined as string 
 * constants with the following naming convention: DEBUG_PROPERTY_XXXX.
 * 
 * LazyQueryView implements mainly batch loading, caching and debug
 * functionalities. When data is sorted old query is discarded and new 
 * constructed with QueryFactory and new sort state.
 * 
 * @author Tommi S.E. Laukkanen 
 */
public class LazyQueryView implements QueryView, ValueChangeListener {
	private static final long serialVersionUID = 1L;

	public static final String DEBUG_PROPERTY_ID_QUERY_INDEX="DEBUG_PROPERTY_ID_QUERY_COUT";
	public static final String DEBUG_PROPERTY_ID_BATCH_INDEX="DEBUG_PROPERTY_ID_BATCH_INDEX";
	public static final String DEBUG_PROPERTY_ID_BATCH_QUERY_TIME="DEBUG_PROPERTY_ID_ACCESS_COUNT";
	
	private int maxCacheSize=1000;
	private int queryCount=0;
	
	private QueryDefinition definition;
	private QueryFactory factory;
	private Query query;

	private Object[] sortPropertyIds; 
	private boolean[] ascendingStates;
	
	private LinkedList<Integer> itemCacheOrder=new LinkedList<Integer>();
	private Map<Integer,Item> itemCache=new HashMap<Integer,Item>();
	private Map<Property,Item> propertyItemMapCache=new HashMap<Property,Item>();
	private Map<Property,Object> propertyIdMapCache=new HashMap<Property,Object>();
	
	/**
	 * Constructs LazyQueryView with DefaultQueryDefinition and the given QueryFactory.
	 * @param factory The QueryFactory to be used.
	 * @param batchSize The batch size to be used when loading data.
	 */
	public LazyQueryView(QueryFactory factory, int batchSize) {
		initialize(new DefaultQueryDefinition(batchSize),factory);
	}
	
	/**
	 * Constructs LazyQueryView with given QueryDefinition and QueryFactory. The role
	 * of this constructor is to enable use of custom QueryDefinition implementations.
	 * @param definition
	 * @param factory
	 */
	public LazyQueryView(QueryDefinition definition, QueryFactory factory) {
		initialize(definition,factory);
	}
	
	private void initialize(QueryDefinition definition, QueryFactory factory) {
		this.definition=definition;
		this.factory=factory;
		this.factory.setQueryDefinition(definition);
		this.sortPropertyIds=new Object[0];
		this.ascendingStates=new boolean[0];		
	}
	
	@Override
	public QueryDefinition getDefinition() {
		return definition;
	}

	@Override
	public void sort(Object[] sortPropertyIds, boolean[] ascendingStates) {
		this.sortPropertyIds=sortPropertyIds;
		this.ascendingStates=ascendingStates;
		refresh();
	}
	
	@Override
	public void refresh() {
		
		for(Property property : propertyItemMapCache.keySet()) {
			if(property instanceof ValueChangeNotifier) {
				ValueChangeNotifier notifier=(ValueChangeNotifier) property;
				notifier.removeListener(this);
			}
		}
		
		query=null;
		itemCache.clear();
		itemCacheOrder.clear();		
		propertyItemMapCache.clear();
		propertyIdMapCache.clear();
	}

	@Override
	public int size() {
		return getQuery().size();
	}

	public int getBatchSize() {
		return definition.getBatchSize();
	}

	@Override
	public Item getItem(int index) {
		if(!itemCache.containsKey(index)) {
			queryItem(index);
		}
		return itemCache.get(index);
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property property=event.getProperty();
		Item item=propertyItemMapCache.get(property);
		Object propertyId=propertyIdMapCache.get(property);
		query.itemValueChange(item, propertyId, property);
	}

	private void queryItem(int index) {		
		int batchSize=getBatchSize();
		int startIndex=index-index%batchSize;
		int count=Math.min(batchSize, size()-startIndex);
		
		long queryStartTime=System.currentTimeMillis();
		List<Item> items=getQuery().getItems(startIndex, count);
		long queryEndTime=System.currentTimeMillis();
		
		for(int i=0;i<count;i++) {
			int itemIndex=startIndex+i;
			Item item=items.get(i);
			if(item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_INDEX)!=null) {
				item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_INDEX).setValue(startIndex/batchSize);
			}
			if(item.getItemProperty(DEBUG_PROPERTY_ID_QUERY_INDEX)!=null) {
				item.getItemProperty(DEBUG_PROPERTY_ID_QUERY_INDEX).setValue(queryCount);
			}
			if(item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_QUERY_TIME)!=null) {
				item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_QUERY_TIME).setValue(queryEndTime-queryStartTime);
			}
						
			for(Object propertyId : item.getItemPropertyIds()) {
				Property property=item.getItemProperty(propertyId);
				if(property instanceof ValueChangeNotifier) {
					ValueChangeNotifier notifier=(ValueChangeNotifier) property;
					notifier.addListener(this);
					propertyItemMapCache.put(property, item);		
					propertyIdMapCache.put(property, propertyId);	
				}
			}
			
			itemCache.put(itemIndex,item);
			itemCacheOrder.addLast(itemIndex);
		}
		
		while(itemCache.size()>maxCacheSize) {
			int removedIndex=itemCacheOrder.removeFirst();
			Item removedItem=itemCache.remove(removedIndex);
						
			for(Object propertyId : removedItem.getItemPropertyIds()) {
				Property property=removedItem.getItemProperty(propertyId);
				if(property instanceof ValueChangeNotifier) {
					ValueChangeNotifier notifier=(ValueChangeNotifier) property;
					notifier.removeListener(this);
					propertyItemMapCache.remove(property);									
					propertyIdMapCache.remove(property);	
				}
			}

		}		
	}

	private Query getQuery() {
		if(query==null) {
			query=factory.constructQuery(sortPropertyIds,ascendingStates);
			queryCount++;
		}
		return query;
	}
	
}

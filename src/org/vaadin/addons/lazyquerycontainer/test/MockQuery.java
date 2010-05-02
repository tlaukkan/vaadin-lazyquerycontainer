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
package org.vaadin.addons.lazyquerycontainer.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * Mock implementation of Query interface for JUnit tests and example application.
 * @author tommilaukkanen
 */
public class MockQuery implements Query {

	private QueryDefinition definition;
	private List<Item> items;
	private int batchQueryMinTime;
	private int batchQueryMaxTime;
	
	public MockQuery(QueryDefinition definition, int size,Object[] sortPropertyIds, boolean[] ascendingStates,int batchQueryMinTime, int batchQueryMaxTime) {
		this.definition=definition;
		this.items=new ArrayList<Item>();
		this.batchQueryMinTime=batchQueryMinTime;
		this.batchQueryMaxTime=batchQueryMaxTime;
		
		for(int i=0;i<size;i++) {
			PropertysetItem item=new PropertysetItem();
			
			for(Object propertyId : this.definition.getPropertyIds()) {
				
				Object value=null;
				
				if("Index".equals(propertyId)) {
					value=i;
				} else if("ReverseIndex".equals(propertyId)) {
					value=size-i;
				} else {
					value=this.definition.getPropertyDefaultValue(propertyId);
				}
				
				item.addItemProperty(propertyId, new ObjectProperty(
						value,
						this.definition.getPropertyType(propertyId),
						this.definition.isPropertyReadOnly(propertyId)
						));
				
			}
			
			this.items.add(item);
		}
		
		if(sortPropertyIds.length!=0) {
			ItemComparator comparator=new ItemComparator(sortPropertyIds,ascendingStates);
			Collections.sort(this.items,comparator);
		}
		
	}
	
	@Override
	public List<Item> getItems(int startIndex, int count) {
		List<Item> resultItems=new ArrayList<Item>();
		
		for(int i=0;i<count;i++) {
			resultItems.add(items.get(startIndex+i));
		}
		
		try {
			Thread.sleep(batchQueryMinTime+(int)(Math.random()*batchQueryMaxTime));
		} catch (InterruptedException e) {
		}
		
		return resultItems;
	}

	@Override
	public int size() {
		return items.size();
	}
	
	public class ItemComparator implements Comparator<Item> {

		private Object[] sortPropertyIds;
		private boolean[] ascendingStates;
		
		public ItemComparator(Object[] sortPropertyIds, boolean[] ascendingStates) {
			this.sortPropertyIds=sortPropertyIds;
			this.ascendingStates=ascendingStates;
		}
		
		@Override
		public int compare(Item o1, Item o2) {
			
			for(int i=0;i<sortPropertyIds.length;i++) {
				Property p1=o1.getItemProperty(sortPropertyIds[i]);
				Property p2=o2.getItemProperty(sortPropertyIds[i]);
				
				int v1=(Integer)p1.getValue();
				int v2=(Integer)p2.getValue();
				
				if(v1!=v2) {
					int comparison=v1-v2;
					if(!ascendingStates[i]) {
						comparison=-comparison;
					}
					return comparison;
				}
			}
			
			return 0;
		}
		
	}

}

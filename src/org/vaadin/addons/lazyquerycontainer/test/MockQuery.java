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
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;

import com.vaadin.data.Item;

/**
 * Mock implementation of Query interface for JUnit tests and example application.
 * @author tommilaukkanen
 */
public class MockQuery implements Query {

	//private QueryDefinition definition;
	private List<Item> items;
	private int batchQueryMinTime;
	private int batchQueryMaxTime;
	
	public MockQuery(List<Item> items,int batchQueryMinTime, int batchQueryMaxTime) {
		this.items=items;
		this.batchQueryMinTime=batchQueryMinTime;
		this.batchQueryMaxTime=batchQueryMaxTime;
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

}

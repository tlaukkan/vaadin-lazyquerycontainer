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

/**
 * Lazy loading implementation of QueryView. This implementation supports
 * lazy loading, batch loading, caching and sorting.
 * @author Tommi S.E. Laukkanen 
 */
public class LazyQueryView implements QueryView {

	private int batchSize=50;
	private int maxCacheSize=1000;
	
	private QueryDefinition definition;
	private QueryFactory factory;
	private Query query;

	private Object[] sortPropertyIds; 
	private boolean[] ascendingStates;
	
	private LinkedList<Integer> itemCacheOrder=new LinkedList<Integer>();
	private Map<Integer,Item> itemCache=new HashMap<Integer,Item>();
	
	public LazyQueryView(QueryFactory factory) {
		initialize(new DefaultQueryDefinition(),factory);
	}
	
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
		query=null;
		itemCache.clear();
		itemCacheOrder.clear();		
	}

	@Override
	public int size() {
		return getQuery().size();
	}

	@Override
	public Item getItem(int index) {
		if(!itemCache.containsKey(index)) {
			queryItem(index);
		}
		
		return itemCache.get(index);
	}

	private void queryItem(int index) {		
		int startIndex=index-index%batchSize;
		int count=Math.min(batchSize, size()-startIndex);
		
		List<Item> items=getQuery().getItems(startIndex, count);
		
		for(int i=0;i<count;i++) {
			int itemIndex=startIndex+i;
			itemCache.put(itemIndex, items.get(i));
			itemCacheOrder.addLast(itemIndex);
		}
		
		while(itemCache.size()>maxCacheSize) {
			int removedIndex=itemCacheOrder.removeFirst();
			itemCache.remove(removedIndex);
		}		
	}

	private Query getQuery() {
		if(query==null) {
			query=factory.constructQuery(sortPropertyIds,ascendingStates);
		}
		return query;
	}
	
}

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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;

/**
 * Simple generics bean query implementation which dynamically injects 
 * missing query definition properties to CompositeItems.
 * @author Tommi S.E. Laukkanen
 * @param <T> Bean class
 */
public abstract class AbstractBeanQuery<T extends Object> implements Query {

        private QueryDefinition definition;
		protected Map<String,Object> queryConfiguration;
		protected Object[] sortPropertyIds;
		protected boolean[] sortStates;
        
		public AbstractBeanQuery() {
        }

        public void setDefinition(QueryDefinition definition) {
			this.definition = definition;
		}

		public void setQueryConfiguration(Map<String, Object> queryConfiguration) {
			this.queryConfiguration = queryConfiguration;
		}
		
		public void setSortPropertyIds(Object[] sortPropertyIds) {
			this.sortPropertyIds = sortPropertyIds;
		}

		public void setSortStates(boolean[] sortStates) {
			this.sortStates = sortStates;
		}
		
        @Override
        public Item constructItem() {
        	try {
            	T bean=constructBean();
	        	BeanInfo info = Introspector.getBeanInfo( bean.getClass() );
	            for ( PropertyDescriptor pd : info.getPropertyDescriptors() ) {
	            	for(Object propertyId : definition.getPropertyIds()) {               
	            		if(pd.getName().equals(propertyId)) {
	            			pd.getWriteMethod().invoke(bean, definition.getPropertyDefaultValue(propertyId));
	            		}
	            	}
	            }
	            return toItem(bean);
        	} catch(Exception e) {
        		throw new RuntimeException("Error in bean construction or property population with default values.",e);
        	}
        }
        
    	protected abstract T constructBean();
        
        @Override
        public abstract int size();

        @Override
        public List<Item> loadItems(int startIndex, int count) {
                List<Item> items=new ArrayList<Item>();
                for(T bean :  loadBeans(startIndex,count)) {
                        items.add(toItem(bean));
                }
                return items;
        }
        
        protected abstract List<T> loadBeans(int startIndex, int count);

        @Override
        public void saveItems(List<Item> addedItems, List<Item> modifiedItems,
                        List<Item> removedItems) {
        	saveBeans(fromItems(addedItems),fromItems(modifiedItems),fromItems(removedItems));
        }
        
        protected abstract void saveBeans(List<T> addedTasks, List<T> modifiedTasks, List<T> removedTasks);
        
        @Override
        public boolean deleteAllItems() {
                throw new UnsupportedOperationException();
        }
        
        private Item toItem(T bean) {
            BeanItem<T> beanItem= new BeanItem<T>(bean);
            
            CompositeItem compositeItem=new CompositeItem();            
            compositeItem.addItem("bean", beanItem);
            
            for(Object propertyId : definition.getPropertyIds()) {               
            	if(compositeItem.getItemProperty(propertyId)==null) {
            		compositeItem.addItemProperty(propertyId,new ObjectProperty(
            				definition.getPropertyDefaultValue(propertyId),
            				definition.getPropertyType(propertyId),
            				definition.isPropertyReadOnly(propertyId)));
            	}
            }
            
            return compositeItem;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
		private T fromItem(Item item) {
        	return (T)((BeanItem)(((CompositeItem)item).getItem("bean"))).getBean();
        }
        
        private List<T> fromItems(List<Item> items) {
        	ArrayList<T> beans=new ArrayList<T>();
        	for(Item item : items) {
        		beans.add(fromItem(item));
        	}
        	return beans;
        }
}
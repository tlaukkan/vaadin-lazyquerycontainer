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

import javax.persistence.EntityManager;

import org.vaadin.addons.lazyquerycontainer.CompositeItem;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;

/**
 * Simple JPA query implementation which dynamically injects 
 * missing query definition properties to CompositeItems.
 * @author Tommi S.E. Laukkanen
 * @param <T> Bean class
 */
public class JpaQuery<T extends Object> implements Query {

        private EntityManager entityManager;
        private QueryDefinition definition;
        private String jpaSelectQuery;
        private String jpaSelectCountQuery;
        private String criteria;
        private Class<T> beanClass;
        
        public JpaQuery(Class<T> beanClass, EntityManager entityManager, 
        		String jpaSelectQuery, String jpaSelectCountQuery, QueryDefinition definition, 
        		Object[] nativeSortPropertyIds, boolean[] nativeSortStates, 
        		Object[] sortPropertyIds, boolean[] sortStates) {
                
                this.entityManager = entityManager;
                this.definition = definition;
                this.beanClass=beanClass;
                this.jpaSelectCountQuery=jpaSelectCountQuery;

                if(nativeSortPropertyIds.length==0) {
            		throw new RuntimeException("Native sort is mandatory. Define at least one native sort property id and corresponding native sort state.");
            	}
                
                if(sortPropertyIds.length>0) {
	                for(int i=0;i<sortPropertyIds.length;i++) {
	                        if(i==0) {
	                                criteria=" ORDER BY";
	                        } else {
	                                criteria+=",";
	                        }
	                        criteria+=" t."+sortPropertyIds[i];
	                        if(sortStates[i]) {
	                                criteria+=" ASC";
	                        }
	                        else {
	                                criteria+=" DESC";                              
	                        }
	                }
                } else {
	                for(int i=0;i<nativeSortPropertyIds.length;i++) {
                        if(i==0) {
                                criteria=" ORDER BY";
                        } else {
                                criteria+=",";
                        }
                        criteria+=" t."+nativeSortPropertyIds[i];
                        if(nativeSortStates[i]) {
                                criteria+=" ASC";
                        }
                        else {
                                criteria+=" DESC";                              
                        }
	                }                	
                }
                
                this.jpaSelectQuery=jpaSelectQuery+criteria;
        }

        @Override
        public Item constructItem() {
        	try {
            	T bean=beanClass.newInstance();
	        	BeanInfo info = Introspector.getBeanInfo( beanClass );
	            for ( PropertyDescriptor pd : info.getPropertyDescriptors() ) {
	            	for(Object propertyId : definition.getPropertyIds()) {               
	            		if(pd.getName().equals(propertyId)) {
	            			pd.getWriteMethod().invoke(bean, definition.getPropertyDefaultValue(propertyId));
	            		}
	            	}
	            }
	            return toItem(bean);
        	} catch(Exception e) {
        		throw new RuntimeException("Error in bean construction or property population with default values.");
        	}
        }
        
        @Override
        public int size() {
                javax.persistence.Query query = entityManager.createQuery(jpaSelectCountQuery);           
                return (int)((Long) query.getSingleResult()).longValue();
        }

        @Override
        public List<Item> loadItems(int startIndex, int count) {
                javax.persistence.Query query = entityManager.createQuery(jpaSelectQuery);
                query.setFirstResult(startIndex);
                query.setMaxResults(count);
                
                @SuppressWarnings("unchecked")
				List<T> beans=query.getResultList();
                List<Item> items=new ArrayList<Item>();
                for(T bean : beans) {
                        items.add(toItem(bean));
                }
                
                return items;
        }

        @Override
        public void saveItems(List<Item> addedItems, List<Item> modifiedItems,
                        List<Item> removedItems) {
        		entityManager.getTransaction().begin();
                for(Item item : addedItems) {
                	entityManager.persist(fromItem(item));
                }
                for(Item item : modifiedItems) {
                	entityManager.persist(fromItem(item));
                }
                for(Item item : removedItems) {
                    entityManager.remove(fromItem(item));
                }
                entityManager.getTransaction().commit();
        }
        
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
}
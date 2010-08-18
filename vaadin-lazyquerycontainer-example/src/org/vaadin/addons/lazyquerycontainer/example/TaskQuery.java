package org.vaadin.addons.lazyquerycontainer.example;

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

public class TaskQuery implements Query {

        private EntityManager entityManager;
        private QueryDefinition definition;
        private String criteria=" ORDER BY t.name ASC";
        
        public TaskQuery(EntityManager entityManager, QueryDefinition definition,Object[] sortPropertyIds, boolean[] sortStates) {
                super();
                this.entityManager = entityManager;
                this.definition = definition;
                
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
        }

        @Override
        public Item constructItem() {
        	Task task=new Task();
        	try {
	        	BeanInfo info = Introspector.getBeanInfo( Task.class );
	            for ( PropertyDescriptor pd : info.getPropertyDescriptors() ) {
	            	for(Object propertyId : definition.getPropertyIds()) {               
	            		if(pd.getName().equals(propertyId)) {
	            			pd.getWriteMethod().invoke(task, definition.getPropertyDefaultValue(propertyId));
	            		}
	            	}
	            }
        	} catch(Exception e) {
        		throw new RuntimeException("Error in bean property population");
        	}
            return toItem(task);
        }
        
        @Override
        public int size() {
                javax.persistence.Query query = entityManager.createQuery("SELECT count(t) from Task as t");           
                return (int)((Long) query.getSingleResult()).longValue();
        }

        @Override
        public List<Item> loadItems(int startIndex, int count) {
                javax.persistence.Query query = entityManager.createQuery("SELECT t from Task as t"+criteria);
                query.setFirstResult(startIndex);
                query.setMaxResults(count);
                
                List<Task> tasks=query.getResultList();
                List<Item> items=new ArrayList<Item>();
                for(Task task : tasks) {
                        items.add(toItem(task));
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
        
        private Item toItem(Task task) {
            BeanItem<Task> beanItem= new BeanItem<Task>(task);
            
            CompositeItem compositeItem=new CompositeItem();
            
            compositeItem.addItem("task", beanItem);
            
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

        private Task fromItem(Item item) {
        	return (Task)((BeanItem)(((CompositeItem)item).getItem("task"))).getBean();
        }
}
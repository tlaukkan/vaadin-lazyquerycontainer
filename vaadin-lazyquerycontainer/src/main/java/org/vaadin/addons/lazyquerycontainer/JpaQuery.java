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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;

/**
 * Simple JPA query implementation which dynamically injects missing query
 * definition properties to CompositeItems.
 * @author Tommi S.E. Laukkanen
 * @param <T> Bean class
 */
public final class JpaQuery<T extends Object> implements Query, Serializable {
    /** Java serialization version UID. */
    private static final long serialVersionUID = 1L;
    /** The JPA EntityManager. */
    private EntityManager entityManager;
    /** QueryDefinition contains definition of the query properties and batch size. */
    private QueryDefinition queryDefinition;
    /** The JPA select query. */
    private String jpaSelectQuery;
    /** The JPA select count query. */
    private String jpaSelectCountQuery;
    /** The sort criteria. */
    private String sortCriteria;
    /** The JPA bean class. */
    private Class<T> beanClass;
    /** Flag reflecting whether application manages transactions. */
    private boolean transactionManagement;

    /**
     * Constructor for configuring the query.
     * @param beanClass The JPA bean class.
     * @param entityManager The JPA EntityManager.
     * @param jpaSelectQuery The JPA select query.
     * @param jpaSelectCountQuery The JPA select count query.
     * @param queryDefinition QueryDefinition contains definition of the query properties and batch size.
     * @param nativeSortPropertyIds The native sort property IDs.
     * @param nativeSortStates The native sort ascending and descending states.
     * @param sortPropertyIds The sort property IDs.
     * @param sortStates The sort ascending and descending states.
     * @param transactionManagement Flag reflecting whether application manages transactions.
     */
    public JpaQuery(final Class<T> beanClass, final EntityManager entityManager, final String jpaSelectQuery, 
            final String jpaSelectCountQuery, final QueryDefinition queryDefinition,
            final Object[] nativeSortPropertyIds, final boolean[] nativeSortStates,
            final Object[] sortPropertyIds, final boolean[] sortStates, final boolean transactionManagement) {

        this.entityManager = entityManager;
        this.queryDefinition = queryDefinition;
        this.beanClass = beanClass;
        this.jpaSelectCountQuery = jpaSelectCountQuery;
        this.transactionManagement = transactionManagement;

        if (nativeSortPropertyIds.length == 0) {
            throw new RuntimeException("Native sort is mandatory."
                    + " Define at least one native sort property id and corresponding native sort state.");
        }

        if (sortPropertyIds.length > 0) {
            for (int i = 0; i < sortPropertyIds.length; i++) {
                if (i == 0) {
                    sortCriteria = " ORDER BY";
                } else {
                    sortCriteria += ",";
                }
                sortCriteria += " t." + sortPropertyIds[i];
                if (sortStates[i]) {
                    sortCriteria += " ASC";
                } else {
                    sortCriteria += " DESC";
                }
            }
        } else {
            for (int i = 0; i < nativeSortPropertyIds.length; i++) {
                if (i == 0) {
                    sortCriteria = " ORDER BY";
                } else {
                    sortCriteria += ",";
                }
                sortCriteria += " t." + nativeSortPropertyIds[i];
                if (nativeSortStates[i]) {
                    sortCriteria += " ASC";
                } else {
                    sortCriteria += " DESC";
                }
            }
        }

        this.jpaSelectQuery = jpaSelectQuery + sortCriteria;
    }

    /**
     * Constructs new item based on QueryDefinition.
     * @return new item.
     */
    public Item constructItem() {
        try {
            T bean = beanClass.newInstance();
            BeanInfo info = Introspector.getBeanInfo(beanClass);
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                for (Object propertyId : queryDefinition.getPropertyIds()) {
                    if (pd.getName().equals(propertyId)) {
                        pd.getWriteMethod().invoke(bean, queryDefinition.getPropertyDefaultValue(propertyId));
                    }
                }
            }
            return toItem(bean);
        } catch (Exception e) {
            throw new RuntimeException("Error in bean construction or property population with default values.");
        }
    }

    /**
     * Number of beans returned by query.
     * @return number of beans.
     */
    public int size() {
        javax.persistence.Query query = entityManager.createQuery(jpaSelectCountQuery);
        return (int) ((Number) query.getSingleResult()).longValue();
    }

    /**
     * Load batch of items.
     * @param startIndex Starting index of the item list.
     * @param count Count of the items to be retrieved.
     * @return List of items.
     */
    public List<Item> loadItems(final int startIndex, final int count) {
        javax.persistence.Query query = entityManager.createQuery(jpaSelectQuery);
        query.setFirstResult(startIndex);
        query.setMaxResults(count);

        @SuppressWarnings("unchecked")
        List<T> beans = query.getResultList();
        List<Item> items = new ArrayList<Item>();
        for (T bean : beans) {
            items.add(toItem(bean));
        }

        return items;
    }

    /**
     * Saves the modifications done by container to the query result.
     * Query will be discarded after changes have been saved
     * and new query loaded so that changed items are sorted
     * appropriately.
     * @param addedItems Items to be inserted.
     * @param modifiedItems Items to be updated.
     * @param removedItems Items to be deleted.
     */
    public void saveItems(final List<Item> addedItems, final List<Item> modifiedItems, final List<Item> removedItems) {
        if (transactionManagement) {
            entityManager.getTransaction().begin();
        }
        for (Item item : addedItems) {
            entityManager.persist(fromItem(item));
        }
        for (Item item : modifiedItems) {
            entityManager.persist(fromItem(item));
        }
        for (Item item : removedItems) {
            entityManager.remove(fromItem(item));
        }
        if (transactionManagement) {
            entityManager.getTransaction().commit();
        }
    }

    /**
     * Removes all items.
     * Query will be discarded after delete all items has been called.
     * @return true if the operation succeeded or false in case of a failure.
     */
    public boolean deleteAllItems() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts bean to Item. Implemented by encapsulating the Bean
     * first to BeanItem and then to CompositeItem.
     * @param bean bean to be converted.
     * @return item converted from bean.
     */
    private Item toItem(final T bean) {
        BeanItem<T> beanItem = new BeanItem<T>(bean);

        CompositeItem compositeItem = new CompositeItem();
        compositeItem.addItem("bean", beanItem);

        for (Object propertyId : queryDefinition.getPropertyIds()) {
            if (compositeItem.getItemProperty(propertyId) == null) {
                compositeItem.addItemProperty(
                        propertyId,
                        new ObjectProperty(queryDefinition.getPropertyDefaultValue(propertyId), queryDefinition
                                .getPropertyType(propertyId), queryDefinition.isPropertyReadOnly(propertyId)));
            }
        }

        return compositeItem;
    }

    /**
     * Converts item back to bean.
     * @param item Item to be converted to bean.
     * @return Resulting bean.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private T fromItem(final Item item) {
        return (T) ((BeanItem) (((CompositeItem) item).getItem("bean"))).getBean();
    }
}
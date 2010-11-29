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
import java.util.Map;

import javax.persistence.EntityManager;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;

/**
 * Entity query implementation which dynamically injects missing query
 * definition properties to CompositeItems.
 * @author Tommi S.E. Laukkanen
 */
public final class EntityQuery implements Query, Serializable {
    /** Java serialization version UID. */
    private static final long serialVersionUID = 1L;
    /** The JPA EntityManager. */
    private EntityManager entityManager;
    /** Flag reflecting whether application manages transactions. */
    private boolean applicationTransactionManagement;
    /** The JPA entity class. */
    private Class<?> entityClass;
    /** The JPA select query. */
    private String selectPsql;
    /** The JPA select count query. */
    private String selectCountPsql;
    /** The parameters to set to JPA query. */
    private Map<String, Object> selectParameters;
    /** QueryDefinition contains definition of the query properties and batch size. */
    private QueryDefinition queryDefinition;
    /** The size of the query. */
    private int querySize = -1;

    /**
     * Constructor for configuring the query.
     * @param entityQueryDefinition The entity query definition.
     */
    public EntityQuery(final EntityQueryDefinition entityQueryDefinition) {
        final EntityQueryDefinition.EntitySelectDefinition selectDefinition =
            entityQueryDefinition.getEntitySelectDefinition();
        this.entityManager = entityQueryDefinition.getEntityManager();
        this.queryDefinition = entityQueryDefinition;
        this.entityClass = entityQueryDefinition.getEntityClass();
        this.selectPsql = selectDefinition.getSelectPsql();
        this.selectCountPsql = selectDefinition.getSelectCountPsql();
        this.selectParameters = entityQueryDefinition.getWhereParameters();
        this.applicationTransactionManagement = entityQueryDefinition.isApplicationManagedTransactions();
    }

    /**
     * Constructs new item based on QueryDefinition.
     * @return new item.
     */
    public Item constructItem() {
        try {
            Object entity = entityClass.newInstance();
            BeanInfo info = Introspector.getBeanInfo(entityClass);
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                for (Object propertyId : queryDefinition.getPropertyIds()) {
                    if (pd.getName().equals(propertyId)) {
                        pd.getWriteMethod().invoke(entity, queryDefinition.getPropertyDefaultValue(propertyId));
                    }
                }
            }
            return toItem(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error in bean construction or property population with default values.", e);
        }
    }

    /**
     * Number of beans returned by query.
     * @return number of beans.
     */
    public int size() {
        if (querySize == -1) {
            javax.persistence.Query query = entityManager.createQuery(selectCountPsql);
            if (selectParameters != null) {
                for (String parameterKey : selectParameters.keySet()) {
                    query.setParameter(parameterKey, selectParameters.get(parameterKey));
                }
            }
            querySize = ((Number) query.getSingleResult()).intValue();
        }
        return querySize;
    }

    /**
     * Load batch of items.
     * @param startIndex Starting index of the item list.
     * @param count Count of the items to be retrieved.
     * @return List of items.
     */
    public List<Item> loadItems(final int startIndex, final int count) {
        javax.persistence.Query query = entityManager.createQuery(selectPsql);
        if (selectParameters != null) {
            for (String parameterKey : selectParameters.keySet()) {
                query.setParameter(parameterKey, selectParameters.get(parameterKey));
            }
        } 
        query.setFirstResult(startIndex);
        query.setMaxResults(count);

        List<?> entities = query.getResultList();
        List<Item> items = new ArrayList<Item>();
        for (Object entity : entities) {
            items.add(toItem(entity));
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
        if (applicationTransactionManagement) {
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
        if (applicationTransactionManagement) {
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
     * @param entity bean to be converted.
     * @return item converted from bean.
     */
    private Item toItem(final Object entity) {
        BeanItem<?> beanItem = new BeanItem<Object>(entity);

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
    @SuppressWarnings({ "rawtypes" })
    private Object fromItem(final Item item) {
        return (Object) ((BeanItem) (((CompositeItem) item).getItem("bean"))).getBean();
    }
}
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

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;

import javax.persistence.EntityManager;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entity query implementation which dynamically injects missing query
 * definition properties to CompositeItems.
 *
 * @author Tommi S.E. Laukkanen
 */
public class EntityQuery implements Query, Serializable {
    /**
     * Java serialization version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The JPA EntityManager.
     */
    private final EntityManager entityManager;
    /**
     * Flag reflecting whether application manages transactions.
     */
    private final boolean applicationTransactionManagement;
    /**
     * The JPA entity class.
     */
    private final Class<?> entityClass;
    /**
     * The JPA select query.
     */
    private final String selectPsql;
    /**
     * The JPA select count query.
     */
    private final String selectCountPsql;
    /**
     * The PSQL for deleting entities.
     */
    private final String deletePsql;
    /**
     * The parameters to set to JPA query.
     */
    private final Map<String, Object> selectParameters;
    /**
     * QueryDefinition contains definition of the query properties and batch
     * size.
     */
    private final EntityQueryDefinition queryDefinition;
    /**
     * The size of the query.
     */
    private int querySize = -1;
    /**
     * The entity PSQL definition.
     */
    private final EntityQueryDefinition.EntityPsqlDefinition entityPsqlDefinition;

    /**
     * Constructor for configuring the query.
     *
     * @param entityQueryDefinition The entity query definition.
     */
    public EntityQuery(final EntityQueryDefinition entityQueryDefinition) {
        this.entityPsqlDefinition = entityQueryDefinition.getEntityPsqlDefinition();
        this.entityManager = entityQueryDefinition.getEntityManager();
        this.queryDefinition = entityQueryDefinition;
        this.entityClass = entityQueryDefinition.getEntityClass();
        this.selectPsql = entityPsqlDefinition.getSelectPsql();
        this.selectCountPsql = entityPsqlDefinition.getSelectCountPsql();
        this.deletePsql = entityPsqlDefinition.getDeletePsql();
        this.selectParameters = entityQueryDefinition.getWhereParameters();
        this.applicationTransactionManagement = entityQueryDefinition.isApplicationManagedTransactions();
    }

    /**
     * Constructs new item based on QueryDefinition.
     *
     * @return new item.
     */
    @Override
    public final Item constructItem() {
        try {
            final Object entity = entityClass.newInstance();
            final BeanInfo info = Introspector.getBeanInfo(entityClass);
            for (final PropertyDescriptor pd : info.getPropertyDescriptors()) {
                for (final Object propertyId : queryDefinition.getPropertyIds()) {
                    if (pd.getName().equals(propertyId)) {
                        pd.getWriteMethod().invoke(entity, queryDefinition.getPropertyDefaultValue(propertyId));
                    }
                }
            }
            return toItem(entity);
        } catch (final Exception e) {
            throw new RuntimeException("Error in bean construction or property population with default values.", e);
        }
    }

    /**
     * Number of beans returned by query.
     *
     * @return number of beans.
     */
    @Override
    public int size() {
        if (querySize == -1) {
            final javax.persistence.Query query = entityManager.createQuery(selectCountPsql);
            if (selectParameters != null) {
                for (final String parameterKey : selectParameters.keySet()) {
                    query.setParameter(parameterKey, selectParameters.get(parameterKey));
                }
            }
            querySize = ((Number) query.getSingleResult()).intValue();
        }
        return querySize;
    }

    /**
     * Load batch of items.
     *
     * @param startIndex Starting index of the item list.
     * @param count      Count of the items to be retrieved.
     * @return List of items.
     */
    @Override
    public List<Item> loadItems(final int startIndex, final int count) {
        final javax.persistence.Query query = entityManager.createQuery(selectPsql);
        if (selectParameters != null) {
            for (final String parameterKey : selectParameters.keySet()) {
                query.setParameter(parameterKey, selectParameters.get(parameterKey));
            }
        }
        query.setFirstResult(startIndex);
        query.setMaxResults(count);

        final List<?> entities = query.getResultList();
        final List<Item> items = new ArrayList<Item>();
        for (final Object entity : entities) {
            if (queryDefinition.isDetachedEntities()) {
                entityManager.detach(entity);
            }
            items.add(toItem(entity));
        }

        return items;
    }

    /**
     * Saves the modifications done by container to the query result. Query will
     * be discarded after changes have been saved and new query loaded so that
     * changed items are sorted appropriately.
     *
     * @param addedItems    Items to be inserted.
     * @param modifiedItems Items to be updated.
     * @param removedItems  Items to be deleted.
     */
    @Override
    public void saveItems(final List<Item> addedItems, final List<Item> modifiedItems, final List<Item> removedItems) {
        if (applicationTransactionManagement) {
            entityManager.getTransaction().begin();
        }
        try {
            for (final Item item : addedItems) {
                if (!removedItems.contains(item)) {
                    entityManager.persist(fromItem(item));
                }
            }
            for (final Item item : modifiedItems) {
                if (!removedItems.contains(item)) {
                    Object entity = fromItem(item);
                    if (queryDefinition.isDetachedEntities()) {
                        entity = entityManager.merge(entity);
                    }
                    entityManager.persist(entity);
                }
            }
            for (final Item item : removedItems) {
                if (!addedItems.contains(item)) {
                    Object entity = fromItem(item);
                    if (queryDefinition.isDetachedEntities()) {
                        entity = entityManager.merge(entity);
                    }
                    entityManager.remove(entity);
                }
            }
            if (applicationTransactionManagement) {
                entityManager.getTransaction().commit();
            }
        } catch (final Exception e) {
            if (applicationTransactionManagement) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes all items. Query will be discarded after delete all items has
     * been called.
     *
     * @return true if the operation succeeded or false in case of a failure.
     */
    @Override
    public boolean deleteAllItems() {
        if (applicationTransactionManagement) {
            entityManager.getTransaction().begin();
        }
        try {
            entityManager.createQuery(deletePsql).executeUpdate();
            if (applicationTransactionManagement) {
                entityManager.getTransaction().commit();
            }
        } catch (final Exception e) {
            if (applicationTransactionManagement) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Converts bean to Item. Implemented by encapsulating the Bean first to
     * BeanItem and then to CompositeItem.
     *
     * @param entity bean to be converted.
     * @return item converted from bean.
     */
    @SuppressWarnings({"rawtypes", "unchecked" })
    protected final Item toItem(final Object entity) {
        if (queryDefinition.isCompositeItems()) {
            final BeanItem<?> beanItem = new BeanItem<Object>(entity);

            final CompositeItem compositeItem = new CompositeItem();
            compositeItem.addItem("bean", beanItem);

            for (final Object propertyId : queryDefinition.getPropertyIds()) {
                if (compositeItem.getItemProperty(propertyId) == null) {
                    compositeItem.addItemProperty(
                            propertyId,
                            new ObjectProperty(queryDefinition.getPropertyDefaultValue(propertyId), queryDefinition
                                    .getPropertyType(propertyId), queryDefinition.isPropertyReadOnly(propertyId)));
                }
            }

            return compositeItem;
        } else {
            return new BeanItem<Object>(entity);
        }
    }

    /**
     * Converts item back to bean.
     *
     * @param item Item to be converted to bean.
     * @return Resulting bean.
     */
    protected final Object fromItem(final Item item) {
        if (queryDefinition.isCompositeItems()) {
            return (Object) ((BeanItem<?>) (((CompositeItem) item).getItem("bean"))).getBean();
        } else {
            return ((BeanItem<?>) item).getBean();
        }
    }

    /**
     * @return the queryDefinition
     */
    protected final EntityQueryDefinition getQueryDefinition() {
        return queryDefinition;
    }

    /**
     * @return the entityPsqlDefinition
     */
    protected final EntityQueryDefinition.EntityPsqlDefinition getEntityPsqlDefinition() {
        return entityPsqlDefinition;
    }
}

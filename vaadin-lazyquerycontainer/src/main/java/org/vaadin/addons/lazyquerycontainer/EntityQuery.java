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

import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.data.util.ObjectProperty;
import com.vaadin.v7.data.util.filter.*;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity query implementation which dynamically injects missing query
 * definition properties to CompositeItems.
 *
 * @param <E> the entity type
 *
 * @author Tommi S.E. Laukkanen
 */
public class EntityQuery<E> implements Query, Serializable {
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
    private final Class<E> entityClass;
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
     * Constructor for configuring the query.
     *
     * @param entityQueryDefinition The entity query definition.
     * @param entityManager The entity manager.
     */
    public EntityQuery(final EntityQueryDefinition entityQueryDefinition, final EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryDefinition = entityQueryDefinition;
        this.entityClass = (Class<E>) entityQueryDefinition.getEntityClass();
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
    public final int size() {

        if (querySize == -1) {
            if (queryDefinition.getBatchSize() == 0) {
                return 0;
            }

            final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            final Root<E> root = cq.from(entityClass);

            cq.select(cb.count(root));

            setWhereCriteria(cb, cq, root);

            final javax.persistence.Query query = entityManager.createQuery(cq);

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
    public final List<Item> loadItems(final int startIndex, final int count) {

        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<E> cq = cb.createQuery(entityClass);
        final Root<E> root = cq.from(entityClass);

        cq.select(root);

        setWhereCriteria(cb, cq, root);

        setOrderClause(cb, cq, root);

        final javax.persistence.TypedQuery<E> query = entityManager.createQuery(cq);

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
     * Sets where criteria of JPA 2.0 Criteria API query according to Vaadin filters.
     * @param cb the CriteriaBuilder
     * @param cq the CriteriaQuery
     * @param root the root
     * @param <SE> the selected entity
     */
    private <SE> void setWhereCriteria(final CriteriaBuilder cb, final CriteriaQuery<SE> cq, final Root<E> root) {
        final List<Container.Filter> filters = new ArrayList<Container.Filter>();
        filters.addAll(queryDefinition.getDefaultFilters());
        filters.addAll(queryDefinition.getFilters());

        final Object[] sortPropertyIds;
        final boolean[] sortPropertyAscendingStates;

        Container.Filter rootFilter;
        if (filters.size() > 0) {
            rootFilter = filters.remove(0);
        } else {
            rootFilter = null;
        }
        while (filters.size() > 0) {
            final Container.Filter filter = filters.remove(0);
            rootFilter = new And(rootFilter, filter);
        }

        if (rootFilter != null) {
            cq.where(setFilter(rootFilter, cb, cq, root));
        }
    }

    /**
     * Sets order clause of JPA 2.0 Criteria API query according to Vaadin sort states.
     * @param cb the CriteriaBuilder
     * @param cq the CriteriaQuery
     * @param root the root
     * @param <SE> the selected entity
     */
    private <SE> void setOrderClause(final CriteriaBuilder cb, final CriteriaQuery<SE> cq, final Root<E> root) {
        Object[] sortPropertyIds;
        boolean[] sortPropertyAscendingStates;

        if (queryDefinition.getSortPropertyIds().length == 0) {
            sortPropertyIds = queryDefinition.getDefaultSortPropertyIds();
            sortPropertyAscendingStates = queryDefinition.getDefaultSortPropertyAscendingStates();
        } else {
            sortPropertyIds = queryDefinition.getSortPropertyIds();
            sortPropertyAscendingStates = queryDefinition.getSortPropertyAscendingStates();
        }

        if (sortPropertyIds.length > 0) {
            final List<Order> orders = new ArrayList<Order>();
            for (int i = 0; i < sortPropertyIds.length; i++) {
                final Expression property = (Expression) getPropertyPath(root, sortPropertyIds[i]);
                if (sortPropertyAscendingStates[i]) {
                    orders.add(cb.asc(property));
                } else {
                    orders.add(cb.desc(property));
                }
            }
            cq.orderBy(orders);
        }
    }

    /**
     * Implements conversion of Vaadin filter to JPA 2.0 Criteria API based predicate.
     * Supports the following operations:
     *
     * And, Between, Compare, Compare.Equal, Compare.Greater, Compare.GreaterOrEqual,
     * Compare.Less, Compare.LessOrEqual, IsNull, Like, Not, Or, SimpleStringFilter
     *
     * @param filter the Vaadin filter
     * @param cb the CriteriaBuilder
     * @param cq the CriteriaQuery
     * @param root the root
     * @return the predicate
     */
    private Predicate setFilter(final Container.Filter filter, final CriteriaBuilder cb,
                                final CriteriaQuery<?> cq, final Root<?> root) {
        if (filter instanceof And) {
            final And and = (And) filter;
            final List<Container.Filter> filters = new ArrayList<Container.Filter>(and.getFilters());

            Predicate predicate = cb.and(setFilter(filters.remove(0), cb, cq, root),
                    setFilter(filters.remove(0), cb, cq, root));

            while (filters.size() > 0) {
                predicate = cb.and(predicate, setFilter(filters.remove(0), cb, cq, root));
            }

            return predicate;
        }

        if (filter instanceof Or) {
            final Or or = (Or) filter;
            final List<Container.Filter> filters = new ArrayList<Container.Filter>(or.getFilters());

            Predicate predicate = cb.or(setFilter(filters.remove(0), cb, cq, root),
                    setFilter(filters.remove(0), cb, cq, root));

            while (filters.size() > 0) {
                predicate = cb.or(predicate, setFilter(filters.remove(0), cb, cq, root));
            }

            return predicate;
        }

        if (filter instanceof Not) {
            final Not not = (Not) filter;
            return cb.not(setFilter(not.getFilter(), cb, cq, root));
        }

        if (filter instanceof Between) {
            final Between between = (Between) filter;
            final Expression property = (Expression) getPropertyPath(root, between.getPropertyId());
            return cb.between(property, (Comparable) between.getStartValue(), (Comparable) between.getEndValue());
        }

        if (filter instanceof Compare) {
            final Compare compare = (Compare) filter;
            final Expression<Comparable> property = (Expression) getPropertyPath(root, compare.getPropertyId());
            switch (compare.getOperation()) {
                case EQUAL:
                    return cb.equal(property, compare.getValue());
                case GREATER:
                    return cb.greaterThan(property, (Comparable) compare.getValue());
                case GREATER_OR_EQUAL:
                    return cb.greaterThanOrEqualTo(property, (Comparable) compare.getValue());
                case LESS:
                    return cb.lessThan(property, (Comparable) compare.getValue());
                case LESS_OR_EQUAL:
                    return cb.lessThanOrEqualTo(property, (Comparable) compare.getValue());
                default:
            }
        }

        if (filter instanceof IsNull) {
            final IsNull isNull = (IsNull) filter;
            return cb.isNull((Expression) getPropertyPath(root, isNull.getPropertyId()));
        }

        if (filter instanceof Like) {
            final Like like = (Like) filter;
            if (like.isCaseSensitive()) {
                return cb.like((Expression) getPropertyPath(root, like.getPropertyId()), like.getValue());
            } else {
                return cb.like(cb.lower((Expression) getPropertyPath(root, like.getPropertyId())),
                        like.getValue().toLowerCase());
            }
        }

       if (filter instanceof SimpleStringFilter) {
            final SimpleStringFilter simpleStringFilter = (SimpleStringFilter) filter;
            final Expression<String> property = (Expression) getPropertyPath(root, simpleStringFilter.getPropertyId());
            if (simpleStringFilter.isIgnoreCase()) {
                return cb.like(cb.lower(property), "%" + simpleStringFilter.getFilterString() + "%");
            } else {
                return cb.like(property, "%" + simpleStringFilter.getFilterString() + "%");
            }
        } 

        throw new UnsupportedOperationException("Vaadin filter: " + filter.getClass().getName() + " is not supported.");
    }

    /**
     * Gets property path.
     * @param root the root where path starts form
     * @param propertyId the property ID
     * @return the path to property
     */
    private Path<Object> getPropertyPath(final Root<?> root, final Object propertyId) {
        final String[] propertyIdParts = ((String) propertyId).split("\\.");

        Path<Object> path = null;
        for (final String part : propertyIdParts) {
            if (path == null) {
                path = root.get(part);
            } else {
                path = path.get(part);
            }
        }
        return path;
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
    public final void saveItems(final List<Item> addedItems, final List<Item> modifiedItems,
                                final List<Item> removedItems) {
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
    public final boolean deleteAllItems() {
        if (applicationTransactionManagement) {
            entityManager.getTransaction().begin();
        }
        try {
            final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            final CriteriaQuery<E> cq = cb.createQuery(entityClass);
            final Root<E> root = cq.from(entityClass);

            cq.select(root);

            setWhereCriteria(cb, cq, root);

            setOrderClause(cb, cq, root);

            final javax.persistence.TypedQuery<E> query = entityManager.createQuery(cq);

            final List<?> entities = query.getResultList();
            for (final Object entity : entities) {
                entityManager.remove(entity);
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
            final NestingBeanItem<?> beanItem = new NestingBeanItem<Object>(entity,
                    queryDefinition.getMaxNestedPropertyDepth(), queryDefinition.getPropertyIds());

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
            return new NestingBeanItem<Object>(entity,
                    queryDefinition.getMaxNestedPropertyDepth(), queryDefinition.getPropertyIds());
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

}

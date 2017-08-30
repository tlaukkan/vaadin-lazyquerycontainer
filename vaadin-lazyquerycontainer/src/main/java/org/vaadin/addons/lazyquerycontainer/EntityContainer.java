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

import com.vaadin.v7.data.util.BeanItem;

import javax.persistence.EntityManager;

/**
 * EntityContainer enables loading JPA entities in non lazy manner in single read operation
 * using same query backend as LCQ.
 *
 * @param <T> Entity class.
 * @author Tommi Laukkanen
 */
public final class EntityContainer<T> extends LazyQueryContainer {
    /**
     * Java serialization version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor which configures query definition for accessing JPA entities.
     *
     * @param entityManager                  The JPA EntityManager.
     * @param entityClass                    The entity class.
     * @param idPropertyId                   The ID of the ID property or null if item index is used as ID.
     * @param maximumQueryResultSize         The maximum size of the query result.
     * @param applicationManagedTransactions True if application manages transactions instead of container.
     * @param detachedEntities               True if entities are detached from PersistenceContext.
     * @param compositeItems                 True f items are wrapped to CompositeItems.
     */
    public EntityContainer(final EntityManager entityManager,
                           final Class<?> entityClass,
                           final Object idPropertyId,
                           final int maximumQueryResultSize,
                           final boolean applicationManagedTransactions,
                           final boolean detachedEntities, final boolean compositeItems) {
        super(new EntityQueryDefinition(applicationManagedTransactions,
                detachedEntities, compositeItems,
                entityClass, maximumQueryResultSize, idPropertyId),
                new EntityQueryFactory(entityManager));
        getQueryView().getQueryDefinition().setMaxQuerySize(maximumQueryResultSize);
    }

    /**
     * Constructor which configures query definition for accessing JPA entities.
     * @param entityManager The JPA EntityManager.
     * @param applicationManagedTransactions True if application manages
     *            transactions instead of container.
     * @param detachedEntities True if entities are detached from
     *            PersistenceContext.
     *            items until commit.
     * @param compositeItems True if native items should be wrapped to
     *            CompositeItems.
     * @param entityClass The entity class.
     * @param maximumQueryResultSize Maximum number of items in the container.
     * @param nativeSortPropertyIds Properties participating in the native sort.
     * @param nativeSortPropertyAscendingStates List of property sort directions
     *            for the native sort.
     * @param idPropertyId Property containing the property ID.
     */
    public EntityContainer(final EntityManager entityManager, final boolean applicationManagedTransactions,
                                        final boolean detachedEntities, final boolean compositeItems,
                                        final Class<?> entityClass, final int maximumQueryResultSize,
                                        final Object[] nativeSortPropertyIds, final boolean[]
            nativeSortPropertyAscendingStates,
                                        final Object idPropertyId) {
        super(new EntityQueryDefinition(applicationManagedTransactions,
                detachedEntities, compositeItems,
                entityClass, maximumQueryResultSize, idPropertyId),
                new EntityQueryFactory(entityManager));
        getQueryView().getQueryDefinition().setMaxQuerySize(maximumQueryResultSize);
        getQueryView().getQueryDefinition().setDefaultSortState(nativeSortPropertyIds,
                nativeSortPropertyAscendingStates);
    }

    /**
     * Adds entity to the container as first item i.e. at index 0.
     *
     * @return the new constructed entity.
     */
    public T addEntity() {
        final Object itemId = addItem();
        return getEntity(indexOfId(itemId));
    }

    /**
     * Removes given entity at given index and returns it.
     *
     * @param index Index of the entity to be removed.
     * @return The removed entity.
     */
    public T removeEntity(final int index) {
        final T entityToRemove = getEntity(index);
        removeItem(getIdByIndex(index));
        return entityToRemove;
    }

    /**
     * Gets entity by ID.
     *
     * @param id The ID of the entity.
     * @return the entity.
     */
    @SuppressWarnings("unchecked")
    public T getEntity(final Object id) {
        return getEntity(indexOfId(id));
    }

    /**
     * Gets entity at given index.
     *
     * @param index The index of the entity.
     * @return the entity.
     */
    @SuppressWarnings("unchecked")
    public T getEntity(final int index) {
        if (getQueryView().getQueryDefinition().isCompositeItems()) {
            final CompositeItem compositeItem = (CompositeItem) getItem(getIdByIndex(index));
            final BeanItem<T> beanItem = (BeanItem<T>) compositeItem.getItem("bean");
            return beanItem.getBean();
        } else {
            return ((BeanItem<T>) getItem(getIdByIndex(index))).getBean();
        }
    }
}

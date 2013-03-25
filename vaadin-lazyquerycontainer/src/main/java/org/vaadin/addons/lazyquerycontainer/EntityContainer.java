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

import com.vaadin.data.util.BeanItem;

import javax.persistence.EntityManager;
/**
 * EntityContainer enables using JPA entities with lazy batch loading, filter, sort
 * and buffered writes.
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
     * @param batchSize                      The batch size.
     * @param applicationManagedTransactions True if application manages transactions instead of container.
     * @param detachedEntities               True if entities are detached from PersistenceContext.
     * @param compositeItems                 True f items are wrapped to CompositeItems.
     */
    public EntityContainer(final EntityManager entityManager,
                           final Class<?> entityClass, final int batchSize, final Object idPropertyId,
                           final boolean applicationManagedTransactions,
                           final boolean detachedEntities, final boolean compositeItems) {
        super(new EntityQueryDefinition(applicationManagedTransactions,
                detachedEntities, compositeItems,
                entityClass, batchSize, idPropertyId),
                new EntityQueryFactory(entityManager));
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

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

import javax.persistence.EntityManager;
/**
 * LazyEntityContainer enables using JPA entities with lazy batch loading, filter, sort
 * and buffered writes.
 *
 * @param <T> Entity class.
 * @author Tommi Laukkanen
 */
public final class LazyEntityContainer<T> extends AbstractEntityContainer<T> {
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
    public LazyEntityContainer(final EntityManager entityManager,
                               final Class<?> entityClass, final int batchSize, final Object idPropertyId,
                               final boolean applicationManagedTransactions,
                               final boolean detachedEntities, final boolean compositeItems) {
        super(new EntityQueryDefinition(applicationManagedTransactions,
                detachedEntities, compositeItems,
                entityClass, batchSize, idPropertyId),
                new EntityQueryFactory(entityManager));
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
    public LazyEntityContainer(final EntityManager entityManager, final boolean applicationManagedTransactions,
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

}

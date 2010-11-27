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

import java.util.Map;

import javax.persistence.EntityManager;

import com.vaadin.data.util.BeanItem;

/**
 * EntityContainer enables using JPA entities with lazy batch loading, filter, sort
 * and buffered writes.
 * @param <T> Entity class.
 * @author Tommi Laukkanen
 */
public final class EntityContainer<T extends Object> extends LazyQueryContainer {
    /** Java serialization version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor which configures query definition for accessing JPA entities.
     * @param entityManager The JPA EntityManager.
     * @param applicationManagedTransactions True if application manages transactions instead of container.
     * @param entityClass The entity class.
     * @param batchSize The batch size.
     * @param nativeSortPropertyIds Properties participating in the native sort.
     * @param nativeSortPropertyAscendingStates List of property sort directions for the native sort.
     */
    public EntityContainer(final EntityManager entityManager, final boolean applicationManagedTransactions,
            final Class<?> entityClass, final int batchSize,
            final Object[] nativeSortPropertyIds, final boolean[] nativeSortPropertyAscendingStates) {
        super(new EntityQueryDefinition(entityManager, applicationManagedTransactions,
                entityClass, batchSize, nativeSortPropertyIds, nativeSortPropertyAscendingStates),
                new EntityQueryFactory());
    }
    /**
     * Filters the container content by setting JPQL where criteria.  The entity expression
     * in generated JPQL queries is "e". Where keyword is not to be included. Refresh of container
     * is automatically invoked after this method is called.
     * Example:
     * whereCriteria = "beginDate<=:beginDate";
     * whereParameters.put("e.beginDate", new Date());
     * @param whereCriteria the where criteria to be included in JPA query or null to clear.
     * @param whereParameters the where parameters to set to JPA query or null to clear.
     */
    public void filter(final String whereCriteria, final Map<String, Object> whereParameters) {
        ((EntityQueryDefinition) getQueryView().getQueryDefinition()).setWhereCriteria(whereCriteria, whereParameters);
        refresh();
    }

    /**
     * Adds entity to the container as first item i.e. at index 0.
     * @return the new constructed entity.
     */
    public T addEntity() {
        final Object itemId = addItem();
        return getEntity((Integer) itemId);
    }

    /**
     * Removes given entity at given index and returns it.
     * @param index Index of the entity to be removed.
     * @return The removed entity.
     */
    public T removeEntity(final int index) {
        final T entityToRemove = getEntity(index);
        removeItem(new Integer(index));
        return entityToRemove;
    }
    
    /**
     * Gets entity at given index.
     * @param index The index of the entity.
     * @return the entity.
     */
    @SuppressWarnings("unchecked")
    public T getEntity(final int index) {
        final CompositeItem compositeItem = (CompositeItem) getItem(new Integer(index));
        final BeanItem<T> beanItem = (BeanItem<T>) compositeItem.getItem("bean");
        return beanItem.getBean();
    }

}

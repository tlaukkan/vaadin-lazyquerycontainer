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

import java.util.Collection;
import java.util.List;

/**
 * Interface for defining properties for a query.
 *
 * @author Tommi S.E. Laukkanen
 */
public interface QueryDefinition {

    /**
     * Set the ID of the ID property or null if item index in result set is used as ID.
     * @param idPropertyId The ID of the ID property or null if item index in result set is used as ID.
     */
    void setIdPropertyId(Object idPropertyId);

    /**
     * Gets the  ID of the ID property or null if item index in result set is used as ID.
     * @return The ID of the ID property or null if item index in result set is used as ID.
     */
    Object getIdPropertyId();

    /**
     * Lists of the property IDs queried.
     *
     * @return A list of property IDs queried.
     */
    Collection<Object> getPropertyIds();

    /**
     * List of the property IDs which can be sorted.
     *
     * @return A list of the property IDs which can be sorted.
     */
    Collection<Object> getSortablePropertyIds();

    /**
     * Gets the property value class of the given property.
     *
     * @param propertyId If of the property of interest.
     * @return The value class of the given property.
     */
    Class<?> getPropertyType(Object propertyId);

    /**
     * Gets the default value of the given property.
     *
     * @param propertyId If of the property of interest.
     * @return The default value of the given property.
     */
    Object getPropertyDefaultValue(Object propertyId);

    /**
     * Returns true if the given property is read only.
     *
     * @param propertyId If of the property of interest.
     * @return True if the given property is read only.
     */
    boolean isPropertyReadOnly(Object propertyId);

    /**
     * Returns true if the given property is sortable.
     *
     * @param propertyId If of the property of interest.
     * @return True if the given property is sortable.
     */
    boolean isPropertySortable(Object propertyId);

    /**
     * Adds a new property to the definition.
     *
     * @param propertyId   Id of the property.
     * @param type         Value class of the property.
     * @param defaultValue Default value of the property.
     * @param readOnly     Read only state of the property.
     * @param sortable     Sortable state of the property.
     */
    void addProperty(Object propertyId, Class<?> type, Object defaultValue, boolean readOnly, boolean sortable);

    /**
     * Removes the given property from the definition.
     *
     * @param propertyId If of the property to be removed.
     */
    void removeProperty(Object propertyId);

    /**
     * Gets the batch size.
     *
     * @return the batch size
     */
    int getBatchSize();

    /**
     * Sets the query batch size.
     * After this method has been called the Query has to be discarded immediately.
     *
     * @param batchSize the batchSize to set
     */
    void setBatchSize(final int batchSize);

    /**
     * True if query wraps items to CompositeItems.
     *
     * @return the compositeItems
     */
    boolean isCompositeItems();

    /**
     * Sets whether query wraps items to CompositeItems.
     * After this method has been called the Query has to be discarded immediately.
     *
     * @param compositeItems the compositeItems to set
     */
    void setCompositeItems(final boolean compositeItems);

    /**
     * Adds default filter to container.
     * @param filter the default filter to add
     */
    void addDefaultFilter(final Container.Filter filter);

    /**
     * Removes default filter to container.
     * @param filter the default filter to add
     */
    void removeDefaultFilter(final Container.Filter filter);

    /**
     * Clears default filters from container.
     */
    void removeDefaultFilters();

    /**
     * Gets default filters.
     * @return the default filters.
     */
    List<Container.Filter> getDefaultFilters();

    /**
     * Adds filter to container.
     * @param filter the default filter to add
     */
    void addFilter(final Container.Filter filter);

    /**
     * Removes filter to container.
     * @param filter the default filter to add
     */
    void removeFilter(final Container.Filter filter);

    /**
     * Clears filters from container.
     */
    void removeFilters();

    /**
     * Gets filters.
     * @return the filters.
     */
    List<Container.Filter> getFilters();

    /**
     * Gets the default sort property IDs.
     * @return the default sort property IDs
     */
    Object[] getDefaultSortPropertyIds();

    /**
     * Sets the default sort propertyIDs.
     * @param defaultSortPropertyIds the default sort property IDs
     */
    void setDefaultSortPropertyIds(final Object[] defaultSortPropertyIds);

    /**
     * Gets default sort property ascending states.
     * @return the default sort property ascending states
     */
    boolean[] getDefaultSortPropertyAscendingStates();

    /**
     * Sets default sort property ascending states.
     * @param defaultSortPropertyAscendingStates the default sort property ascending states.
     */
    void setDefaultSortPropertyAscendingStates(final boolean[] defaultSortPropertyAscendingStates);

    /**
     * Gets sort property IDs.
     * @return the sort property IDs
     */
    Object[] getSortPropertyIds();

    /**
     * Sets sort property IDs.
     * @param sortPropertyIds the sort property IDs
     */
    void setSortPropertyIds(final Object[] sortPropertyIds);

    /**
     * Gets sort property ascending states.
     * @return the sort property ascending states
     */
    boolean[] getSortPropertyAscendingStates();

    /**
     * Sets sort property ascending states.
     * @param sortPropertyAscendingStates the sort property ascending states.
     */
    void setSortPropertyAscendingStates(final boolean[] sortPropertyAscendingStates);

    /**
     * Sets the default sort state.
     *
     * @param sortPropertyIds             Properties participating in the sorting.
     * @param sortPropertyAscendingStates List of sort direction for the properties.
     */
    void setDefaultSortState(final Object[] sortPropertyIds, final boolean[] sortPropertyAscendingStates);

    /**
     * Sets the sort state.
     *
     * @param sortPropertyIds             Properties participating in the sorting.
     * @param sortPropertyAscendingStates List of sort direction for the properties.
     */
    void setSortState(final Object[] sortPropertyIds, final boolean[] sortPropertyAscendingStates);

    /**
     * Gets the max query size.
     *
     * @return the max query size
     */
    int getMaxQuerySize();

    /**
     * Sets the max query size.
     *
     * @param maxQuerySize the max query size
     */
    void setMaxQuerySize(final int maxQuerySize);

    /**
     * Sets the maxNestedPropertyDepth
     *
     * @return maxNestedPropertyDepth
     */
    int getMaxNestedPropertyDepth();

    /**
     * Gets the maxNestedPropertyDepth
     *
     * @param maxNestedPropertyDepth maxNestedPropertyDepth
     */
    void setMaxNestedPropertyDepth(int maxNestedPropertyDepth);
}

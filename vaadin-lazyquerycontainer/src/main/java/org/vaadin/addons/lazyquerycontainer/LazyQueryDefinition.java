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

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * Default implementation of Query Definition. Contains the property, filtering and sorting information
 * of query.
 *
 * @author Tommi S.E. Laukkanen
 */
public class LazyQueryDefinition implements QueryDefinition, Serializable {
    /**
     * Java serialization version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Lust of property IDs included in this QueryDefinition.
     */
    private final List<Object> propertyIds = new ArrayList<Object>();
    /**
     * Map of types of the properties.
     */
    private final Map<Object, Object> propertyTypes = new HashMap<Object, Object>();
    /**
     * Default values for the properties.
     */
    private final Map<Object, Object> defaultValues = new HashMap<Object, Object>();
    /**
     * Flags reflecting whether the properties are read only.
     */
    private final Map<Object, Boolean> readOnlyStates = new HashMap<Object, Boolean>();
    /**
     * Sort states of the properties.
     */
    private final Map<Object, Boolean> sortableStates = new HashMap<Object, Boolean>();
    /**
     * List of default filters.
     */
    private final List<Container.Filter> defaultFilters = new ArrayList<Container.Filter>();
    /**
     * List of default filters.
     */
    private final List<Container.Filter> filters = new ArrayList<Container.Filter>();
    /**
     * The sort property IDs.
     */
    private Object[] defaultSortPropertyIds = new Object[0];
    /**
     * The sort ascending and descending states.
     */
    private boolean[] defaultSortPropertyAscendingStates = new boolean[0];
    /**
     * The sort property IDs.
     */
    private Object[] sortPropertyIds = new Object[0];
    /**
     * The sort ascending and descending states.
     */
    private boolean[] sortPropertyAscendingStates = new boolean[0];
    /**
     * Batch size of the query.
     */
    private int batchSize;
    /**
     * True if native items should be wrapped to CompositeItems.
     */
    private boolean compositeItems;
    /**
     * The ID of the ID property or null if item index in result set is used as ID.
     */
    private Object idPropertyId;
    /**
     * The query max size.
     */
    private int maxQuerySize = -1;
    /**
     * The max depth of nested properties.
     */
    private int maxNestedPropertyDepth = 0;

    /**
     * Constructor which sets the batch size.
     *
     * @param compositeItems True if native items should be wrapped to
     *                       CompositeItems.
     * @param batchSize      Value for batch size.
     * @param idPropertyId   The ID of the ID property or null if item index in result set is used as ID.
     */
    public LazyQueryDefinition(final boolean compositeItems, final int batchSize, final Object idPropertyId) {
        this.compositeItems = compositeItems;
        this.batchSize = batchSize;
        this.idPropertyId = idPropertyId;
    }

    /**
     * @return the propertyIds
     */
    @Override
    public final Collection<Object> getPropertyIds() {
        return Collections.unmodifiableCollection(propertyIds);
    }

    /**
     * List of sortable property IDs.
     *
     * @return the sortablePropertyIds
     */
    @Override
    public final Collection<Object> getSortablePropertyIds() {
        final List<Object> sortablePropertyIds = new ArrayList<Object>();
        for (final Object propertyId : propertyIds) {
            if (isPropertySortable(propertyId)) {
                sortablePropertyIds.add(propertyId);
            }
        }
        return sortablePropertyIds;
    }

    /**
     * Gets the default value for a given property.
     *
     * @param propertyId ID identifying the property.
     * @return the default value to be used or null.
     */
    @Override
    public final Object getPropertyDefaultValue(final Object propertyId) {
        return defaultValues.get(propertyId);
    }

    /**
     * Gets the type for a given property.
     *
     * @param propertyId ID identifying the property.
     * @return the type of the property.
     */
    @Override
    public final Class<?> getPropertyType(final Object propertyId) {
        return (Class<?>) propertyTypes.get(propertyId);
    }

    /**
     * Checks whether given property is read only.
     *
     * @param propertyId ID identifying the property.
     * @return true if property is read only.
     */
    @Override
    public final boolean isPropertyReadOnly(final Object propertyId) {
        return readOnlyStates.get(propertyId);
    }

    /**
     * Checks whether property is sortable.
     *
     * @param propertyId ID identifying the property.
     * @return true if property is sortable.
     */
    @Override
    public final boolean isPropertySortable(final Object propertyId) {
        return sortableStates.get(propertyId);
    }

    /**
     * Adds property.
     *
     * @param propertyId   ID of the property.
     * @param type         Type of the property.
     * @param defaultValue Default value of the property.
     * @param readOnly     True if property is read only.
     * @param sortable     True if property is sortable.
     */
    @Override
    public final void addProperty(final Object propertyId, final Class<?> type, final Object defaultValue,
                                  final boolean readOnly, final boolean sortable) {
        propertyIds.add(propertyId);
        propertyTypes.put(propertyId, type);
        defaultValues.put(propertyId, defaultValue);
        readOnlyStates.put(propertyId, readOnly);
        sortableStates.put(propertyId, sortable);
    }

    /**
     * Removes property.
     *
     * @param propertyId ID identifying the property.
     */
    @Override
    public final void removeProperty(final Object propertyId) {
        propertyIds.remove(propertyId);
        propertyTypes.remove(propertyId);
        defaultValues.remove(propertyId);
        readOnlyStates.remove(propertyId);
        sortableStates.remove(propertyId);
    }

    /**
     * @return the compositeItems
     */
    @Override
    public final boolean isCompositeItems() {
        return compositeItems;
    }

    /**
     * @param compositeItems the compositeItems to set
     */
    @Override
    public final void setCompositeItems(final boolean compositeItems) {
        this.compositeItems = compositeItems;
    }

    /**
     * @return the batchSize.
     */
    @Override
    public final int getBatchSize() {
        return batchSize;
    }

    /**
     * After this method has been called the Query has to be discarded
     * immediately.
     *
     * @param batchSize the batchSize to set
     */
    @Override
    public final void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Gets the  ID of the ID property or null if item index in result set is used as ID.
     * @return The ID of the ID property or null if item index in result set is used as ID.
     */
    @Override
    public final Object getIdPropertyId() {
        return idPropertyId;
    }

    /**
     * Set the ID of the ID property or null if item index in result set is used as ID.
     * @param idPropertyId The ID of the ID property or null if item index in result set is used as ID.
     */
    @Override
    public final void setIdPropertyId(final Object idPropertyId) {
        this.idPropertyId = idPropertyId;
    }

    /**
     * Adds default filter to container.
     * @param filter the default filter to add
     */
    public final void addDefaultFilter(final Container.Filter filter) {
        defaultFilters.add(filter);
    }

    /**
     * Removes default filter to container.
     * @param filter the default filter to add
     */
    public final void removeDefaultFilter(final Container.Filter filter) {
        defaultFilters.remove(filter);
    }

    /**
     * Clears default filters from container.
     */
    public final void removeDefaultFilters() {
        defaultFilters.clear();
    }

    /**
     * Gets default filters.
     * @return the default filters.
     */
    public final List<Container.Filter> getDefaultFilters() {
        return defaultFilters;
    }

    /**
     * Adds filter to container.
     * @param filter the default filter to add
     */
    public final void addFilter(final Container.Filter filter) {
        filters.add(filter);
    }

    /**
     * Removes filter to container.
     * @param filter the default filter to add
     */
    public final void removeFilter(final Container.Filter filter) {
        filters.remove(filter);
    }

    /**
     * Clears filters from container.
     */
    public final void removeFilters() {
        filters.clear();
    }

    /**
     * Gets filters.
     * @return the filters.
     */
    public final List<Container.Filter> getFilters() {
        return filters;
    }

    /**
     * Gets the default sort property IDs.
     * @return the default sort property IDs
     */
    public final Object[] getDefaultSortPropertyIds() {
        return defaultSortPropertyIds;
    }

    /**
     * Sets the default sort propertyIDs.
     * @param defaultSortPropertyIds the default sort property IDs
     */
    public final void setDefaultSortPropertyIds(final Object[] defaultSortPropertyIds) {
        this.defaultSortPropertyIds = defaultSortPropertyIds;
    }

    /**
     * Gets default sort property ascending states.
     * @return the default sort property ascending states
     */
    public final boolean[] getDefaultSortPropertyAscendingStates() {
        return defaultSortPropertyAscendingStates;
    }

    /**
     * Sets default sort property ascending states.
     * @param defaultSortPropertyAscendingStates the default sort property ascending states.
     */
    public final void setDefaultSortPropertyAscendingStates(final boolean[] defaultSortPropertyAscendingStates) {
        this.defaultSortPropertyAscendingStates = defaultSortPropertyAscendingStates;
    }

    /**
     * Gets sort property IDs.
     * @return the sort property IDs
     */
    public final Object[] getSortPropertyIds() {
        return sortPropertyIds;
    }

    /**
     * Sets sort property IDs.
     * @param sortPropertyIds the sort property IDs
     */
    public final void setSortPropertyIds(final Object[] sortPropertyIds) {
        this.sortPropertyIds = sortPropertyIds;
    }

    /**
     * Gets sort property ascending states.
     * @return the sort property ascending states
     */
    public final boolean[] getSortPropertyAscendingStates() {
        return sortPropertyAscendingStates;
    }

    /**
     * Sets sort property ascending states.
     * @param sortPropertyAscendingStates the sort property ascending states.
     */
    public final void setSortPropertyAscendingStates(final boolean[] sortPropertyAscendingStates) {
        this.sortPropertyAscendingStates = sortPropertyAscendingStates;
    }

    /**
     * Sets the sort state.
     *
     * @param sortPropertyIds             Properties participating in the sorting.
     * @param sortPropertyAscendingStates List of sort direction for the properties.
     */
    public final void setDefaultSortState(final Object[] sortPropertyIds, final boolean[] sortPropertyAscendingStates) {
        setDefaultSortPropertyIds(sortPropertyIds);
        setDefaultSortPropertyAscendingStates(sortPropertyAscendingStates);
        if (sortPropertyIds.length != sortPropertyAscendingStates.length) {
            throw new InvalidParameterException("Sort state arrays need to have same length.");
        }
    }

    /**
     * Sets the sort state.
     *
     * @param sortPropertyIds             Properties participating in the sorting.
     * @param sortPropertyAscendingStates List of sort direction for the properties.
     */
    public final void setSortState(final Object[] sortPropertyIds, final boolean[] sortPropertyAscendingStates) {
        setSortPropertyIds(sortPropertyIds);
        setSortPropertyAscendingStates(sortPropertyAscendingStates);
        if (sortPropertyIds.length != sortPropertyAscendingStates.length) {
            throw new InvalidParameterException("Sort state arrays need to have same length.");
        }
    }

    /**
     * Gets the max query size.
     * @return the max query size
     */
    @Override
    public final int getMaxQuerySize() {
        return maxQuerySize;
    }

    /**
     * Sets the max query size.
     * @param maxQuerySize the max query size
     */
    @Override
    public final void setMaxQuerySize(final int maxQuerySize) {
        this.maxQuerySize = maxQuerySize;
    }

    /**
     * Sets the maxNestedPropertyDepth
     *
     * @return maxNestedPropertyDepth
     */
    @Override
    public final int getMaxNestedPropertyDepth() {
        return maxNestedPropertyDepth;
    }

    /**
     * Gets the maxNestedPropertyDepth
     *
     * @param maxNestedPropertyDepth maxNestedPropertyDepth
     */
    @Override
    public final void setMaxNestedPropertyDepth(final int maxNestedPropertyDepth) {
        this.maxNestedPropertyDepth = maxNestedPropertyDepth;
    }
}

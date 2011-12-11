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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of Query Definition. Stores the property information
 * of query to simple Map structure.
 * 
 * @author Tommi S.E. Laukkanen
 */
public class LazyQueryDefinition implements QueryDefinition, Serializable {
    /** Java serialization version UID. */
    private static final long serialVersionUID = 1L;
    /** Lust of property IDs included in this QueryDefinition. */
    private final List<Object> propertyIds = new ArrayList<Object>();
    /** Map of types of the properties. */
    private final Map<Object, Object> propertyTypes = new HashMap<Object, Object>();
    /** Default values for the properties. */
    private final Map<Object, Object> defaultValues = new HashMap<Object, Object>();
    /** Flags reflecting whether the properties are read only. */
    private final Map<Object, Boolean> readOnlyStates = new HashMap<Object, Boolean>();
    /** The sort states of the properties. */
    private final Map<Object, Boolean> sortableStates = new HashMap<Object, Boolean>();
    /** Batch size of the query. */
    private int batchSize;
    /** True if native items should be wrapped to CompositeItems. */
    private boolean compositeItems;

    /**
     * Constructor which sets the batch size.
     * @param compositeItems True if native items should be wrapped to
     *            CompositeItems.
     * @param batchSize Value for batch size.
     */
    public LazyQueryDefinition(final boolean compositeItems, final int batchSize) {
        this.compositeItems = compositeItems;
        this.batchSize = batchSize;
    }

    /**
     * @return the propertyIds
     */
    @Override
    public final Collection<?> getPropertyIds() {
        return Collections.unmodifiableCollection(propertyIds);
    }

    /**
     * List of sortable property IDs.
     * @return the sortablePropertyIds
     */
    @Override
    public final Collection<?> getSortablePropertyIds() {
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
     * @param propertyId ID identifying the property.
     * @return the default value to be used or null.
     */
    @Override
    public final Object getPropertyDefaultValue(final Object propertyId) {
        return defaultValues.get(propertyId);
    }

    /**
     * Gets the type for a given property.
     * @param propertyId ID identifying the property.
     * @return the type of the property.
     */
    @Override
    public final Class<?> getPropertyType(final Object propertyId) {
        return (Class<?>) propertyTypes.get(propertyId);
    }

    /**
     * Checks whether given property is read only.
     * @param propertyId ID identifying the property.
     * @return true if property is read only.
     */
    @Override
    public final boolean isPropertyReadOnly(final Object propertyId) {
        return readOnlyStates.get(propertyId);
    }

    /**
     * Checks whether property is sortable.
     * @param propertyId ID identifying the property.
     * @return true if property is sortable.
     */
    @Override
    public final boolean isPropertySortable(final Object propertyId) {
        return sortableStates.get(propertyId);
    }

    /**
     * Adds property.
     * @param propertyId ID of the property.
     * @param type Type of the property.
     * @param defaultValue Default value of the property.
     * @param readOnly True if property is read only.
     * @param sortable True if property is sortable.
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
     * @param batchSize the batchSize to set
     */
    @Override
    public final void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

}

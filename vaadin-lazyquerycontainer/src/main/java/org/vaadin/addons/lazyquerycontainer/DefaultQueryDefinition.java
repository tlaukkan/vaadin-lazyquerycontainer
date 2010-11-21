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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Default implementation of Query Definition. Stores the property information
 * of query to simple Map structure.
 * 
 * @author Tommi S.E. Laukkanen
 */
public final class DefaultQueryDefinition implements QueryDefinition {
    /** Lust of property IDs included in this QueryDefinition. */
    private List<Object> propertIds = new ArrayList<Object>();
    /** Map of types of the properties. */
    private Map<Object, Object> propertyTypes = new TreeMap<Object, Object>();
    /** Default values for the properties. */
    private Map<Object, Object> defaultValues = new HashMap<Object, Object>();
    /** Flags reflecting whether the properties are read only. */
    private Map<Object, Boolean> readOnlyStates = new HashMap<Object, Boolean>();
    /** The sort states of the properties. */
    private Map<Object, Boolean> sortableStates = new HashMap<Object, Boolean>();
    /** Batch size of the query. */
    private final int batchSize;

    /**
     * Constructor which sets the batch size.
     * @param batchSize Value for batch size.
     */
    public DefaultQueryDefinition(final int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * @return the propertyIds
     */
    public Collection<?> getPropertyIds() {
        return Collections.unmodifiableCollection(propertIds);
    }

    /**
     * List of sortable property IDs.
     * @return the sortablePropertyIds
     */
    public Collection<?> getSortablePropertyIds() {
        List<Object> sortablePropertyIds = new ArrayList<Object>();
        for (Object propertyId : propertIds) {
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
    public Object getPropertyDefaultValue(final Object propertyId) {
        return defaultValues.get(propertyId);
    }

    /**
     * Gets the type for a given property.
     * @param propertyId ID identifying the property.
     * @return the type of the property.
     */
    public Class<?> getPropertyType(final Object propertyId) {
        return (Class<?>) propertyTypes.get(propertyId);
    }

    /**
     * Checks whether given property is read only.
     * @param propertyId ID identifying the property.
     * @return true if property is read only.
     */
    public boolean isPropertyReadOnly(final Object propertyId) {
        return readOnlyStates.get(propertyId);
    }

    /**
     * Checks whether property is sortable.
     * @param propertyId ID identifying the property.
     * @return true if property is sortable.
     */
    public boolean isPropertySortable(final Object propertyId) {
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
    public void addProperty(final Object propertyId, final Class<?> type, final Object defaultValue,
            final boolean readOnly, final boolean sortable) {
        propertIds.add(propertyId);
        propertyTypes.put(propertyId, type);
        defaultValues.put(propertyId, defaultValue);
        readOnlyStates.put(propertyId, readOnly);
        sortableStates.put(propertyId, sortable);
    }

    /**
     * Removes property.
     * @param propertyId ID identifying the property.
     */
    public void removeProperty(final Object propertyId) {
        propertIds.remove(propertyId);
        propertyTypes.remove(propertyId);
        defaultValues.remove(propertyId);
        readOnlyStates.remove(propertyId);
        sortableStates.remove(propertyId);
    }

    /**
     * @return the batchSize.
     */
    public int getBatchSize() {
        return batchSize;
    }

}

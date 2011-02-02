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

import java.util.Collection;

/**
 * Interface for defining properties for a query.
 * @author Tommi S.E. Laukkanen
 */
public interface QueryDefinition {
	/**
	 * Lists of the property IDs queried.
	 * @return A list of property IDs queried.
	 */
	Collection<?> getPropertyIds();
	/**
	 * List of the property IDs which can be sorted.
	 * @return A list of the property IDs which can be sorted.
	 */
	Collection<?> getSortablePropertyIds();
	/**
	 * Gets the property value class of the given property.
	 * @param propertyId If of the property of interest.
	 * @return The value class of the given property.
	 */
	Class<?> getPropertyType(Object propertyId);
	/**
	 * Gets the default value of the given property.
	 * @param propertyId If of the property of interest.
	 * @return The default value of the given property.
	 */
	Object getPropertyDefaultValue(Object propertyId);
	/**
	 * Returns true if the given property is read only.
	 * @param propertyId If of the property of interest.
	 * @return True if the given property is read only.
	 */
	boolean isPropertyReadOnly(Object propertyId);
	/**
	 * Returns true if the given property is sortable.
	 * @param propertyId If of the property of interest.
	 * @return True if the given property is sortable.
	 */
	boolean isPropertySortable(Object propertyId);
	/**
	 * Adds a new property to the definition.
	 * @param propertyId Id of the property.
	 * @param type Value class of the property.
	 * @param defaultValue Default value of the property.
	 * @param readOnly Read only state of the property.
	 * @param sortable Sortable state of the property.
	 */
	void addProperty(Object propertyId, Class<?> type, Object defaultValue, boolean readOnly, boolean sortable);
	/**
	 * Removes the given property from the definition.
	 * @param propertyId If of the property to be removed.
	 */
	void removeProperty(Object propertyId);	
	/**
	 * Gets the batch size.
	 * @return the batch size
	 */
	int getBatchSize();
    /**
     * Sets the query batch size.
     * After this method has been called the Query has to be discarded immediately.
     * @param batchSize the batchSize to set
     */
    void setBatchSize(final int batchSize);
    /**
     * True if query wraps items to CompositeItems.
     * @return the compositeItems
     */
    boolean isCompositeItems();
    /**
     * Sets whether query wraps items to CompositeItems.
     * After this method has been called the Query has to be discarded immediately.
     * @param compositeItems the compositeItems to set
     */
    void setCompositeItems(final boolean compositeItems);
}

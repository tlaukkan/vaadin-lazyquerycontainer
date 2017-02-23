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

import java.util.Collection;
import java.util.List;

/**
 * Interface for sorting and browsing data from a business service.
 *
 * @author Tommi S.E. Laukkanen
 */
public interface QueryView {
    /**
     * Returns the definition of properties provided by this view.
     *
     * @return the query definition
     */
    QueryDefinition getQueryDefinition();

    /**
     * Gets list of item IDs present in this view.
     * @return list of item IDs present in this view.
     */
    List<?> getItemIdList();

    /**
     * @return the maxCacheSize
     */
    int getMaxCacheSize();

    /**
     * @param maxCacheSize the maxCacheSize to set
     */
    void setMaxCacheSize(int maxCacheSize);

    /**
     * Sorts the items according to the provided sort state.
     *
     * @param sortPropertyIds Properties participating in the sorting.
     * @param ascendingStates List of sort order for the properties.
     */
    void sort(Object[] sortPropertyIds, boolean[] ascendingStates);

    /**
     * Refreshes data from business service and notifies listeners of changed
     * item set.
     */
    void refresh();

    /**
     * Returns the number of items currently available through the view.
     *
     * @return Number of items available.
     */
    int size();

    /**
     * Gets item at the given index.
     *
     * @param index The index of the item.
     * @return The item identified by the index.
     */
    Item getItem(int index);

    /**
     * Adds a new item to the end of the query result set.
     *
     * @return The index of the new item.
     */
    int addItem();

    /**
     * Removes item at given index.
     *
     * @param index Index of the Item to be removed.
     */
    void removeItem(int index);

    /**
     * Removes all items.
     */
    void removeAllItems();

    /**
     * Check if query view contains modifications.
     *
     * @return true if query has been modified.
     */
    boolean isModified();

    /**
     * Saves changes. Refresh has to be invoked after this method to clear the cache.
     */
    void commit();

    /**
     * Cancels changes. Refresh has to be invoked after this method to clear the cache.
     */
    void discard();

    /**
     * Get list of added buffered items.
     *
     * @return list of added buffered items
     */
    List<Item> getAddedItems();

    /**
     * Get list of modified buffered items.
     *
     * @return list of modified buffered items
     */
    List<Item> getModifiedItems();

    /**
     * Get list of removed buffered items.
     *
     * @return list of removed buffered items
     */
    List<Item> getRemovedItems();

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
     * @return collection of filters.
     */
    Collection<Container.Filter> getFilters();
}

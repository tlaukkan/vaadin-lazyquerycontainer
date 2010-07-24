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

import com.vaadin.data.Item;

/**
 * Interface for sorting and browsing data from a business service.
 * @author Tommi S.E. Laukkanen
 */
public interface QueryView {
	/**
	 * Returns the definition of properties provided by this view.
	 * @return the query definition
	 */
	public QueryDefinition getDefinition();
	/**
	 * Sorts the items according to the provided sort state.
	 * @param sortPropertyIds Properties participating in the sorting.
	 * @param ascendingStates List of sort order for the properties.
	 */
	public void sort(Object[] sortPropertyIds, boolean[] ascendingStates);
	/**
	 * Refreshes data from business service and notifies listeners of changed
	 * item set.
	 */
	public void refresh();
	/**
	 * Returns the number of items currently available through the view.
	 * @return Number of items available.
	 */
	public int size();
	/**
	 * Gets item at the given index.
	 * @param index The index of the item.
	 * @return The item identified by the index.
	 */
	public Item getItem(int index);
	/**
	 * Adds a new item to the end of the query result set.
	 * @return The index of the new item.
	 */
	public int addItem();
	/**
	 * Removes item at given index.
	 * @param index Index of the Item to be removed.
	 */
	public void removeItem(int index);
	/**
	 * Removes all items.
	 */
	public void removeAllItems();
	/**
	 * Check if query view contains modifications.
	 * @return
	 */
	public boolean isModified();
	/**
	 * Saves modifications.
	 */
	public void commit();
	/**
	 * Cancels modifications.
	 */
	public void discard();
}

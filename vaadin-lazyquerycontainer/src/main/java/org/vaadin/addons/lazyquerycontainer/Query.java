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

import com.vaadin.v7.data.Item;

import java.util.List;

/**
 * Interface for loading data in batches and saving modifications.
 *
 * @author Tommi S.E. Laukkanen
 */
public interface Query {
    /**
     * Gets number of items available through this query.
     *
     * @return Number of items.
     */
    int size();

    /**
     * Load batch of items.
     *
     * @param startIndex Starting index of the item list.
     * @param count      Count of the items to be retrieved.
     * @return List of items.
     */
    List<Item> loadItems(int startIndex, int count);

    /**
     * Saves the modifications done by container to the query result.
     * Query will be discarded after changes have been saved
     * and new query loaded so that changed items are sorted
     * appropriately.
     *
     * @param addedItems    Items to be inserted.
     * @param modifiedItems Items to be updated.
     * @param removedItems  Items to be deleted.
     */
    void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems);

    /**
     * Removes all items.
     * Query will be discarded after delete all items has been called.
     *
     * @return true if the operation succeeded or false in case of a failure.
     */
    boolean deleteAllItems();

    /**
     * Constructs new item to be used when adding items.
     *
     * @return The new item.
     */
    Item constructItem();
}

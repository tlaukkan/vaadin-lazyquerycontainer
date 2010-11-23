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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;

/**
 * Lazy loading implementation of QueryView. This implementation supports lazy
 * loading, batch loading, caching and sorting. LazyQueryView supports debug
 * properties which will be filled with debug information when they exist in
 * query definition. The debug property IDs are defined as string constants with
 * the following naming convention: DEBUG_PROPERTY_XXXX.
 * 
 * LazyQueryView implements mainly batch loading, caching and debug
 * functionalities. When data is sorted old query is discarded and new
 * constructed with QueryFactory and new sort state.
 * 
 * @author Tommi S.E. Laukkanen
 */
public final class LazyQueryView implements QueryView, ValueChangeListener {
    /** Java serialization UID. */
    private static final long serialVersionUID = 1L;

    /** Query count debug property ID. */
    public static final String DEBUG_PROPERTY_ID_QUERY_INDEX = "DEBUG_PROPERTY_ID_QUERY_COUT";
    /** Batch index debug property ID. */
    public static final String DEBUG_PROPERTY_ID_BATCH_INDEX = "DEBUG_PROPERTY_ID_BATCH_INDEX";
    /** Batch query time debug property ID. */
    public static final String DEBUG_PROPERTY_ID_BATCH_QUERY_TIME = "DEBUG_PROPERTY_ID_ACCESS_COUNT";
    /** Item status property ID. */
    public static final String PROPERTY_ID_ITEM_STATUS = "PROPERTY_ID_ITEM_STATUS";
    /** Initial maximum cache size. */
    private static final int INITIAL_MAX_CACHE_SIZE = 1000;

    /** Maximum items in cache before old ones are evicted. */
    private int maxCacheSize = INITIAL_MAX_CACHE_SIZE;
    /** Number of query executions. */
    private int queryCount = 0;
    /** QueryDefinition containing query properties and batch size. */
    private QueryDefinition queryDefinition;
    /** QueryFactory for constructing new queries when sort state changes. */
    private QueryFactory queryFactor;
    /** Currenct query used by view. */
    private Query query;

    /** Property IDs participating in sort. */
    private Object[] sortPropertyIds;
    /** Sort state of the properties participating in sort. If true then ascending else descending. */
    private boolean[] ascendingStates;

    /** List of item indexes in cache in order of access. */
    private LinkedList<Integer> itemCacheAccessLog = new LinkedList<Integer>();
    /** Map of items in cache. */
    private Map<Integer, Item> itemCache = new HashMap<Integer, Item>();
    /** Map from properties to items for items which are in cache. */
    private Map<Property, Item> propertyItemMapCache = new HashMap<Property, Item>();

    /** List of added items since last commit/rollback. */
    private List<Item> addedItems = new ArrayList<Item>();
    /** List of modified items since last commit/rollback. */
    private List<Item> modifiedItems = new ArrayList<Item>();
    /** List of deleted items since last commit/rollback. */
    private List<Item> removedItems = new ArrayList<Item>();

    /**
     * Constructs LazyQueryView with DefaultQueryDefinition and the given
     * QueryFactory.
     * @param queryFactory The QueryFactory to be used.
     * @param batchSize The batch size to be used when loading data.
     */
    public LazyQueryView(final QueryFactory queryFactory, final int batchSize) {
        initialize(new DefaultQueryDefinition(batchSize), queryFactory);
    }

    /**
     * Constructs LazyQueryView with given QueryDefinition and QueryFactory. The
     * role of this constructor is to enable use of custom QueryDefinition
     * implementations.
     * @param queryDefinition The QueryDefinition to be used.
     * @param queryFactory The QueryFactory to be used.
     */
    public LazyQueryView(final QueryDefinition queryDefinition, final QueryFactory queryFactory) {
        initialize(queryDefinition, queryFactory);
    }

    /**
     * Initializes the LazyQueryView.
     * @param queryDefinition The QueryDefinition to be used.
     * @param queryFactory The QueryFactory to be used.
     */
    private void initialize(final QueryDefinition queryDefinition, final QueryFactory queryFactory) {
        this.queryDefinition = queryDefinition;
        this.queryFactor = queryFactory;
        this.queryFactor.setQueryDefinition(queryDefinition);
        this.sortPropertyIds = new Object[0];
        this.ascendingStates = new boolean[0];
    }

    /**
     * Gets the QueryDefinition.
     * @return the QueryDefinition
     */
    public QueryDefinition getDefinition() {
        return queryDefinition;
    }

    /**
     * Sets new sort state and refreshes view.
     * @param sortPropertyIds The IDs of the properties participating in sort.
     * @param ascendingStates The sort state of the properties participating in sort. True means ascending.
     */
    public void sort(final Object[] sortPropertyIds, final boolean[] ascendingStates) {
        this.sortPropertyIds = sortPropertyIds;
        this.ascendingStates = ascendingStates;
        refresh();
    }

    /**
     * Refreshes the view by clearing cache, discarding buffered changes and current query instance.
     * New query is created on demand.
     */
    public void refresh() {

        for (Property property : propertyItemMapCache.keySet()) {
            if (property instanceof ValueChangeNotifier) {
                ValueChangeNotifier notifier = (ValueChangeNotifier) property;
                notifier.removeListener(this);
            }
        }

        query = null;
        itemCache.clear();
        itemCacheAccessLog.clear();
        propertyItemMapCache.clear();

        discard();
    }

    /**
     * Returns the total size of query and added items since last commit.
     * @return total number of items in the view.
     */
    public int size() {
        return getQuery().size() + addedItems.size();
    }

    /**
     * Gets the batch size i.e. how many items is fetched at a time from storage.
     * @return the batch size.
     */
    public int getBatchSize() {
        return queryDefinition.getBatchSize();
    }

    /**
     * Gets item at given index from addedItems, cache and loads new batch on demand
     * if required.
     * @param index The item index.
     * @return the item at given index.
     */
    public Item getItem(final int index) {
        int addedItemCount = addedItems.size();
        if (index < addedItemCount) {
            // an item from the addedItems was requested
            return addedItems.get(index);
        }
        if (!itemCache.containsKey(index - addedItemCount)) {
            // item is not in our cache, ask the query for more items
            queryItem(index - addedItemCount);
        } else {
            // item is already in our cache
            // refresh cache access log.
            itemCacheAccessLog.remove(new Integer(index));
            itemCacheAccessLog.addLast(new Integer(index));
        }

        return itemCache.get(index - addedItemCount);
    }

    /**
     * Query item and the surrounding batch of items.
     * @param index The index of item requested to be queried.
     */
    private void queryItem(final int index) {
        int batchSize = getBatchSize();
        int startIndex = index - index % batchSize;
        int count = Math.min(batchSize, getQuery().size() - startIndex);

        long queryStartTime = System.currentTimeMillis();
        // load more items
        List<Item> items = getQuery().loadItems(startIndex, count);
        long queryEndTime = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            int itemIndex = startIndex + i;
            Item item = items.get(i);

            itemCache.put(itemIndex, item);
            itemCacheAccessLog.addLast(itemIndex);
        }

        for (int i = 0; i < count; i++) {
            Item item = items.get(i);

            if (item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_INDEX) != null) {
                item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_INDEX).setReadOnly(false);
                item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_INDEX).setValue(startIndex / batchSize);
                item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_INDEX).setReadOnly(true);
            }
            if (item.getItemProperty(DEBUG_PROPERTY_ID_QUERY_INDEX) != null) {
                item.getItemProperty(DEBUG_PROPERTY_ID_QUERY_INDEX).setReadOnly(false);
                item.getItemProperty(DEBUG_PROPERTY_ID_QUERY_INDEX).setValue(queryCount);
                item.getItemProperty(DEBUG_PROPERTY_ID_QUERY_INDEX).setReadOnly(true);
            }
            if (item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_QUERY_TIME) != null) {
                item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_QUERY_TIME).setReadOnly(false);
                item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_QUERY_TIME).setValue(queryEndTime - queryStartTime);
                item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_QUERY_TIME).setReadOnly(true);
            }

            for (Object propertyId : item.getItemPropertyIds()) {
                Property property = item.getItemProperty(propertyId);
                if (property instanceof ValueChangeNotifier) {
                    ValueChangeNotifier notifier = (ValueChangeNotifier) property;
                    notifier.addListener(this);
                    propertyItemMapCache.put(property, item);
                }
            }

        }

        // Evict items from cache if cache size exceeds max cache size
        int counter = 0;
        while (itemCache.size() > maxCacheSize) {
            int firstIndex = itemCacheAccessLog.getFirst();
            Item firstItem = itemCache.get(firstIndex);

            // Remove oldest item in cache access log if it is not modified or
            // removed.
            if (!modifiedItems.contains(firstItem) && !removedItems.contains(firstItem)) {
                itemCacheAccessLog.remove(new Integer(firstIndex));
                itemCache.remove(firstIndex);

                for (Object propertyId : firstItem.getItemPropertyIds()) {
                    Property property = firstItem.getItemProperty(propertyId);
                    if (property instanceof ValueChangeNotifier) {
                        ValueChangeNotifier notifier = (ValueChangeNotifier) property;
                        notifier.removeListener(this);
                        propertyItemMapCache.remove(property);
                    }
                }

            } else {
                itemCacheAccessLog.remove(firstIndex);
                itemCacheAccessLog.addLast(firstIndex);
            }

            // Break from loop if entire cache has been iterated (all items are
            // modified).
            counter++;
            if (counter > itemCache.size()) {
                break;
            }
        }
    }

    /**
     * Gets current query or constructs one on demand.
     * @return The current query.
     */
    private Query getQuery() {
        if (query == null) {
            query = queryFactor.constructQuery(sortPropertyIds, ascendingStates);
            queryCount++;
        }
        return query;
    }

    /**
     * Constructs and adds item to added items and returns index.
     * Change can be committed or discarded with respective methods.
     * @return index of the new item.
     */
    public int addItem() {
        Item item = getQuery().constructItem();
        if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.Added);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
        }
        addedItems.add(0, item);
        return 0;
    }

    /**
     * Event handler for value change events. Adds the item to modified list if value was actually changed.
     * Change can be committed or discarded with respective methods.
     * @param event the ValueChangeEvent
     */
    public void valueChange(final ValueChangeEvent event) {
        Property property = event.getProperty();
        Item item = propertyItemMapCache.get(property);
        if (property == item.getItemProperty(PROPERTY_ID_ITEM_STATUS)) {
            return;
        }
        if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null
                && ((QueryItemStatus) item.getItemProperty(PROPERTY_ID_ITEM_STATUS).getValue())
                != QueryItemStatus.Modified) {
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.Modified);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
        }
        modifiedItems.add(item);
    }

    /**
     * Removes item at given index by adding it to the removed list.
     * Change can be committed or discarded with respective methods.
     * @param index of the item to be removed.
     */
    public void removeItem(final int index) {
        Item item = getItem(index);

        if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.Removed);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
        }

        for (Object propertyId : item.getItemPropertyIds()) {
            Property property = item.getItemProperty(propertyId);
            property.setReadOnly(true);
        }

        removedItems.add(item);
    }

    /**
     * Removes all items in the view. This method is immediately commited to the storage.
     */
    public void removeAllItems() {
        getQuery().deleteAllItems();
    }

    /**
     * Checks whether view has been modified.
     * @return True if view has been modified.
     */
    public boolean isModified() {
        return addedItems.size() != 0 || modifiedItems.size() != 0 || removedItems.size() != 0;
    }

    /**
     * Commits changes in the view.
     */
    public void commit() {
        for (Item item : addedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        for (Item item : modifiedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        for (Item item : removedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        getQuery().saveItems(addedItems, modifiedItems, removedItems);
        addedItems.clear();
        modifiedItems.clear();
        removedItems.clear();
    }

    /**
     * Discards changes in the view.
     */
    public void discard() {
        for (Item item : addedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        for (Item item : modifiedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        for (Item item : removedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        addedItems.clear();
        modifiedItems.clear();
        removedItems.clear();
    }

}

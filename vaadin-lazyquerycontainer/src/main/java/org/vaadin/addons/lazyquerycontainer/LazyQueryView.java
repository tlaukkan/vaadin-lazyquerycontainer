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
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Property.ValueChangeNotifier;

import java.io.Serializable;
import java.util.*;

/**
 * Lazy loading implementation of QueryView. This implementation supports lazy
 * loading, batch loading, caching and sorting. LazyQueryView supports debug
 * properties which will be filled with debug information when they exist in
 * query definition. The debug property IDs are defined as string constants with
 * the following naming convention: DEBUG_PROPERTY_XXXX.
 * LazyQueryView implements mainly batch loading, caching and debug
 * functionalities. When data is sorted old query is discarded and new
 * constructed with QueryFactory and new sort state.
 *
 * @author Tommi S.E. Laukkanen
 */
public final class LazyQueryView implements QueryView, ValueChangeListener, Serializable {
    /**
     * Java serialization UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Query count debug property ID.
     */
    public static final String DEBUG_PROPERTY_ID_QUERY_INDEX = "DEBUG_PROPERTY_ID_QUERY_COUT";
    /**
     * Batch index debug property ID.
     */
    public static final String DEBUG_PROPERTY_ID_BATCH_INDEX = "DEBUG_PROPERTY_ID_BATCH_INDEX";
    /**
     * Batch query time debug property ID.
     */
    public static final String DEBUG_PROPERTY_ID_BATCH_QUERY_TIME = "DEBUG_PROPERTY_ID_BATCH_QUERY_TIME";
    /**
     * Item status property ID.
     */
    public static final String PROPERTY_ID_ITEM_STATUS = "PROPERTY_ID_ITEM_STATUS";
    /**
     * Initial maximum cache size.
     */
    private static final int DEFAULT_MAX_CACHE_SIZE = 1000;

    /**
     * Maximum items in cache before old ones are evicted.
     */
    private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
    /**
     * Number of query executions.
     */
    private int queryCount = 0;
    /**
     * Number of batches read.
     */
    private int batchCount = 0;
    /**
     * QueryDefinition containing query properties and batch size.
     */
    private QueryDefinition queryDefinition;
    /**
     * QueryFactory for constructing new queries when sort state changes.
     */
    private QueryFactory queryFactory;
    /**
     * Currenct query used by view.
     */
    private Query query;
    /**
     * Size of the query.
     */
    private int querySize = -1;
    /**
     * Property IDs participating in sort.
     */
    private Object[] sortPropertyIds;
    /**
     * Sort state of the properties participating in sort. If true then
     * ascending else descending.
     */
    private boolean[] ascendingStates;
    /**
     * List of item IDs.
     */
    private List<?> itemIdList;
    /**
     * List of item indexes in cache in order of access.
     */
    private final LinkedList<Integer> itemCacheAccessLog = new LinkedList<Integer>();
    /**
     * Map of items in cache.
     */
    private final Map<Integer, Item> itemCache = new HashMap<Integer, Item>();
    /**
     * Map from properties to items for items which are in cache.
     */
    private Map<Property, Item> propertyItemMapCache = new HashMap<Property, Item>();

    /**
     * List of added items since last commit/rollback.
     */
    private final List<Item> addedItems = new ArrayList<Item>();
    /**
     * List of modified items since last commit/rollback.
     */
    private final List<Item> modifiedItems = new ArrayList<Item>();
    /**
     * List of deleted items since last commit/rollback.
     */
    private final List<Item> removedItems = new ArrayList<Item>();

    /**
     * Constructs LazyQueryView with given QueryDefinition and QueryFactory. The
     * role of this constructor is to enable use of custom QueryDefinition
     * implementations.
     *
     * @param queryDefinition The QueryDefinition to be used.
     * @param queryFactory    The QueryFactory to be used.
     */
    public LazyQueryView(final QueryDefinition queryDefinition, final QueryFactory queryFactory) {
        initialize(queryDefinition, queryFactory);
    }

    /**
     * Initializes the LazyQueryView.
     *
     * @param queryDefinition The QueryDefinition to be used.
     * @param queryFactory    The QueryFactory to be used.
     */
    private void initialize(final QueryDefinition queryDefinition, final QueryFactory queryFactory) {
        this.queryDefinition = queryDefinition;
        this.queryFactory = queryFactory;
        this.sortPropertyIds = new Object[0];
        this.ascendingStates = new boolean[0];
    }

    /**
     * Gets the QueryDefinition.
     *
     * @return the QueryDefinition
     */
    @Override
    public QueryDefinition getQueryDefinition() {
        return queryDefinition;
    }

    /**
     * Sets new sort state and refreshes view.
     *
     * @param sortPropertyIds The IDs of the properties participating in sort.
     * @param ascendingStates The sort state of the properties participating in
     *                        sort. True means ascending.
     */
    @Override
    public void sort(final Object[] sortPropertyIds, final boolean[] ascendingStates) {
        this.sortPropertyIds = sortPropertyIds;
        this.ascendingStates = ascendingStates;
        refresh();
    }

    /**
     * Refreshes the view by clearing cache, discarding buffered changes and
     * current query instance. New query is created on demand.
     */
    @Override
    public void refresh() {

        for (final Property property : propertyItemMapCache.keySet()) {
            if (property instanceof ValueChangeNotifier) {
                final ValueChangeNotifier notifier = (ValueChangeNotifier) property;
                notifier.removeValueChangeListener(this);
            }
        }

        query = null;
        batchCount = 0;
        itemIdList = null;
        itemCache.clear();
        itemCacheAccessLog.clear();
        propertyItemMapCache.clear();

        discard();
    }

    /**
     * Returns the total size of query and added items since last commit.
     *
     * @return total number of items in the view.
     */
    @Override
    public int size() {
        return getQuerySize() + addedItems.size();
    }

    /**
     * Gets the batch size i.e. how many items is fetched at a time from
     * storage.
     *
     * @return the batch size.
     */
    public int getBatchSize() {
        return queryDefinition.getBatchSize();
    }

    /**
     * @return the maxCacheSize
     */
    @Override
    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     * @param maxCacheSize the maxCacheSize to set
     */
    @Override
    public void setMaxCacheSize(final int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Gets item at given index from addedItems, cache and loads new batch on
     * demand if required.
     *
     * @param index The item index.
     * @return the item at given index.
     */
    @Override
    public Item getItem(final int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Container size: " + size() + " and item index  requested: " + index);
        }
        final int addedItemCount = addedItems.size();
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
     *
     * @param index The index of item requested to be queried.
     */
    private void queryItem(final int index) {
        final int batchSize = getBatchSize();
        final int startIndex = index - index % batchSize;
        final int count = Math.min(batchSize, getQuerySize() - startIndex);

        final long queryStartTime = System.currentTimeMillis();
        // load more items
        final List<Item> items = getQuery().loadItems(startIndex, count);
        final long queryEndTime = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            final int itemIndex = startIndex + i;

            final Item item;

            if (i >= items.size()) {
                item = getQuery().constructItem();
            } else {
                item = items.get(i);
            }

            itemCache.put(itemIndex, item);

            if (i >= items.size()) {
                removeItem(itemIndex);
            }

            if (itemCacheAccessLog.contains(itemIndex)) {
                itemCacheAccessLog.remove((Object) itemIndex);
            }
            itemCacheAccessLog.addLast(itemIndex);
        }

        for (int i = 0; i < count; i++) {
            final int itemIndex = startIndex + i;

            final Item item = itemCache.get(itemIndex);

            if (item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_INDEX) != null) {
                item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_INDEX).setReadOnly(false);
                item.getItemProperty(DEBUG_PROPERTY_ID_BATCH_INDEX).setValue(batchCount);
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

            for (final Object propertyId : item.getItemPropertyIds()) {
                final Property property = item.getItemProperty(propertyId);
                if (property instanceof ValueChangeNotifier) {
                    final ValueChangeNotifier notifier = (ValueChangeNotifier) property;
                    notifier.addValueChangeListener(this);
                    propertyItemMapCache.put(property, item);
                }
            }

        }

        // Increase batch count.
        batchCount++;

        // Evict items from cache if cache size exceeds max cache size
        int counter = 0;
        while (itemCache.size() > maxCacheSize) {
            final int firstIndex = itemCacheAccessLog.getFirst();
            final Item firstItem = itemCache.get(firstIndex);

            // Remove oldest item in cache access log if it is not modified or
            // removed.
            if (!modifiedItems.contains(firstItem) && !removedItems.contains(firstItem)) {
                itemCacheAccessLog.removeFirst();
                itemCache.remove(firstIndex);

                for (final Object propertyId : firstItem.getItemPropertyIds()) {
                    final Property property = firstItem.getItemProperty(propertyId);
                    if (property instanceof ValueChangeNotifier) {
                        final ValueChangeNotifier notifier = (ValueChangeNotifier) property;
                        notifier.removeValueChangeListener(this);
                        propertyItemMapCache.remove(property);
                    }
                }

            } else {
                itemCacheAccessLog.removeFirst();
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
     * Get the query size.
     * @return the query size
     */
    private int getQuerySize() {
        if (query == null) {
            getQuery();
        }
        return querySize;
    }

    /**
     * Gets current query or constructs one on demand.
     *
     * @return The current query.
     */
    private Query getQuery() {
        if (query == null) {
            queryDefinition.setSortPropertyIds(sortPropertyIds);
            queryDefinition.setSortPropertyAscendingStates(ascendingStates);
            query = queryFactory.constructQuery(queryDefinition);
            querySize = query.size();
            if (queryDefinition.getMaxQuerySize() > -1 && queryDefinition.getMaxQuerySize() < querySize) {
                querySize = queryDefinition.getMaxQuerySize();
            }
            queryCount++;
        }
        return query;
    }

    /**
     * Constructs and adds item to added items and returns index. Change can be
     * committed or discarded with respective methods.
     *
     * @return index of the new item.
     */
    @Override
    public int addItem() {
        final Item item = getQuery().constructItem();
        if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.Added);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
        }
        addedItems.add(0, item);
        if (itemIdList instanceof NaturalNumberIdsList) {
            itemIdList = null;
        }
        return 0;
    }

    /**
     * Event handler for value change events. Adds the item to modified list if
     * value was actually changed. Change can be committed or discarded with
     * respective methods.
     *
     * @param event the ValueChangeEvent
     */
    @Override
    public void valueChange(final ValueChangeEvent event) {
        final Property property = event.getProperty();
        final Item item = propertyItemMapCache.get(property);
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
        if (!addedItems.contains(item) && !modifiedItems.contains(item)) {
            modifiedItems.add(item);
        }
    }

    /**
     * Removes item at given index by adding it to the removed list. Change can
     * be committed or discarded with respective methods.
     *
     * @param index of the item to be removed.
     */
    @Override
    public void removeItem(final int index) {
        final Item item = getItem(index);

        if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.Removed);
            item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
        }

        for (final Object propertyId : item.getItemPropertyIds()) {
            final Property property = item.getItemProperty(propertyId);
            property.setReadOnly(true);
        }

        removedItems.add(item);
    }

    /**
     * Removes all items in the view. This method is immediately commited to the
     * storage.
     */
    @Override
    public void removeAllItems() {
        getQuery().deleteAllItems();
    }

    /**
     * Checks whether view has been modified.
     *
     * @return True if view has been modified.
     */
    @Override
    public boolean isModified() {
        return addedItems.size() != 0 || modifiedItems.size() != 0 || removedItems.size() != 0;
    }

    /**
     * Commits changes in the view.
     */
    @Override
    public void commit() {
        for (final Item item : addedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        for (final Item item : modifiedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        for (final Item item : removedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }

        // Reverse added items so that they are saved in order of addition.
        final List<Item> addedItemReversed = new ArrayList<Item>(addedItems);
        Collections.reverse(addedItemReversed);
        getQuery().saveItems(addedItemReversed, modifiedItems, removedItems);
        addedItems.clear();
        modifiedItems.clear();
        removedItems.clear();
    }

    /**
     * Discards changes in the view.
     */
    @Override
    public void discard() {
        for (final Item item : addedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        for (final Item item : modifiedItems) {
            if (item.getItemProperty(PROPERTY_ID_ITEM_STATUS) != null) {
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(false);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setValue(QueryItemStatus.None);
                item.getItemProperty(PROPERTY_ID_ITEM_STATUS).setReadOnly(true);
            }
        }
        for (final Item item : removedItems) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> getAddedItems() {
        return Collections.<Item>unmodifiableList(addedItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> getModifiedItems() {
        return Collections.<Item>unmodifiableList(modifiedItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> getRemovedItems() {
        return Collections.<Item>unmodifiableList(removedItems);
    }

    /**
     * Used to set implementation property item cache map.
     *
     * @param propertyItemCacheMap the propertyItemMapCache to set
     */
    public void setPropertyItemCacheMap(final Map<Property, Item> propertyItemCacheMap) {
        this.propertyItemMapCache = propertyItemCacheMap;
    }

    /**
     * Gets list of item IDs present in this view.
     * @return list of item IDs present in this view.
     */
    public List<?> getItemIdList() {
        if (itemIdList == null) {
            if (queryDefinition.getIdPropertyId() != null) {
                itemIdList = new LazyIdList<Object>(this, queryDefinition.getIdPropertyId());
            } else {
                itemIdList = new NaturalNumberIdsList(size());
            }
        }

        return itemIdList;
    }

    @Override
    public void addFilter(final Container.Filter filter) {
        queryDefinition.addFilter(filter);
        refresh();
    }

    @Override
    public void removeFilter(final Container.Filter filter) {
        queryDefinition.removeFilter(filter);
        refresh();
    }

    @Override
    public void removeFilters() {
        queryDefinition.removeFilters();
        refresh();
    }

    @Override
    public Collection<Container.Filter> getFilters() {
        return queryDefinition.getFilters();
    }
}

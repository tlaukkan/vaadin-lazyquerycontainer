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

import com.vaadin.v7.data.*;
import com.vaadin.v7.data.Container.Indexed;
import com.vaadin.v7.data.Container.ItemSetChangeNotifier;
import com.vaadin.v7.data.Container.PropertySetChangeNotifier;
import com.vaadin.v7.data.Container.Sortable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * LazyQueryContainer provides lazy loading of items from business services. See
 * package level documentation for detailed description. This implements event
 * notification functionality and delegates other methods to QueryView
 * aggregate.
 *
 * @author Tommi S.E. Laukkanen
 */
public class LazyQueryContainer implements Indexed, Sortable, ItemSetChangeNotifier, PropertySetChangeNotifier,
        Buffered, Container.Filterable, Serializable {
    /**
     * Java serialization UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * QueryView where LazyQueryContainer delegates method calls to.
     */
    private QueryView queryView;
    /**
     * List of registered ItemSetChangeListener.
     */
    private List<ItemSetChangeListener> itemSetChangeListeners = new ArrayList<ItemSetChangeListener>();
    /**
     * List of registered PropertySetChangeListeners.
     */
    private List<PropertySetChangeListener> propertySetChangeListeners = new ArrayList<PropertySetChangeListener>();

    /**
     * Constructs LazyQueryContainer with LazyQueryView and given queryFactory.
     *
     * @param queryFactory                  The query factory to be used.
     * @param idPropertyId                  The ID of the ID property or null if item index is used as ID.
     * @param batchSize                     The batch size to be used when loading data.
     * @param compositeItems                True if items are wrapped to CompositeItems.
     */
    public LazyQueryContainer(final QueryFactory queryFactory,
                              final Object idPropertyId,
                              final int batchSize,
                              final boolean compositeItems) {
        this.queryView = new LazyQueryView(new LazyQueryDefinition(compositeItems, batchSize, idPropertyId),
                queryFactory);
    }

    /**
     * Constructs LazyQueryContainer with LazyQueryView and given queryFactory
     * and queryDefinition.
     *
     * @param queryFactory    The query factory to be used.
     * @param queryDefinition The query definition to be used.
     */
    public LazyQueryContainer(final QueryDefinition queryDefinition, final QueryFactory queryFactory) {
        this.queryView = new LazyQueryView(queryDefinition, queryFactory);
    }

    /**
     * Constructs LazyQueryContainer with the given QueryView. This constructor
     * role is to enable use of custom view implementations.
     *
     * @param queryView The query view to be used.
     */
    public LazyQueryContainer(final QueryView queryView) {
        this.queryView = queryView;
    }

    /**
     * Sets new sort state and refreshes container.
     *
     * @param sortPropertyIds The IDs of the properties participating in sort.
     * @param ascendingStates The sort state of the properties participating in sort. True means ascending.
     */
    public final void sort(final Object[] sortPropertyIds, final boolean[] ascendingStates) {
        queryView.sort(sortPropertyIds, ascendingStates);
        notifyItemSetChanged();
    }

    /**
     * Lists of the property IDs queried.
     *
     * @return A list of property IDs queried.
     */
    public final Collection<?> getContainerPropertyIds() {
        return queryView.getQueryDefinition().getPropertyIds();
    }

    /**
     * List of the property IDs which can be sorted.
     *
     * @return A list of the property IDs which can be sorted.
     */
    public final Collection<?> getSortableContainerPropertyIds() {
        return queryView.getQueryDefinition().getSortablePropertyIds();
    }

    /**
     * Gets the property value class of the given property.
     *
     * @param propertyId If of the property of interest.
     * @return The value class of the given property.
     */
    public final Class<?> getType(final Object propertyId) {
        return queryView.getQueryDefinition().getPropertyType(propertyId);
    }

    /**
     * Adds a new property to the definition.
     *
     * @param propertyId   Id of the property.
     * @param type         Value class of the property.
     * @param defaultValue Default value of the property.
     * @return always true.
     */
    public final boolean addContainerProperty(final Object propertyId, final Class<?> type, final Object defaultValue) {
        queryView.getQueryDefinition().addProperty(propertyId, type, defaultValue, true, false);
        notifyPropertySetChanged();
        return true;
    }

    /**
     * Adds a new property to the definition.
     *
     * @param propertyId   Id of the property.
     * @param type         Value class of the property.
     * @param defaultValue Default value of the property.
     * @param readOnly     Read only state of the property.
     * @param sortable     Sortable state of the property.
     * @return always true.
     */
    public final boolean addContainerProperty(final Object propertyId, final Class<?> type, final Object defaultValue,
                                              final boolean readOnly, final boolean sortable) {
        queryView.getQueryDefinition().addProperty(propertyId, type, defaultValue, readOnly, sortable);
        notifyPropertySetChanged();
        return true;
    }

    /**
     * Removes the given property from the definition.
     *
     * @param propertyId If of the property to be removed.
     * @return always true.
     */
    public final boolean removeContainerProperty(final Object propertyId) {
        queryView.getQueryDefinition().removeProperty(propertyId);
        notifyPropertySetChanged();
        return true;
    }

    /**
     * Number of items in the container.
     *
     * @return number of items.
     */
    public final int size() {
        return queryView.size();
    }

    /**
     * Gets list of item indexes in the container.
     *
     * @return Collection of Integers.
     */
    public final Collection<?> getItemIds() {
        return queryView.getItemIdList();
    }

    /**
     * Gets item at given index.
     *
     * @param itemId index of the item.
     * @return Item at the given index.
     */
    public final Item getItem(final Object itemId) {
        if (itemId == null) {
            return null;
        } else {
            int index = queryView.getItemIdList().indexOf(itemId);
            if (index == -1) return null;
            return queryView.getItem(index);
        }
    }

    /**
     * Gets property of an item.
     *
     * @param itemId     The index of the item.
     * @param propertyId ID of the property.
     * @return the property corresponding to given IDs.
     */
    public final Property getContainerProperty(final Object itemId, final Object propertyId) {
        final Item item = getItem(itemId);
        if (item == null) return null;
        return item.getItemProperty(propertyId);
    }

    /**
     * Gets ID of given index.
     * In other words returns the index itself.
     *
     * @param index The index of the item.
     * @return the object ID i.e. index.
     */
    public final Object getIdByIndex(final int index) {
        return queryView.getItemIdList().get(index);
    }

    /**
     * Gets index of given ID.
     * In other words returns the index itself.
     *
     * @param itemId the item ID.
     * @return the index.
     */
    public final int indexOfId(final Object itemId) {
        return queryView.getItemIdList().indexOf(itemId);
    }

    /**
     * True if container contains the given index.
     *
     * @param itemId Item index.
     * @return true if container contains the given index.
     */
    public final boolean containsId(final Object itemId) {
        return queryView.getItemIdList().contains(itemId);
    }

    /**
     * Check if given index is first i.e. 0.
     *
     * @param itemId the object index.
     * @return true if index is 0.
     */
    public final boolean isFirstId(final Object itemId) {
        return queryView.getItemIdList().indexOf(itemId) == 0;
    }

    /**
     * Check if given index is first i.e. size() - 1.
     *
     * @param itemId the object index.
     * @return true if index is size() - 1.
     */
    public final boolean isLastId(final Object itemId) {
        return queryView.getItemIdList().indexOf(itemId) == size() - 1;
    }

    /**
     * @return first item ID i.e. 0.
     */
    public final Object firstItemId() {
        return queryView.getItemIdList().get(0);
    }

    /**
     * @return last item ID i.e. size() - 1
     */
    public final Object lastItemId() {
        return queryView.getItemIdList().get(size() - 1);
    }

    /**
     * @param itemId the item index
     * @return itemId + 1, or <code>null</code> if no such item
     */
    public final Object nextItemId(final Object itemId) {
    	List<?> itemIdList = queryView.getItemIdList();
    	int currentIndex = itemIdList.indexOf(itemId);
    	if (currentIndex == -1 || currentIndex == itemIdList.size() - 1) {
    		return null;
    	}
    	else {
    		return itemIdList.get(currentIndex + 1);
    	}
    }

    /**
     * @param itemId the item index
     * @return itemId - 1, or <code>null</code> if no such item
     */
    public final Object prevItemId(final Object itemId) {
    	List<?> itemIdList = queryView.getItemIdList();
    	int currentIndex = itemIdList.indexOf(itemId);
    	if (currentIndex == -1 || currentIndex == 0) {
    		return null;
    	}
    	else {
    		return itemIdList.get(currentIndex - 1);
    	}
    }

    /**
     * Not supported.
     *
     * @param index item index
     * @return new item ID.
     */
    public final Object addItemAt(final int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     *
     * @param previousItemId ID of previous item.
     * @return new item ID.
     */
    public final Object addItemAfter(final Object previousItemId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     *
     * @param index     item index
     * @param newItemId ID of new item
     * @return new Item
     */
    public final Item addItemAt(final int index, final Object newItemId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     *
     * @param previousItemId ID of previous item.
     * @param newItemId      ID of new item
     * @return new Item
     */
    public final Item addItemAfter(final Object previousItemId, final Object newItemId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     *
     * @param itemId itemId
     * @return new Item
     */
    public final Item addItem(final Object itemId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Constructs and adds new item.
     *
     * @return item index.
     */
    public final Object addItem() {
        final int itemIndex = queryView.addItem();
        final Object itemId = queryView.getItemIdList().get(itemIndex);
        notifyItemSetChanged();
        return itemId;
    }

    /**
     * Removes Item at given index.
     *
     * @param itemId Item index.
     * @return always true.
     */
    public final boolean removeItem(final Object itemId) {
        queryView.removeItem(indexOfId(itemId));
        notifyItemSetChanged();
        return true;
    }

    /**
     * Removes all items.
     *
     * @return always true.
     */
    public final boolean removeAllItems() {
        queryView.removeAllItems();
        refresh();
        return true;
    }

    /**
     * Adds ItemSetChangeListener.
     *
     * @param listener ItemSetChangeListener to be added.
     */
    @Deprecated
    public final void addListener(final ItemSetChangeListener listener) {
        addItemSetChangeListener(listener);

    }

    /**
     * Removes ItemSetChangeListener.
     *
     * @param listener ItemSetChangeListener to be removed.
     */
    @Deprecated
    public final void removeListener(final ItemSetChangeListener listener) {
        removeItemSetChangeListener(listener);
    }

    @Override
    public final void removeItemSetChangeListener(final ItemSetChangeListener listener) {
        itemSetChangeListeners.remove(listener);
    }

    /**
     * Refreshes container.
     */
    public final void refresh() {
        queryView.refresh();
        notifyItemSetChanged();
    }

    /**
     * Notifies that item set has been changed.
     */
    private void notifyItemSetChanged() {
        QueryItemSetChangeEvent event = new QueryItemSetChangeEvent(this);
        for (ItemSetChangeListener listener : itemSetChangeListeners) {
            listener.containerItemSetChange(event);
        }
    }

    /**
     * Adds PropertySetChangeListener.
     *
     * @param listener PropertySetChangeListener to be added.
     */
    @Deprecated
    public final void addListener(final PropertySetChangeListener listener) {
        addPropertySetChangeListener(listener);
    }

    @Override
    public final void addItemSetChangeListener(final ItemSetChangeListener listener) {
        itemSetChangeListeners.add(listener);
    }

    @Override
    public final void addPropertySetChangeListener(final PropertySetChangeListener listener) {
        propertySetChangeListeners.add(listener);
    }

    /**
     * Removes PropertySetChangeListener.
     *
     * @param listener PropertySetChangeListener to be removed.
     */
    @Deprecated
    public final void removeListener(final PropertySetChangeListener listener) {
        removePropertySetChangeListener(listener);
    }

    @Override
    public final void removePropertySetChangeListener(final PropertySetChangeListener listener) {
        propertySetChangeListeners.remove(listener);
    }

    /**
     * Notifies that property set has been changed.
     */
    private void notifyPropertySetChanged() {
        QueryPropertySetChangeEvent event = new QueryPropertySetChangeEvent(this);
        for (PropertySetChangeListener listener : propertySetChangeListeners) {
            listener.containerPropertySetChange(event);
        }
    }

    @Override
    public final void addContainerFilter(final Filter filter) {
        getQueryView().addFilter(filter);
        notifyItemSetChanged();
    }

    @Override
    public final void removeContainerFilter(final Filter filter) {
        getQueryView().removeFilter(filter);
        notifyItemSetChanged();
    }

    @Override
    public final void removeAllContainerFilters() {
        getQueryView().removeFilters();
        notifyItemSetChanged();
    }

    @Override
    public Collection<Filter> getContainerFilters() {
        return getQueryView().getFilters();
    }

    /**
     * Adds default filter to underlying QueryDefinition.
     * @param filter the default filter
     */
    public final void addDefaultFilter(final Filter filter) {
        getQueryView().getQueryDefinition().addDefaultFilter(filter);
        notifyItemSetChanged();
    }

    /**
     * Removes default filter from underlying QueryDefinition.
     * @param filter the default filter
     */
    public final void removeDefaultFilter(final Filter filter) {
        getQueryView().getQueryDefinition().removeDefaultFilter(filter);
        notifyItemSetChanged();
    }

    /**
     * Removes all default filters from underlying QueryDefinition.
     */
    public final void removeDefaultFilters() {
        getQueryView().getQueryDefinition().removeDefaultFilters();
        notifyItemSetChanged();
    }

    /**
     * Private ItemSetChangeEvent implementation.
     *
     * @author Tommi Laukkanen
     */
    private class QueryItemSetChangeEvent implements ItemSetChangeEvent {
        /**
         * Java serialization version UID.
         */
        private static final long serialVersionUID = 1L;
        /**
         * The container where event occurred.
         */
        private LazyQueryContainer container;

        /**
         * Constructor for setting the container.
         *
         * @param container the Container.
         */
        public QueryItemSetChangeEvent(final LazyQueryContainer container) {
            this.container = container;
        }

        /**
         * Gets the container where event occurred.
         *
         * @return the Container.
         */
        public Container getContainer() {
            return container;
        }

    }

    /**
     * Private PropertySetChangeEvent implementation.
     *
     * @author Tommi Laukkanen
     */
    private class QueryPropertySetChangeEvent implements PropertySetChangeEvent {
        /**
         * Java serialization version UID.
         */
        private static final long serialVersionUID = 1L;
        /**
         * The container where event occurred.
         */
        private LazyQueryContainer container;

        /**
         * Constructor for setting the container.
         *
         * @param container the Container.
         */
        public QueryPropertySetChangeEvent(final LazyQueryContainer container) {
            this.container = container;
        }

        /**
         * Gets the container where event occurred.
         *
         * @return the Container.
         */
        public Container getContainer() {
            return container;
        }

    }

    /**
     * Commits changed and refreshes container.
     */
    public final void commit() {
        queryView.commit();
        refresh();
    }

    /**
     * Discards changes and refreshes container.
     */
    public final void discard() {
        queryView.discard();
        refresh();
    }

    /**
     * @return true if container contains not commited modifications.
     */
    public final boolean isModified() {
        return queryView.isModified();
    }

    /**
     * @return the queryView
     */
    public final QueryView getQueryView() {
        return queryView;
    }

    @Override
    public final void setBuffered(final boolean buffered) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean isBuffered() {
        return true;
    }

    @Override
    public final List<?> getItemIds(final int startIndex, final int numberOfItems) {
        return ContainerHelpers.getItemIdsUsingGetIdByIndex(startIndex, numberOfItems, this);
    }

}

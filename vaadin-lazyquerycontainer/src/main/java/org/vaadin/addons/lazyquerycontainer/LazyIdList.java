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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lazy ID list implementation used in LazyQueryContainer.getItemIds
 * method. Wraps LazyQueryView and gets item ID's from the view on demand.
 * @param <T> the id class
 *
 * @author Tommi Laukkanen
 */
public final class LazyIdList<T> extends AbstractList<T> implements Serializable {
    /**
     * Java serialization version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The composite LazyQueryView.
     */
    private final LazyQueryView lazyQueryView;
    /**
     * The ID of the item ID property.
     */
    private final Object idPropertyId;
    /**
     * Map containing index to item ID mapping for IDs already loaded through this list.
     */
    private final Map<Object, Integer> idIndexMap = new HashMap<Object, Integer>();

    /**
     * Constructor which sets composite LazyQueryView and ID of the item ID property.
     *
     * @param lazyQueryView the LazyQueryView where IDs can be read from.
     * @param idPropertyId Property containing the property ID.
     */
    public LazyIdList(final LazyQueryView lazyQueryView, final Object idPropertyId) {
        this.lazyQueryView = lazyQueryView;
        this.idPropertyId = idPropertyId;
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return lazyQueryView.size();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized T[] toArray() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] a) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public T get(final int index) {
        if (index < 0 || index >= lazyQueryView.size()) {
            throw new IndexOutOfBoundsException();
        }
        final T itemId = (T) lazyQueryView.getItem(index).getItemProperty(idPropertyId).getValue();
        // Do not put added item ids to id index map and make sure that
        // existing item indexes start from 0 i.e. ignore added items as they
        // are compensated for in indexOf method.
        final int addedItemSize = lazyQueryView.getAddedItems().size();
        if (index >= addedItemSize) {
            idIndexMap.put(itemId, index - addedItemSize);
        }
        return itemId;
    }

    /**
     * {@inheritDoc}
     */
    public Integer set(final int index, final Integer element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public int indexOf(final Object o) {
        if (o == null) {
            return -1;
        }
        // Brute force added items first. There should only be a few.
        final List<Item> addedItems = lazyQueryView.getAddedItems();
        for (int i = 0; i < addedItems.size(); i++) {
            if (o.equals(addedItems.get(i).getItemProperty(idPropertyId).getValue())) {
                return i;
            }
        }
        // Check from mapping cache.
        if (idIndexMap.containsKey(o)) {
            return addedItems.size() + idIndexMap.get(o);
        }
        // Switching to brute forcing.
        for (int i = addedItems.size(); i < lazyQueryView.size(); i++) {
            if (o.equals(lazyQueryView.getItem(i).getItemProperty(idPropertyId).getValue())) {
                return i;
            }
        }
        // Not found.
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(final Object o) {
        return indexOf(o) != -1;
    }
}

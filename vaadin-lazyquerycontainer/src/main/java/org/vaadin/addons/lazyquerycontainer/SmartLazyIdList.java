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
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.vaadin.data.Item;
import com.vaadin.ui.ComboBox;

/**
 * The SmartLazyIdList is basically a copy of the {@link LazyIdList} with one big difference.
 * <br>
 * It requires the IdProperties to be a comparable and assumes those comparables are
 * in ascending ordner within the container. 
 * <br>
 * With this assumption it uses a Binary Search for finding the index of an item. If no index can
 * be retrieved, it switches to a linear search, like the default implementation.
 * <br>
 * The {@link SmartLazyIdList} makes the the {@link LazyQueryContainer} useful as a container for 
 * Vaadins {@link ComboBox} in combination with very large results from databases or other. 
 * <br>
 * The ComboBox needs the index right after filtering and wheres a linear approach might be enough
 * in many cases, all effort to efficiently filter the container would be wasted if all items
 * would be paged through anyway right after filtering. 
 * 
 * @author Michael J. Simons, 2013-11-28
 *
 * @param <T>
 */
public class SmartLazyIdList<T extends Comparable<T>> extends AbstractList<T> implements Serializable {	
	/**
     * Java serialization version UID.
     */
	private static final long serialVersionUID = -1709414329089271002L;
	/** The logger. */
    private static final Logger LOGGER = Logger.getLogger(SmartLazyIdList.class);
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
    public SmartLazyIdList(final LazyQueryView lazyQueryView, final Object idPropertyId) {
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
    @SuppressWarnings("unchecked")
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
        
        final Comparable<T> comparable = (Comparable<T>) o;        
        int low = addedItems.size();
        int high = lazyQueryView.size() - 1;
        int rv = -1;
        int iterationCount = 0;
        while(low<=high && rv < 0) {
        	int mid = low + (high - low) / 2;        	
        	int c = comparable.compareTo((T) lazyQueryView.getItem(mid).getItemProperty(idPropertyId).getValue());
        	if(c<0)
        		high = mid - 1;
        	else if(c>0)
        		low = mid + 1;
        	else
        		rv = mid;
        	++iterationCount;
        }
                
        if(rv >= 0) {
        	LOGGER.debug(String.format("Used %d iterations to retrieve indexOf item from %d items", iterationCount, lazyQueryView.size()));
        } else {
        	LOGGER.warn("Didn't find an index. Please make sure the property Ids are sorted! Switching to linear search.");
        	 // Switching to brute forcing.
            for (int i = addedItems.size(); i < lazyQueryView.size(); i++) {
                if (o.equals(lazyQueryView.getItem(i).getItemProperty(idPropertyId).getValue())) {
                    return i;
                }
            }
        }
                
        return rv;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(final Object o) {
        return indexOf(o) != -1;
    }
}

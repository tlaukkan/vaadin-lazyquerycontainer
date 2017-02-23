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
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.PropertysetItem;

import java.io.Serializable;
import java.util.*;

/**
 * CompositeItem enables joining multiple items as single item. CompositeItem
 * contains PropertysetItem as default item to support adding and removing of
 * properties.
 *
 * @author Tommi Laukkanen
 */
public final class CompositeItem implements Item, Serializable {
    /**
     * Serial version UID for this class.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Key for default item.
     */
    public static final String DEFAULT_ITEM_KEY = "default-item";
    /**
     * List of item keys.
     */
    private List<String> itemKeys = new ArrayList<String>();
    /**
     * Map of items.
     */
    private Map<String, Item> items = new HashMap<String, Item>();
    /**
     * The default item.
     */
    private Item defaultItem = new PropertysetItem();

    /**
     * Default constructor initializes default Item.
     */
    public CompositeItem() {
        addItem(DEFAULT_ITEM_KEY, defaultItem);
    }

    /**
     * Adds new Item.
     *
     * @param key  Key of new Item.
     * @param item Item to be added.
     */
    public void addItem(final String key, final Item item) {
        itemKeys.add(key);
        items.put(key, item);
    }

    /**
     * Removes item.
     *
     * @param key Key of the item to be removed.
     */
    public void removeItem(final String key) {
        itemKeys.remove(key);
        items.remove(key);
    }

    /**
     * Gets keys of Items.
     *
     * @return List of keys.
     */
    public List<String> getItemKeys() {
        return Collections.unmodifiableList(itemKeys);
    }

    /**
     * Gets Item identified by Key.
     *
     * @param key Key of the item to be retrieved.
     * @return Item corresponding to the given key.
     */
    public Item getItem(final String key) {
        return items.get(key);
    }

    /**
     * Lists IDs of the properties in the item.
     *
     * @return Collection of property IDs.
     */
    public Collection<?> getItemPropertyIds() {
        List<Object> itemPropertyIds = new ArrayList<Object>();
        for (String itemKey : itemKeys) {
            Item item = items.get(itemKey);
            for (Object propertyId : item.getItemPropertyIds()) {
                itemPropertyIds.add(propertyId);
            }
        }
        return itemPropertyIds;
    }

    /**
     * Gets Item property by Item id.
     *
     * @param id ID of the property to be retrieved.
     * @return property corresponding to the given ID or null if no matching property is found.
     */
    public Property getItemProperty(final Object id) {
        for (String itemKey : itemKeys) {
            Item item = items.get(itemKey);
            Property property = item.getItemProperty(id);
            if (property != null) {
                return property;
            }
        }
        return null;
    }

    /**
     * Adds property to default Item.
     *
     * @param id       ID of the property to be added.
     * @param property Property to be added.
     * @return true if Property was added successfully.
     */
    public boolean addItemProperty(final Object id, final Property property) {
        return defaultItem.addItemProperty(id, property);
    }

    /**
     * Removes item from default Item.
     *
     * @param id ID of the property to be removed.
     * @return true if Property was removed successfully.
     */
    public boolean removeItemProperty(final Object id) {
        return defaultItem.removeItemProperty(id);
    }

}

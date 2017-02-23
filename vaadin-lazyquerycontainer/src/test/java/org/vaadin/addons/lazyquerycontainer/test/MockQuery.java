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
package org.vaadin.addons.lazyquerycontainer.test;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.ObjectProperty;
import com.vaadin.v7.data.util.PropertysetItem;
import org.vaadin.addons.lazyquerycontainer.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of Query interface for JUnit tests and example application.
 *
 * @author tommilaukkanen
 */
public class MockQuery implements Query {

    private int addedItemCounter = 0;
    private MockQueryFactory queryFactory;
    private List<Item> items;
    private int batchQueryMinTime;
    private int batchQueryMaxTime;
    private Map<Item, Item> cloneMap = new HashMap<Item, Item>();

    public MockQuery(MockQueryFactory queryFactory, List<Item> items, int batchQueryMinTime, int batchQueryMaxTime) {
        this.queryFactory = queryFactory;
        this.items = items;
        this.batchQueryMinTime = batchQueryMinTime;
        this.batchQueryMaxTime = batchQueryMaxTime;
    }


    public List<Item> loadItems(int startIndex, int count) {
        List<Item> resultItems = new ArrayList<Item>();
        for (int i = 0; i < count; i++) {
            // Returning clones to be able to control commit/discard of modifications.
            Item original = items.get(startIndex + i);
            Item clone = cloneItem(original);
            resultItems.add(clone);
            cloneMap.put(clone, original);
        }

        try {
            Thread.sleep(batchQueryMinTime + (int) (Math.random() * batchQueryMaxTime));
        } catch (InterruptedException e) {
        }

        return resultItems;
    }


    public int size() {
        return items.size();
    }


    public Item constructItem() {
        addedItemCounter++;
        return queryFactory.constructItem(-addedItemCounter, -addedItemCounter);
    }


    public boolean deleteAllItems() {
        items.clear();
        return true;
    }

    public void saveItems(List<Item> addedItems, List<Item> modifiedItems,
                          List<Item> removedItems) {
        items.addAll(0, addedItems);
        for (Item clone : removedItems) {
            Item original = cloneMap.get(clone);
            if (addedItems.contains(clone)) {
                // If item is new then it is not mapped through clone map.
                items.remove(clone);
            } else {
                items.remove(original);
            }
        }
        for (Item clone : modifiedItems) {
            Item original = cloneMap.get(clone);
            copyItemValues(original, clone);
        }
    }

    private Item cloneItem(Item originalItem) {
        PropertysetItem newItem = new PropertysetItem();
        for (Object propertyId : originalItem.getItemPropertyIds()) {
            Property originalProperty = originalItem.getItemProperty(propertyId);
            newItem.addItemProperty(propertyId,
                    new ObjectProperty(
                            originalProperty.getValue(),
                            originalProperty.getType(),
                            originalProperty.isReadOnly()
                    ));
        }
        return newItem;
    }

    private void copyItemValues(Item target, Item source) {
        for (Object propertyId : source.getItemPropertyIds()) {
            Property sourceProperty = source.getItemProperty(propertyId);
            Property targetProperty = target.getItemProperty(propertyId);
            boolean readonlyState = targetProperty.isReadOnly();
            targetProperty.setReadOnly(false);
            target.getItemProperty(propertyId).setValue(sourceProperty.getValue());
            targetProperty.setReadOnly(readonlyState);
        }
    }

}

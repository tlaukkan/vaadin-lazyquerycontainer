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
package org.vaadin.addons.lazyquerycontainer.example.mock;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.ObjectProperty;
import com.vaadin.v7.data.util.PropertysetItem;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Mock implementation of QueryFactory interface for JUnit tests and example application.
 *
 * @author Tommi S.E. Laukkanen
 */
public class MockQueryFactory implements QueryFactory {

    private List<Item> items;
    private QueryDefinition definition;
    private int resultSize;
    private int batchQueryMinTime;
    private int batchQueryMaxTime;

    public MockQueryFactory(int resultSize, int batchQueryMinTime, int batchQueryMaxTime) {
        this.resultSize = resultSize;
        this.batchQueryMinTime = batchQueryMinTime;
        this.batchQueryMaxTime = batchQueryMaxTime;
    }

    public void setQueryDefinition(QueryDefinition definition) {
        this.definition = definition;
    }

    public Query constructQuery(QueryDefinition definition) {
        this.definition = definition;
        // Creating items on demand when constructQuery is first time called.
        if (items == null) {
            items = new ArrayList<Item>();

            for (int i = 0; i < resultSize; i++) {

                this.items.add(constructItem(items.size(), resultSize - items.size()));
            }
        }

        if (definition.getSortPropertyIds().length != 0) {
            ItemComparator comparator = new ItemComparator(definition.getSortPropertyIds(),
                    definition.getSortPropertyAscendingStates());
            Collections.sort(this.items, comparator);
        }

        return new MockQuery(this, this.items, batchQueryMinTime, batchQueryMaxTime);
    }

    public Item constructItem(int indexColumnValue, int reverseIndexColumnValue) {
        // since construct item needs to know what the current size is (including added items)
        // to populate Index and ReverseIndex we should provide it somehow here!
        // At the moment adding multiple items leads to strange behaviour.
        PropertysetItem item = new PropertysetItem();
        for (Object propertyId : this.definition.getPropertyIds()) {

            Object value = null;

            if ("Index".equals(propertyId)) {
                value = indexColumnValue;
            } else if ("ReverseIndex".equals(propertyId)) {
                value = reverseIndexColumnValue;
            } else {
                value = this.definition.getPropertyDefaultValue(propertyId);
            }

            item.addItemProperty(propertyId, new ObjectProperty(
                    value,
                    this.definition.getPropertyType(propertyId),
                    this.definition.isPropertyReadOnly(propertyId)
            ));

        }
        return item;
    }

    public void addProperty(Object propertyId, Class<?> type,
                            Object defaultValue, boolean readOnly, boolean sortable) {
        for (Item item : this.items) {
            ((PropertysetItem) item).addItemProperty(
                    propertyId, new ObjectProperty(defaultValue, type, readOnly));

        }
    }

    public class ItemComparator implements Comparator<Item> {
        private Object[] sortPropertyIds;
        private boolean[] ascendingStates;

        public ItemComparator(Object[] sortPropertyIds, boolean[] ascendingStates) {
            this.sortPropertyIds = sortPropertyIds;
            this.ascendingStates = ascendingStates;
        }

        public int compare(Item o1, Item o2) {

            for (int i = 0; i < sortPropertyIds.length; i++) {
                Property p1 = o1.getItemProperty(sortPropertyIds[i]);
                Property p2 = o2.getItemProperty(sortPropertyIds[i]);

                int v1 = (Integer) p1.getValue();
                int v2 = (Integer) p2.getValue();

                if (v1 != v2) {
                    int comparison = v1 - v2;
                    if (!ascendingStates[i]) {
                        comparison = -comparison;
                    }
                    return comparison;
                }
            }

            return 0;
        }

    }

}

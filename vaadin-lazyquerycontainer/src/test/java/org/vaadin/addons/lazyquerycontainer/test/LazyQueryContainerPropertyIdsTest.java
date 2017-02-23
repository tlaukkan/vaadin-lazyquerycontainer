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

import com.vaadin.v7.data.Container.ItemSetChangeEvent;
import com.vaadin.v7.data.Container.ItemSetChangeListener;
import com.vaadin.v7.data.Container.PropertySetChangeEvent;
import com.vaadin.v7.data.Container.PropertySetChangeListener;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import junit.framework.TestCase;
import org.vaadin.addons.lazyquerycontainer.*;

import java.util.Collection;
import java.util.Iterator;

/**
 * JUnit test for testing LazyQueryContainer implementation.
 *
 * @author Tommi S.E. Laukkanen
 */
@SuppressWarnings("serial")
public class LazyQueryContainerPropertyIdsTest extends TestCase implements ItemSetChangeListener, PropertySetChangeListener {

    private final int viewSize = 100;
    private LazyQueryContainer container;
    private boolean itemSetChangeOccurred = false;
    private boolean propertySetChangeOccurred = false;

    protected void setUp() throws Exception {
        super.setUp();

        LazyQueryDefinition definition = new LazyQueryDefinition(true, this.viewSize, "Index");
        definition.addProperty("Index", Integer.class, 0, true, true);
        definition.addProperty("Reverse Index", Integer.class, 0, true, false);
        definition.addProperty("Editable", String.class, "", false, false);
        definition.addProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS, QueryItemStatus.class, QueryItemStatus.None,
                true, false);

        MockQueryFactory factory = new MockQueryFactory(viewSize, 0, 0);
        factory.setQueryDefinition(definition);
        QueryView view = new LazyQueryView(definition, factory);
        container = new LazyQueryContainer(view);
        container.addItemSetChangeListener(this);
        container.addPropertySetChangeListener(this);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSize() {
        assertEquals(viewSize, container.size());
    }

    public void testGetItemIds() {
        Collection<?> ids = container.getItemIds();
        Iterator<?> idIterator = ids.iterator();
        for (int i = 0; i < viewSize; i++) {
            assertEquals(i, idIterator.next());
        }
    }

    public void testGetItem() {
        for (int i = 0; i < viewSize; i++) {
            Item item = container.getItem(container.getIdByIndex(i));
            Property indexProperty = item.getItemProperty("Index");
            assertEquals(i, indexProperty.getValue());
            assertTrue(indexProperty.isReadOnly());
        }
    }

    public void testAscendingSort() {
        container.sort(new Object[]{"Index"}, new boolean[]{true});

        for (int i = 0; i < viewSize; i++) {
            Item item = container.getItem(container.getIdByIndex(i));
            Property indexProperty = item.getItemProperty("Index");
            assertEquals(i, indexProperty.getValue());
            assertTrue(indexProperty.isReadOnly());
        }
    }

    public void testDescendingSort() {
        container.sort(new Object[]{"Index"}, new boolean[]{false});

        for (int i = 0; i < viewSize; i++) {
            Item item = container.getItem(container.getIdByIndex(i));
            Property indexProperty = item.getItemProperty("Index");
            assertEquals(viewSize - i - 1, indexProperty.getValue());
            assertTrue(indexProperty.isReadOnly());
        }
    }

    public void testGetSortablePropertyIds() {
        Collection<?> sortablePropertyIds = container.getSortableContainerPropertyIds();
        assertEquals(1, sortablePropertyIds.size());
        assertEquals("Index", sortablePropertyIds.iterator().next());
    }

    public void testItemSetChangeNotification() {
        container.refresh();
        assertTrue(itemSetChangeOccurred);
    }

    public void containerItemSetChange(ItemSetChangeEvent event) {
        itemSetChangeOccurred = true;
    }

    public void testPropertySetChangeNotification() {
        container.addContainerProperty("NewProperty", Integer.class, 1, true, true);
        assertTrue(propertySetChangeOccurred);
    }

    public void containerPropertySetChange(PropertySetChangeEvent event) {
        propertySetChangeOccurred = true;
    }

    public void testAddCommitItem() {
        int originalViewSize = container.size();
        assertFalse(container.isModified());
        int addItemId = (Integer) container.addItem();
        assertEquals("Item must be added at the beginning", addItemId, -1);
        assertEquals(originalViewSize + 1, container.size());
        assertEquals(QueryItemStatus.Added,
                container.getItem(addItemId).getItemProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());
        assertTrue(container.isModified());
        container.commit();
        assertFalse(container.isModified());
        assertEquals(QueryItemStatus.None,
                container.getItem(addItemId).getItemProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());
    }

    public void testAddTwiceCommitItem() {
        int originalViewSize = container.size();
        assertFalse(container.isModified());
        // Add the first Item
        int addedId = (Integer) container.addItem();
        assertEquals("Item must be added at the beginning", addedId, -1);
        assertEquals(originalViewSize + 1, container.size());
        assertEquals(QueryItemStatus.Added,
                container.getItem(addedId).getItemProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());
        assertTrue(container.isModified());
        // Add a second Item
        addedId = (Integer) container.addItem();
        assertEquals("Second item must be added first as well.", addedId, -2);
        assertEquals(originalViewSize + 2, container.size());
        assertEquals(QueryItemStatus.Added,
                container.getItem(addedId).getItemProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());

        Collection allItemIds = container.getItemIds();
        for (Object itemId : allItemIds) {
            assertEquals(itemId, container.getItem(itemId).getItemProperty("Index").getValue());
        }

        assertTrue(container.isModified());
        container.commit();
        assertFalse(container.isModified());
        assertEquals(QueryItemStatus.None,
                container.getItem(addedId).getItemProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());
    }

    public void testAddDiscardItem() {
        int originalViewSize = container.size();
        assertFalse(container.isModified());
        int addedId = (Integer) container.addItem();
        assertEquals("Item must be added at the beginning", addedId, -1);
        assertEquals(originalViewSize + 1, container.size());
        assertEquals(QueryItemStatus.Added,
                container.getItem(addedId).getItemProperty(
                        LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());
        assertTrue(container.isModified());
        container.discard();
        assertFalse(container.isModified());
        assertEquals(originalViewSize, container.size());
    }

    public void testModifyCommitItem() {
        int modifyIndex = 0;
        assertFalse(container.isModified());
        container.getItem(container.getIdByIndex(modifyIndex)).getItemProperty("Editable").setValue("test");
        assertTrue(container.isModified());
        container.commit();
        assertFalse(container.isModified());
        assertEquals("test", container.getItem(container.getIdByIndex(modifyIndex)).getItemProperty(
                "Editable").getValue());
    }

    public void testModifyDiscardItem() {
        int modifyIndex = 0;
        assertFalse(container.isModified());
        container.getItem(container.getIdByIndex(modifyIndex)).getItemProperty("Editable").setValue("test");
        assertTrue(container.isModified());
        container.discard();
        assertFalse(container.isModified());
        assertEquals("", container.getItem(container.getIdByIndex(modifyIndex)).getItemProperty("Editable").getValue());
    }

    public void testRemoveCommitItem() {
        int removeIndex = 0;
        int originalViewSize = container.size();
        assertFalse(container.isModified());
        assertFalse(container.getItem(container.getIdByIndex(removeIndex)).getItemProperty("Editable").isReadOnly());
        container.removeItem(removeIndex);
        assertEquals(originalViewSize, container.size());
        assertEquals(QueryItemStatus.Removed,
                container.getItem(removeIndex).getItemProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());
        assertTrue(container.getItem(removeIndex).getItemProperty("Editable").isReadOnly());
        assertTrue(container.isModified());
        container.commit();
        assertFalse(container.isModified());
        assertEquals(originalViewSize - 1, container.size());
        assertEquals(removeIndex + 1, container.getItem(container.getIdByIndex(removeIndex)).getItemProperty(
                "Index").getValue());
    }

    public void testRemoveDiscardItem() {
        int removeIndex = 0;
        int originalViewSize = container.size();
        assertFalse(container.isModified());
        assertFalse(container.getItem(container.getIdByIndex(removeIndex)).getItemProperty("Editable").isReadOnly());
        container.removeItem(container.getIdByIndex(removeIndex));
        assertEquals(originalViewSize, container.size());
        assertEquals(QueryItemStatus.Removed,
                container.getItem(container.getIdByIndex(removeIndex)).getItemProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());
        assertTrue(container.getItem(container.getIdByIndex(removeIndex)).getItemProperty("Editable").isReadOnly());
        assertTrue(container.isModified());
        container.discard();
        assertFalse(container.isModified());
        assertEquals(originalViewSize, container.size());
        assertEquals(removeIndex, container.getItem(container.getIdByIndex(removeIndex)).getItemProperty("Index").getValue());
        assertFalse(container.getItem(container.getIdByIndex(removeIndex)).getItemProperty("Editable").isReadOnly());
    }

}

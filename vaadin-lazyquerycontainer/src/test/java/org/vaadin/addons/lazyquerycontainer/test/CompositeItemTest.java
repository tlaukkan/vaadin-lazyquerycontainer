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

import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.ObjectProperty;
import com.vaadin.v7.data.util.PropertysetItem;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.addons.lazyquerycontainer.CompositeItem;

/**
 * Unit tests for CompositeItem.
 *
 * @author Tommi Laukkanen
 */
public class CompositeItemTest {

    private CompositeItem testItem;
    private static final String TEST_PROPERTY_ID = "test-property-id";
    private static final Property testProperty = new ObjectProperty(new Object());
    private static final String TEST_PROPERTY_ID_2 = "test-property-id-2";
    private static final Property testProperty2 = new ObjectProperty(new Object());
    private static final String TEST_ITEM_KEY = "test-item-key";
    private static final PropertysetItem testCompositeItem = new PropertysetItem();

    @Before
    public void setUp() throws Exception {
        testItem = new CompositeItem();
        testItem.addItemProperty(TEST_PROPERTY_ID, testProperty);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.vaadin.addons.lazyquerycontainer.CompositeItem#addItem(java.lang.String, com.vaadin.data.Item)}.
     */
    @Test
    public void testAddItem() {
        testItem.addItem(TEST_ITEM_KEY, testCompositeItem);
        Assert.assertEquals("Does added test item key exists", 2, testItem.getItemKeys().size());
        Assert.assertEquals("Is added test item key correct?", TEST_ITEM_KEY, testItem.getItemKeys().get(1));
        testCompositeItem.addItemProperty(TEST_PROPERTY_ID_2, testProperty2);
        Assert.assertEquals("Does property id list contain the test property and new property", 2, testItem.getItemPropertyIds().size());
        Assert.assertEquals("Is the new property returned correctly?", testProperty2, testItem.getItemProperty(TEST_PROPERTY_ID_2));
    }

    /**
     * Test method for {@link org.vaadin.addons.lazyquerycontainer.CompositeItem#removeItem(java.lang.String, com.vaadin.data.Item)}.
     */
    @Test
    public void testRemoveItem() {
        testItem.removeItem(CompositeItem.DEFAULT_ITEM_KEY);
        Assert.assertEquals("Is item key size 0 after removal of default item.?", 0, testItem.getItemKeys().size());
        Assert.assertNull("Does default item key return null after removal of default item?", testItem.getItem(CompositeItem.DEFAULT_ITEM_KEY));
    }

    /**
     * Test method for {@link org.vaadin.addons.lazyquerycontainer.CompositeItem#getItemKeys()}.
     */
    @Test
    public void testGetItemKeys() {
        Assert.assertEquals("Does default item key exists?", 1, testItem.getItemKeys().size());
        Assert.assertEquals("Is default item key correct?", CompositeItem.DEFAULT_ITEM_KEY, testItem.getItemKeys().get(0));
    }

    /**
     * Test method for {@link org.vaadin.addons.lazyquerycontainer.CompositeItem#getItem(java.lang.String)}.
     */
    @Test
    public void testGetItem() {
        Assert.assertNotNull("Is default item returned correctly?", testItem.getItem(CompositeItem.DEFAULT_ITEM_KEY));
    }

    /**
     * Test method for {@link org.vaadin.addons.lazyquerycontainer.CompositeItem#getItemPropertyIds()}.
     */
    @Test
    public void testGetItemPropertyIds() {
        Assert.assertEquals("Does property id list contain the test property", 1, testItem.getItemPropertyIds().size());
        Assert.assertEquals("Does test property exists?", testProperty, testItem.getItemProperty(TEST_PROPERTY_ID));
    }

    /**
     * Test method for {@link org.vaadin.addons.lazyquerycontainer.CompositeItem#getItemProperty(java.lang.Object)}.
     */
    @Test
    public void testGetItemProperty() {
        Assert.assertEquals("Does test property exists?", testProperty, testItem.getItemProperty(TEST_PROPERTY_ID));
    }

    /**
     * Test method for {@link org.vaadin.addons.lazyquerycontainer.CompositeItem#addItemProperty(java.lang.Object, com.vaadin.data.Property)}.
     */
    @Test
    public void testAddItemProperty() {
        testItem.addItemProperty(TEST_PROPERTY_ID_2, testProperty2);
        Assert.assertEquals("Does property id list contain the test property and new property", 2, testItem.getItemPropertyIds().size());
        Assert.assertEquals("Is the new property returned correctly?", testProperty2, testItem.getItemProperty(TEST_PROPERTY_ID_2));
    }

    /**
     * Test method for {@link org.vaadin.addons.lazyquerycontainer.CompositeItem#removeItemProperty(java.lang.Object)}.
     */
    @Test
    public void testRemoveItemProperty() {
        testItem.removeItemProperty(TEST_PROPERTY_ID);
        Assert.assertEquals("Is property ids length 0 after remove of the test property?", 0, testItem.getItemPropertyIds().size());
        Assert.assertNull("Does asking the property return null after remove", testItem.getItemProperty(TEST_PROPERTY_ID));
    }

}

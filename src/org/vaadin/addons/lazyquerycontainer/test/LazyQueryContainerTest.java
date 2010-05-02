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

import java.util.Collection;
import java.util.Iterator;

import org.vaadin.addons.lazyquerycontainer.DefaultQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryView;
import org.vaadin.addons.lazyquerycontainer.QueryView;

import junit.framework.TestCase;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.data.Container.PropertySetChangeListener;

/**
 * JUnit test for testing LazyQueryContainer implementation.
 * @author Tommi S.E. Laukkanen
 */
@SuppressWarnings("serial")
public class LazyQueryContainerTest extends TestCase implements ItemSetChangeListener, PropertySetChangeListener {

	private final int viewSize=100;
	private LazyQueryContainer container;
	private boolean itemSetChangeOccurred=false;
	private boolean propertySetChangeOccurred=false;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		DefaultQueryDefinition definition=new DefaultQueryDefinition();
		definition.addProperty("Index", Integer.class, 0, true, true);
		definition.addProperty("Reverse Index", Integer.class, 0, true, false);		
		
		MockQueryFactory factory=new MockQueryFactory(viewSize);
		QueryView view=new LazyQueryView(definition,factory);
		container=new LazyQueryContainer(view);
		container.addListener((ItemSetChangeListener)this);
		container.addListener((PropertySetChangeListener)this);		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testSize() {
		assertEquals(viewSize,container.size());
	}
	
	public void testGetItemIds() {
		Collection<?> ids=container.getItemIds();
		Iterator<?> idIterator=ids.iterator();
		for(int i=0;i<viewSize;i++) {
			assertEquals(i,idIterator.next());
		}
	}
	
	public void testGetItem() {
		for(int i=0;i<viewSize;i++) {
			Item item=container.getItem(i);
			Property indexProperty=item.getItemProperty("Index");
			assertEquals(i,indexProperty.getValue());
			assertTrue(indexProperty.isReadOnly());
		}
	}
	
	public void testAscendingSort() {
		container.sort(new Object[]{"Index"},new boolean[]{true});
		
		for(int i=0;i<viewSize;i++) {
			Item item=container.getItem(i);
			Property indexProperty=item.getItemProperty("Index");
			assertEquals(i,indexProperty.getValue());
			assertTrue(indexProperty.isReadOnly());
		}
	}

	public void testDescendingSort() {
		container.sort(new Object[]{"Index"},new boolean[]{false});
		
		for(int i=0;i<viewSize;i++) {
			Item item=container.getItem(i);
			Property indexProperty=item.getItemProperty("Index");
			assertEquals(viewSize-i-1,indexProperty.getValue());
			assertTrue(indexProperty.isReadOnly());
		}
	}
	
	public void testGetSortablePropertyIds() {
		Collection<?> sortablePropertyIds=container.getSortableContainerPropertyIds();
		assertEquals(1,sortablePropertyIds.size());
		assertEquals("Index",sortablePropertyIds.iterator().next());
	}
	
	public void testItemSetChangeNotification() {
		container.refresh();
		assertTrue(itemSetChangeOccurred);
	}

	@Override
	public void containerItemSetChange(ItemSetChangeEvent event) {
		itemSetChangeOccurred=true;
	}

	public void testPropertySetChangeNotification() {
		container.addContainerProperty("NewProperty", Integer.class, 1, true, true);
		assertTrue(propertySetChangeOccurred);
	}
		
	@Override
	public void containerPropertySetChange(PropertySetChangeEvent event) {
		propertySetChangeOccurred=true;
	}

}

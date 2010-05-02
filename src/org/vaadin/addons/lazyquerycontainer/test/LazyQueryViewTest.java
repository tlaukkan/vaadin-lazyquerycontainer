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

import org.vaadin.addons.lazyquerycontainer.DefaultQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.LazyQueryView;

import junit.framework.TestCase;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * JUnit test for testing LazyQueryView implementation.
 * @author Tommi S.E. Laukkanen
 */
public class LazyQueryViewTest extends TestCase {

	private final int viewSize=100;
	private LazyQueryView view;
		
	protected void setUp() throws Exception {
		super.setUp();
		
		DefaultQueryDefinition definition=new DefaultQueryDefinition();
		definition.addProperty("Index", Integer.class, 0, true, true);
		definition.addProperty("Reverse Index", Integer.class, 0, true, false);
		MockQueryFactory factory=new MockQueryFactory(viewSize);
		factory.setQueryDefinition(definition);
		view=new LazyQueryView(definition,factory);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testSize() {
		assertEquals(viewSize, view.size());		
	}
	
	public void testGetItem() {
		for(int i=0;i<viewSize;i++) {
			Item item=view.getItem(i);
			Property indexProperty=item.getItemProperty("Index");
			assertEquals(i,indexProperty.getValue());
			assertTrue(indexProperty.isReadOnly());
		}
	}
	
	public void testAscendingSort() {
		view.sort(new Object[]{"Index"},new boolean[]{true});
		
		for(int i=0;i<viewSize;i++) {
			Item item=view.getItem(i);
			Property indexProperty=item.getItemProperty("Index");
			assertEquals(i,indexProperty.getValue());
			assertTrue(indexProperty.isReadOnly());
		}
	}

	public void testDescendingSort() {
		view.sort(new Object[]{"Index"},new boolean[]{false});
		
		for(int i=0;i<viewSize;i++) {
			Item item=view.getItem(i);
			Property indexProperty=item.getItemProperty("Index");
			assertEquals(viewSize-i-1,indexProperty.getValue());
			assertTrue(indexProperty.isReadOnly());
		}
	}
	
	public void testGetSortablePropertyIds() {
		Collection<?> sortablePropertyIds=view.getDefinition().getSortablePropertyIds();
		assertEquals(1,sortablePropertyIds.size());
		assertEquals("Index",sortablePropertyIds.iterator().next());
	}

}

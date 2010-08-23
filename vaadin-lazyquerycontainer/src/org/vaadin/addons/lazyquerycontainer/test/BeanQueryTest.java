package org.vaadin.addons.lazyquerycontainer.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.addons.lazyquerycontainer.BeanQueryFactory;
import org.vaadin.addons.lazyquerycontainer.DefaultQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.LazyQueryView;
import org.vaadin.addons.lazyquerycontainer.QueryItemStatus;

import com.vaadin.data.Item;

public class BeanQueryTest {

	private LazyQueryView view;
	
	@Before
	public void setUp() throws Exception {
		MockBeanQuery.reset();
		
		DefaultQueryDefinition definition=new DefaultQueryDefinition(50);
		definition.addProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS, QueryItemStatus.class, QueryItemStatus.None, true, false);
		definition.addProperty("name", String.class, "test-bean-2", true, false);		
		BeanQueryFactory<MockBeanQuery,MockBean> factory=new BeanQueryFactory<MockBeanQuery,MockBean>(MockBeanQuery.class,MockBean.class);
		factory.setQueryDefinition(definition);
		view=new LazyQueryView(definition,factory);
	}

	@Test
	public void testLoadItems() {
		Assert.assertEquals(1, view.size());
		Item item=view.getItem(0);
		Assert.assertEquals("test-bean-1", item.getItemProperty("name").getValue());
	}
	
	@Test
	public void testSaveItems() {
		Assert.assertEquals(1, view.size());
		int index=view.addItem();
		Assert.assertEquals(2, view.size());
		Item item=view.getItem(index);
		Assert.assertEquals("test-bean-2", item.getItemProperty("name").getValue());
		Assert.assertFalse((Boolean)item.getItemProperty("saved").getValue());
		view.commit();	
		Assert.assertTrue((Boolean)item.getItemProperty("saved").getValue());
	}

}

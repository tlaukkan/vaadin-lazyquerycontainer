package org.vaadin.addons.lazyquerycontainer.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.myfaces.shared_impl.util.Assert;
import org.vaadin.addons.lazyquerycontainer.AbstractBeanQuery;

public class MockBeanQuery extends AbstractBeanQuery<MockBean> {

	private static ArrayList<MockBean> beans=new ArrayList<MockBean>();
	
	public static void reset() {
		beans.clear();
		MockBean mockBean=new MockBean();
		mockBean.setName("test-bean-1");
		beans.add(mockBean);
	}
	
	public MockBeanQuery() {
	}
	
	@Override
	public int size() {
		return beans.size();
	}

	@Override
	protected List<MockBean> loadBeans(int startIndex, int count) {
		return beans.subList(startIndex, startIndex+count);
	}

	@Override
	protected void saveBeans(List<MockBean> addedItems, List<MockBean> modifiedItems, List<MockBean> removedItems) {
		for(MockBean bean : addedItems) {
			Assert.notNull(bean.getName());
			bean.setSaved(true);
		}
		for(MockBean bean : modifiedItems) {
			Assert.notNull(bean.getName());
			bean.setSaved(true);
		}
		for(MockBean bean : removedItems) {
			Assert.notNull(bean.getName());
			bean.setSaved(true);
		}
	}

}

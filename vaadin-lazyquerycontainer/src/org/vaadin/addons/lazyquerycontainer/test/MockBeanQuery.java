package org.vaadin.addons.lazyquerycontainer.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.myfaces.shared_impl.util.Assert;
import org.vaadin.addons.lazyquerycontainer.AbstractBeanQuery;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

public class MockBeanQuery extends AbstractBeanQuery<MockBean> {

	public MockBeanQuery(QueryDefinition definition,
			Map<String, Object> queryConfiguration, Object[] sortPropertyIds,
			boolean[] sortStates) {
		super(definition, queryConfiguration, sortPropertyIds, sortStates);
	}

	private static ArrayList<MockBean> beans=new ArrayList<MockBean>();
	
	public static void reset() {
		beans.clear();
		MockBean mockBean=new MockBean();
		mockBean.setName("test-bean-1");
		mockBean.setDescription("test-bean-description-1");
		beans.add(mockBean);
	}
	
	@Override
	public int size() {
		return beans.size();
	}
	
	@Override
	protected MockBean constructBean() {
		MockBean mockBean=new MockBean();
		mockBean.setDescription((String)queryConfiguration.get("description"));
		return mockBean;
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

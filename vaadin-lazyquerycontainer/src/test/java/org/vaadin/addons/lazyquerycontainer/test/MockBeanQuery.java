package org.vaadin.addons.lazyquerycontainer.test;

import junit.framework.Assert;
import org.vaadin.addons.lazyquerycontainer.AbstractBeanQuery;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockBeanQuery extends AbstractBeanQuery<MockBean> {

    public MockBeanQuery() {
        super();
    }


    public MockBeanQuery(QueryDefinition definition,
                         Map<String, Object> queryConfiguration, Object[] sortPropertyIds,
                         boolean[] sortStates) {
        super(definition, queryConfiguration, sortPropertyIds, sortStates);
        Assert.assertEquals(definition, getQueryDefinition());
        Assert.assertEquals(sortPropertyIds, getSortPropertyIds());
        Assert.assertEquals(sortStates, getSortStates());
    }

    private static ArrayList<MockBean> beans = new ArrayList<MockBean>();

    public static void reset() {
        beans.clear();
        MockBean mockBean = new MockBean();
        mockBean.setName("test-bean-1");
        mockBean.setDescription("test-bean-description-1");
        beans.add(mockBean);
    }


    public int size() {
        return beans.size();
    }


    protected MockBean constructBean() {
        MockBean mockBean = new MockBean();
        mockBean.setDescription((String) getQueryConfiguration().get("description"));
        return mockBean;
    }


    protected List<MockBean> loadBeans(int startIndex, int count) {
        return beans.subList(startIndex, startIndex + count);
    }

    protected void saveBeans(List<MockBean> addedItems, List<MockBean> modifiedItems, List<MockBean> removedItems) {
        for (MockBean bean : addedItems) {
            Assert.assertNotNull(bean.getName());
            bean.setSaved(true);
        }
        for (MockBean bean : modifiedItems) {
            Assert.assertNotNull(bean.getName());
            bean.setSaved(true);
        }
        for (MockBean bean : removedItems) {
            Assert.assertNotNull(bean.getName());
            bean.setSaved(true);
        }
    }

}

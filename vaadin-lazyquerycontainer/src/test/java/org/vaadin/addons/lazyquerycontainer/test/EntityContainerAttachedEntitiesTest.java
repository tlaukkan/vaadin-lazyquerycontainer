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
import com.vaadin.v7.data.util.filter.And;
import com.vaadin.v7.data.util.filter.Compare;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.addons.lazyquerycontainer.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Unit test for LazyEntityContainer.
 *
 * @author Tommi Laukkanen
 */
public class EntityContainerAttachedEntitiesTest {

    /**
     * Query cache size.
     */
    private static final int QUERY_CACHE_SIZE = 1000;
    /**
     * Item count for cache test.
     */
    private static final int ITEM_COUNT_FOR_CACHE_TEST = 2000;
    /**
     * Batch size for entity container.
     */
    private static final int ENTITY_CONTAINER_BATCH_SIZE = 100;
    /**
     * The JPA EntityManagerFactory.
     */
    private static EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("vaadin-lazyquerycontainer-test");
    /**
     * The JPA EntityManager.
     */
    private EntityManager entityManager;

    /**
     * Unit test setup.
     */
    @Before
    public void before() {
        entityManager = entityManagerFactory
                .createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery("delete from Task").executeUpdate();
        entityManager.getTransaction().commit();
    }

    /**
     * Unit test teardown.
     */
    @After
    public void after() {
    }

    /**
     * Test for entity container functionality.
     */
    @Test
    public final void testEntityContainer() {
        final LazyEntityContainer<Task> entityContainer = new LazyEntityContainer<Task>(
                entityManager, Task.class,
                ENTITY_CONTAINER_BATCH_SIZE, null, true, false, true);
        entityContainer.getQueryView().getQueryDefinition().setDefaultSortState(
                new String[]{"name"}, new boolean[]{true});
        entityContainer.getQueryView().getQueryDefinition().setMaxNestedPropertyDepth(3);

        final Company company = new Company();
        company.setName("test-company");
        final Author author = new Author();
        author.setName("test-author");
        author.setCompany(company);

        final Task taskAlpha = entityContainer.addEntity();
        taskAlpha.setName("alpha");
        taskAlpha.setAssignee("assignee-alpha");
        taskAlpha.setReporter("reporter-alpha");
        taskAlpha.setAuthor(author);

        entityContainer.commit();
        entityContainer.commit();

        Assert.assertEquals("Verify entity alpha is in container", 1, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha, entityContainer.getEntity(0));

        final Task taskBeta = entityContainer.addEntity();
        taskBeta.setName("beta");
        taskBeta.setAssignee("assignee-beta");
        taskBeta.setReporter("reporter-beta");
        taskBeta.setAuthor(author);

        entityContainer.commit();

        Assert.assertEquals("Verify entity alpha and beta are in container", 2, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha, entityContainer.getEntity(0));
        Assert.assertEquals("Verify entity beta is same", taskBeta, entityContainer.getEntity(1));

        entityContainer.sort(new String[]{"name", "assignee"}, new boolean[]{false, false});

        Assert.assertEquals("Verify entity alpha and beta are in container", 2, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha, entityContainer.getEntity(1));
        Assert.assertEquals("Verify entity beta is same", taskBeta, entityContainer.getEntity(0));

        /*final Map<String, Object> whereParameters = new HashMap<String, Object>();
        whereParameters.put("name", "alpha");
        whereParameters.put("assignee", "assignee-alpha");
        entityContainer.filter("e.name=:name and e.assignee=:assignee", whereParameters);*/

        entityContainer.addContainerFilter(
                new And(new Compare.Equal("name", "alpha"), new Compare.Equal("assignee", "assignee-alpha")));

        Assert.assertEquals("Verify entity alpha is in container", 1, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha, entityContainer.getEntity(0));

        entityContainer.removeAllContainerFilters();

        Assert.assertEquals("Verify entity alpha and beta are in container", 2, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha, entityContainer.getEntity(1));
        Assert.assertEquals("Verify entity beta is same", taskBeta, entityContainer.getEntity(0));

        entityContainer.getItem(new Integer(1)).getItemProperty("name").setValue("gamme");

        entityContainer.commit();

        Assert.assertEquals("Verify entity alpha and beta are in container", 2, entityContainer.size());
        Assert.assertEquals("Verify entity alpha is same", taskAlpha, entityContainer.getEntity(0));
        Assert.assertEquals("Verify entity beta is same", taskBeta, entityContainer.getEntity(1));

        final Task removedTask = entityContainer.removeEntity(0);
        Assert.assertEquals("Verify entity alpha was the removed entity", taskAlpha, removedTask);

        entityContainer.commit();

        Assert.assertEquals("Verify entity beta is in container", 1, entityContainer.size());
        Assert.assertEquals("Verify entity beta is same", taskBeta, entityContainer.getEntity(0));

        final Item betaItemBeforeRefresh = entityContainer.getItem(new Integer(0));
        Assert.assertNull("Verify new property does not exist before refresh.",
                betaItemBeforeRefresh.getItemProperty("description"));

        entityContainer.addContainerProperty("description", String.class, "");
        entityContainer.addContainerProperty("author.name", String.class, "");
        entityContainer.addContainerProperty("author.company.name", String.class, "");

        entityContainer.refresh();

        final Item betaItem = entityContainer.getItem(new Integer(0));
        Assert.assertNotNull("Verify new property exists.", betaItem.getItemProperty("description"));
        Assert.assertEquals("Verify new property has correct default value.",
                "", betaItem.getItemProperty("description").getValue());

        Assert.assertEquals("Verify new property has correct value.",
                "test-author", betaItem.getItemProperty("author.name").getValue());
        Assert.assertEquals("Verify new property has correct value.",
                "test-company", betaItem.getItemProperty("author.company.name").getValue());

        taskBeta.setAuthor(null);
        entityManager.persist(taskBeta);
        entityContainer.refresh();

        Assert.assertEquals("Verify new property has correct value.",
                null, betaItem.getItemProperty("author.name").getValue());
        Assert.assertEquals("Verify new property has correct value.",
                null, betaItem.getItemProperty("author.company.name").getValue());

        Assert.assertEquals("Verify item is CompositeItem", CompositeItem.class, betaItem.getClass());

        entityContainer.getQueryView().getQueryDefinition().setCompositeItems(false);
        entityContainer.refresh();
        Assert.assertEquals("Verify item is BeanItem", NestingBeanItem.class, entityContainer.getItem(
                new Integer(0)).getClass());
        Assert.assertNotNull("Verify that entity can be accessed", entityContainer.getEntity(new Integer(0)));

        entityContainer.removeAllItems();
        Assert.assertEquals("Verify container is empty after remove all.", 0, entityContainer.size());

        final Object itemId = entityContainer.addItem();
        entityContainer.removeItem(itemId);

        entityContainer.commit();
        Assert.assertEquals("Verify container is empty after add remove item.", 0, entityContainer.size());

    }


    /**
     * Test cache.
     */
    @Test
    public final void testCache() {
        final EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("vaadin-lazyquerycontainer-test");
        final EntityManager entityManager = entityManagerFactory
                .createEntityManager();
        final LazyEntityContainer<Task> entityContainer = new LazyEntityContainer<Task>(
                entityManager, Task.class,
                ENTITY_CONTAINER_BATCH_SIZE, null, true, false, true);
        entityContainer.getQueryView().getQueryDefinition().setDefaultSortState(
                new String[]{"name"}, new boolean[]{true});

        entityContainer.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX,
                Integer.class, new Integer(0));

        for (int i = 0; i < ITEM_COUNT_FOR_CACHE_TEST; i++) {
            entityContainer.addItem();
        }
        entityContainer.commit();
        for (int i = 0; i < ITEM_COUNT_FOR_CACHE_TEST; i++) {
            final Item item = entityContainer.getItem(i);
            Assert.assertEquals("Verify batch index", i / ENTITY_CONTAINER_BATCH_SIZE,
                    item.getItemProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX).getValue());
        }
        for (int i = ITEM_COUNT_FOR_CACHE_TEST - 1; i >= QUERY_CACHE_SIZE; i--) {
            final Item item = entityContainer.getItem(i);
            Assert.assertEquals("Verify batch index", i / ENTITY_CONTAINER_BATCH_SIZE,
                    item.getItemProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX).getValue());
        }
        for (int i = QUERY_CACHE_SIZE - 1; i >= 0; i--) {
            final Item item = entityContainer.getItem(i);
            Assert.assertEquals("Verify batch index", ITEM_COUNT_FOR_CACHE_TEST / ENTITY_CONTAINER_BATCH_SIZE
                    + (QUERY_CACHE_SIZE - 1 - i) / ENTITY_CONTAINER_BATCH_SIZE,
                    item.getItemProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX).getValue());
        }
    }

    /**
     * Test query with background deleted entities after initialization.
     */
    @Test
    public final void testQueryWithBackgroudDeletedEntities() {
        final EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("vaadin-lazyquerycontainer-test");
        final EntityManager entityManager = entityManagerFactory
                .createEntityManager();
        final LazyEntityContainer<Task> entityContainer = new LazyEntityContainer<Task>(
                entityManager, Task.class,
                ENTITY_CONTAINER_BATCH_SIZE, null, true, false, true);
        entityContainer.getQueryView().getQueryDefinition().setDefaultSortState(
                new String[]{"name"}, new boolean[]{true});
        entityContainer.addContainerProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS,
                QueryItemStatus.class, QueryItemStatus.None);

        entityContainer.addEntity();
        final Task taskTwo = entityContainer.addEntity();
        entityContainer.commit();

        Assert.assertEquals("Verify container size", 2, entityContainer.size());

        entityManager.getTransaction().begin();
        entityManager.remove(taskTwo);
        entityManager.getTransaction().commit();

        Assert.assertEquals("Verify container size", 2, entityContainer.size());

        Assert.assertEquals("Verify that entity is deleted.",
                QueryItemStatus.None,
                entityContainer.getItem(0).getItemProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());

        Assert.assertEquals("Verify that entity is deleted.",
                QueryItemStatus.Removed,
                entityContainer.getItem(1).getItemProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS).getValue());

        entityContainer.commit();

        Assert.assertEquals("Verify container size", 1, entityContainer.size());
    }
}

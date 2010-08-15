package org.vaadin.addons.lazyquerycontainer.example.test;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.addons.lazyquerycontainer.example.LazyQueryContainerExampleApplication;
import org.vaadin.addons.lazyquerycontainer.example.Task;

public class TaskTest {

	private EntityManager entityManager;
	private static final String TEST_TASK_NAME="test-task-name";
	private static final String TEST_TASK_REPORTER="test-task-reporter";
	private static final String TEST_TASK_ASSIGNEE="test-task-assignee";
	
	@Before
	public void setUp() throws Exception {
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(LazyQueryContainerExampleApplication.PERSISTENCE_UNIT);
		entityManager = entityManagerFactory.createEntityManager();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testTaskPersistence() {
		Task task=new Task();
		task.setName(TEST_TASK_NAME);
		task.setReporter(TEST_TASK_REPORTER);
		task.setAssignee(TEST_TASK_ASSIGNEE);
		
		EntityTransaction transaction=entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(task);
		transaction.commit();
		
		Query query=entityManager.createQuery("select t from Task as t");
		List<Task> result=query.getResultList();
		Assert.assertEquals("Did select find the saved task.",1,result.size());
		
	}

}

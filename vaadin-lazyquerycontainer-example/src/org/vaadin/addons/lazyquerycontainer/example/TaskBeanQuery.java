package org.vaadin.addons.lazyquerycontainer.example;

import java.util.List;

import org.vaadin.addons.lazyquerycontainer.AbstractBeanQuery;

public class TaskBeanQuery extends AbstractBeanQuery<Task> {

	@Override
	protected Task constructBean() {
		return new Task();
	}

	@Override
	public int size() {
		TaskService taskService=(TaskService)queryConfiguration.get("taskService");		
		return taskService.countTasks();
	}

	@Override
	protected List<Task> loadBeans(int startIndex, int count) {
		TaskService taskService=(TaskService)queryConfiguration.get("taskService");		
		return taskService.loadTasks(startIndex, count, sortPropertyIds, sortStates);
	}

	@Override
	protected void saveBeans(List<Task> addedTasks, List<Task> modifiedTasks, List<Task> removedTasks) {
		TaskService taskService=(TaskService)queryConfiguration.get("taskService");
		taskService.saveTasks(addedTasks, modifiedTasks, removedTasks);
	}

}

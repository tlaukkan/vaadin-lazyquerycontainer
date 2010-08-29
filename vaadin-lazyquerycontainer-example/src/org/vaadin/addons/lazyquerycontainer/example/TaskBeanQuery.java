package org.vaadin.addons.lazyquerycontainer.example;

import java.util.List;
import java.util.Map;

import org.vaadin.addons.lazyquerycontainer.AbstractBeanQuery;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

public class TaskBeanQuery extends AbstractBeanQuery<Task> {

	public TaskBeanQuery(QueryDefinition definition,
			Map<String, Object> queryConfiguration, Object[] sortPropertyIds,
			boolean[] sortStates) {
		super(definition, queryConfiguration, sortPropertyIds, sortStates);
	}

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

package org.vaadin.addons.lazyquerycontainer.example.jpa;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: Task
 *
 */
@Entity

public class Task implements Serializable {

	
	private int taskId;
	private String name;
	private String reporter;
	private String assignee;
	private static final long serialVersionUID = 1L;

	public Task() {
		super();
	}
	
	@Id
	@GeneratedValue
	public int getTaskId() {
		return this.taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}   
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}   
	public String getReporter() {
		return this.reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}   
	public String getAssignee() {
		return this.assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}   
  
}

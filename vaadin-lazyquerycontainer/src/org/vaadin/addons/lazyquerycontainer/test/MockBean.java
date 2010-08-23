package org.vaadin.addons.lazyquerycontainer.test;

public class MockBean {
	private String name;
	private boolean saved;
	
	public MockBean() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}
	
}

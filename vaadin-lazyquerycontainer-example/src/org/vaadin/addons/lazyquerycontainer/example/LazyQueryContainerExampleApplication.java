package org.vaadin.addons.lazyquerycontainer.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.vaadin.addons.lazyquerycontainer.BeanQueryFactory;
import org.vaadin.addons.lazyquerycontainer.JpaQueryFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryView;
import org.vaadin.addons.lazyquerycontainer.QueryItemStatus;
import org.vaadin.addons.lazyquerycontainer.QueryItemStatusColumnGenerator;

import com.vaadin.Application;
import com.vaadin.ui.AbstractSelect.MultiSelectMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

/**
 * Example application demonstrating the Lazy Query Container features.
 * @author Tommi S.E. Laukkanen
 */
@SuppressWarnings("rawtypes")
public class LazyQueryContainerExampleApplication extends Application implements ClickListener {
	private static final long serialVersionUID = 1L;

	public static final String PERSISTENCE_UNIT="vaadin-lazyquerycontainer-example";
	
	private Button refreshButton;
	private Button editButton;
	private Button saveButton;
	private Button cancelButton;
	private Button addItemButton;
	private Button removeItemButton;

	private Table tableOne;
	private LazyQueryContainer containerOne;

	private Table tableTwo;
	private LazyQueryContainer containerTwo;

	
	private ArrayList<Object> visibleColumnIds=new ArrayList<Object>();
	private ArrayList<String> visibleColumnLabels=new ArrayList<String>();
	
	
	@Override
	public void init() {
		
		Window mainWindow = new Window("Lazycontainer Application");
		
		VerticalLayout mainLayout=new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainWindow.setContent(mainLayout);
						
		Panel buttonPanel=new Panel();
		buttonPanel.addStyleName(Runo.PANEL_LIGHT);
		HorizontalLayout buttonLayout=new HorizontalLayout();
		buttonLayout.setMargin(false);
		buttonLayout.setSpacing(true);
		buttonPanel.setContent(buttonLayout);		
		mainWindow.addComponent(buttonPanel);

		Panel buttonPanel2=new Panel();
		buttonPanel2.addStyleName(Runo.PANEL_LIGHT);
		HorizontalLayout buttonLayout2=new HorizontalLayout();
		buttonLayout2.setMargin(false);
		buttonLayout2.setSpacing(true);
		buttonPanel2.setContent(buttonLayout2);		
		mainWindow.addComponent(buttonPanel2);

		
		refreshButton=new Button("Refresh");
		refreshButton.addListener(this);
		buttonPanel.addComponent(refreshButton);
		
		editButton=new Button("Edit");
		editButton.addListener(this);
		buttonPanel.addComponent(editButton);
		
		saveButton=new Button("Save");
		saveButton.addListener(this);
		saveButton.setEnabled(false);
		buttonPanel2.addComponent(saveButton);

		cancelButton=new Button("Cancel");
		cancelButton.addListener(this);
		cancelButton.setEnabled(false);
		buttonPanel2.addComponent(cancelButton);

		addItemButton=new Button("Add Row");
		addItemButton.addListener(this);
		addItemButton.setEnabled(false);
		buttonPanel2.addComponent(addItemButton);

		removeItemButton=new Button("Remove Row");
		removeItemButton.addListener(this);
		removeItemButton.setEnabled(false);
		buttonPanel2.addComponent(removeItemButton);
						
		visibleColumnIds.add(LazyQueryView.PROPERTY_ID_ITEM_STATUS);
		visibleColumnIds.add("name");
		visibleColumnIds.add("reporter");
		visibleColumnIds.add("assignee");
		visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX);
		visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX);
		visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME);

		visibleColumnLabels.add("");
		visibleColumnLabels.add("Name");
		visibleColumnLabels.add("Reporter");
		visibleColumnLabels.add("Assignee");
		visibleColumnLabels.add("Query");
		visibleColumnLabels.add("Batch");
		visibleColumnLabels.add("Time [ms]");

		
		EntityManagerFactory entityManagerFactory = Persistence
		.createEntityManagerFactory(PERSISTENCE_UNIT);
		EntityManager entityManager = entityManagerFactory
		.createEntityManager();

		{
			tableOne = new Table();

			tableOne.setCaption("JpaQuery");
			tableOne.setPageLength(10);

			JpaQueryFactory<Task> queryFactory = new JpaQueryFactory<Task>(
					entityManager, Task.class, "SELECT t from Task as t",
					"SELECT count(t) from Task as t", new Object[] { "name" },
					new boolean[] { true }, true);
			containerOne = new LazyQueryContainer(queryFactory, 50);

			containerOne.addContainerProperty(
					LazyQueryView.PROPERTY_ID_ITEM_STATUS,
					QueryItemStatus.class, QueryItemStatus.None, true, false);

			containerOne.addContainerProperty("name", String.class, "", true,
					true);
			containerOne.addContainerProperty("reporter", String.class, "",
					true, true);
			containerOne.addContainerProperty("assignee", String.class, "",
					true, true);

			containerOne.addContainerProperty(
					LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX, Integer.class,
					0, true, false);
			containerOne.addContainerProperty(
					LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX, Integer.class,
					0, true, false);
			containerOne.addContainerProperty(
					LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME,
					Integer.class, 0, true, false);

			tableOne.setContainerDataSource(containerOne);

			tableOne.setColumnWidth("name", 135);
			tableOne.setColumnWidth("reporter", 135);
			tableOne.setColumnWidth("assignee", 135);

			tableOne.setVisibleColumns(visibleColumnIds.toArray());
			tableOne.setColumnHeaders(visibleColumnLabels
					.toArray(new String[0]));

			tableOne.setColumnWidth(LazyQueryView.PROPERTY_ID_ITEM_STATUS, 16);
			tableOne.addGeneratedColumn(LazyQueryView.PROPERTY_ID_ITEM_STATUS,
					new QueryItemStatusColumnGenerator(this));

			tableOne.setEditable(false);
			tableOne.setMultiSelect(true);
			tableOne.setMultiSelectMode(MultiSelectMode.DEFAULT);
			tableOne.setSelectable(true);
			tableOne.setWriteThrough(true);

			mainWindow.addComponent(tableOne);
		}
		
		{
			tableTwo = new Table();

			tableTwo.setCaption("BeanQuery with JPA Backend");
			tableTwo.setPageLength(10);
			
			TaskService taskService=new TaskService(entityManager);
			
			BeanQueryFactory<TaskBeanQuery> queryFactory = new BeanQueryFactory<TaskBeanQuery>(TaskBeanQuery.class);

			Map<String,Object> queryConfiguration=new HashMap<String,Object>();
			queryConfiguration.put("taskService",taskService);
			
			queryFactory.setQueryConfiguration(queryConfiguration);
			
			
			containerTwo = new LazyQueryContainer(queryFactory, 50);

			containerTwo.addContainerProperty(
					LazyQueryView.PROPERTY_ID_ITEM_STATUS,
					QueryItemStatus.class, QueryItemStatus.None, true, false);

			containerTwo.addContainerProperty("name", String.class, "", true,
					true);
			containerTwo.addContainerProperty("reporter", String.class, "",
					true, true);
			containerTwo.addContainerProperty("assignee", String.class, "",
					true, true);

			containerTwo.addContainerProperty(
					LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX, Integer.class,
					0, true, false);
			containerTwo.addContainerProperty(
					LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX, Integer.class,
					0, true, false);
			containerTwo.addContainerProperty(
					LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME,
					Integer.class, 0, true, false);

			tableTwo.setContainerDataSource(containerTwo);

			tableTwo.setColumnWidth("name", 135);
			tableTwo.setColumnWidth("reporter", 135);
			tableTwo.setColumnWidth("assignee", 135);

			tableTwo.setVisibleColumns(visibleColumnIds.toArray());
			tableTwo.setColumnHeaders(visibleColumnLabels
					.toArray(new String[0]));

			tableTwo.setColumnWidth(LazyQueryView.PROPERTY_ID_ITEM_STATUS, 16);
			tableTwo.addGeneratedColumn(LazyQueryView.PROPERTY_ID_ITEM_STATUS,
					new QueryItemStatusColumnGenerator(this));

			tableTwo.setEditable(false);
			tableTwo.setMultiSelect(true);
			tableTwo.setMultiSelectMode(MultiSelectMode.DEFAULT);
			tableTwo.setSelectable(true);
			tableTwo.setWriteThrough(true);

			mainWindow.addComponent(tableTwo);
		}

		setMainWindow(mainWindow);
	}

	private void setEditMode(boolean editMode) {
		if(editMode) {
			tableOne.setEditable(true);
			tableOne.setSortDisabled(true);
			tableTwo.setEditable(true);
			tableTwo.setSortDisabled(true);
			refreshButton.setEnabled(false);
			editButton.setEnabled(false);
			saveButton.setEnabled(true);
			cancelButton.setEnabled(true);
			addItemButton.setEnabled(true);
			removeItemButton.setEnabled(true);
		} else {
			tableOne.setEditable(false);
			tableOne.setSortDisabled(false);
			tableTwo.setEditable(false);
			tableTwo.setSortDisabled(false);
			refreshButton.setEnabled(true);
			editButton.setEnabled(true);
			saveButton.setEnabled(false);
			cancelButton.setEnabled(false);
			addItemButton.setEnabled(false);
			removeItemButton.setEnabled(false);
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==refreshButton) {
			containerOne.refresh();
			containerTwo.refresh();
		}
		if(event.getButton()==editButton) {
			setEditMode(true);
		}
		if(event.getButton()==saveButton) {
			containerOne.commit();
			containerTwo.commit();
			containerOne.refresh();
			setEditMode(false);
		}
		if(event.getButton()==cancelButton) {
			containerOne.discard();
			containerTwo.discard();
			containerOne.refresh();
			setEditMode(false);
		}
		if(event.getButton()==addItemButton) {
			containerOne.addItem();
			containerTwo.addItem();
		}
		if(event.getButton()==removeItemButton) {
			{
				Object selection = tableOne.getValue();
				if (selection == null) {
					return;
				}
				if (selection instanceof Integer) {
					Integer selectedIndex = (Integer) selection;
					if (selectedIndex != null) {
						containerOne.removeItem(selectedIndex);
					}
				}
				if (selection instanceof Collection) {
					Collection selectionIndexes = (Collection) selection;
					for (Object selectedIndex : selectionIndexes) {
						containerOne.removeItem((Integer) selectedIndex);
					}
				}
			}
			{
				Object selection = tableTwo.getValue();
				if (selection == null) {
					return;
				}
				if (selection instanceof Integer) {
					Integer selectedIndex = (Integer) selection;
					if (selectedIndex != null) {
						containerTwo.removeItem(selectedIndex);
					}
				}
				if (selection instanceof Collection) {
					Collection selectionIndexes = (Collection) selection;
					for (Object selectedIndex : selectionIndexes) {
						containerTwo.removeItem((Integer) selectedIndex);
					}
				}
			}
		}
	}

}

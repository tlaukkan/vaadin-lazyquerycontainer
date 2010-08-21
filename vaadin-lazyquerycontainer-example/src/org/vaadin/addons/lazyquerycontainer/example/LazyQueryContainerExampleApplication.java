package org.vaadin.addons.lazyquerycontainer.example;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
	
	private LazyQueryContainer container;
	private Button refreshButton;
	private Button editButton;
	private Button saveButton;
	private Button cancelButton;
	private Button addItemButton;
	private Button removeItemButton;

	private Table table;

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
				
		table=new Table();
		
		table.setPageLength(20);
		
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
				
		JpaQueryFactory<Task> queryFactory=new JpaQueryFactory<Task>(entityManager,Task.class,"SELECT t from Task as t","SELECT count(t) from Task as t",new Object[]{"name"},new boolean[]{true});
		container=new LazyQueryContainer(queryFactory,50);
		
		container.addContainerProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS, QueryItemStatus.class, QueryItemStatus.None, true, false);
		
		container.addContainerProperty("name", String.class, "", true, true);
		container.addContainerProperty("reporter", String.class, "", true, true);
		container.addContainerProperty("assignee", String.class, "", true, true);

		container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX, Integer.class, 0, true, false);
		container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX, Integer.class, 0, true, false);
		container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME, Integer.class, 0, true, false);
		
		table.setContainerDataSource(container);
	
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
		
		table.setColumnWidth("name", 135);
		table.setColumnWidth("reporter", 135);
		table.setColumnWidth("assignee", 135);

		table.setVisibleColumns(visibleColumnIds.toArray());
		table.setColumnHeaders(visibleColumnLabels.toArray(new String[0]));
		
		table.setColumnWidth(LazyQueryView.PROPERTY_ID_ITEM_STATUS, 16);
		table.addGeneratedColumn(LazyQueryView.PROPERTY_ID_ITEM_STATUS, new QueryItemStatusColumnGenerator(this));

		table.setEditable(false);
		table.setMultiSelect(true);
		table.setMultiSelectMode(MultiSelectMode.DEFAULT);
		table.setSelectable(true);
		table.setWriteThrough(true);
				
		mainWindow.addComponent(table);

		setMainWindow(mainWindow);
	}

	private void setEditMode(boolean editMode) {
		if(editMode) {
			table.setEditable(true);
			table.setSortDisabled(true);
			refreshButton.setEnabled(false);
			editButton.setEnabled(false);
			saveButton.setEnabled(true);
			cancelButton.setEnabled(true);
			addItemButton.setEnabled(true);
			removeItemButton.setEnabled(true);
		} else {
			table.setEditable(false);
			table.setSortDisabled(false);
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
			container.refresh();
		}
		if(event.getButton()==editButton) {
			container.commit();
			setEditMode(true);
		}
		if(event.getButton()==saveButton) {
			container.commit();
			setEditMode(false);
		}
		if(event.getButton()==cancelButton) {
			container.discard();
			setEditMode(false);
		}
		if(event.getButton()==addItemButton) {
			container.addItem();
		}
		if(event.getButton()==removeItemButton) {
			Object selection=table.getValue();
			if(selection==null) {
				return;
			}
			if(selection instanceof Integer) {
				Integer selectedIndex=(Integer)selection;
				if(selectedIndex!=null) {
					container.removeItem(selectedIndex);
				}
			}
			if(selection instanceof Collection) {
				Collection selectionIndexes=(Collection)selection;
				for(Object selectedIndex : selectionIndexes) {
					container.removeItem((Integer)selectedIndex);
				}
			}			
		}
	}

}

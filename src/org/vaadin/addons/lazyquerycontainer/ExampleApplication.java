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
package org.vaadin.addons.lazyquerycontainer;

import java.util.ArrayList;

import org.vaadin.addons.lazyquerycontainer.test.MockQueryFactory;


import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Runo;

/**
 * Example application demonstrating the Lazy Query Container features.
 * @author Tommi S.E. Laukkanen
 */
public class ExampleApplication extends Application implements ClickListener {
	private static final long serialVersionUID = 1L;

	private LazyQueryContainer container;
	private MockQueryFactory mockQueryFactory;
	private Button refreshButton;
	private Button saveChangesButton;
	private Button discardChangesButton;
	private Button addItemButton;
	private Button removeItemButton;
	private Button removeAllItemsButton;
	private Button addPropertyButton;
	private Table table;
	private int addedPropertyCount=0;
	
	private ArrayList<Object> visibleColumnIds=new ArrayList<Object>();
	private ArrayList<String> visibleColumnLabels=new ArrayList<String>();
	
	
	@Override
	public void init() {
		Window mainWindow = new Window("Lazycontainer Application");
		
		Panel mainPanel=new Panel();
		VerticalLayout mainLayout=new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainPanel.setContent(mainLayout);
		mainWindow.addComponent(mainPanel);
		
		Panel buttonPanel=new Panel();
		buttonPanel.addStyleName(Runo.PANEL_LIGHT);
		HorizontalLayout buttonLayout=new HorizontalLayout();
		buttonLayout.setMargin(false);
		buttonLayout.setSpacing(true);
		buttonPanel.setContent(buttonLayout);		
		mainPanel.addComponent(buttonPanel);

		
		refreshButton=new Button("Refresh");
		refreshButton.addListener(this);
		buttonPanel.addComponent(refreshButton);
		
		saveChangesButton=new Button("Save Changes");
		saveChangesButton.addListener(this);
		buttonPanel.addComponent(saveChangesButton);

		discardChangesButton=new Button("Discard Changes");
		discardChangesButton.addListener(this);
		buttonPanel.addComponent(discardChangesButton);

		addItemButton=new Button("Add Row");
		addItemButton.addListener(this);
		buttonPanel.addComponent(addItemButton);

		removeItemButton=new Button("Remove Row");
		removeItemButton.addListener(this);
		buttonPanel.addComponent(removeItemButton);
		
		removeAllItemsButton=new Button("Remove All Rows");
		removeAllItemsButton.addListener(this);
		buttonPanel.addComponent(removeAllItemsButton);
		
		addPropertyButton=new Button("Add Column");
		addPropertyButton.addListener(this);
		buttonPanel.addComponent(addPropertyButton);
		
		table=new Table();
				
		mockQueryFactory=new MockQueryFactory(100,25,50);
		LazyQueryView view=new LazyQueryView(mockQueryFactory,5);
		container=new LazyQueryContainer(view);
		
		container.addContainerProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS, ItemStatus.class, ItemStatus.None, true, false);
		container.addContainerProperty("Index", Integer.class, 0, true, true);
		container.addContainerProperty("ReverseIndex", Integer.class, 0, true, true);
		container.addContainerProperty("Editable", String.class, "", false, false);
		container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX, Integer.class, 0, true, false);
		container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX, Integer.class, 0, true, false);
		container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME, Integer.class, 0, true, false);
		
		table.setContainerDataSource(container);
	
		visibleColumnIds.add(LazyQueryView.PROPERTY_ID_ITEM_STATUS);
		visibleColumnIds.add("Index");
		visibleColumnIds.add("ReverseIndex");
		visibleColumnIds.add("Editable");
		visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX);
		visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX);
		visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME);

		visibleColumnLabels.add("Status");
		visibleColumnLabels.add("Index");
		visibleColumnLabels.add("Reverse Index");
		visibleColumnLabels.add("Editable");
		visibleColumnLabels.add("Query");
		visibleColumnLabels.add("Batch");
		visibleColumnLabels.add("Time [ms]");

		table.setVisibleColumns(visibleColumnIds.toArray());
		table.setColumnHeaders(visibleColumnLabels.toArray(new String[0]));
		
		table.setEditable(true);
		table.setSelectable(true);
		//table.setImmediate(true);
		//table.setReadThrough(true);
		table.setWriteThrough(true);
		
		mainPanel.addComponent(table);

		setMainWindow(mainWindow);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==refreshButton) {
			container.refresh();
		}
		if(event.getButton()==saveChangesButton) {
			container.commit();
		}
		if(event.getButton()==discardChangesButton) {
			container.discard();
		}
		if(event.getButton()==addItemButton) {
			container.addItem();
		}
		if(event.getButton()==removeItemButton) {
			Integer selectedIndex=(Integer)table.getValue();
			if(selectedIndex!=null) {
				container.removeItem(selectedIndex);
			}
		}
		if(event.getButton()==removeAllItemsButton) {
			container.removeAllItems();
		}
		if(event.getButton()==addPropertyButton) {
			addedPropertyCount++;

			String newPropertyId="Property-"+addedPropertyCount;
			
			container.addContainerProperty(newPropertyId, Integer.class, 0, false, true);
			mockQueryFactory.addProperty(newPropertyId, Integer.class, 0, false, true);
			container.refresh();

			visibleColumnIds.add(newPropertyId);
			visibleColumnLabels.add(newPropertyId);	

			table.setVisibleColumns(visibleColumnIds.toArray());
			table.setColumnHeaders(visibleColumnLabels.toArray(new String[0]));
			table.requestRepaint();
		}
	}

}

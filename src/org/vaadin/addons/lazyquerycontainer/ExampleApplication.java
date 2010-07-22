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
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

/**
 * Example application demonstrating the Lazy Query Container features.
 * @author Tommi S.E. Laukkanen
 */
public class ExampleApplication extends Application implements ClickListener {
	private static final long serialVersionUID = 1L;

	private LazyQueryContainer container;
	private MockQueryFactory mockQueryFactory;
	private Button refreshButton;
	private Button addPropertyButton;
	private Table table;
	private int addedPropertyCount=0;
	
	private ArrayList<Object> visibleColumnIds=new ArrayList<Object>();
	private ArrayList<String> visibleColumnLabels=new ArrayList<String>();
	
	
	@Override
	public void init() {
		Window mainWindow = new Window("Lazycontainer Application");

		refreshButton=new Button("Refresh");
		refreshButton.addListener(this);
		mainWindow.addComponent(refreshButton);
		
		addPropertyButton=new Button("Add Column");
		addPropertyButton.addListener(this);
		mainWindow.addComponent(addPropertyButton);
		
		table=new Table("Example Table");
				
		mockQueryFactory=new MockQueryFactory(100,25,50);
		LazyQueryView view=new LazyQueryView(mockQueryFactory,5);
		container=new LazyQueryContainer(view);
		
		container.addContainerProperty("Index", Integer.class, 0, true, true);
		container.addContainerProperty("ReverseIndex", Integer.class, 0, true, true);
		container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX, Integer.class, 0, false, false);
		container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX, Integer.class, 0, false, false);
		container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME, Integer.class, 0, false, false);
		
		table.setContainerDataSource(container);
	
		visibleColumnIds.add("Index");
		visibleColumnIds.add("ReverseIndex");
		visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX);
		visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX);
		visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME);

		visibleColumnLabels.add("Index");
		visibleColumnLabels.add("Reverse Index");
		visibleColumnLabels.add("Query");
		visibleColumnLabels.add("Batch");
		visibleColumnLabels.add("Time [ms]");

		table.setVisibleColumns(visibleColumnIds.toArray());
		table.setColumnHeaders(visibleColumnLabels.toArray(new String[0]));
		
		table.setEditable(true);

		mainWindow.addComponent(table);

		setMainWindow(mainWindow);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==refreshButton) {
			container.refresh();
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

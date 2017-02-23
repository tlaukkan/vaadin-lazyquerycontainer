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
package org.vaadin.addons.lazyquerycontainer.example.mock;

import com.vaadin.annotations.Title;
import com.vaadin.server.ClassResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.v7.ui.themes.Runo;
import org.vaadin.addons.lazyquerycontainer.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example application demonstrating the Lazy Query Container features.
 *
 * @author Tommi S.E. Laukkanen
 */
@SuppressWarnings("rawtypes")
@Title("Lazycontainer Application")
public class VaadinApplication extends UI implements ClickListener {
    private static final long serialVersionUID = 1L;

    private LazyQueryContainer container;
    private MockQueryFactory mockQueryFactory;
    private Button refreshButton;
    private Button editButton;
    private Button saveButton;
    private Button cancelButton;
    private Button addItemButton;
    private Button removeItemButton;
    private Button removeAllItemsButton;
    private Button addPropertyButton;
    private Table table;
    private int addedPropertyCount = 0;

    private ArrayList<Object> visibleColumnIds = new ArrayList<Object>();
    private ArrayList<String> visibleColumnLabels = new ArrayList<String>();

    @Override
    protected void init(VaadinRequest request) {

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        setContent(mainLayout);

        Panel buttonPanel = new Panel();
        buttonPanel.addStyleName(Runo.PANEL_LIGHT);
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setMargin(false);
        buttonLayout.setSpacing(true);
        buttonPanel.setContent(buttonLayout);
        mainLayout.addComponent(buttonPanel);

        Panel buttonPanel2 = new Panel();
        buttonPanel2.addStyleName(Runo.PANEL_LIGHT);
        HorizontalLayout buttonLayout2 = new HorizontalLayout();
        buttonLayout2.setMargin(false);
        buttonLayout2.setSpacing(true);
        buttonPanel2.setContent(buttonLayout2);
        mainLayout.addComponent(buttonPanel2);

        refreshButton = new Button("Refresh");
        refreshButton.setIcon(new ClassResource(QueryItemStatusColumnGenerator.class, "images/table_refresh.png"));
        refreshButton.addClickListener(this);
        buttonLayout.addComponent(refreshButton);

        editButton = new Button("Edit");
        editButton.setIcon(new ClassResource(QueryItemStatusColumnGenerator.class, "images/table_edit.png"));
        editButton.addClickListener(this);
        buttonLayout.addComponent(editButton);

        addPropertyButton = new Button("Add Column");
        addPropertyButton.setIcon(new ClassResource(QueryItemStatusColumnGenerator.class, "images/tab_add.png"));
        addPropertyButton.addClickListener(this);
        buttonLayout.addComponent(addPropertyButton);

        removeAllItemsButton = new Button("Remove All Rows");
        removeAllItemsButton.setIcon(new ClassResource(QueryItemStatusColumnGenerator.class, "images/delete.png"));
        removeAllItemsButton.addClickListener(this);
        buttonLayout.addComponent(removeAllItemsButton);

        saveButton = new Button("Save");
        saveButton.setIcon(new ClassResource(QueryItemStatusColumnGenerator.class, "images/table_save.png"));
        saveButton.addClickListener(this);
        saveButton.setEnabled(false);
        buttonLayout2.addComponent(saveButton);

        cancelButton = new Button("Cancel");
        cancelButton.setIcon(new ClassResource(QueryItemStatusColumnGenerator.class, "images/cancel.png"));
        cancelButton.addClickListener(this);
        cancelButton.setEnabled(false);
        buttonLayout2.addComponent(cancelButton);

        addItemButton = new Button("Add Row");
        addItemButton.setIcon(new ClassResource(QueryItemStatusColumnGenerator.class, "images/table_row_insert.png"));
        addItemButton.addClickListener(this);
        addItemButton.setEnabled(false);
        buttonLayout2.addComponent(addItemButton);

        removeItemButton = new Button("Remove Row");
        removeItemButton.setIcon(new ClassResource(QueryItemStatusColumnGenerator.class, "images/table_row_delete.png"));
        removeItemButton.addClickListener(this);
        removeItemButton.setEnabled(false);
        buttonLayout2.addComponent(removeItemButton);

        table = new Table();
        mainLayout.addComponent(table);

        table.setPageLength(20);

        mockQueryFactory = new MockQueryFactory(2000, 10, 20);
        container = new LazyQueryContainer(new LazyQueryDefinition(true, 50, "Index"), mockQueryFactory);

        container.addContainerProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS, QueryItemStatus.class, QueryItemStatus.None, true, false);
        container.addContainerProperty("Index", Integer.class, 0, true, true);
        container.addContainerProperty("ReverseIndex", Integer.class, 0, true, true);
        container.addContainerProperty("Editable", String.class, "", false, false);
        container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX, Integer.class, 0, true, false);
        container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX, Integer.class, 0, true, false);
        container.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME, Long.class, 0, true, false);

        table.setContainerDataSource(container);

        visibleColumnIds.add(LazyQueryView.PROPERTY_ID_ITEM_STATUS);
        visibleColumnIds.add("Index");
        visibleColumnIds.add("ReverseIndex");
        visibleColumnIds.add("Editable");
        visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX);
        visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX);
        visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME);

        visibleColumnLabels.add("");
        visibleColumnLabels.add("Index");
        visibleColumnLabels.add("Reverse Index");
        visibleColumnLabels.add("Editable");
        visibleColumnLabels.add("Query");
        visibleColumnLabels.add("Batch");
        visibleColumnLabels.add("Time [ms]");

        table.setColumnWidth("Editable", 135);

        table.setVisibleColumns(visibleColumnIds.toArray());
        table.setColumnHeaders(visibleColumnLabels.toArray(new String[0]));

        table.setColumnWidth(LazyQueryView.PROPERTY_ID_ITEM_STATUS, 16);
        table.addGeneratedColumn(LazyQueryView.PROPERTY_ID_ITEM_STATUS, new QueryItemStatusColumnGenerator());

        table.setEditable(false);
        table.setMultiSelect(true);
        table.setMultiSelectMode(MultiSelectMode.DEFAULT);
        table.setSelectable(true);
        //table.setWriteThrough(true);
    }

    private void setEditMode(boolean editMode) {
        if (editMode) {
            table.setEditable(true);
            table.setSortEnabled(false);
            refreshButton.setEnabled(false);
            removeAllItemsButton.setEnabled(false);
            addPropertyButton.setEnabled(false);
            editButton.setEnabled(false);
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            addItemButton.setEnabled(true);
            removeItemButton.setEnabled(true);
        } else {
            table.setEditable(false);
            table.setSortEnabled(true);
            refreshButton.setEnabled(true);
            removeAllItemsButton.setEnabled(true);
            addPropertyButton.setEnabled(true);
            editButton.setEnabled(true);
            saveButton.setEnabled(false);
            cancelButton.setEnabled(false);
            addItemButton.setEnabled(false);
            removeItemButton.setEnabled(false);
        }
    }

    public void buttonClick(ClickEvent event) {
        if (event.getButton() == refreshButton) {
            container.refresh();
        }
        if (event.getButton() == editButton) {
            container.commit();
            setEditMode(true);
        }
        if (event.getButton() == saveButton) {
            container.commit();
            setEditMode(false);
        }
        if (event.getButton() == cancelButton) {
            container.discard();
            setEditMode(false);
        }
        if (event.getButton() == addItemButton) {
            container.addItem();
        }
        if (event.getButton() == removeItemButton) {
            Object selection = table.getValue();
            if (selection == null) {
                return;
            }
            if (selection instanceof Integer) {
                Integer selectedIndex = (Integer) selection;
                container.removeItem(selectedIndex);
            }
            if (selection instanceof Collection) {
                Collection selectionIndexes = (Collection) selection;
                for (Object selectedIndex : selectionIndexes) {
                    container.removeItem((Integer) selectedIndex);
                }
            }
        }
        if (event.getButton() == removeAllItemsButton) {
            container.removeAllItems();
        }
        if (event.getButton() == addPropertyButton) {
            addedPropertyCount++;

            String newPropertyId = "Property-" + addedPropertyCount;

            container.addContainerProperty(newPropertyId, Integer.class, 0, false, true);
            mockQueryFactory.addProperty(newPropertyId, Integer.class, 0, false, true);
            container.refresh();

            visibleColumnIds.add(newPropertyId);
            visibleColumnLabels.add(newPropertyId);

            table.setVisibleColumns(visibleColumnIds.toArray());
            table.setColumnHeaders(visibleColumnLabels.toArray(new String[0]));
            table.markAsDirty();
        }
    }

}

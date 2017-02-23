package org.vaadin.addons.lazyquerycontainer.example.jpa;

import com.vaadin.annotations.Title;
import com.vaadin.v7.data.util.filter.Compare;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.v7.ui.themes.Runo;
import org.vaadin.addons.lazyquerycontainer.LazyEntityContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryView;
import org.vaadin.addons.lazyquerycontainer.QueryItemStatus;
import org.vaadin.addons.lazyquerycontainer.QueryItemStatusColumnGenerator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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

    public static final String PERSISTENCE_UNIT = "vaadin-lazyquerycontainer-example";
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
    private EntityManager entityManager;

    private TextField nameFilterField;

    private Button refreshButton;
    private Button editButton;
    private Button saveButton;
    private Button cancelButton;
    private Button addItemButton;
    private Button removeItemButton;

    private Table table;
    private LazyEntityContainer<Task> entityContainer;

    private final ArrayList<Object> visibleColumnIds = new ArrayList<Object>();
    private final ArrayList<String> visibleColumnLabels = new ArrayList<String>();

    @Override
    protected void init(VaadinRequest request) {

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        setContent(mainLayout);

        final Panel filterPanel = new Panel();
        filterPanel.addStyleName(Runo.PANEL_LIGHT);
        final HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setMargin(false);
        filterLayout.setSpacing(true);
        filterPanel.setContent(filterLayout);
        mainLayout.addComponent(filterPanel);

        final Panel buttonPanel = new Panel();
        buttonPanel.addStyleName(Runo.PANEL_LIGHT);
        final HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setMargin(false);
        buttonLayout.setSpacing(true);
        buttonPanel.setContent(buttonLayout);
        mainLayout.addComponent(buttonPanel);

        final Panel buttonPanel2 = new Panel();
        buttonPanel2.addStyleName(Runo.PANEL_LIGHT);
        final HorizontalLayout buttonLayout2 = new HorizontalLayout();
        buttonLayout2.setMargin(false);
        buttonLayout2.setSpacing(true);
        buttonPanel2.setContent(buttonLayout2);
        mainLayout.addComponent(buttonPanel2);

        nameFilterField = new TextField("Name");
        filterLayout.addComponent(nameFilterField);

        refreshButton = new Button("Refresh");
        refreshButton.addClickListener(this);
        buttonLayout.addComponent(refreshButton);

        editButton = new Button("Edit");
        editButton.addClickListener(this);
        buttonLayout.addComponent(editButton);

        saveButton = new Button("Save");
        saveButton.addClickListener(this);
        saveButton.setEnabled(false);
        buttonLayout2.addComponent(saveButton);

        cancelButton = new Button("Cancel");
        cancelButton.addClickListener(this);
        cancelButton.setEnabled(false);
        buttonLayout2.addComponent(cancelButton);

        addItemButton = new Button("Add Row");
        addItemButton.addClickListener(this);
        addItemButton.setEnabled(false);
        buttonLayout2.addComponent(addItemButton);

        removeItemButton = new Button("Remove Row");
        removeItemButton.addClickListener(this);
        removeItemButton.setEnabled(false);
        buttonLayout2.addComponent(removeItemButton);

        visibleColumnIds.add(LazyQueryView.PROPERTY_ID_ITEM_STATUS);
        visibleColumnIds.add("taskId");
        visibleColumnIds.add("name");
        visibleColumnIds.add("author.name");
        visibleColumnIds.add("author.company.name");
        visibleColumnIds.add("reporter");
        visibleColumnIds.add("assignee");
        visibleColumnIds.add("alpha");
        visibleColumnIds.add("beta");
        visibleColumnIds.add("gamma");
        visibleColumnIds.add("delta");
        visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX);
        visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX);
        visibleColumnIds.add(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME);

        visibleColumnLabels.add("");
        visibleColumnLabels.add("Task ID");
        visibleColumnLabels.add("Name");
        visibleColumnLabels.add("Author");
        visibleColumnLabels.add("Company");
        visibleColumnLabels.add("Reporter");
        visibleColumnLabels.add("Assignee");
        visibleColumnLabels.add("Alpha");
        visibleColumnLabels.add("Beta");
        visibleColumnLabels.add("Gamma");
        visibleColumnLabels.add("Delta");
        visibleColumnLabels.add("Query");
        visibleColumnLabels.add("Batch");
        visibleColumnLabels.add("Time [ms]");

        entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();

        entityContainer = new LazyEntityContainer<Task>(entityManager, Task.class, 100, "taskId", true, true, true);
        entityContainer.getQueryView().getQueryDefinition().setDefaultSortState(
                new Object[]{"name"}, new boolean[]{true});
        entityContainer.getQueryView().getQueryDefinition().setMaxNestedPropertyDepth(3);

        entityContainer.addContainerProperty(LazyQueryView.PROPERTY_ID_ITEM_STATUS, QueryItemStatus.class,
                QueryItemStatus.None, true, false);
        entityContainer.addContainerProperty("taskId", Long.class, 0L, true, true);
        entityContainer.addContainerProperty("name", String.class, "", true, true);
        entityContainer.addContainerProperty("author.name", String.class, "", true, true);
        entityContainer.addContainerProperty("author.company.name", String.class, "", true, true);
        entityContainer.addContainerProperty("reporter", String.class, "", true, true);
        entityContainer.addContainerProperty("assignee", String.class, "", true, true);
        entityContainer.addContainerProperty("alpha", String.class, "", false, true);
        entityContainer.addContainerProperty("beta", String.class, "", false, true);
        entityContainer.addContainerProperty("gamma", String.class, "", false, true);
        entityContainer.addContainerProperty("delta", String.class, "", false, true);
        entityContainer.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX, Integer.class, 0, true, false);
        entityContainer.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX, Integer.class, 0, true, false);
        entityContainer.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME, Long.class, 0, true,
                false);

        final Company company = new Company();
        company.setName("test-company");
        final Author author = new Author();
        author.setName("test-author");
        author.setCompany(company);
        Task entity = null;
        for (int i = 0; i < 40; i++) {
            entity = entityContainer.addEntity();
            entity.setName("task-" + Integer.toString(i));
            entity.setAssignee("assignee-" + Integer.toString(i));
            entity.setReporter("reporter-" + Integer.toString(i));
            entity.setAuthor(author);
            entity.setAlpha(Integer.toString(i));
            entity.setBeta(Integer.toString(i));
            entity.setGamma(Integer.toString(i));
            entity.setDelta(Integer.toString(i));
        }

        entityContainer.commit();

        table = new Table();
        mainLayout.addComponent(table);

        table.setCaption("JpaQuery");
        table.setPageLength(5);

        table.setContainerDataSource(entityContainer);

        table.setColumnWidth("name", 135);
        table.setColumnWidth("reporter", 135);
        table.setColumnWidth("assignee", 135);

        table.setVisibleColumns(visibleColumnIds.toArray());
        table.setColumnHeaders(visibleColumnLabels.toArray(new String[0]));

        table.setColumnWidth(LazyQueryView.PROPERTY_ID_ITEM_STATUS, 16);
        table.addGeneratedColumn(LazyQueryView.PROPERTY_ID_ITEM_STATUS, new QueryItemStatusColumnGenerator());

        table.setImmediate(true);
        table.setEditable(false);
        table.setMultiSelect(true);
        table.setMultiSelectMode(MultiSelectMode.DEFAULT);
        table.setSelectable(true);

//        entityManager.getTransaction().begin();
//        entityManager.remove(entity);
//        entityManager.getTransaction().commit();
    }

    private void setEditMode(final boolean editMode) {
        if (editMode) {
            table.setEditable(true);
            table.setSortEnabled(false);
            refreshButton.setEnabled(false);
            editButton.setEnabled(false);
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            addItemButton.setEnabled(true);
            removeItemButton.setEnabled(true);
            nameFilterField.setEnabled(false);
        } else {
            table.setEditable(false);
            table.setSortEnabled(true);
            refreshButton.setEnabled(true);
            editButton.setEnabled(true);
            saveButton.setEnabled(false);
            cancelButton.setEnabled(false);
            addItemButton.setEnabled(false);
            removeItemButton.setEnabled(false);
            nameFilterField.setEnabled(true);
        }
    }

    @Override
    public void buttonClick(final ClickEvent event) {
        if (event.getButton() == refreshButton) {
            final String nameFilter = nameFilterField.getValue();
            if (nameFilter != null && nameFilter.length() != 0) {
                entityContainer.removeAllContainerFilters();
                entityContainer.addContainerFilter(new Compare.Equal("name", nameFilter));
            } else {
                entityContainer.removeAllContainerFilters();
            }
            entityContainer.refresh();
        }
        if (event.getButton() == editButton) {
            setEditMode(true);
        }
        if (event.getButton() == saveButton) {
            entityContainer.commit();
            entityContainer.refresh();
            setEditMode(false);
        }
        if (event.getButton() == cancelButton) {
            entityContainer.discard();
            entityContainer.refresh();
            setEditMode(false);
        }
        if (event.getButton() == addItemButton) {
            entityContainer.addItem();
        }
        if (event.getButton() == removeItemButton) {
            final Object selection = table.getValue();
            if (selection == null) {
                return;
            }
            if (selection instanceof Integer) {
                final Integer selectedIndex = (Integer) selection;
                entityContainer.removeItem(selectedIndex);
            }
            if (selection instanceof Collection) {
                final Collection selectionIndexes = (Collection) selection;
                for (final Object selectedIndex : selectionIndexes) {
                    entityContainer.removeItem(selectedIndex);
                }
            }
        }
    }

}

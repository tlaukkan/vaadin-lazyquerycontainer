package org.vaadin.addons.lazyquerycontainer;

import java.util.Collection;

import com.vaadin.data.util.BeanItem;

/**
 * An abstract class to provide default behaviors to EntityContainer's
 * @author Eduardo Frazao - edufrazao@gmail.com
 *
 * @param <T> Entity Type
 */
public abstract class AbstractEntityContainer<T> extends LazyQueryContainer {

	private static final long serialVersionUID = 1L;

	public AbstractEntityContainer(QueryDefinition queryDefinition, QueryFactory queryFactory) {
		super(queryDefinition, queryFactory);
	}

	public AbstractEntityContainer(QueryFactory queryFactory, Object idPropertyId, int batchSize, boolean compositeItems) {
		super(queryFactory, idPropertyId, batchSize, compositeItems);
	}

	public AbstractEntityContainer(QueryView queryView) {
		super(queryView);
	}
	
	/**
     * Adds entity to the container as first item i.e. at index 0.
     *
     * @return the new constructed entity.
     */
    public T addEntity() {
        final Object itemId = addItem();
        return getEntity(indexOfId(itemId));
    }

    /**
     * Removes given entity at given index and returns it.
     *
     * @param index Index of the entity to be removed.
     * @return The removed entity.
     */
    public T removeEntity(final int index) {
        final T entityToRemove = getEntity(index);
        removeItem(getIdByIndex(index));
        return entityToRemove;
    }

    /**
     * Gets entity by ID.
     *
     * @param id The ID of the entity.
     * @return the entity.
     */
    public T getEntity(final Object id) {
        return getEntity(indexOfId(id));
    }

    /**
     * Gets entity at given index.
     *
     * @param index The index of the entity.
     * @return the entity.
     */
    @SuppressWarnings("unchecked")
    public T getEntity(final int index) {
        if (getQueryView().getQueryDefinition().isCompositeItems()) {
            final CompositeItem compositeItem = (CompositeItem) getItem(getIdByIndex(index));
            final BeanItem<T> beanItem = (BeanItem<T>) compositeItem.getItem("bean");
            return beanItem.getBean();
        } else {
            return ((BeanItem<T>) getItem(getIdByIndex(index))).getBean();
        }
    }

	/**
     * Returns an unmodifiable collection of user defined nested properties
     * @return
     */
    public Collection<String> getNestedProperties() {
    	return getQueryDefinitionFromQueryView().getNestedProperties();
    }
    
    /**
     * Add an nested property to this container<br>
     * It uses the Java Beans API and conventions to access/invoke methods.<br>
     * All access to properties will be done via default Getters and Setters, and not directly field acess. Be sure that your entity is<br>
     * Java Beans conform.
     * They will work only if you enable composite itens on this container
     * @param nestedProperty - Nested properties on Java Beans notation (ie: entity.anotherEntity.propertyName)
     */
    public void addNestedProperty(final String nestedProperty) {
    	if(nestedProperty != null && !nestedProperty.trim().isEmpty()) {
    		Class<?> entityClass = getQueryDefinitionFromQueryView().getEntityClass();
    		Class<?> propertyClass = EntityNestedProperty.getTypeFromPropertyPath(nestedProperty, entityClass);
    		
    		getQueryDefinitionFromQueryView().addNestedProperty(nestedProperty);
        	super.addContainerProperty(nestedProperty, propertyClass, null);
    	}
    }
    
    /**
     * Removes an nested property from this container
     * @param nestedProperty
     */
    public void removeNestedProperty(final String nestedProperty) {
    	if(getNestedProperties().contains(nestedProperty)) {
    		getQueryDefinitionFromQueryView().removeNestedProperty(nestedProperty);
        	super.removeContainerProperty(nestedProperty);
    	}
    }
	
    /**
     * A convenience method that returns the EntityQueryDefinition with the correct type casted.
     * @return
     */
    private EntityQueryDefinition getQueryDefinitionFromQueryView() {
    	return (EntityQueryDefinition) getQueryView().getQueryDefinition();
    }
    
}

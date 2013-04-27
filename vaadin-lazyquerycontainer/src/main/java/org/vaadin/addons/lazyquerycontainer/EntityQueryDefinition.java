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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Defines entity query definition to be used with JPA entity managers.
 *
 * @author Tommi Laukkanen
 */
public class EntityQueryDefinition extends LazyQueryDefinition {
    /**
     * Serial version UID for this class.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Class of the persistent entity type.
     */
    private Class<?> entityClass;
    /**
     * True if entities are detached from PersistenceContext.
     */
    private boolean detachedEntities;
    /**
     * True if application manages transactions instead of container.
     */
    private boolean applicationManagedTransactions;
    
    /**
     * Nested properties collection
     */
    private Collection<String> nestedProperties = new LinkedHashSet<String>();


    /**
     * Constructor for configuring query definition.
     *
     * @param applicationManagedTransactions True if application manages transactions instead of container.
     * @param detachedEntities               True of entities are detached from PersistenceContext.
     * @param compositeItems                 True f items are wrapped to CompositeItems.
     * @param entityClass                    The entity class.
     * @param batchSize                      The batch size.
     * @param idPropertyId                   The ID of the ID property or null if item index is used as ID.
     */
    public EntityQueryDefinition(final boolean applicationManagedTransactions,
                                 final boolean detachedEntities, final boolean compositeItems,
                                 final Class<?> entityClass, final int batchSize,
                                 final Object idPropertyId) {
        super(compositeItems, batchSize, idPropertyId);
        this.applicationManagedTransactions = applicationManagedTransactions;
        this.entityClass = entityClass;
        this.detachedEntities = detachedEntities;
    }

    /**
     * @return the applicationManagedTransactions
     */
    public final boolean isApplicationManagedTransactions() {
        return applicationManagedTransactions;
    }

    /**
     * @return the detachedEntities
     */
    public final boolean isDetachedEntities() {
        return detachedEntities;
    }

    /**
     * Gets class of the persistent entity type.
     *
     * @return the entityClass
     */
    public final Class<?> getEntityClass() {
        return entityClass;
    }
    
    /**
     * Returns an unmodifiable collection of user defined nested properties
     * @return
     */
    public Collection<String> getNestedProperties() {
    	return Collections.unmodifiableCollection(nestedProperties);
    }
    
    /**
     * Add an nested property to this query definition
     * @param nestedProperty
     */
    public void addNestedProperty(final String nestedProperty) {
    	nestedProperties.add(nestedProperty);
    }
    
    /**
     * Removes an nested property from this query definition
     * @param nestedProperty
     */
    public void removeNestedProperty(final String nestedProperty) {
    	nestedProperties.remove(nestedProperty);
    }

}

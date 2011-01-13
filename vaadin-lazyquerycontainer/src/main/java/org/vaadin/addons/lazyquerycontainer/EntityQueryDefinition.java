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

import java.security.InvalidParameterException;
import java.util.Map;

import javax.persistence.EntityManager;

/**
 * Defines entity query definition to be used with JPA entity managers.
 * @author Tommi Laukkanen
 */
public class EntityQueryDefinition extends LazyQueryDefinition {
    /** Serial version UID for this class. */
    private static final long serialVersionUID = 1L;
    /** The JPA EntityManager. */
    private final EntityManager entityManager;
    /** applicationManagedTransactions True if application manages transactions instead of container. */
    private boolean applicationManagedTransactions;
    /** Class of the persistent entity type. */
    private Class<?> entityClass;
    /** The where criteria excluding where keyword. */
    private String whereCriteria;
    /** The where parameters to set to JPA query. */
    private Map<String, Object> whereParameters;
    /** The native sort property IDs. */
    private Object[] nativeSortPropertyIds;
    /** The native sort ascending and descending states. */
    private boolean[] nativeSortPropertyAscendingStates;
    /** The sort property IDs. */
    private Object[] sortPropertyIds;
    /** The sort ascending and descending states. */
    private boolean[] sortPropertyAscendingStates;
    /** Whether entities are detached from PersistenceContext. */
    private boolean detachedEntities;
    
    /**
     * Constructor for configuring query definition.
     * @param entityManager The JPA EntityManager.
     * @param applicationManagedTransactions True if application manages transactions instead of container.
     * @param detachedEntities Whether entities are detached from PersistenceContext.
     * @param entityClass The entity class.
     * @param batchSize The batch size.
     * @param nativeSortPropertyIds Properties participating in the native sort.
     * @param nativeSortPropertyAscendingStates List of property sort directions for the native sort.
     */
    public EntityQueryDefinition(final EntityManager entityManager, final boolean applicationManagedTransactions,
            final boolean detachedEntities, final Class<?> entityClass, final int batchSize,
            final Object[] nativeSortPropertyIds, final boolean[] nativeSortPropertyAscendingStates) {
        super(batchSize);
        this.entityManager = entityManager;
        this.applicationManagedTransactions = applicationManagedTransactions;
        this.entityClass = entityClass;
        this.nativeSortPropertyIds = nativeSortPropertyIds;
        this.nativeSortPropertyAscendingStates = nativeSortPropertyAscendingStates;
        this.detachedEntities = detachedEntities;
        if (nativeSortPropertyIds.length == 0) {
            throw new InvalidParameterException("Native sort order is mandatory.");
        }
        if (nativeSortPropertyIds.length != nativeSortPropertyAscendingStates.length) {
            throw new InvalidParameterException(
                    "Native sort properties have to have matching amount of ascending states.");
        }
    }

    /**
     * Sets the where criteria. Where keyword is not to be included.
     * @param whereCriteria the where criteria to be included in JPA query.
     * @param whereParameters the where parameters to set to JPA query.
     */
    public final void setWhereCriteria(final String whereCriteria, final Map<String, Object> whereParameters) {
        this.whereCriteria = whereCriteria;
        this.whereParameters = whereParameters;
    }

    /**
     * Sets the sort state.
     * @param sortPropertyIds Properties participating in the sorting.
     * @param sortPropertyAscendingStates List of sort direction for the properties.
     */
    public final void setSortState(final Object[] sortPropertyIds, final boolean[] sortPropertyAscendingStates) {
        this.sortPropertyIds = sortPropertyIds;
        this.sortPropertyAscendingStates = sortPropertyAscendingStates;
    }

    /**
     * @return the entityManager
     */
    public final EntityManager getEntityManager() {
        return entityManager;
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
     * @return the entityClass
     */
    public final Class<?> getEntityClass() {
        return entityClass;
    }
    
    /**
     * Method which generates and returns entity PSQLs based
     * on query definition parameters.
     * @return the generated EntitySelectDefinition.
     */
    public final EntityPsqlDefinition getEntityPsqlDefinition() {
        final StringBuilder fromBuilder = new StringBuilder(" from ");
        fromBuilder.append(entityClass.getSimpleName());
        fromBuilder.append(" as e");
        final StringBuilder whereBuilder = new StringBuilder(" where ");
        whereBuilder.append(whereCriteria);
        final StringBuilder orderByBuilder = new StringBuilder(" order by");        
        if (sortPropertyIds.length == 0) {
            sortPropertyIds = nativeSortPropertyIds;
            sortPropertyAscendingStates = nativeSortPropertyAscendingStates;
        }
        for (int i = 0; i < sortPropertyIds.length; i++) {
            if (i != 0) {
                orderByBuilder.append(",");
            }
            orderByBuilder.append(" e.");
            orderByBuilder.append(sortPropertyIds[i]);
            if (sortPropertyAscendingStates[i]) {
                orderByBuilder.append(" asc");
            } else {
                orderByBuilder.append(" desc");
            }
        }
        
        final StringBuilder selectBuilder = new StringBuilder("select e");
        selectBuilder.append(fromBuilder.toString());
        if (whereCriteria != null && whereCriteria.length() != 0) {
            selectBuilder.append(whereBuilder.toString());
        }
        selectBuilder.append(orderByBuilder.toString());
        
        final StringBuilder selectCountBuilder = new StringBuilder("select count(e)");
        selectCountBuilder.append(fromBuilder.toString());
        if (whereCriteria != null && whereCriteria.length() != 0) {
            selectCountBuilder.append(whereBuilder.toString());
        }
        
        final StringBuilder deleteBuilder = new StringBuilder("delete");
        deleteBuilder.append(fromBuilder.toString());
        if (whereCriteria != null && whereCriteria.length() != 0) {
            deleteBuilder.append(whereBuilder.toString());
        }

        return new EntityPsqlDefinition(selectBuilder.toString(), selectCountBuilder.toString(),
                deleteBuilder.toString());
    }

    /**
     * @return the whereParameters
     */
    public final Map<String, Object> getWhereParameters() {
        if (whereCriteria != null && whereCriteria.length() != 0) {
            return whereParameters;
        } else {
            return null;
        }
    }

    /**
     * Java bean which contains the entity select and delete PSQLs.
     * @author Tommi Laukkanen
     */
    public final class EntityPsqlDefinition {
        /** The PSQL for selecting entities. */
        private String selectPsql;
        /** The PSQL for selecting count of entities. */
        private String selectCountPsql;
        /** The PSQL for deleting entities. */
        private String deletePsql;
        /**
         * Constructor which initializes entity select definition.
         * @param selectPsql The PSQL for selecting entities.
         * @param selectCountPsql The PSQL for selecting count of entities.
         * @param deletePsql The PSQL for deleting entities.
         */
        public EntityPsqlDefinition(final String selectPsql, final String selectCountPsql,
                final String deletePsql) {
            super();
            this.selectPsql = selectPsql;
            this.selectCountPsql = selectCountPsql;
            this.deletePsql = deletePsql;
        }
        /**
         * @return the selectPsql
         */
        public String getSelectPsql() {
            return selectPsql;
        }
        /**
         * @return the selectCountPsql
         */
        public String getSelectCountPsql() {
            return selectCountPsql;
        }
        /**
         * @return the deletePsql
         */
        public String getDeletePsql() {
            return deletePsql;
        }
    }
}

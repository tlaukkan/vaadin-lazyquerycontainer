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
    
    /**
     * Constructor for configuring query definition.
     * @param entityManager The JPA EntityManager.
     * @param applicationManagedTransactions True if application manages transactions instead of container.
     * @param entityClass The entity class.
     * @param batchSize The batch size.
     * @param nativeSortPropertyIds Properties participating in the native sort.
     * @param nativeSortPropertyAscendingStates List of property sort directions for the native sort.
     */
    public EntityQueryDefinition(final EntityManager entityManager, final boolean applicationManagedTransactions,
            final Class<?> entityClass, final int batchSize,
            final Object[] nativeSortPropertyIds, final boolean[] nativeSortPropertyAscendingStates) {
        super(batchSize);
        this.entityManager = entityManager;
        this.applicationManagedTransactions = applicationManagedTransactions;
        this.entityClass = entityClass;
        this.nativeSortPropertyIds = nativeSortPropertyIds;
        this.nativeSortPropertyAscendingStates = nativeSortPropertyAscendingStates;
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
     * Gets class of the persistent entity type.
     * @return the entityClass
     */
    public final Class<?> getEntityClass() {
        return entityClass;
    }
    
    /**
     * Method which generates and returns entity select definition based
     * on query definition parameters.
     * @return the generated EntitySelectDefinition.
     */
    public final EntitySelectDefinition getEntitySelectDefinition() {
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
        if (whereCriteria != null) {
            selectBuilder.append(whereBuilder.toString());
        }
        selectBuilder.append(orderByBuilder.toString());
        
        final StringBuilder selectCountBuilder = new StringBuilder("select count(e)");
        selectCountBuilder.append(fromBuilder.toString());
        if (whereCriteria != null) {
            selectCountBuilder.append(whereBuilder.toString());
        }

        return new EntitySelectDefinition(selectBuilder.toString(), selectCountBuilder.toString());
    }

    /**
     * @return the whereParameters
     */
    public final Map<String, Object> getWhereParameters() {
        if (whereCriteria != null) {
            return whereParameters;
        } else {
            return null;
        }
    }

    /**
     * Java bean which contains the entity select definition.
     * @author Tommi Laukkanen
     */
    public final class EntitySelectDefinition {
        /** The PSQL for selecting entities. */
        private String selectPsql;
        /** The PSQL for selecting count of entities. */
        private String selectCountPsql;
        /**
         * Constructor which initializes entity select definition.
         * @param selectPsql The PSQL for selecting entities.
         * @param selectCountPsql The PSQL for selecting count of entities.
         */
        public EntitySelectDefinition(final String selectPsql, final String selectCountPsql) {
            super();
            this.selectPsql = selectPsql;
            this.selectCountPsql = selectCountPsql;
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
    }
}

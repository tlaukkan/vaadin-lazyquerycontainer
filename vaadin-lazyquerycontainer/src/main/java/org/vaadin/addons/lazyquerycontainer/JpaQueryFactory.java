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

import javax.persistence.EntityManager;

/**
 * Query factory implementation for JpaQuery.
 * 
 * @author Tommi Laukkanen
 * @param <T>
 *            The value bean class
 */
public final class JpaQueryFactory<T> implements QueryFactory {
    /** The JPA EntityManager. */
    private EntityManager entityManager;
    /** The JPA bean class. */
    private Class<T> beanClass;
    /** QueryDefinition contains definition of the query properties. */    
    private QueryDefinition queryDefinition;
    /** The JPA select query. */
    private String jpaSelectQuery;
    /** The JPA select count query. */
    private String jpaSelectCountQuery;
    /** The native sort property IDs. */
    private Object[] nativeSortPropertyIds;
    /** The native sort ascending and descending states. */
    private boolean[] nativeSortStates;
    /** Flag reflecting whether application manages transactions. */
    private boolean transactionManagement;

    /**
     * Constructs JpaQueryFactory and sets the user defined parameters.
     * @param entityManager The entity manager to be used.
     * @param beanClass The value bean class.
     * @param jpaSelectQuery The select query used to select batches of value beans.
     * @param jpaSelectCountQuery The select count query which returns total number of beans.
     * @param nativeSortPropertyIds The properties used in native sort.
     * @param nativeSortStates The native sort states. True corresponds ascending and false descending.
     * @param transactionManagement True if application managed transactions are to be used.
     */
    public JpaQueryFactory(final EntityManager entityManager, final Class<T> beanClass, final String jpaSelectQuery,
            final String jpaSelectCountQuery, final Object[] nativeSortPropertyIds, final boolean[] nativeSortStates,
            final boolean transactionManagement) {
        super();
        this.entityManager = entityManager;
        this.beanClass = beanClass;
        this.jpaSelectQuery = jpaSelectQuery;
        this.jpaSelectCountQuery = jpaSelectCountQuery;
        this.nativeSortPropertyIds = nativeSortPropertyIds;
        this.nativeSortStates = nativeSortStates;
        this.transactionManagement = transactionManagement;
    }

    /**
     * Gets the definition of properties to be queried.
     * @param queryDefinition The query definition.
     */
    public void setQueryDefinition(final QueryDefinition queryDefinition) {
        this.queryDefinition = queryDefinition;
    }

    /**
     * Constructs a new query according to the given sort state.
     * @param sortPropertyIds Properties participating in the sorting.
     * @param sortStates List of sort order for the properties. True corresponds ascending and false descending.
     * @return A new query constructed according to the given sort state.
     */
    public Query constructQuery(final Object[] sortPropertyIds, final boolean[] sortStates) {
        return new JpaQuery<T>(beanClass, entityManager, jpaSelectQuery, jpaSelectCountQuery, queryDefinition,
                nativeSortPropertyIds, nativeSortStates, sortPropertyIds, sortStates, transactionManagement);
    }

}

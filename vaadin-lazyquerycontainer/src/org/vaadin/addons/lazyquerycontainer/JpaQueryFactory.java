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

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

/**
 * Query factory implementation for JpaQuery.
 * @author Tommi Laukkanen
 * @param <T> The value bean class
 */
public class JpaQueryFactory<T> implements QueryFactory {

        private EntityManager entityManager;
        private Class<T> beanClass;
        private QueryDefinition definition;
        private String jpaSelectQuery;
        private String jpaSelectCountQuery;
        private Object[] nativeSortPropertyIds;
        private boolean[] nativeSortStates;
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
        public JpaQueryFactory(EntityManager entityManager, Class<T> beanClass, String jpaSelectQuery, String jpaSelectCountQuery, Object[] nativeSortPropertyIds, boolean[] nativeSortStates, boolean transactionManagement) {
                super();
                this.entityManager = entityManager;
                this.beanClass=beanClass;
                this.jpaSelectQuery=jpaSelectQuery;
                this.jpaSelectCountQuery=jpaSelectCountQuery;
                this.nativeSortPropertyIds=nativeSortPropertyIds;
                this.nativeSortStates=nativeSortStates;
                this.transactionManagement=transactionManagement;
        }

        @Override
        public void setQueryDefinition(QueryDefinition definition) {
                this.definition=definition;
        }
        
        @Override
        public Query constructQuery(Object[] sortPropertyIds, boolean[] sortStates) {
                return new JpaQuery<T>(beanClass, entityManager, jpaSelectQuery, jpaSelectCountQuery, definition, nativeSortPropertyIds, nativeSortStates, sortPropertyIds, sortStates, transactionManagement);
        }

}


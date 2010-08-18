package org.vaadin.addons.lazyquerycontainer.example;

import javax.persistence.EntityManager;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

public class TaskQueryFactory implements QueryFactory {

        private EntityManager entityManager;
        private QueryDefinition definition;
        
        public TaskQueryFactory(EntityManager entityManager) {
                super();
                this.entityManager = entityManager;
        }

        @Override
        public void setQueryDefinition(QueryDefinition definition) {
                this.definition=definition;
        }
        
        @Override
        public Query constructQuery(Object[] sortPropertyIds, boolean[] sortStates) {
                return new TaskQuery(entityManager,definition,sortPropertyIds,sortStates);
        }

}


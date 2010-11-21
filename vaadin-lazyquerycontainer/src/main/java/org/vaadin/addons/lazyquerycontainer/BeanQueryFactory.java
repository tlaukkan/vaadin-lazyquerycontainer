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

import java.util.Map;

/**
 * QueryFactory implementation for BeanQuery.
 * @author Tommi Laukkanen
 * @param <Q> The BeanQuery implementation class
 * @param <T> The value bean class
 */
@SuppressWarnings("rawtypes")
public class BeanQueryFactory<Q extends AbstractBeanQuery> implements QueryFactory {

		private QueryDefinition definition;
        private Class<Q> queryClass;
        private Map<String,Object> queryConfiguration;

        /**
         * Constructs BeanQuery and sets the user defined parameters.
         * @param queryClass The BeanQuery class;
         * @param beanClass The value bean class.
         */
        public BeanQueryFactory(Class<Q> queryClass) {
                super();
                this.queryClass=queryClass;
        }
        
        /**
         * Sets the query configuration for the custom query implementation.
         * @param queryConfiguration The query configuration to be used by the custom query implementation.
         */
        public void setQueryConfiguration(Map<String,Object> queryConfiguration) {
        	this.queryConfiguration=queryConfiguration;
        }

        public void setQueryDefinition(QueryDefinition definition) {
                this.definition=definition;
        }
        
        public Query constructQuery(Object[] sortPropertyIds, boolean[] sortStates) {
        	Q query;

        	try {
				query = queryClass.getConstructor(new Class[]{
						QueryDefinition.class,Map.class,Object[].class,boolean[].class
						}).newInstance( new Object[]{
								definition,queryConfiguration,sortPropertyIds,sortStates
						});
			} catch (Exception e) {
				throw new RuntimeException("Error instantiating query.");
			}
			        	
        	return query;
        }

}


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

import java.util.AbstractList;

/**
 * The SmartItemIdListFactory uses a {@link SmartLazyIdList} when
 * the queryDefinition provides an idPropertyId and the the propertytype
 * of the idPropertyId is a comparable.
 * 
 * @author Michael J. Simons, 2013-11-29
 */
public class SmartItemIdListFactory implements ItemIdListFactory {
	private final DefaultItemIdListFactory delegate = new DefaultItemIdListFactory();

	@SuppressWarnings("rawtypes")
	@Override
	public AbstractList<?> produce(QueryDefinition queryDefinition, LazyQueryView queryView) {
		AbstractList<?> rv = null;
		if(queryDefinition.getIdPropertyId() != null && Comparable.class.isAssignableFrom(queryDefinition.getPropertyType(queryDefinition.getIdPropertyId()))) {			
			rv = new SmartLazyIdList(queryView, queryDefinition.getIdPropertyId());
		} else {
			rv = delegate.produce(queryDefinition, queryView);
		}
		return rv;
	}	
}
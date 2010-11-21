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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Default implementation of Query Definition. Stores the property information
 * of query to simple Map structure.
 * @author Tommi S.E. Laukkanen
 */
public class DefaultQueryDefinition implements QueryDefinition {

	private List<Object> propertIds=new ArrayList<Object>();
	private Map<Object,Object> propertyTypes=new TreeMap<Object,Object>();
	private Map<Object,Object> defaultValues=new HashMap<Object,Object>();
	private Map<Object,Boolean> readOnlyStates=new HashMap<Object,Boolean>();
	private Map<Object,Boolean> sortableStates=new HashMap<Object,Boolean>();
	private final int batchSize;
	
	public DefaultQueryDefinition(int batchSize) {
	    this.batchSize = batchSize;
	}
	
	public Collection<?> getPropertyIds() {
		return Collections.unmodifiableCollection(propertIds);
	}
	
	public Collection<?> getSortablePropertyIds() {
		List<Object> sortablePropertyIds=new ArrayList<Object>();
		for(Object propertyId : propertIds) {
			if(isPropertySortable(propertyId)) {
				sortablePropertyIds.add(propertyId);
			}			
		}
		return sortablePropertyIds;
	}

	public Object getPropertyDefaultValue(Object propertyId) {
		return defaultValues.get(propertyId);
	}

	public Class<?> getPropertyType(Object propertyId) {
		return (Class<?>) propertyTypes.get(propertyId);
	}
	
	public boolean isPropertyReadOnly(Object propertyId) {
		return readOnlyStates.get(propertyId);
	}

	public boolean isPropertySortable(Object propertyId) {
		return sortableStates.get(propertyId);
	}

	public void addProperty(Object propertyId, Class<?> type,
			Object defaultValue, boolean readOnly, boolean sortable) {
		propertIds.add(propertyId);
		propertyTypes.put(propertyId, type);
		defaultValues.put(propertyId, defaultValue);
		readOnlyStates.put(propertyId, readOnly);
		sortableStates.put(propertyId, sortable);
	}

	public void removeProperty(Object propertyId) {
		propertIds.remove(propertyId);
		propertyTypes.remove(propertyId);
		defaultValues.remove(propertyId);
		readOnlyStates.remove(propertyId);
		sortableStates.remove(propertyId);
	}

	public int getBatchSize() {
	    return batchSize;
	}

}

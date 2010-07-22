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

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Interface for querying data in batches.
 * @author Tommi S.E. Laukkanen
 */
public interface Query {
	/**
	 * Gets a list of items.
	 * @param startIndex Starting index of the item list.
	 * @param count Count of the items to be retrieved.
	 * @return List of items.
	 */
	public List<Item> getItems(int startIndex,int count);
	/**
	 * Invoked by QueryView to notify Query that value has been changed and should be stored
	 * to data store.
	 * @param item Item containing the changed property.
	 * @param propertyId The id of the property containing changed value.
	 * @param property The property containing the changed value.
	 */
	public void itemValueChange(Item item, Object propertyId, Property property);
	/**
	 * Gets number of items available through this query.
	 * @return Number of items.
	 */
	public int size();
}

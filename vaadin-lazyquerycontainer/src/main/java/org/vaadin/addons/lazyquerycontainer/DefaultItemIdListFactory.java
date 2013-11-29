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
 * Produces a LazyIdList when an IdPropertyId is definied in the querydefinition,
 * uses otherwise a list of natural numbers.
 * 
 * Be careful with the LazyIdList on large containers in combination with Vaadins combobox.
 * 
 * @author Michael J. Simons, 2013-11-28
 */
public class DefaultItemIdListFactory implements ItemIdListFactory {
	@Override
	public AbstractList<?> produce(final QueryDefinition queryDefinition, final LazyQueryView queryView) {
		AbstractList<?> rv = null;
		if (queryDefinition.getIdPropertyId() != null) {
			rv = new LazyIdList<Object>(queryView, queryDefinition.getIdPropertyId());
        } else {
            rv = new NaturalNumberIdsList(queryView.size());
        }
		return rv;
	}	
}
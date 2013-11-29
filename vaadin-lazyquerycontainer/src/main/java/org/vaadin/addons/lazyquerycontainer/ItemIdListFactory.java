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
 * Produces new ItemIdLists for the {@link LazyQueryView}
 * 
 * @author Michael J. Simons, 2013-11-28
 */
public interface ItemIdListFactory {
	/**
	 * @param queryDefinition
	 * @param queryView The query view which uses the ItemIdLists
	 * @return
	 */
	public AbstractList<?> produce(final QueryDefinition queryDefinition, final LazyQueryView queryView); 
}
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

/**
 * Enumeration defining the query item states.
 * @author Tommi S.E. Laukkanen
 */
public enum QueryItemStatus {
    /** Item state is up to date which storage. */
	None,
	/** Item is newly added and needs to be inserted to storage. */
	Added,
	/** Item has been modified and needs to updated to storage. */
	Modified,
	/** Item has been removed and needs to be deleted from storage. */
	Removed
}

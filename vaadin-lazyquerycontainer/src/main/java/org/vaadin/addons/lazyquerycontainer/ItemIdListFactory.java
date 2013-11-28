package org.vaadin.addons.lazyquerycontainer;

import java.util.AbstractList;

/**
 * Produces new ItemIdLists for the {@link LazyQueryView}
 * @author Michael J. Simons
 */
public interface ItemIdListFactory {
	/**
	 * @param queryDefinition
	 * @param queryView The query view which uses the ItemIdLists
	 * @return
	 */
	public AbstractList<?> produce(final QueryDefinition queryDefinition, final LazyQueryView queryView); 
}
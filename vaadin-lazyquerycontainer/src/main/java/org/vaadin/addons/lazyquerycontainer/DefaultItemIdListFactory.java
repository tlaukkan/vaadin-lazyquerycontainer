package org.vaadin.addons.lazyquerycontainer;

import java.util.AbstractList;

/**
 * Produces a LazyIdList when an IdPropertyId is definied in the querydefinition,
 * uses otherwise a list of natural numbers.
 * 
 * Be careful with the LazyIdList on large containers in combination with Vaadins combobox.
 * 
 * @author Michael J. Simons
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
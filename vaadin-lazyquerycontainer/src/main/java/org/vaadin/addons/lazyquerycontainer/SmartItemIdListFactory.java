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
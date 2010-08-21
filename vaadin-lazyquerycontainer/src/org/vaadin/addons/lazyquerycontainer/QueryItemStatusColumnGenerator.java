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

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

/**
 * Helper class for Vaadin tables to generate status column.
 * @author Tommi S.E. Laukkanen
 */
public class QueryItemStatusColumnGenerator implements ColumnGenerator, ValueChangeListener {
	private static final long serialVersionUID = 1L;

	private Application application;
	private Resource noneIconResource;
	private Resource addedIconResource;
	private Resource modifiedIconResource;
	private Resource removedIconResource;
	private Embedded statusIcon;
	
	public QueryItemStatusColumnGenerator(Application application) {
		this.application=application;
	}
	
	@Override
	public Component generateCell(Table source, Object itemId, Object columnId) {
		Property statusProperty = source.getItem(itemId).getItemProperty(columnId);

		noneIconResource = new ClassResource(QueryItemStatusColumnGenerator.class, "images/textfield.png", this.application);		
		addedIconResource = new ClassResource(QueryItemStatusColumnGenerator.class, "images/textfield_add.png", this.application);		
		modifiedIconResource = new ClassResource(QueryItemStatusColumnGenerator.class, "images/textfield_rename.png", this.application);		
		removedIconResource = new ClassResource(QueryItemStatusColumnGenerator.class, "images/textfield_delete.png", this.application);		
		
		statusIcon=new Embedded(null,noneIconResource);
		statusIcon.setHeight("16px");

		if(statusProperty instanceof ValueChangeNotifier) {
			ValueChangeNotifier notifier=(ValueChangeNotifier) statusProperty;
			notifier.addListener(this);
		}

		refreshImage(statusProperty);
		
		return statusIcon;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		refreshImage(event.getProperty());	
		statusIcon.requestRepaint();
	}
	
	public void refreshImage(Property statusProperty) {
		if(statusProperty.getValue()==null) {
			statusIcon.setSource(noneIconResource);
			return;
		}	
		QueryItemStatus status=(QueryItemStatus)statusProperty.getValue();
		if(status==QueryItemStatus.None) {
			statusIcon.setSource(noneIconResource);
		}
		if(status==QueryItemStatus.Modified) {
			statusIcon.setSource(modifiedIconResource);
		}
		if(status==QueryItemStatus.Added) {
			statusIcon.setSource(addedIconResource);
		}
		if(status==QueryItemStatus.Removed) {
			statusIcon.setSource(removedIconResource);
		}				
	}

}

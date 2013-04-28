package org.vaadin.addons.lazyquerycontainer.test;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Test;
import org.vaadin.addons.lazyquerycontainer.EntityNestedProperty;

/**
 * Unit tests for the EntityNestedProperty
 * @author Eduardo Frazao - edufrazao@gmail.com
 *
 */
public class EntityNestedPropertyTest {

	SomeEntity entity = new SomeEntity(1, "A Name", null);
	
	@Test
	public void testReadNullNestedProperties() {
		EntityNestedProperty<Object> propertyId = new EntityNestedProperty<Object>(entity, "anotherEntity.id");
		
		Assert.assertNull("Property read on null nested property will only return null", propertyId.getValue());
	}
	
	@Test
	public void testReadNullNestesPropertiesManyLevels() {
		EntityNestedProperty<Object> propertyId = new EntityNestedProperty<Object>(entity, "anotherEntity.anotherEntity.anotherEntity.id");
		Assert.assertNull("Property read on null nested property will only return null", propertyId.getValue());
	}
	
	@Test
	public void testReadNotNullNestedProperties() {
		final Integer nestedPropertyId = 2;
		final String nestedPropertyName = "Another Name";
		
		SomeEntity nestedProperty = new SomeEntity(new Integer(nestedPropertyId), new String(nestedPropertyName), null);
		entity.setAnotherEntity(nestedProperty);
		
		EntityNestedProperty<Object> propertyId = new EntityNestedProperty<Object>(entity, "anotherEntity.id");
		EntityNestedProperty<Object> propertyName = new EntityNestedProperty<Object>(entity, "anotherEntity.name");
		
		Assert.assertEquals(nestedPropertyId, propertyId.getValue());
		Assert.assertEquals(nestedPropertyName, propertyName.getValue());
	}
	
	@Test
	public void testReadNotNullNestesPropertiesManyLevels() {
		final Integer nestedPropertyId = 4;
		final String nestedPropertyName = "Entity 4";
		
		SomeEntity nestedProperty4 = new SomeEntity(new Integer(nestedPropertyId), new String(nestedPropertyName), null);
		SomeEntity nestedProperty3 = new SomeEntity(3, "Does not matter", nestedProperty4);
		SomeEntity nestedProperty2 = new SomeEntity(2, "Does not matter", nestedProperty3);
		
		entity.setAnotherEntity(nestedProperty2);
		
		EntityNestedProperty<Object> propertyId = new EntityNestedProperty<Object>(entity, "anotherEntity.anotherEntity.anotherEntity.id");
		EntityNestedProperty<Object> propertyName = new EntityNestedProperty<Object>(entity, "anotherEntity.anotherEntity.anotherEntity.name");
		
		Assert.assertEquals(nestedPropertyId, propertyId.getValue());
		Assert.assertEquals(nestedPropertyName, propertyName.getValue());
	}
	
	@Test
	public void testReadNotNullFromIntermediaryLevel() {
		final Integer nestedPropertyId = 2;
		final String nestedPropertyName = "Level 2";
		
		SomeEntity nestedProperty4 = new SomeEntity(4, "Does not matter", null);
		SomeEntity nestedProperty3 = new SomeEntity(new Integer(nestedPropertyId), new String(nestedPropertyName), nestedProperty4);
		SomeEntity nestedProperty2 = new SomeEntity(2, "Does not matter", nestedProperty3);
		
		entity.setAnotherEntity(nestedProperty2);
		
		EntityNestedProperty<Object> propertyId = new EntityNestedProperty<Object>(entity, "anotherEntity.anotherEntity.id");
		EntityNestedProperty<Object> propertyName = new EntityNestedProperty<Object>(entity, "anotherEntity.anotherEntity.name");
		
		Assert.assertEquals(nestedPropertyId, propertyId.getValue());
		Assert.assertEquals(nestedPropertyName, propertyName.getValue());
	}
	
	@Test
	public void testReturnTypes() {
		EntityNestedProperty<Object> propertyId = new EntityNestedProperty<Object>(entity, "anotherEntity.id");
		EntityNestedProperty<Object> propertyName = new EntityNestedProperty<Object>(entity, "anotherEntity.name");
		
		Assert.assertEquals(Integer.class, propertyId.getType());
		Assert.assertEquals(String.class, propertyName.getType());
	}
	
	@Test
	public void testReturnTypesFromManyLevels() {
		EntityNestedProperty<Object> propertyId = new EntityNestedProperty<Object>(entity, "anotherEntity.anotherEntity.anotherEntity.id");
		EntityNestedProperty<Object> propertyName = new EntityNestedProperty<Object>(entity, "anotherEntity.anotherEntity.anotherEntity.name");
		
		Assert.assertEquals(Integer.class, propertyId.getType());
		Assert.assertEquals(String.class, propertyName.getType());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddingInexistentProperties() {
		
		new EntityNestedProperty<Object>(entity, "anotherEntity.anotherEntity.inexistentProperty.id");
		
	}
	
	// Write value
	@Test(expected=NullPointerException.class)
	public void testSetValueOnNullNestedProperty() {
		EntityNestedProperty<Object> propertyId = new EntityNestedProperty<Object>(entity, "anotherEntity.id");
		propertyId.setValue(1);
	}
	
	@Test
	public void testSetValueOnNotNullNestedProperty() {
		SomeEntity nestedEntity = new SomeEntity(null, "Does not matter", null);
		entity.setAnotherEntity(nestedEntity);
		
		EntityNestedProperty<Integer> propertyId = new EntityNestedProperty<Integer>(entity, "anotherEntity.id");
		propertyId.setValue(125);
		
		Assert.assertEquals(new Integer(125), propertyId.getValue());
	}
	
	@Test
	public void testSetValueMultipleLevels() {
		final Integer nestedPropertyId = 4;
		final String nestedPropertyName = "Entity 4";
		
		SomeEntity nestedProperty4 = new SomeEntity(new Integer(nestedPropertyId), null, null);
		SomeEntity nestedProperty3 = new SomeEntity(3, "Does not matter", nestedProperty4);
		SomeEntity nestedProperty2 = new SomeEntity(2, "Does not matter", nestedProperty3);
		
		entity.setAnotherEntity(nestedProperty2);
		
		EntityNestedProperty<String> propertyName = new EntityNestedProperty<String>(entity, "anotherEntity.anotherEntity.anotherEntity.name");
		propertyName.setValue(new String(nestedPropertyName));
		
		Assert.assertEquals(nestedPropertyName, propertyName.getValue());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSetValueWrongTyped() {
		SomeEntity nestedEntity = new SomeEntity(null, "Does not matter", null);
		entity.setAnotherEntity(nestedEntity);
		
		EntityNestedProperty<Object> propertyId = new EntityNestedProperty<Object>(entity, "anotherEntity.id");
		propertyId.setValue("Setting a string on a Integer property");
	}
	
	// An simple entity for tests
	public static class SomeEntity implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public SomeEntity() {
			super();
		}
		
		public SomeEntity(Integer id, String name, SomeEntity anotherEntity) {
			this();
			this.id = id;
			this.name = name;
			this.anotherEntity = anotherEntity;
		}

		// Normal properties
		private Integer id;
		private String name;
		
		// An nested property
		private SomeEntity anotherEntity;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public SomeEntity getAnotherEntity() {
			return anotherEntity;
		}

		public void setAnotherEntity(SomeEntity anotherEntity) {
			this.anotherEntity = anotherEntity;
		}
	}

}

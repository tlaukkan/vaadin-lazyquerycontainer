package org.vaadin.addons.lazyquerycontainer;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;

import com.vaadin.data.util.AbstractProperty;

/**
 * An implementation of <tt>Property<tt> that supports 1:1 nested properties<br>
 * on Java Beans even with null references
 * @author Eduardo Frazao - edufrazao@gmail.com
 *
 * @param <T> Type of Property
 */
public class EntityNestedProperty<T> extends AbstractProperty<T> {

	private static final long serialVersionUID = 1L;

	private Serializable entity;
	
	private String nestedProperty;
	private PropertyDescriptor nestedPropertyDescriptor;
	private Object nestedTarget;
	private Class<T> nestedTargetClass;
	
	/**
	 * Returns a new Item
	 * @param entity
	 * @param nestedProperty
	 */
	public EntityNestedProperty(final Serializable entity, final String nestedProperty) {
		super();
		if(entity == null || nestedProperty == null) {
			throw new NullPointerException("Constructor fields cannot be null");
		}
		if(nestedProperty.trim().isEmpty()) {
			throw new IllegalArgumentException("Nested property path cannot be empty");
		}
		this.entity = entity;
		this.nestedProperty = nestedProperty.trim();
		
		nestedTargetClass = getTypeFromPropertyPath();
	}

	@Override
	public T getValue() {
		findPropertyDescriptor(); // Refreshs local references
		if(nestedTarget != null) {
			Method method = nestedPropertyDescriptor.getReadMethod();
			try {
				return getType().cast(method.invoke(nestedTarget, new Object[0]));
			} catch (Exception e) {
				throw new IllegalArgumentException(
						String.format("Unable to invoke read method [%s] from the nested property [%s]", 
								method.getName(), nestedTarget.getClass().getName()));
			}
		}
		return null;
	}

	@Override
	public void setValue(T newValue) throws com.vaadin.data.Property.ReadOnlyException {
		if(isReadOnly()) {
			throw new ReadOnlyException();
		}
		findPropertyDescriptor(); // Refreshs local references
		if(nestedTarget != null) {
			Method method = nestedPropertyDescriptor.getWriteMethod();
			if(method == null) {
				throw new IllegalStateException(
						String.format("The property [%] does not have an write method", nestedPropertyDescriptor.getName()));
			}
			try {
				if(newValue != null && !method.getParameterTypes()[0].isAssignableFrom(newValue.getClass())) {
					throw new IllegalArgumentException(
							String.format("Unable to set value of type [%s] on the property [%] with type [%]",
							newValue.getClass().getName(),
							nestedPropertyDescriptor.getName(),
							method.getParameterTypes()[0].getName()
							));
				}
				method.invoke(nestedTarget, new Object[] {newValue});
			} catch (Exception e) {
				throw new IllegalArgumentException(
						String.format("Unable to invoke write method [%s] from the nested property [%s]", 
								method.getName(), nestedTarget.getClass().getName()));
			}
		} else {
			throw new NullPointerException("Unable to set value because the nested property is null");
		}
	}
	
	@Override
	public Class<? extends T> getType() {
		return nestedTargetClass;
	}
	
	/**
	 * 
	 * Recursivelly iterates properties to reach the last target if possible (null is acceptable)
	 * This method will keep a reference to the very last property descriptor, and the last target
	 * object that owns the property. This will cache this recursive operation and permits that
	 * the getValue() methods directly calls the getter of property without cache this result.
	 * 
	 */
	private void findPropertyDescriptor() {
		if(nestedProperty.contains(".")) {
			String[] properties = nestedProperty.split("\\.");
			String targetProperty = properties[ properties.length -1 ];
			Object target = entity;
			PropertyDescriptor pd = null;
			for(String property : properties) {
				try {
					if(target != null) {
						pd = getPropertyDescriptorFromProperty(property, target.getClass());
						if(!property.equals(targetProperty)) {
							target = pd.getReadMethod().invoke(target, new Object[0]);
						}
					} else {
						pd = null;
						target = null;
						break;
					}
				} catch (Exception e) {
					throw new IllegalArgumentException(
							String.format("Unable to invoke read method from property [%s] of target [%s] while iterating nested property path", property, target.getClass().getName()));
				}
			}
			nestedPropertyDescriptor = pd;
			nestedTarget = target;
		} else {
			nestedPropertyDescriptor = getPropertyDescriptorFromProperty(nestedProperty, entity.getClass());
		}
	}
	
	
	/**
	 * Local helper that uses class context objects do call static methods
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<T> getTypeFromPropertyPath() {
		return (Class<T>) getTypeFromPropertyPath(nestedProperty, entity.getClass());
	}
	
	
	// Local and Package Helper Methods
	/**
	 * Returns the type of the property using its read method as reference
	 * @param propertyName
	 * @param ownerClass
	 * @return
	 */
	protected static Class<?> getTypeOfProperty(String propertyName, Class<?> ownerClass) {
		PropertyDescriptor pd = getPropertyDescriptorFromProperty(propertyName, ownerClass);
		Method method = pd.getReadMethod();
		if(method != null) {
			return method.getReturnType();
		}
		throw new IllegalArgumentException(String.format("Property [%s] not found on target [%s]", propertyName, ownerClass.getName()));
	}
	
	/**
	 * Returns the last property type of the nested property dotted expression
	 * @param nestedProperty
	 * @param entityClass
	 * @return
	 */
	protected static Class<?> getTypeFromPropertyPath(final String nestedProperty, final Class<?> entityClass) {
		if(nestedProperty.contains(".")) {
			Class<?> clazz = entityClass;
			String[] properties = nestedProperty.split("\\.");
			for(String property : properties) {
				clazz = getTypeOfProperty(property, clazz);
				if(Collection.class.isAssignableFrom(clazz) || clazz.isArray()) {
					throw new IllegalArgumentException(
							String.format("This implementation does not support nested collections or arrays as properties: [%s] of [%s]",
									property,
									clazz.getName()
									));
				}
			}
			return clazz;
		} else {
			return getTypeOfProperty(nestedProperty, entityClass);
		}
	}

	/**
	 * Fetch the Java Beans property descriptor of an target
	 * @param propertyName The name of property
	 * @param target Target to Collect
	 * @return Java Bean property descriptor
	 * @throws IllegalArgumentException - In the case that no property descriptor is found for this property name
	 */
	protected static PropertyDescriptor getPropertyDescriptorFromProperty(String propertyName, Class<?> objectClass) throws IllegalArgumentException {
		
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(objectClass);
			
			for(PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
				if(pd.getName().equals(propertyName)) {
					return pd;
				}
			}
		} catch (IntrospectionException ex) {
			throw new IllegalStateException(String.format("Unable to introspect class [%s]", objectClass.getName()));
		}
		
		throw new IllegalArgumentException(String.format("No property descriptor found for the property [%s] of target class [%s]", propertyName, objectClass.getName()));
		
	}
	
}

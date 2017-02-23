/**
 * Copyright 2000-2013 Vaadin Ltd.
 * Copyright 2013 Tommi S.E. Laukkanen
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

import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.data.util.LazyNestedMethodProperty;
import com.vaadin.v7.data.util.MethodPropertyDescriptor;
import com.vaadin.v7.data.util.VaadinPropertyDescriptor;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Specialized version of BeanItem to allow for automated expansion of nested properties.
 * Partly copied from BeanItem.
 *
 * @param <BT> the bean type
 * @author Vaadin Ltd.
 * @author Tommi S.E. Laukkanen
 */
@SuppressWarnings("serial")
public class NestingBeanItem<BT> extends BeanItem<BT> {

    /**
     * The max nested property depth.
     */
    private final int maxNestedPropertyDepth;

    /**
     * Constructor for defining the nested bean item parameters.
     *
     * @param bean the bean
     * @param maxNestedPropertyDepth the max nested property depth.
     * @param propertyIds the propertyIds
     */
    public NestingBeanItem(final BT bean, final int maxNestedPropertyDepth, final Collection<Object> propertyIds) {
        super(bean);

        this.maxNestedPropertyDepth = maxNestedPropertyDepth;

        if (maxNestedPropertyDepth > 0) {
            for (final Object propertyId : propertyIds) {
                final String propertyName = (String) propertyId;
                if (propertyName.indexOf('.') > -1) {
                    final String[] parts = propertyName.split("\\.");
                    final StringBuilder nameBuilder = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (nameBuilder.length() == 0) {
                            nameBuilder.append(parts[i]);
                        } else {
                            nameBuilder.append('.');
                            nameBuilder.append(parts[i]);
                        }

                        final String parentPropertyName = nameBuilder.toString();
                        final String childPropertyName = parentPropertyName + "." + parts[i + 1];
                        if (!getItemPropertyIds().contains(childPropertyName)) {
                            expandProperty(parentPropertyName, parts[i + 1]);
                        }
                    }
                }
            }
        }
    }

    /**
     * Expands nested bean properties by replacing a top-level property with
     * some or all of its sub-properties. The expansion is not recursive.
     *
     * @param propertyId
     *            property id for the property whose sub-properties are to be
     *            expanded,
     * @param subPropertyIds
     *            sub-properties to expand, all sub-properties are expanded if
     *            not specified
     */
    public void expandProperty(final String propertyId, final String... subPropertyIds) {
        Set<String> subPropertySet = new HashSet<String>(
                Arrays.asList(subPropertyIds));

        if (0 == subPropertyIds.length) {
            // Enumerate all sub-properties
            Class<?> propertyType = getItemProperty(propertyId).getType();
            Map<String, ?> pds = getPropertyDescriptors(propertyType);
            subPropertySet.addAll(pds.keySet());
        }

        for (String subproperty : subPropertySet) {
            String qualifiedPropertyId = propertyId + "." + subproperty;
            addNestedProperty(qualifiedPropertyId);
        }

    }

    /**
     * Adds a nested property to the item.
     *
     * @param nestedPropertyId
     *            property id to add. This property must not exist in the item
     *            already and must of of form "field1.field2" where field2 is a
     *            field in the object referenced to by field1
     */
    public void addNestedProperty(final String nestedPropertyId) {
        addItemProperty(nestedPropertyId, new LazyNestedMethodProperty<Object>(
                getBean(), nestedPropertyId));
    }

    /**
     * <p>
     * Perform introspection on a Java Bean class to find its properties.
     * </p>
     *
     * <p>
     * Note : This version only supports introspectable bean properties and
     * their getter and setter methods. Stand-alone <code>is</code> and
     * <code>are</code> methods are not supported.
     * </p>
     *
     * @param beanClass
     *            the Java Bean class to get properties for.
     * @param <BT> the bean type
     * @return an ordered map from property names to property descriptors
     */
    static <BT> LinkedHashMap<String, VaadinPropertyDescriptor<BT>> getPropertyDescriptors(
            final Class<BT> beanClass) {
        final LinkedHashMap<String, VaadinPropertyDescriptor<BT>> pdMap = new LinkedHashMap<String, VaadinPropertyDescriptor<BT>>();

        // Try to introspect, if it fails, we just have an empty Item
        try {
            List<PropertyDescriptor> propertyDescriptors = getBeanPropertyDescriptor(beanClass);

            // Add all the bean properties as MethodProperties to this Item
            // later entries on the list overwrite earlier ones
            for (PropertyDescriptor pd : propertyDescriptors) {
                final Method getMethod = pd.getReadMethod();
                if ((getMethod != null)
                        && getMethod.getDeclaringClass() != Object.class) {
                    VaadinPropertyDescriptor<BT> vaadinPropertyDescriptor = new MethodPropertyDescriptor<BT>(
                            pd.getName(), pd.getPropertyType(),
                            pd.getReadMethod(), pd.getWriteMethod());
                    pdMap.put(pd.getName(), vaadinPropertyDescriptor);
                }
            }
        } catch (final java.beans.IntrospectionException ignored) {
        }

        return pdMap;
    }

    /**
     * Returns the property descriptors of a class or an interface.
     *
     * For an interface, superinterfaces are also iterated as Introspector does
     * not take them into account (Oracle Java bug 4275879), but in that case,
     * both the setter and the getter for a property must be in the same
     * interface and should not be overridden in subinterfaces for the discovery
     * to work correctly.
     *
     * For interfaces, the iteration is depth first and the properties of
     * superinterfaces are returned before those of their subinterfaces.
     *
     * @param beanClass the bean class
     * @return list of property descriptors
     * @throws IntrospectionException
     */
    private static List<PropertyDescriptor> getBeanPropertyDescriptor(
            final Class<?> beanClass) throws IntrospectionException {
        // Oracle bug 4275879: Introspector does not consider superinterfaces of
        // an interface
        if (beanClass.isInterface()) {
            List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();

            for (Class<?> cls : beanClass.getInterfaces()) {
                propertyDescriptors.addAll(getBeanPropertyDescriptor(cls));
            }

            BeanInfo info = Introspector.getBeanInfo(beanClass);
            propertyDescriptors.addAll(Arrays.asList(info
                    .getPropertyDescriptors()));

            return propertyDescriptors;
        } else {
            BeanInfo info = Introspector.getBeanInfo(beanClass);
            return Arrays.asList(info.getPropertyDescriptors());
        }
    }
}

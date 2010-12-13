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
import java.util.RandomAccess;

/**
 * Natural numbers list implementation used in LazyQueryContainer.getItemIds
 * method. This list is immutable and contains integer numbers from 0 to size - 1.
 * @author HRE@github.com, Tommi Laukkanen
 */
public final class NaturalNumbersList extends AbstractList<Integer> implements
        RandomAccess, java.io.Serializable {
    /** Java serialization version UID. */
    private static final long serialVersionUID = 1L;
    /** The size of the list. */
    private final int size;
    /** Array containing list values. This array is created on demand. */
    private Integer[] array = null;

    /**
     * Constructor which sets the size of the constructed list.
     * @param size Size of the constructed list.
     */
    public NaturalNumbersList(final int size) {
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Integer[] toArray() {
        if (array == null) {
            array = new Integer[size];
            for (int i = 0; i < size; i++) {
                array[i] = i;
            }
        }
        return array.clone();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] a) {
        if (a.length < size) {
           return (T[]) toArray();
        }
        for (int i = 0; i < size; i++) {
            a[i] = (T) Integer.valueOf(i);
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    /**
     * {@inheritDoc}
     */
    public Integer get(final int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return index;
    }

    /**
     * {@inheritDoc}
     */
    public Integer set(final int index, final Integer element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public int indexOf(final Object o) {
        if (o == null) {
            return -1;
        }
        if (o instanceof Integer) {
            int i = (Integer) o;
            if (i < 0 || i >= size) {
                return -1;
            }
            return i;
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(final Object o) {
        return indexOf(o) != -1;
    }
}
/* Copyright (C) 2013-2024 TU Dortmund University
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.common.util.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import net.automatalib.common.smartcollection.ArrayWritable;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractRangeTest<I, R extends AbstractList<I> & ArrayWritable<I>> {

    protected final R range;
    private final List<I> reference;

    public AbstractRangeTest(R range) {
        this.range = range;
        this.reference = new ArrayList<>(range);
    }

    @Test
    public void testSize() {
        Assert.assertEquals(this.range.size(), this.reference.size());
    }

    @Test
    public void testGet() {
        for (int i = 0; i < this.reference.size(); i++) {
            Assert.assertEquals(this.range.get(i), this.reference.get(i));
        }
    }

    @Test
    public void testIndexOf() {
        int idx = 0;
        for (I i : this.reference) {
            Assert.assertEquals(this.range.indexOf(i), idx);
            Assert.assertEquals(this.range.lastIndexOf(i), idx);
            idx++;
        }

        Assert.assertEquals(this.range.indexOf(null), -1);
        Assert.assertEquals(this.range.indexOf(new Object()), -1);
        Assert.assertEquals(this.range.lastIndexOf(null), -1);
        Assert.assertEquals(this.range.lastIndexOf(new Object()), -1);
    }

    @Test
    public void testListIterator() {
        final int size = range.size();

        Assert.assertThrows(IndexOutOfBoundsException.class, () -> range.listIterator(size + 1));

        final ListIterator<I> iterator = range.listIterator(size);
        Assert.assertEquals(iterator.nextIndex(), size);
        Assert.assertThrows(NoSuchElementException.class, iterator::next);

        for (int i = size - 1; i >= 0; i--) {
            Assert.assertTrue(iterator.hasPrevious());
            Assert.assertEquals(iterator.previous(), reference.get(i));
        }

        Assert.assertFalse(iterator.hasPrevious());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.nextIndex(), 0);
        Assert.assertEquals(iterator.previousIndex(), -1);
        Assert.assertThrows(NoSuchElementException.class, iterator::previous);
    }

    @Test
    public void testImmutability() {
        Assert.assertThrows(UnsupportedOperationException.class, () -> this.range.add(null));

        final Iterator<I> iterator = this.range.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNotNull(iterator.next());
        Assert.assertThrows(UnsupportedOperationException.class, iterator::remove);

        final ListIterator<I> listIterator = this.range.listIterator();
        Assert.assertTrue(listIterator.hasNext());
        Assert.assertNotNull(listIterator.next());
        Assert.assertThrows(UnsupportedOperationException.class, listIterator::remove);
        Assert.assertThrows(UnsupportedOperationException.class, () -> listIterator.add(null));
        Assert.assertThrows(UnsupportedOperationException.class, () -> listIterator.set(null));
    }

    @Test
    public void testArrayWritable() {
        final int size = this.range.size();
        final Object[] target = new Object[size];

        // write all
        this.range.writeToArray(0, target, 0, size);
        Assert.assertEquals(target, this.reference.toArray());

        // write first range half into second array half
        final int offset = size / 2;
        this.range.writeToArray(0, target, offset, size - offset);

        for (int i = 0; i < offset; i++) {
            Assert.assertEquals(target[i], this.range.get(i));
        }
        for (int i = offset; i < size; i++) {
            Assert.assertEquals(target[i], this.range.get(i - offset));
        }

        // write second range half into first array half
        this.range.writeToArray(size - offset, target, 0, offset);

        for (int i = 0; i < offset; i++) {
            Assert.assertEquals(target[i], this.range.get(size - offset + i));
        }
        for (int i = offset; i < size; i++) {
            Assert.assertEquals(target[i], this.range.get(i - offset));
        }
    }
}

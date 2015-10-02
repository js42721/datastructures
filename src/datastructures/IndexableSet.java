package datastructures;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Unordered set with random access. Performs insertion, deletion, and search
 * in O(1) time.
 *
 * @param <E> the element type
 */
public class IndexableSet<E> extends AbstractSet<E>
        implements Set<E>, Collection<E>, Serializable {

    private static final long serialVersionUID = 4873154254434468972L;

    private final ArrayList<E> items;
    private final HashMap<E, Integer> indices;
    private transient int modCount;

    /** Creates an empty set with an initial capacity of 8.*/
    public IndexableSet() {
        this(8);
    }

    /**
     * Creates an empty set with the specified initial capacity.
     *
     * @param  initialCapacity the initial capacity of the set
     * @throws IllegalArgumentException if the specified capacity is negative
     */
    public IndexableSet(int initialCapacity) {
        items = new ArrayList<E>(initialCapacity);
        indices = new HashMap<E, Integer>(initialCapacity);
    }

    /**
     * Returns the element at the specified index.
     *
     * @param  index the index of the element to return
     * @return the element at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public E get(int index) {
        return items.get(index);
    }

    /**
     * Removes the element at the specified index.
     *
     * @param  index the index of the element to remove
     * @return the removed element
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public E removeAt(int index) {
        E removed = items.get(index); // Throws.
        ++modCount;
        int i = indices.remove(removed); // Should never throw.
        E last = items.remove(items.size() - 1);
        if (i < items.size()) {
            items.set(i, last);
            indices.put(last, i);
        }
        return last;
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean contains(Object o) {
        return indices.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        if (indices.containsKey(e)) {
            return false;
        }
        ++modCount;
        indices.put(e, items.size());
        return items.add(e);
    }

    @Override
    public boolean remove(Object o) {
        Integer index = indices.get(o);
        if (index == null) {
            return false;
        }
        removeAt(index);
        return true;
    }

    @Override
    public void clear() {
        ++modCount;
        items.clear();
        indices.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return new IndexableSetIterator();
    }

    private class IndexableSetIterator implements Iterator<E> {
        int index;
        int lastReturned = -1;
        int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public E next() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = index++;
            return get(lastReturned);
        }

        @Override
        public void remove() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (lastReturned == -1) {
                throw new IllegalStateException();
            }
            removeAt(lastReturned);
            --index;
            lastReturned = -1;
            expectedModCount = modCount;
        }
    }
}

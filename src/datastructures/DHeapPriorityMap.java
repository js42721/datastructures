package datastructures;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A priority queue/map hybrid backed by a d-ary heap. Allows for duplicate
 * values but not keys. Supports O(1) find-min, O(lg n) insert, O(lg n) delete
 * (for any element), O(lg n) update (performed by the {@link #put} method),
 * and O(1) lookup by key.
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public class DHeapPriorityMap<K, V> {
    private static final int DEFAULT_D = 4;
    private static final int DEFAULT_CAPACITY = 8;

    private final Comparator<? super V> comparator;
    private Map<K, Integer> indices;
    private Entry<K, V>[] array;
    private final int d;
    private int size;

    /**
     * Constructs a map with the default initial capacity (8) and the default
     * fan-out for the underlying heap (4). Priority will be determined by the
     * natural ordering of the map's values.
     */
    public DHeapPriorityMap() {
        this(DEFAULT_CAPACITY, DEFAULT_D, null);
    }

    /**
     * Constructs a map with the default initial capacity (8) and the default
     * fan-out for the underlying heap (4). Priority will be determined by the
     * supplied comparator.
     * 
     * @param comparator the comparator which will be used to determine priority
     */
    public DHeapPriorityMap(Comparator<? super V> comparator) {
        this(DEFAULT_CAPACITY, DEFAULT_D, comparator);
    }

    /**
     * Constructs a map with the specified initial capacity and heap fan-out.
     * Priority will be determined by the natural ordering of the map's values.
     * 
     * @param  capacity the initial capacity of the map
     * @param  d the fan-out of the underlying heap
     * @throws IllegalArgumentException if capacity is less than 0 or if d is
     *         less than 2
     */
    public DHeapPriorityMap(int capacity, int d) {
        this(capacity, d, null);
    }

    /**
     * Constructs a map with the specified initial capacity and heap fan-out.
     * Priority will be determined by the supplied comparator.
     *
     * @param  capacity the initial capacity of the map
     * @param  d the fan-out of the underlying heap
     * @param  comparator the comparator which will be used to determine priority
     * @throws IllegalArgumentException if capacity is less than 0 or if d is
     *         less than 2
     */
    @SuppressWarnings("unchecked")
    public DHeapPriorityMap(int capacity, int d, Comparator<? super V> comparator) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must not be less than 0");
        }
        if (d < 2) {
            throw new IllegalArgumentException("Value of d must be 2 or greater");
        }
        this.d = d;
        this.comparator = comparator;
        array = new Entry[capacity > 0 ? capacity : 1];
        indices = new HashMap<K, Integer>(capacity);
    }

    /**
     * Returns {@code true} if the map contains no entries.
     * 
     * @return {@code true} if the map contains no entries
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of entries in the map.
     * 
     * @return the number of entries in the map
     */
    public int size() {
        return size;
    }

    /**
     * Returns the key of the entry with the highest priority (this is the entry
     * with the smallest value if a comparator is not provided).
     * 
     * @return the key of the entry with the highest priority (could be {@code
     *         null}) or {@code null} if the map is empty
     */
    public K peekKey() {
        return size == 0 ? null : array[0].key;
    }

    /**
     * Returns the value of the entry with the highest priority (this is the
     * entry with the smallest value if a comparator is not provided).
     * 
     * @return the value of the entry with the highest priority or {@code null}
     *         if the map is empty
     */
    public V peekValue() {
        return size == 0 ? null : array[0].value;
    }

    /**
     * Returns the value mapped to the specified key.
     * 
     * @param  key the key whose value is to be returned
     * @return the value mapped to the key or {@code null} if the map does not
     *         contain the key
     */
    public V get(Object key) {
        Integer i = indices.get(key);
        return i == null ? null : array[i].value;
    }

    /**
     * Inserts a key along with its associated value. If the map already
     * contains the key, the old value is replaced.
     * 
     * @param  key the key to be inserted
     * @param  value the value associated with the key
     * @return the value previously associated with the key or {@code null} if
     *         the map did not contain the key
     * @throws ClassCastException if the value cannot be compared with the
     *         values in the map
     * @throws NullPointerException if the value is null
     */
    public V put(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        V old = update(key, value);
        if (old != null) {
            return old;
        }
        int last = size;
        if (++size > array.length) {
            array = resizeArray(array, array.length * 2);
        }
        array[last] = new Entry<K, V>(key, value);
        siftUp(last);
        return null;
    }

    /**
     * Removes the entry with the highest priority.
     * 
     * @return {@code true} if the map changed as a result of the call
     */
    public boolean remove() {
        if (size == 0) {
            return false;
        }
        Entry<K, V> deleted = array[0];
        array[0] = array[--size];
        siftDown(0);
        array[size] = null;
        indices.remove(deleted.key);
        return true;
    }

    /**
     * Removes the entry for the specified key.
     * 
     * @param  key the key of the entry to be removed
     * @return the value which was mapped to the key or {@code null} if the map
     *         did not have an entry for the key
     */
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        Integer find = indices.get(key);
        if (find == null) {
            return null;
        }
        int i = find; // Avoids additional unboxing.
        Entry<K, V> deleted = array[i];
        array[i] = array[--size];
        /* 
         * The replacement element might move UP the array if it is not on
         * the same path as the deleted element. This fact should be kept
         * in mind when implementing an iterator.
         */
        if (comparator != null) {
            if (comparator.compare(array[i].value, array[parent(i)].value) < 0) {
                siftUpComparator(i);
            } else {
                siftDownComparator(i);
            }
        } else {
            if (((Comparable<? super V>)array[i].value)
                    .compareTo(array[parent(i)].value) < 0) {
                siftUp(i);
            } else {
                siftDown(i);
            }
        }
        array[size] = null;
        indices.remove(deleted.key);
        return deleted.value;
    }

    /** Empties the map. */
    public void clear() {
        for (int i = 0; i < size; ++i) {
            array[i] = null;
        }
        size = 0;
        indices.clear();
    }

    private int parent(int i) {
        return (i - 1) / d;
    }

    private int firstBranch(int i) {
        return d * i + 1;
    }

    /** Finds the highest-priority branch of a given element. */
    @SuppressWarnings("unchecked")
    private int successor(int i) {
        int best = firstBranch(i);
        int current = best + 1;
        int end = Math.min(size, best + d);
        while (current < end) {
            if (((Comparable<? super V>)array[current].value)
                    .compareTo(array[best].value) < 0) {
                best = current;
            }
            ++current;
        }
        return best;
    }
    
    /** Comparator version of successor. */
    private int successorComparator(int i) {
        int best = firstBranch(i);
        int current = best + 1;
        int end = Math.min(size, best + d);
        while (current < end) {
            if (comparator.compare(array[current].value, array[best].value) < 0) {
                best = current;
            }
            ++current;
        }
        return best;
    }

    /** Moves an element up the heap while updating the index table. */
    @SuppressWarnings("unchecked")
    private void siftUp(int i) {
        Entry<K, V> tmp = array[i];
        int parent = parent(i);
        Comparable<? super V> t = (Comparable<? super V>)tmp.value;
        while (i > 0 && t.compareTo(array[parent].value) < 0) {
            array[i] = array[parent];
            indices.put(array[i].key, i);
            i = parent;
            parent = parent(i);
        }
        array[i] = tmp;
        indices.put(tmp.key, i);
    }

    /** Comparator version of siftUp. */
    private void siftUpComparator(int i) {
        Entry<K, V> tmp = array[i];
        int parent = parent(i);
        while (i > 0 && comparator.compare(tmp.value, array[parent].value) < 0) {
            array[i] = array[parent];
            indices.put(array[i].key, i);
            i = parent;
            parent = parent(i);
        }
        array[i] = tmp;
        indices.put(tmp.key, i);
    }    

    /** Moves an element down the heap while updating the index table. */
    @SuppressWarnings("unchecked")
    private void siftDown(int i) {
        Entry<K, V> tmp = array[i];
        int successor;
        Comparable<? super V> t = (Comparable<? super V>)tmp.value;
        while (firstBranch(i) < size) {
            successor = successor(i);
            if (t.compareTo(array[successor].value) <= 0) {
                break;
            }
            array[i] = array[successor];
            indices.put(array[i].key, i);
            i = successor;
        }
        array[i] = tmp;
        indices.put(tmp.key, i);
    }
    
    /** Comparator version of siftDown. */
    private void siftDownComparator(int i) {
        Entry<K, V> tmp = array[i];
        int successor;
        while (firstBranch(i) < size) {
            successor = successorComparator(i);
            if (comparator.compare(tmp.value, array[successor].value) <= 0) {
                break;
            }
            array[i] = array[successor];
            indices.put(array[i].key, i);
            i = successor;
        }
        array[i] = tmp;
        indices.put(tmp.key, i);
    }

    /**
     * Updates the value mapped to the specified key and fixes the heap if
     * necessary. Returns the old value or, if the key is not present, null.
     */
    @SuppressWarnings("unchecked")
    private V update(K key, V value) {
        Integer find = indices.remove(key);
        if (find == null) {
            return null;
        }
        int i = find; // Avoids additional unboxing.
        V old = array[i].value;
        array[i].value = value;
        if (comparator != null) {
            if (comparator.compare(value, old) < 0) {
                siftUpComparator(i);
            } else {
                siftDownComparator(i);
            }
        } else {
            if (((Comparable<? super V>)value).compareTo(old) < 0) {
                siftUp(i);
            } else {
                siftDown(i);
            }
        }
        return old;
    }
    
    /** Turns an arbitrarily ordered array into a heap in O(n) time. */
    @SuppressWarnings("unused")
    private void heapify() {
        for (int i = parent(size - 1); i >= 0; --i) {
            siftDown(i);
        }
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] resizeArray(Entry<K, V>[] original, int newLength) {
        Entry<K, V>[] copy = new Entry[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    private static class Entry<K, V> {
        final K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}

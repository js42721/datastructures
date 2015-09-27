package datastructures;

import java.util.Comparator;
import java.util.HashMap;

/**
 * A priority queue/map hybrid backed by a mutable d-ary heap. Supports O(1)
 * find-min, O(lg n) insert, O(lg n) delete (for any element), O(lg n) update,
 * and O(1) lookup by key.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class DHeapPriorityMap<K, V> {
    private static final int DEFAULT_D = 4;
    private static final int DEFAULT_CAPACITY = 8;

    private final Comparator<? super V> comparator;
    private final HashMap<K, Integer> indices;
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
        array = new Entry[capacity];
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
        return (size == 0) ? null : array[0].key;
    }

    /**
     * Returns the value of the entry with the highest priority (this is the
     * entry with the smallest value if a comparator is not provided).
     * 
     * @return the value of the entry with the highest priority or {@code null}
     *         if the map is empty
     */
    public V peekValue() {
        return (size == 0) ? null : array[0].value;
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
        return (i == null) ? null : array[i].value;
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
        int end = size;
        if (end == array.length) {
            grow(end + 1);
        }
        ++size;
        array[end] = new Entry<K, V>(key, value);
        siftUp(end);
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
        if (comparator != null) {
            if (comparator.compare(array[i].value, deleted.value) <= 0) {
                siftUpComparator(i);
            } else {
                siftDownComparator(i);
            }
        } else {
            if (((Comparable<? super V>)array[i].value).compareTo(deleted.value) <= 0) {
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
        V bestVal = array[best].value;
        while (current < end) {
            V val = array[current].value;
            if (((Comparable<? super V>)val).compareTo(bestVal) < 0) {
                bestVal = val;
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
        V bestVal = array[best].value;
        while (current < end) {
            V val = array[current].value;
            if (comparator.compare(val, bestVal) < 0) {
                bestVal = val;
                best = current;
            }
            ++current;
        }
        return best;
    }

    /** Moves an element up the heap while updating the index table. */
    @SuppressWarnings("unchecked")
    private void siftUp(int i) {
        Entry<K, V> e = array[i];
        Comparable<? super V> val = (Comparable<? super V>)e.value;
        while (i > 0) {
            int parent = parent(i);
            Entry<K, V> p = array[parent];
            if (val.compareTo(p.value) >= 0) {
                break;
            }
            array[i] = p;
            indices.put(array[i].key, i);
            i = parent;
        }
        array[i] = e;
        indices.put(e.key, i);
    }

    /** Comparator version of siftUp. */
    private void siftUpComparator(int i) {
        Entry<K, V> e = array[i];
        while (i > 0) {
            int parent = parent(i);
            Entry<K, V> p = array[parent];
            if (comparator.compare(e.value, p.value) >= 0) {
                break;
            }
            array[i] = p;
            indices.put(array[i].key, i);
            i = parent;
        }
        array[i] = e;
        indices.put(e.key, i);
    }    

    /** Moves an element down the heap while updating the index table. */
    @SuppressWarnings("unchecked")
    private void siftDown(int i) {
        Entry<K, V> e = array[i];
        Comparable<? super V> val = (Comparable<? super V>)e.value;
        while (firstBranch(i) < size) {
            int successor = successor(i);
            Entry<K, V> s = array[successor];
            if (val.compareTo(s.value) <= 0) {
                break;
            }
            array[i] = s;
            indices.put(array[i].key, i);
            i = successor;
        }
        array[i] = e;
        indices.put(e.key, i);
    }
    
    /** Comparator version of siftDown. */
    private void siftDownComparator(int i) {
        Entry<K, V> e = array[i];
        while (firstBranch(i) < size) {
            int successor = successorComparator(i);
            Entry<K, V> s = array[successor];
            if (comparator.compare(e.value, s.value) <= 0) {
                break;
            }
            array[i] = s;
            indices.put(array[i].key, i);
            i = successor;
        }
        array[i] = e;
        indices.put(e.key, i);
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
            if (comparator.compare(value, old) <= 0) {
                siftUpComparator(i);
            } else {
                siftDownComparator(i);
            }
        } else {
            if (((Comparable<? super V>)value).compareTo(old) <= 0) {
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
    private void grow(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError();
        }
        int oldCapacity = array.length;
        int newCapacity = oldCapacity + (oldCapacity >>> 1);
        newCapacity = (newCapacity < 0) ?
                      Integer.MAX_VALUE :
                      Math.max(minCapacity, newCapacity);
        Entry<K, V>[] resized = new Entry[newCapacity];
        System.arraycopy(array, 0, resized, 0, oldCapacity);
        array = resized;
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

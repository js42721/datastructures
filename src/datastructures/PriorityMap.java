package datastructures;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Comparator;

/**
 * A priority queue/map hybrid backed by a mutable binary heap. Supports O(1)
 * find-min, O(lg n) insert, O(lg n) delete (for any element), O(lg n) update,
 * and O(1) lookup by key.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class PriorityMap<K, V> {
    private final Comparator<? super V> comparator;
    private final TObjectIntHashMap<K> indices;
    private Entry<K, V>[] array;
    private int size;

    /**
     * Constructs a map with the default initial capacity (8). Priority will be
     * determined by the natural ordering of the map's values.
     */
    public PriorityMap() {
        this(8, null);
    }

    /**
     * Constructs a map with the default initial capacity (8). Priority will be
     * determined by the supplied comparator.
     * 
     * @param comparator the comparator which will be used to determine priority
     */
    public PriorityMap(Comparator<? super V> comparator) {
        this(8, comparator);
    }

    /**
     * Constructs a map with the specified initial capacity. Priority will be
     * determined by the natural ordering of the map's values.
     * 
     * @param  capacity the initial capacity of the map
     * @throws IllegalArgumentException if capacity is negative
     */
    public PriorityMap(int capacity) {
        this(capacity, null);
    }

    /**
     * Constructs a map with the specified initial capacity. Priority will be
     * determined by the supplied comparator.
     *
     * @param  capacity the initial capacity of the map
     * @param  comparator the comparator which will be used to determine priority
     * @throws IllegalArgumentException if capacity is negative
     */
    @SuppressWarnings("unchecked")
    public PriorityMap(int capacity, Comparator<? super V> comparator) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must not be negative");
        }
        this.comparator = comparator;
        array = new Entry[capacity];
        indices = new TObjectIntHashMap<K>(capacity, 0.5f, -1);
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
        int i = indices.get(key);
        return (i == -1) ? null : array[i].value;
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
        if (comparator != null) {
            siftUpComparator(end, new Entry<K, V>(key, value));
        } else {
            siftUp(end, new Entry<K, V>(key, value));
        }
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
        int end = --size;
        K deleted = array[0].key;
        if (comparator != null) {
            siftDownComparator(0, array[end]);
        } else {
            siftDown(0, array[end]);
        }
        array[end] = null;
        indices.remove(deleted);
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
        int i = indices.get(key);
        if (i == -1) {
            return null;
        }
        int end = --size;
        Entry<K, V> deleted = array[i];
        Entry<K, V> last = array[end];
        if (comparator != null) {
            if (comparator.compare(last.value, deleted.value) <= 0) {
                siftUpComparator(i, last);
            } else {
                siftDownComparator(i, last);
            }
        } else {
            if (((Comparable<? super V>) last.value).compareTo(deleted.value) <= 0) {
                siftUp(i, last);
            } else {
                siftDown(i, last);
            }
        }
        array[end] = null;
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

    /** Moves an element up the heap while updating the index table. */
    @SuppressWarnings("unchecked")
    private void siftUp(int i, Entry<K, V> e) {
        Comparable<? super V> val = (Comparable<? super V>) e.value;
        while (i > 0) {
            int parent = (i - 1) >> 1;
            Entry<K, V> p = array[parent];
            if (val.compareTo(p.value) >= 0) {
                break;
            }
            array[i] = p;
            indices.put(p.key, i);
            i = parent;
        }
        array[i] = e;
        indices.put(e.key, i);
    }

    /** Comparator version of siftUp. */
    private void siftUpComparator(int i, Entry<K, V> e) {
        while (i > 0) {
            int parent = (i - 1) >> 1;
            Entry<K, V> p = array[parent];
            if (comparator.compare(e.value, p.value) >= 0) {
                break;
            }
            array[i] = p;
            indices.put(p.key, i);
            i = parent;
        }
        array[i] = e;
        indices.put(e.key, i);
    }

    /** Moves an element down the heap while updating the index table. */
    @SuppressWarnings("unchecked")
    private void siftDown(int i, Entry<K, V> e) {
        Comparable<? super V> val = (Comparable<? super V>) e.value;
        int limit = (size - 2) >> 1;
        while (i <= limit) {
            int successor = (i << 1) + 1;
            Entry<K, V> s = array[successor];
            int right = successor + 1;
            if (right < size) {
                Entry<K, V> r = array[right];
                if (((Comparable<? super V>) s.value).compareTo(r.value) > 0) {
                    successor = right;
                    s = r;
                }
            }
            if (val.compareTo(s.value) <= 0) {
                break;
            }
            array[i] = s;
            indices.put(s.key, i);
            i = successor;
        }
        array[i] = e;
        indices.put(e.key, i);
    }

    /** Comparator version of siftDown. */
    private void siftDownComparator(int i, Entry<K, V> e) {
        int limit = (size - 2) >> 1;
        while (i <= limit) {
            int successor = (i << 1) + 1;
            Entry<K, V> s = array[successor];
            int right = successor + 1;
            if (right < size) {
                Entry<K, V> r = array[right];
                if (comparator.compare(s.value, r.value) > 0) {
                    successor = right;
                    s = r;
                }
            }
            if (comparator.compare(e.value, s.value) <= 0) {
                break;
            }
            array[i] = s;
            indices.put(s.key, i);
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
        int i = indices.get(key);
        if (i == -1) {
            return null;
        }
        Entry<K, V> e = array[i];
        V old = e.value;
        e.value = value;
        if (comparator != null) {
            if (comparator.compare(value, old) <= 0) {
                siftUpComparator(i, e);
            } else {
                siftDownComparator(i, e);
            }
        } else {
            if (((Comparable<? super V>) value).compareTo(old) <= 0) {
                siftUp(i, e);
            } else {
                siftDown(i, e);
            }
        }
        return old;
    }

    @SuppressWarnings("unchecked")
    private void grow(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError();
        }
        int oldCapacity = array.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        newCapacity = (newCapacity < 0) ?
                      Integer.MAX_VALUE :
                      Math.max(minCapacity, newCapacity);
        Entry<K, V>[] resized = new Entry[newCapacity];
        System.arraycopy(array, 0, resized, 0, size);
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

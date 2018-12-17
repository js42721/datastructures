package datastructures;

/**
 * A fixed-capacity mutable min-heap of {@code double} values where each value
 * is associated with an {@code int} index.
 */
public class MinHeap {
    final double[] a;
    final int[] htoi;
    final int[] itoh;
    int size;

    /**
     * Creates a min-heap with a capacity of n.
     *
     * @param  n the capacity
     * @throws IllegalArgumentException if n is negative
     */
    public MinHeap(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative");
        }
        a = new double[n];
        htoi = new int[n];
        itoh = new int[n];
        for (int i = 0; i < n; ++i) {
            itoh[i] = -1;
        }
    }

    /** Returns the capacity of this heap. */
    public int capacity() {
        return a.length;
    }

    /** Returns the number of values in this heap. */
    public int size() {
        return size;
    }

    /**
     * Checks if this heap is empty.
     *
     * @return {@code true} if this heap is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the index associated with the minimum value.
     *
     * @return the index associated with the minimum value or -1 if the heap
     *         is empty
     */
    public int peek() {
        return (size == 0) ? -1 : htoi[0];
    }

    /**
     * Returns the minimum value.
     *
     * @return the minimum value or NaN if the heap is empty
     */
    public double peekVal() {
        return (size == 0) ? Double.NaN : a[htoi[0]];
    }

    /**
     * Checks if the value associated with an index is present.
     *
     * @param  i the index
     * @return {@code true} if the heap contains a value for i
     * @throws IndexOutOfBoundsException if i is outside [0, capacity)
     */
    public boolean contains(int i) {
        if (i < 0 || i >= itoh.length) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        return itoh[i] != -1;
    }

    /**
     * Returns the value associated with an index.
     *
     * @param  i the index
     * @return the value associated with i or NaN if there is no such value
     * @throws IndexOutOfBoundsException if i is outside [0, capacity)
     */
    public double get(int i) {
        return contains(i) ? a[i] : Double.NaN;
    }

    /**
     * Inserts a value and associates it with an index unless there is already
     * a value for the index.
     *
     * @param  i the index
     * @param  val the value
     * @return {@code true} if the heap does not already contain a value for i
     * @throws IndexOutOfBoundsException if i is outside [0, capacity)
     * @throws IllegalArgumentException if val is NaN
     */
    public boolean insert(int i, double val) {
        if (Double.isNaN(val)) {
            throw new IllegalArgumentException("Value is NaN");
        }
        if (contains(i)) {
            return false;
        }
        siftUp(size++, i, val);
        a[i] = val;
        return true;
    }

    /**
     * Removes the minimum value.
     *
     * @return the index associated with the minimum value or -1 if the heap
     *         is empty
     */
    public int delete() {
        if (size == 0) {
            return -1;
        }
        int min = htoi[0];
        int i = htoi[--size];
        siftDown(0, i, a[i]);
        itoh[min] = -1;
        return min;
    }

    /**
     * Removes the value associated with an index.
     *
     * @param  i the index
     * @return the value associated with i or NaN if there is no such value
     * @throws IndexOutOfBoundsException if i is outside [0, capacity)
     */
    public double delete(int i) {
        if (!contains(i)) {
            return Double.NaN;
        }
        int last = htoi[--size];
        double lv = a[last];
        double val = a[i];
        if (lv <= val) {
            siftUp(itoh[i], last, lv);
        } else {
            siftDown(itoh[i], last, lv);
        }
        itoh[i] = -1;
        return val;
    }

    /**
     * Replaces the value associated with an index.
     *
     * @param  i the index
     * @param  val the replacement value
     * @return the old value or NaN if there is no value associated with i
     * @throws IndexOutOfBoundsException if i is outside [0, capacity)
     * @throws IllegalArgumentException if val is NaN
     */
    public double update(int i, double val) {
        if (Double.isNaN(val)) {
            throw new IllegalArgumentException("Value is NaN");
        }
        if (!contains(i)) {
            return Double.NaN;
        }
        double old = a[i];
        if (val <= old) {
            siftUp(itoh[i], i, val);
        } else {
            siftDown(itoh[i], i, val);
        }
        a[i] = val;
        return old;
    }

    /** Empties the heap. */
    public void clear() {
        for (int i = 0; i < itoh.length; ++i) {
            itoh[i] = -1;
        }
        size = 0;
    }

    private void siftUp(int h, int i, double val) {
        while (h > 0) {
            int parent = (h - 1) >> 1;
            int p = htoi[parent];
            if (val >= a[p]) {
                break;
            }
            itoh[p] = h;
            htoi[h] = p;
            h = parent;
        }
        htoi[h] = i;
        itoh[i] = h;
    }

    private void siftDown(int h, int i, double val) {
        int limit = ((size - 1) - 1) >> 1;
        while (h <= limit) {
            int successor = (h << 1) + 1;
            int s = htoi[successor];
            double sv = a[s];
            int right = successor + 1;
            if (right < size) {
                int r = htoi[right];
                double rv = a[r];
                if (rv < sv) {
                    successor = right;
                    sv = rv;
                    s = r;
                }
            }
            if (val <= sv) {
                break;
            }
            itoh[s] = h;
            htoi[h] = s;
            h = successor;
        }
        htoi[h] = i;
        itoh[i] = h;
    }
}

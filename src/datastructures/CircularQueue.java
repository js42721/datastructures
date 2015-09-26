package datastructures;

/**
 * A circular queue which removes the front element when the queue is full and
 * a new element is added. This implementation uses a sentinel value (null) to
 * determine whether the queue is empty or full when the head and tail indices
 * point to the same element.
 * 
 * @param <E> the element type
 */
public class CircularQueue<E> {
    private final E[] array;
    private int head;
    private int tail;

    /**
     * Constructs a bounded queue with the specified capacity.
     * 
     * @param  capacity the capacity of the queue
     * @throws IllegalArgumentException if capacity is 0 or less
     */
    @SuppressWarnings("unchecked")
    public CircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        array = (E[])new Object[capacity];
    }

    /**
     * Returns {@code true} if the queue contains no elements.
     * 
     * @return {@code true} if the queue contains no elements
     */
    public boolean isEmpty() {
        return array[head] == null;
    }
    
    /**
     * Returns {@code true} if the queue is at full capacity.
     *  
     * @return {@code true} if the queue is at full capacity
     */
    public boolean isAtFullCapacity() {
        return head == tail && !isEmpty();
    }

    /**
     * Returns the number of elements in the queue.
     * 
     * @return the number of elements in the queue
     */
    public int size() {
        if (isAtFullCapacity()) {
            return array.length;
        }
        if (head > tail) {
            return tail - head + array.length;
        }
        return tail - head;
    }

    /**
     * Returns the maximum number of elements that the queue can contain.
     * 
     * @return the maximum number of elements that the queue can contain
     */
    public int capacity() {
        return array.length;
    }

    /**
     * Returns the element at the front of the queue.
     * 
     * @return the element at the front of the queue, or {@code null} if the
     *         queue is empty
     */
    public E peek() {
        return array[head];
    }

    /**
     * Returns the element at the specified position.
     * 
     * @param  index index of the element to return
     * @return the element at the specified position 
     * @throws IndexOutOfBoundsException if index is outside the range [0, size)
     */
    public E get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        return array[translate(index)];
    }

    /**
     * Replaces the element at the specified position.
     * 
     * @param  index index of the element to replace
     * @param  e the new element
     * @return the old element
     * @throws NullPointerException if the new element is null
     * @throws IndexOutOfBoundsException if index is outside the range [0, size)
     */
    public E set(int index, E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        int i = translate(index);
        E old = array[i];
        array[i] = e;
        return old;
    }

    /**
     * Inserts an element at the end of the queue. If the queue is full, the
     * element at the front of the queue will be removed.
     * 
     * @param  e the element to be inserted
     * @return {@code true} (always)
     * @throws NullPointerException if the element is null
     */
    public boolean add(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        if (isAtFullCapacity()) {
            head = increment(head);
        }
        array[tail] = e;
        tail = increment(tail);
        return true;
    }

    /**
     * Removes and returns the element at the front of the queue.
     * 
     * @return the element at the front of the queue, or {@code null} if the
     *         queue is empty
     */
    public E poll() {
        if (isEmpty()) {
            return null;
        }
        E front = array[head];
        array[head] = null;
        head = increment(head);
        return front;
    }

    /** Removes all elements from the queue. */
    public void clear() {
        for (int i = 0; i < array.length; ++i) {
            array[i] = null;
        }
        head = tail = 0;
    }

    private int increment(int index) {
        int i = index + 1;
        return (i == array.length) ? 0 : i;
    }

    private int translate(int index) {
        int i = head + index;
        int j = i - array.length; // Used to test for arithmetic overflow.
        return (j >= 0) ? j : i;
    }
}

package datastructures;

import java.util.Comparator;
import java.util.Random;

/**
 * A red-black tree whose nodes are augmented with sampling weights. Supports
 * O(lg n) sampling and O(lg n) weight updates.
 * <p>
 * The red-black tree algorithms are based on those found in CLRS.
 *
 * @param <E> the element type
 */
public class WeightedSamplingTree<E> {
    private static final boolean RED   = false;
    private static final boolean BLACK = true;

    private final Comparator<? super E> comparator;
    private final Random rnd;
    private final Node<E> nil;
    private Node<E> root;
    private int size;

    /**
     * Constructs an empty tree. Elements will be ordered according to their
     * natural ordering.
     */
    public WeightedSamplingTree() {
        this(null);
    }

    /**
     * Constructs an empty tree with a comparator. Elements will be ordered
     * according to the comparator.
     *
     * @param comparator the comparator which will be used to order elements
     */
    public WeightedSamplingTree(Comparator<? super E> comparator) {
        this.comparator = comparator;
        rnd = new Random();
        root = nil = new Node<E>(null);
        nil.left = nil.right = nil;
        nil.color = BLACK;
    }

    /**
     * Returns the number of elements contained in the tree.
     *
     * @return the number of elements contained in the tree
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if the tree contains no elements.
     *
     * @return {@code true} if the tree contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all elements from the tree.
     */
    public void clear() {
        size = 0;
        root = nil;
    }

    /**
     * Searches the tree for the specified element.
     *
     * @param  o the element to search for
     * @return {@code true} if the tree contains the element
     * @throws ClassCastException if the type of the specified element is
     *         incompatible with the tree
     * @throws NullPointerException if the specified element is null
     */
    public boolean contains(Object o) {
        return findNode(o) != nil;
    }

    /**
     * Inserts an element into the tree if it is not already present and assigns
     * it a sampling weight of 1.
     *
     * @param  e the element to be inserted
     * @return {@code true} if the tree changed as a result of the call
     * @throws ClassCastException if the type of the specified element is
     *         incompatible with the tree
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        return add(e, 1);
    }

    /**
     * Inserts an element into the tree and assigns it the specified nonnegative
     * sampling weight if the element is not already present.
     *
     * @param  e the element to be inserted
     * @param  weight the sampling weight of the element
     * @return {@code true} if the tree changed as a result of the call
     * @throws ClassCastException if the type of the specified element is
     *         incompatible with the tree
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if the specified weight is negative
     */
    @SuppressWarnings("unchecked")
    public boolean add(E e, int weight) {
        if (e == null) {
            throw new NullPointerException();
        }
        if (weight < 0) {
            throw new IllegalArgumentException("Weight must be nonnegative");
        }
        int cmp = 0;
        Node<E> parent = nil;
        Node<E> current = root;
        if (comparator != null) {
            while (current != nil) {
                parent = current;
                cmp = comparator.compare(e, current.element);
                if (cmp < 0) {
                    current.subtreeWeight += weight;
                    current = current.left;
                } else if (cmp > 0) {
                    current.subtreeWeight += weight;
                    current = current.right;
                } else {
                    /*
                     * The tree already contains the element so we undo the
                     * weight changes we made along the way.
                     */
                    setWeight(current.parent, weightOf(current.parent) - weight);
                    return false;
                }
            }
        } else {
            Comparable<? super E> c = (Comparable<? super E>) e;
            while (current != nil) {
                parent = current;
                cmp = c.compareTo(current.element);
                if (cmp < 0) {
                    current.subtreeWeight += weight;
                    current = current.left;
                } else if (cmp > 0) {
                    current.subtreeWeight += weight;
                    current = current.right;
                } else {
                    /*
                     * The tree already contains the element so we undo the
                     * weight changes we made along the way.
                     */
                    setWeight(current.parent, weightOf(current.parent) - weight);
                    return false;
                }
            }
        }
        Node<E> x = new Node<E>(e);
        x.parent = parent;
        x.left = x.right = nil;
        x.color = RED;
        x.subtreeWeight = weight;
        if (parent == nil) {
            root = x;
        } else if (cmp < 0) {
            parent.left = x;
        } else {
            parent.right = x;
        }
        fixAfterInsert(x);
        ++size;
        return true;
    }

    /**
     * Removes the specified element from the tree.
     *
     * @param  o the element to be removed
     * @return {@code true} if the tree changed as a result of the call
     * @throws ClassCastException if the type of the specified element is
     *         incompatible with the tree
     * @throws NullPointerException if the specified element is null
     */
    public boolean remove(Object o) {
        Node<E> node = findNode(o);
        if (node == nil) {
            return false;
        }
        deleteNode(node);
        return true;
    }

    /**
     * Retrieves a sample from the tree. The probability of a particular element
     * being selected depends on its sampling weight.
     *
     * @return an element in the tree or {@code null} if the tree is empty or
     *         if each of its elements has a weight of 0
     */
    public E sample() {
        return sample(root).element;
    }

    /**
     * Sets the sampling weight of an element to the specified nonnegative
     * value.
     *
     * @param  o the element whose weight is to be set
     * @param  weight the desired sampling weight for the element
     * @return the previous weight of the element or -1 if the element is not
     *         contained in the tree
     * @throws ClassCastException if the type of the specified element is
     *         incompatible with the tree
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if the specified weight is negative
     */
    public int setWeight(Object o, int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight must be nonnegative");
        }
        Node<E> node = findNode(o);
        if (node == nil) {
            return -1;
        }
        int old = weightOf(node);
        if (old != weight) {
            setWeight(node, weight);
        }
        return old;
    }

    /**
     * Returns the sampling weight of the specified element.
     *
     * @param  o the element whose sampling weight is to be retrieved
     * @return the sampling weight of the element or -1 if the element is not
     *         contained in the tree
     * @throws ClassCastException if the type of the specified element is
     *         incompatible with the tree
     * @throws NullPointerException if the specified element is null
     */
    public int getWeight(Object o) {
        Node<E> node = findNode(o);
        return (node == nil) ? -1 : weightOf(node);
    }

    /** Selects and returns a node based on probability. */
    private Node<E> sample(Node<E> x) {
        if (x.subtreeWeight == 0) {
            return nil;
        }
        long r = (long) (rnd.nextDouble() * x.subtreeWeight);
        while (r < x.left.subtreeWeight + x.right.subtreeWeight) {
            if (r < x.left.subtreeWeight) {
                x = x.left;
            } else {
                r -= x.left.subtreeWeight;
                x = x.right;
            }
        }
        return x;
    }

    /** Returns the individual weight of a node. */
    private int weightOf(Node<E> x) {
        return (int) (x.subtreeWeight - x.left.subtreeWeight - x.right.subtreeWeight);
    }

    /** Sets the weight of a node and adjusts its ancestors' weights. */
    private void setWeight(Node<E> x, int weight) {
        int difference = weight - weightOf(x);
        while (x != nil) {
            x.subtreeWeight += difference;
            x = x.parent;
        }
    }

    @SuppressWarnings("unchecked")
    private Node<E> findNode(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        Node<E> current = root;
        if (comparator != null) {
            E e = (E) o;
            while (current != nil) {
                int cmp = comparator.compare(e, current.element);
                if (cmp < 0) {
                    current = current.left;
                } else if (cmp > 0) {
                    current = current.right;
                } else {
                    return current;
                }
            }
        } else {
            Comparable<? super E> c = (Comparable<? super E>) o;
            while (current != nil) {
                int cmp = c.compareTo(current.element);
                if (cmp < 0) {
                    current = current.left;
                } else if (cmp > 0) {
                    current = current.right;
                } else {
                    return current;
                }
            }
        }
        return nil;
    }

    /** Deletes a node and removes its weight. */
    private void deleteNode(Node<E> z) {
        --size;
        Node<E> y;
        if (z.left == nil || z.right == nil) {
            y = z;
            setWeight(y, 0); // Removes the deleted node's weight from the tree.
        } else {
            y = successor(z);
            int weightY = weightOf(y);
            Node<E> current = y;
            /* Fixes weights from the replacement up to the deleted node. */
            while (current != z) {
                current.subtreeWeight -= weightY;
                current = current.parent;
            }
            z.element = y.element;
            setWeight(z, weightY); // Fixes the weight of the replacement.
        }
        Node<E> x = (y.left != nil) ? y.left : y.right;
        x.parent = y.parent;
        if (y.parent == nil) {
            root = x;
        } else if (y == y.parent.left) {
            y.parent.left = x;
        } else {
            y.parent.right = x;
        }
        if (y.color == BLACK) {
            fixAfterDelete(x);
        }
    }

    /** Returns the in-order successor of a node. */
    private Node<E> successor(Node<E> x) {
        Node<E> y;
        if (x.right != nil) {
            y = x.right;
            while (y.left != nil) {
                y = y.left;
            }
            return y;
        }
        y = x.parent;
        while (y != nil && x == y.right) {
            x = y;
            y = y.parent;
        }
        return y;
    }

    /** Performs a left rotation while adjusting node weights. */
    private void rotateLeft(Node<E> x) {
        Node<E> y = x.right;
        x.right = y.left;
        x.subtreeWeight -= y.subtreeWeight;
        y.subtreeWeight -= y.left.subtreeWeight;
        if (y.left != nil) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
        x.subtreeWeight += x.right.subtreeWeight;
        y.subtreeWeight += x.subtreeWeight;
    }

    /** Performs a right rotation while adjusting node weights. */
    private void rotateRight(Node<E> x) {
        Node<E> y = x.left;
        x.left = y.right;
        x.subtreeWeight -= y.subtreeWeight;
        y.subtreeWeight -= y.right.subtreeWeight;
        if (y.right != nil) {
            y.right.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.right) {
            x.parent.right = y;
        } else {
            x.parent.left = y;
        }
        y.right = x;
        x.parent = y;
        x.subtreeWeight += x.left.subtreeWeight;
        y.subtreeWeight += x.subtreeWeight;
    }

    /** Restores red-black tree invariants after insertion. */
    private void fixAfterInsert(Node<E> x) {
        while (x != root && x.parent.color == RED) {
            if (x.parent == x.parent.parent.left) {
                Node<E> y = x.parent.parent.right;
                if (y.color == RED) {
                    x.parent.color = BLACK;
                    y.color = BLACK;
                    x.parent.parent.color = RED;
                    x = x.parent.parent;
                } else {
                    if (x == x.parent.right) {
                        x = x.parent;
                        rotateLeft(x);
                    }
                    x.parent.color = BLACK;
                    x.parent.parent.color = RED;
                    rotateRight(x.parent.parent);
                }
            } else {
                Node<E> y = x.parent.parent.left;
                if (y.color == RED) {
                    x.parent.color = BLACK;
                    y.color = BLACK;
                    x.parent.parent.color = RED;
                    x = x.parent.parent;
                } else {
                    if (x == x.parent.left) {
                        x = x.parent;
                        rotateRight(x);
                    }
                    x.parent.color = BLACK;
                    x.parent.parent.color = RED;
                    rotateLeft(x.parent.parent);
                }
            }
        }
        root.color = BLACK;
    }

    /** Restores red-black tree invariants after deletion. */
    private void fixAfterDelete(Node<E> x) {
        while (x != root && x.color == BLACK) {
            if (x == x.parent.left) {
                Node<E> w = x.parent.right;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.parent.color = RED;
                    rotateLeft(x.parent);
                    w = x.parent.right;
                }
                if (w.left.color == BLACK && w.right.color == BLACK) {
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (w.right.color == BLACK) {
                        w.left.color = BLACK;
                        w.color = RED;
                        rotateRight(w);
                        w = x.parent.right;
                    }
                    w.color = x.parent.color;
                    x.parent.color = BLACK;
                    w.right.color = BLACK;
                    rotateLeft(x.parent);
                    x = root;
                }
            } else {
                Node<E> w = x.parent.left;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.parent.color = RED;
                    rotateRight(x.parent);
                    w = x.parent.left;
                }
                if (w.right.color == BLACK && w.left.color == BLACK) {
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (w.left.color == BLACK) {
                        w.right.color = BLACK;
                        w.color = RED;
                        rotateLeft(w);
                        w = x.parent.left;
                    }
                    w.color = x.parent.color;
                    x.parent.color = BLACK;
                    w.left.color = BLACK;
                    rotateRight(x.parent);
                    x = root;
                }
            }
        }
        x.color = BLACK;
    }

    private static class Node<E> {
        E element;
        Node<E> parent;
        Node<E> left;
        Node<E> right;
        boolean color;
        long subtreeWeight;

        Node(E element) {
            this.element = element;
        }
    }
}

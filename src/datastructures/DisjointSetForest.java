package datastructures;

import java.util.Arrays;

/** Allows for efficient union/find operations. */
public class DisjointSetForest {
    private final int[] forest;

    /** 
     * Creates a disjoint set forest made up of n disjoint sets.
     * 
     * @throws IllegalArgumentException if n is negative
     */
    public DisjointSetForest(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be nonnegative");
        }
        forest = new int[n];
        Arrays.fill(forest, -1);
    }

    /** 
     * Returns the set representative of a given element.
     * 
     * @throws IndexOutOfBoundsException if x is outside the range [0, n)
     */
    public int find(int x) {
        if (x < 0 || x >= forest.length) {
            throw new IndexOutOfBoundsException(String.valueOf(x));
        }
        int root = forest[x];
        int index = x;
        while (root >= 0) {
            index = root;
            root = forest[root];
        }
        root = index;
        /* Performs path compression. */
        int current = x;
        while (current != root) {
            int old = current;
            current = forest[current];
            forest[old] = root;
        }
        return root;
    }

    /** 
     * Merges the set containing x with the set containing y.
     * 
     * @return false if x and y already belong to the same set
     * @throws IndexOutOfBoundsException if x or y is outside the range [0, n)
     */
    public boolean union(int x, int y) {
        int xRoot = find(x);
        int yRoot = find(y);
        if (xRoot == yRoot) {
            return false;
        }
        /* The lower-ranking root gets attached to the higher-ranking one. */
        if (forest[yRoot] < forest[xRoot]) {
            forest[xRoot] = yRoot;
        } else {
            if (forest[xRoot] == forest[yRoot]) {
                --forest[xRoot]; // Increases rank.
            }
            forest[yRoot] = xRoot;
        }
        return true;
    }
}

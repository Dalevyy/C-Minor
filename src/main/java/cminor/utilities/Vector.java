package cminor.utilities;

import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Container class for the C Minor compiler.
 * <p><br>
 *     This class creates a unified list structure that every part of the compiler
 *     will use. This avoids the need to use multiple different list types.
 * </p>
 * @param <T>
 * @author Daniel Levy
 */
public class Vector<T> extends AbstractList<T> implements Iterable<T> {

    private final ArrayList<T> lst; // Internal ArrayList to store all elements

    public Vector() { lst = new ArrayList<>(); }

    public Vector(T arg) {
        this();
        this.lst.add(arg);
    }

    public Vector(T[] args) {
        this();
        this.lst.addAll(Arrays.asList(args));
    }

    @Override
    public boolean add(T item) {
        lst.add(item);
        return true;
    }

    @Override
    public void add(int pos, T item) {
        if(pos < 0 || pos > lst.size())
            throw new RuntimeException("Vector size is " + lst.size() + ", so insert position " + pos + " is invalid!");
        lst.add(pos,item);
    }

    @Override
    public T remove(int pos) {
        if(pos < 0 || pos >= lst.size())
            throw new RuntimeException("Vector size is " + lst.size() + ", so position " + pos + " is not removable!");
        return lst.remove(pos);
    }

    public void removeAll(T item) { lst.removeIf(n -> n.equals(item)); }

    public T pop() {
        if(!lst.isEmpty()) { return lst.remove(lst.size()-1); }
        else throw new RuntimeException("An element can't be popped since the vector is empty!");
    }

    public T top() {
        if(!lst.isEmpty()) { return lst.get(lst.size()-1); }
        else { return null; }
    }

    public void merge(T item) { this.lst.add(item); }
    public void merge(Vector<T> lst) { this.lst.addAll(lst); }

    @Override
    public T get(int i) { return lst.get(i); }

    @Override
    public T set(int i, T item) { return lst.set(i,item); }

    @Override
    public int size() { return lst.size(); }

    public void print() {
        for(T item : lst)
            System.out.println(item);
    }

    /**
     * An iterator that iterates through a {@link Vector} object.
     * @param <T> The type of the elements stored in a {@link Vector} object.
     */
    public static class VectorIterator<T> implements Iterator {

        /**
         * {@link Vector} we will iterate through.
         */
        private final Vector<T> vector;

        /**
         * Current position we are at in {@link #vector}.
         */
        private int pos = 0;

        /**
         * Default constructor for {@link VectorIterator}.
         * @param vector {@link Vector} we want to iterate through.
         */
        public VectorIterator(Vector<T> vector) {
            this.vector = vector;
            this.pos = 0;
        }

        /**
         * Checks if we have iterated through the entire {@link #vector}.
         * @return {@code True} if the iteration can continue, {@code False} otherwise.
         */
        @Override
        public boolean hasNext() { return pos < vector.size(); }

        /**
         * Returns the next element in {@link #vector}.
         * @return Next element within the {@link #vector}.
         */
        @Override
        public T next() { return vector.get(pos++); }
    }
}

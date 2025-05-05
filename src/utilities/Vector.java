package utilities;

import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;

/*
__________________________________ Vector __________________________________
This is a container class designed to store items throughout the C Minor
compiler. The point of this class is to create a unified list structure that
every part of the compiler uses, so we do not have multiple list types being
used in different locations.
____________________________________________________________________________
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

    public void merge(T item) { this.lst.add(item); }
    public void merge(Vector<T> lst) { this.lst.addAll(lst); }

    @Override
    public T get(int i) { return lst.get(i); }

    @Override
    public int size() { return lst.size(); }

    public Iterator<T> iterator() { return new VectorIterator(); }

    class VectorIterator implements Iterator<T> {

        private int pos = 0;

        public boolean hasNext() { return pos < size(); }
        public T next() { return get(pos++); }
    }
}

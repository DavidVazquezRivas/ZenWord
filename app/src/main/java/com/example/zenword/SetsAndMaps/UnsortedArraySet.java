package com.example.zenword.SetsAndMaps;

import java.util.Iterator;

public class UnsortedArraySet<E> implements Set<E> {
    
    private final E[] arr;
    private int n;
    
    public UnsortedArraySet(int max) {
        this.arr = (E[]) new Object[max];
        this.n = 0;
    }

    /*
    O(n)
     */
    @Override
    public boolean contains(E elem) {
        for (int i = 0; i < n; i++) {
            if (arr[i].equals(elem)) return true;
        }
        return false;
    }

    /*
    O(1)
     */
    @Override
    public boolean add(E elem) {
        if (contains(elem) || n >= arr.length) return false;
        
        arr[n++] = elem;
        return true;
    }

    /*
    O(n)
     */
    @Override
    public boolean remove(E elem) {

        for (int i = 0; i < n; i++) {
            if (arr[i].equals(elem)) {
                arr[i] = arr[--n];
                return true;
            }
        }
        return false;
    }

    /*
    O(1)
     */
    @Override
    public boolean isEmpty() {
        return n == 0;
    }

    @Override
    public Iterator iterator() {
        return new IteratorUnsortedArraySet();
    }
    
    private class IteratorUnsortedArraySet implements Iterator {
        private int index;
        private IteratorUnsortedArraySet() {
            this.index = 0;
        }

        
        @Override
        public boolean hasNext() {
            return index < n;
        }

        @Override
        public Object next() {
            return arr[index++];
        }
        
    }
}

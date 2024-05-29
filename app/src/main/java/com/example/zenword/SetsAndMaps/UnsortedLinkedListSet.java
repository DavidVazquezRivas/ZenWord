package com.example.zenword.SetsAndMaps;

import java.util.Iterator;

public class UnsortedLinkedListSet<E> implements Set<E> {
    
    
    private Node first;
    
    public UnsortedLinkedListSet() {
        first = null;
    }

    /*
    O(n)
     */
    @Override
    public boolean contains(E elem) {
        Node n = first;
        while (n != null) {
            if (n.elem.equals(elem)) return true;
            n = n.next;
        }
        return false;
    }

    /*
    O(n)
     */
    @Override
    public boolean add(E elem) {
        if (contains(elem)) return false;
        
        Node n = new Node();
        n.elem = elem;
        n.next = first;
        first = n;
        return true;
    }

    /*
    O(n)
     */
    @Override
    public boolean remove(E elem) {
        Node prev = null;
        Node n = first;
        while (n != null) {
            if (n.elem.equals(elem)) {
                if (prev == null) first = null;
                else prev.next = n.next;
                return true;
            }
            prev = n;
            n = n.next;
        }
        return false;
    }

    /*
    O(1)
     */
    @Override
    public boolean isEmpty() {
        return first == null;
    }

    @Override
    public Iterator iterator() {
        Iterator it = new UnsortedLinkedListSetIterator();
        return it;
    }
    
    private class Node {
        private E elem;
        private Node next;
    }
    
    private class UnsortedLinkedListSetIterator implements Iterator {
        
        private Node node;
        
        private UnsortedLinkedListSetIterator() {
            node = first;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public Object next() {
            Node n = node;
            node = node.next;
            return n.elem;
        }
        
    }
}

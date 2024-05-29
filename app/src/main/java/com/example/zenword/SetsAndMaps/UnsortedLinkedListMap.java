package com.example.zenword.SetsAndMaps;

public class UnsortedLinkedListMap<K, V> implements Map<K, V> {
    
    private Node first;
    
    public UnsortedLinkedListMap() {
        first = null;
    }

    /*
    O(n)
     */
    @Override
    public V get(K key) {
        Node n = first;
        while (n != null) {
            if (n.key.equals(key)) return n.value;
            n = n.next;
        }
        return null;
    }

    /*
    O(n)
     */
    @Override
    public V put(K key, V value) {
        Node n = first;
        while (n != null) {
            if (n.key.equals(key)) {
                V v = n.value;
                n.value = value;
                return v;
            }
            n = n.next;
        }
        
        n = new Node();
        n.key = key;
        n.value = value;
        n.next = first;
        first = n;
        
        return null;
    }

    /*
    O(n)
     */
    @Override
    public V remove(K key) {
        Node prev = null;
        Node n = first;
        while (n != null) {
            if (n.key.equals(key)) {
                V v = n.value;
                if (prev == null) first = null;
                else prev.next = n.next;
                return v;
            }
            prev = n;
            n = n.next;
        }
        return null;
    }

    /*
    O(1)
     */
    @Override
    public boolean isEmpty() {
        return first == null;
    }
    
    private class Node {
        private K key;
        private V value;
        private Node next;
    }
    
}

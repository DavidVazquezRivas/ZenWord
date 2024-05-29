package com.example.zenword.SetsAndMaps;

public class UnsortedArrayMap<K, V> implements Map<K, V> {
    
    private final K[] keys;
    private final V[] values;
    private int n;

    public UnsortedArrayMap(int max) {
        keys = (K[]) new Object[max];
        values = (V[]) new Object[max];
        n = 0;
    }

    /*
    O(n)
     */
    @Override
    public V get(K key) {
        for (int i = 0; i < n; i++) {
            if (keys[i].equals(key)) return values[i];
        }
        return null;
    }

    /*
    O(n)
     */
    @Override
    public V put(K key, V value) {
        for (int i = 0; i < n; i++) {
            if (keys[i].equals(key)) {
                V v = values[i];
                values[i] = value;
                return v;
            }
        }
        
        keys[n] = key;
        values[n++] = value;
        return null;
    }

    /*
    O(n)
     */
    @Override
    public V remove(K key) {
        for (int i = 0; i < n; i++) {
            if (keys[i].equals(key)) {
                V v = values[i];
                keys[i] = keys[--n];
                values[i] = values[n];
                return v;
            }
        }
        return null;
    }

    /*
    O(1)
     */
    @Override
    public boolean isEmpty() {
        return n == 0;
    }
    
}

package ru.bpcbt.misc;

public class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K t, V u) {
        this.key = t;
        this.value = u;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
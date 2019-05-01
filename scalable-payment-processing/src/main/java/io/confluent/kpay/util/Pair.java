package io.confluent.kpay.util;

public class Pair<K,V> {
    private K k;
    private V v;


    public Pair(){};
    public Pair(K k, V v) {

        this.k = k;
        this.v = v;
    }

    public K getK() {
        return k;
    }

    public V getV() {
        return v;
    }

    public void setK(K k) {
        this.k = k;
    }

    public void setV(V v) {
        this.v = v;
    }
}

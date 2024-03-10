package com.scalpelred.chatcircuit;

import scala.Array;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class HashArrayList<T> implements Iterable<T> {

    private final HashSet<T> hash = new HashSet<>();
    private final ArrayList<T> array = new ArrayList<>();

    public HashArrayList() {

    }

    public boolean add(T element) {
        if (hash.contains(element)) return false;
        hash.add(element);
        array.add(element);
        return true;
    }
    
    public boolean insert(T element, int index) {
        if (hash.contains(element)) return false;
        hash.add(element);
        array.add(index, element);
        return true;
    }

    public boolean replaceEqual(T element) {
        if (hash.contains(element)) {
            int index = array.indexOf(element);
            array.remove(index);
            array.add(index, element);
            return true;
        }
        return false;
    }

    public boolean remove(T element) {
        if (hash.contains(element)) {
            hash.remove(element);
            array.remove(element);
            return true;
        }
        return false;
    }

    public void removeAt(int index) {
        T element = array.get(index);
        hash.remove(element);
        array.remove(index);
    }

    public boolean contains(T element) {
        return hash.contains(element);
    }

    public T getAt(int index) {
        return array.get(index);
    }

    public int indexOf(T element) {
        if (hash.contains(element)) return array.indexOf(element);
        return -1;
    }

    @Override
    public Iterator<T> iterator() {
        return array.iterator();
    }

    public T[] toArray(T[] arr) {
        return array.toArray(arr);
    }
}

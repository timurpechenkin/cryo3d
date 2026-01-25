package io.github.timurpechenkin.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractLibrary<T> implements Library<T> {
    private final List<T> list = new ArrayList<>();
    private final Map<String, Integer> nameToIndex = new HashMap<>();

    public int add(String name, T item) {
        if (nameToIndex.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate item name: " + name);
        }
        int index = list.size();
        list.add(item);
        nameToIndex.put(name, index);
        return index;
    }

    public int indexOf(String name) {
        Integer idx = nameToIndex.get(name);
        if (idx == null) {
            throw new IllegalArgumentException("Unknown item name: " + name);
        }
        return idx;
    }

    public T getByIndex(int index) {
        return list.get(index);
    }

    public T getByName(String name) {
        return list.get(indexOf(name));
    }

    public int size() {
        return list.size();
    }
}

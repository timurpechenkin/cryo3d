package io.github.timurpechenkin.domain.bc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoundaryConditionLibrary {
    private final List<BoundaryCondition> list = new ArrayList<>();
    private final Map<String, Integer> nameToIndex = new HashMap<>();

    public int add(String name, BoundaryCondition bc) {
        if (nameToIndex.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate boundary condition name: " + name);
        }
        int index = list.size();
        list.add(bc);
        nameToIndex.put(name, index);
        return index;
    }

    public int indexOf(String name) {
        Integer idx = nameToIndex.get(name);
        if (idx == null) {
            throw new IllegalArgumentException("Unknown boundary condition name: " + name);
        }
        return idx;
    }

    public BoundaryCondition getByIndex(int index) {
        return list.get(index);
    }

    public BoundaryCondition getByName(String name) {
        return list.get(indexOf(name));
    }

    public int size() {
        return list.size();
    }
}

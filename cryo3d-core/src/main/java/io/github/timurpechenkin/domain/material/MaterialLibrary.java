package io.github.timurpechenkin.domain.material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MaterialLibrary {
    private final List<Material> list = new ArrayList<>();
    private final Map<String, Integer> nameToIndex = new HashMap<>();

    public int add(String name, Material material) {
        if (nameToIndex.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate material name: " + name);
        }
        int index = list.size();
        list.add(material);
        nameToIndex.put(name, index);
        return index;
    }

    public int indexOf(String name) {
        Integer idx = nameToIndex.get(name);
        if (idx == null) {
            throw new IllegalArgumentException("Unknown material name: " + name);
        }
        return idx;
    }

    public Material getByIndex(int index) {
        return list.get(index);
    }

    public Material getByName(String name) {
        return list.get(indexOf(name));
    }

    public int size() {
        return list.size();
    }
}

package io.github.timurpechenkin.domain.model;

public interface Library<T> {

    int add(String name, T item);

    int indexOf(String name);

    T getByIndex(int index);

    T getByName(String name);

    int size();
}

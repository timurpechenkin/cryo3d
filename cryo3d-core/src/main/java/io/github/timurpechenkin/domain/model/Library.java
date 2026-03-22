package io.github.timurpechenkin.domain.model;

public interface Library<T> {

    int add(String name, T item);

    int idOf(String name);

    T getById(int index);

    T getByName(String name);

    int size();
}

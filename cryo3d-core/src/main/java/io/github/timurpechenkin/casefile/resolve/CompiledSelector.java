package io.github.timurpechenkin.casefile.resolve;

@FunctionalInterface
public interface CompiledSelector {
    boolean contains(int cx2, int cy2, int cz2);

}

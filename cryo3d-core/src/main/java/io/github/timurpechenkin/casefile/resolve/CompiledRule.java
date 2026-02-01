package io.github.timurpechenkin.casefile.resolve;

public record CompiledRule(String name, CompiledSelector selector, int itemIndex) {
}
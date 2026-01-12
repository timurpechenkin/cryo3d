package io.github.timurpechenkin.casefile.dto.selector;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BoxSelector.class, name = "BOX"),
        @JsonSubTypes.Type(value = ZRangeSelector.class, name = "Z_RANGE")
})
public sealed interface Selector permits BoxSelector, ZRangeSelector {
}
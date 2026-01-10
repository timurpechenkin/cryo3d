package io.github.timurpechenkin.casefile.temperature;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConstantTemperature.class, name = "CONSTANT")
})
public sealed interface TemperatureValue permits ConstantTemperature {
}
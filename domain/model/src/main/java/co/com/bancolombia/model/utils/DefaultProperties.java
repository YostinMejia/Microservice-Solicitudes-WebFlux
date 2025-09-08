package co.com.bancolombia.model.utils;

import lombok.Getter;

@Getter
public enum DefaultProperties {
    INITIAL_STATE_NAME("Pendiente de reivisión");

    private final String property;

    DefaultProperties(String property){
        this.property= property;
    }
}

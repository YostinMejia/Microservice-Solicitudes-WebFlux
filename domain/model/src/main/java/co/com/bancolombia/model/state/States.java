package co.com.bancolombia.model.state;

import lombok.Getter;

@Getter
public enum States {
    APPROVED("aprovado"),
    REJECTED("rechazado");

    private String value;

    States(String value) {
        this.value = value;
    }

}

package co.com.bancolombia.model.auth;

import lombok.Getter;

@Getter
public enum Role {
    ADVISOR("asesor"),
    ADMINISTRATOR("administrador"),
    CLIENT("cliente");

    private final String value;
    Role(String value){
        this.value = value;
    }
}

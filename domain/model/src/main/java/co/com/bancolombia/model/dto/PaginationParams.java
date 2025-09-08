package co.com.bancolombia.model.dto;

import lombok.Getter;


public record PaginationParams(
        int page,
        int limit
) {
    public int offset() {
        return page * limit;
    }
    public int limit(){
        return limit;
    }
}

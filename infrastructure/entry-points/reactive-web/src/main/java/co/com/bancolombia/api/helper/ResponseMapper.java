package co.com.bancolombia.api.helper;

import co.com.bancolombia.model.dto.Response;
import co.com.bancolombia.model.utils.ResponseMessage;

public class ResponseMapper {

    public static <T, E extends ResponseMessage> Response<T> mapBodyResponse(E responseCode, T data){
        return  new Response<>(responseCode.getMessage(), responseCode.getBusinessCode(), data);
    }

}

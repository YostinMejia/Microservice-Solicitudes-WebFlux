package co.com.bancolombia.api.helper;

import co.com.bancolombia.model.dto.ResponseDto;
import co.com.bancolombia.model.utils.ResponseMessage;

public class ResponseMapper {

    public static <T, E extends ResponseMessage> ResponseDto<T> mapBodyResponse(E responseCode, T data){
        return  new ResponseDto<>(responseCode.getMessage(), responseCode.getBusinessCode(), data);
    }

}

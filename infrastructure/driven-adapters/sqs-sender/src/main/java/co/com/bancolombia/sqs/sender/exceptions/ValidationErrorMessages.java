package co.com.bancolombia.sqs.sender.exceptions;

import co.com.bancolombia.model.utils.ResponseMessage;
import lombok.Getter;

@Getter
public enum ValidationErrorMessages implements ResponseMessage {
    JSON_PARSE_FAILED("Json parse failed", "B400-01");


   private final String message;
   private final String businessCode;

   ValidationErrorMessages(String message, String businessCode){
       this.message = message;
       this.businessCode = businessCode;
   }

}

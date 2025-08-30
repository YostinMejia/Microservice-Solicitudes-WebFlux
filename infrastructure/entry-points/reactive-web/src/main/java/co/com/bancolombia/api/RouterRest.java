package co.com.bancolombia.api;

import co.com.bancolombia.api.config.ApplicationPath;
import co.com.bancolombia.api.dto.CreateApplicationDto;
import co.com.bancolombia.model.dto.MultipleErrorsResponseDto;
import co.com.bancolombia.model.dto.ResponseDto;
import co.com.bancolombia.model.dto.SingleErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final ApplicationHandler applicationHandler;
    private final ApplicationPath applicationPath;


    @RouterOperations(

            value = {
                    @RouterOperation(method = RequestMethod.POST, path = "/api/v1/solicitud",
                            operation = @Operation(operationId = "save", summary = "Save Application", tags = {"Application"},
                                    responses = {
                                            @ApiResponse(responseCode = "201", description = "Successful save", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class)))
                                            , @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultipleErrorsResponseDto.class)))
                                            , @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SingleErrorResponseDto.class)))
                                    },
                                    requestBody = @RequestBody(
                                            required = true,
                                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateApplicationDto.class))
                                    )


                            )),

            }

    )

    @Bean
    public RouterFunction<ServerResponse> routerFunction(ApplicationHandler applicationHandler) {
        return route(POST(applicationPath.getApplications()), applicationHandler::listenPOSTApplication);
    }
}

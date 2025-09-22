package co.com.bancolombia.api;

import co.com.bancolombia.api.application.ApplicationHandler;
import co.com.bancolombia.api.application.config.ApplicationPath;
import co.com.bancolombia.api.application.dto.CreateApplicationDto;
import co.com.bancolombia.api.application.dto.DebtCapacityDto;
import co.com.bancolombia.api.application.dto.UpdateState;
import co.com.bancolombia.model.dto.MultipleErrorsResponse;
import co.com.bancolombia.model.dto.PaginationResponse;
import co.com.bancolombia.model.dto.Response;
import co.com.bancolombia.model.dto.SingleErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final ApplicationPath applicationPath;

    @RouterOperations(

            value = {
                    @RouterOperation(method = RequestMethod.POST, path = "/api/v1/solicitudes",
                            operation = @Operation(operationId = "save", summary = "Save Application", tags = {"Application"},

                                    responses = {
                                            @ApiResponse(responseCode = "201", description = "Successful save", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class)))
                                            , @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultipleErrorsResponse.class)))
                                            , @ApiResponse(responseCode = "400", description = "User is not allowed to create a loan for another person", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultipleErrorsResponse.class)))
                                            ,@ApiResponse(responseCode = "403", description = "No Authorization Header", content = @Content(mediaType = "application/json"))
                                            , @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SingleErrorResponse.class)))
                                    },
                                    requestBody = @RequestBody(
                                            required = true,
                                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateApplicationDto.class))
                                    )


                            )),

                    @RouterOperation(method = RequestMethod.GET, path = "/api/v1/solicitudes",
                            operation = @Operation(operationId = "save", summary = "Get the application filtered", tags = {"Application"},
                                    parameters = {
                                            @Parameter(name = "limit", in = ParameterIn.QUERY, description = "Número máximo de registros por página", schema = @Schema(type = "integer", defaultValue = "10")),
                                            @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número de página a consultar", schema = @Schema(type = "integer", defaultValue = "1")),
                                            @Parameter(name = "states", in = ParameterIn.QUERY, description = "Lista de estados separados por coma", schema = @Schema(type = "string", example = "rechazado,aprovado")),
                                            @Parameter(name = "manualCheck", in = ParameterIn.QUERY, description = "Si se filtra por revisión manual", schema = @Schema(type = "boolean"))
                                    },
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Successful retrieve", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginationResponse.class)))
                                            , @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultipleErrorsResponse.class)))
                                            ,@ApiResponse(responseCode = "403", description = "No Authorization Header", content = @Content(mediaType = "application/json"))
                                            , @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SingleErrorResponse.class)))
                                    }

                            )),



                    @RouterOperation(method = RequestMethod.PUT, path = "/api/v1/solicitudes",
                            operation = @Operation(operationId = "updateApplicationState", summary = "Update Application State", tags = {"Application"},

                                    responses = {
                                            @ApiResponse(responseCode = "201", description = "Successful updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class)))
                                            , @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultipleErrorsResponse.class)))
                                            ,@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json"))
                                            , @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SingleErrorResponse.class)))
                                    },
                                    requestBody = @RequestBody(
                                            required = true,
                                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdateState.class))
                                    )

                            )),

                    @RouterOperation(method = RequestMethod.POST, path = "/api/v1/calcular-capacidad",
                            operation = @Operation(operationId = "calculateDebtCapacity", summary = "Calculate Debt Capacity", tags = {"Application"},

                                    responses = {
                                            @ApiResponse(responseCode = "201", description = "Calculation request created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class)))
                                            , @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultipleErrorsResponse.class)))
                                            ,@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json"))
                                            , @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SingleErrorResponse.class)))
                                    },
                                    requestBody = @RequestBody(
                                            required = true,
                                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DebtCapacityDto.class))
                                    )

                            )),

            }

    )

    @Bean
    public RouterFunction<ServerResponse> routerFunction(ApplicationHandler applicationHandler) {
        return route(POST(applicationPath.getApplications()), applicationHandler::listenPOSTApplication)
                .andRoute(PUT(applicationPath.getApplications()),applicationHandler::listenUPDATEApplicationState)
                .andRoute(GET(applicationPath.getApplications()),applicationHandler::listenGETFindByFilter)
                .andRoute(POST(applicationPath.getCalculateDebtCapacity()),applicationHandler::listenCalculateDebtCapacity);

    }
}
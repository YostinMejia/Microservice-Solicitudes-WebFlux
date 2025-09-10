package co.com.bancolombia.api;

import co.com.bancolombia.api.application.ApplicationHandler;
import co.com.bancolombia.api.application.dto.ApplicationResponseDataDto;
import co.com.bancolombia.api.application.dto.CreateApplicationDto;
import co.com.bancolombia.api.helper.RequestValidator;
import co.com.bancolombia.api.application.mapper.ApplicationDtoMapper;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationDetails;
import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.dto.PaginationParams;
import co.com.bancolombia.model.dto.PaginationResponse;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.States;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import co.com.bancolombia.model.utils.ResponseCode;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ApplicationHandlerTest {

    @InjectMocks
    private ApplicationHandler applicationHandler;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private ApplicationDtoMapper applicationDtoMapper;

    @Mock
    private ApplicationUseCase applicationUseCase;

    private CreateApplicationDto requestDto;

    @BeforeEach
    void setUp() {

        requestDto = new CreateApplicationDto(
                1000, "2025-12-01", "Automóvil", "12345", "test@example.com"
        );
    }

    @Test
    void listenPOSTApplication_whenSuccess_shouldReturnCreated() {
        // Arrange
        Application domainApplication = Application.builder().amount(1000).term(LocalDate.of(2025, 12, 1)).build();
        Application savedApplication = domainApplication.toBuilder().id(UUID.randomUUID()).build();
        ApplicationResponseDataDto applicationResponseDataDto = new ApplicationResponseDataDto(savedApplication.getAmount(),savedApplication.getTerm().toString(), savedApplication.getDocument(),savedApplication.getEmail(),savedApplication.getIdState(),savedApplication.getIdTypeLoan());
        // Mock dependencies to pass
        given(requestValidator.validator(any(CreateApplicationDto.class))).willReturn(Mono.just(requestDto));
        given(applicationDtoMapper.toApplication(any(CreateApplicationDto.class))).willReturn(domainApplication);

        given(applicationUseCase.save(any(Application.class),any(String.class),any(String.class),any(String.class),any())).willReturn(Mono.just(savedApplication));
        given(applicationDtoMapper.toResponseData(any(Application.class))).willReturn(applicationResponseDataDto);

        MockServerRequest serverRequest = MockServerRequest.builder().body(Mono.just(requestDto));

        // Act & Assert
        StepVerifier.create(applicationHandler.listenPOSTApplication(serverRequest))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.CREATED)
                .verifyComplete();
    }

    @Test
    void listenPOSTApplication_whenUserDoesNotExist_shouldReturnBadRequest() {
        // Arrange
        given(requestValidator.validator(any(CreateApplicationDto.class))).willReturn(Mono.just(requestDto));
        given(applicationDtoMapper.toApplication(any(CreateApplicationDto.class))).willReturn(Application.builder().build());
        given(applicationUseCase.save(any(Application.class), any(String.class), any(String.class), any(String.class), any()))
                .willReturn(Mono.error(new BusinessException(BusinessErrorCode.USER_NOT_FOUND)));


        MockServerRequest serverRequest = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .body(Mono.just(requestDto));

        // Act & Assert
        StepVerifier.create(applicationHandler.listenPOSTApplication(serverRequest))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getCode().equals(BusinessErrorCode.USER_NOT_FOUND.getBusinessCode()))
                .verify();
    }

    @Test
    void listenPOSTApplication_whenValidationFails_shouldReturnBadRequest() {
        // Arrange
        CreateApplicationDto invalidDto = new CreateApplicationDto(
                -100, "2025-12-01", "Automóvil", "12345", "test@example.com"
        );
        given(requestValidator.validator(any(CreateApplicationDto.class)))
                .willReturn(Mono.error(new BusinessException(
                        List.of("amount must be > 0"),
                        BusinessErrorCode.VALIDATION_FAILED
                )));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .body(Mono.just(invalidDto));

        // Act & Assert
        StepVerifier.create(applicationHandler.listenPOSTApplication(serverRequest))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getCode().equals(BusinessErrorCode.VALIDATION_FAILED.getBusinessCode()))
                .verify();
    }

    @Test
    void listenPOSTApplication_whenTypeLoanDoesNotExist_shouldReturnBadRequest() {
        // Arrange
        given(requestValidator.validator(any(CreateApplicationDto.class))).willReturn(Mono.just(requestDto));
        given(applicationDtoMapper.toApplication(any(CreateApplicationDto.class))).willReturn(Application.builder().build());
        given(applicationUseCase.save(any(Application.class), any(String.class), any(String.class), any(String.class), any()))
                .willReturn(Mono.error(new BusinessException(BusinessErrorCode.TYPE_LOAN_NOT_FOUND)));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        // Act & Assert
        StepVerifier.create(applicationHandler.listenPOSTApplication(serverRequest))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getCode().equals(BusinessErrorCode.TYPE_LOAN_NOT_FOUND.getBusinessCode()))
                .verify();
    }

//    @Test
//    void listenGETFindByFilter_whenNoPaginationParamsGiven_shouldCreateDefaultOnes(){
//        given(applicationUseCase.findByFilter(any(ApplicationFilter.class),any(PaginationParams.class),any(String.class))).willReturn()
//        MockServerRequest serverRequest = MockServerRequest.builder()
//                .method(HttpMethod.GET)
//                .uri(URI.create("/applications?limit=5&page=2&states=APPROVED,PENDING"))
//                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
//                .build();
//
//
//    }

    @Test
    void listenGETFindByFilter_whenDataMatch_shouldReturnItPaginated() {
        final PaginationResponse<ApplicationDetails> fakeApprovedApplications = getDetailsPaginationResponse();


        given(applicationUseCase.findByFilter(any(ApplicationFilter.class), any(PaginationParams.class), any(String.class))).willReturn(Mono.just(fakeApprovedApplications));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .method(HttpMethod.GET)
                .uri(URI.create("/applications?limit=5&page=2&states=APPROVED,PENDING"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .build();

        StepVerifier.create(applicationHandler.listenGETFindByFilter(serverRequest))
                .assertNext(serverResponse -> {
                    assertThat(serverResponse.statusCode().value()).isEqualTo(200);
                })
                .verifyComplete();
    }

    @NotNull
    private static PaginationResponse<ApplicationDetails> getDetailsPaginationResponse() {
        List<ApplicationDetails> responses = List.of(new ApplicationDetails(
                        "juan.perez@mail.com",
                        5000L,
                        LocalDate.of(2025, 12, 1),
                        "Libre Inversión",
                        15,
                        States.APPROVED.getValue(),
                        450.0f
                ),

                new ApplicationDetails(
                        "maria.lopez@mail.com",
                        12000L,
                        LocalDate.of(2026, 6, 15),
                        "Vivienda",
                        12,
                        States.APPROVED.getValue(),
                        1100.0f
                ));

        return new PaginationResponse<>(
                ResponseCode.STATES_PAGINATED.getMessage(),
                ResponseCode.STATES_PAGINATED.getBusinessCode(),
                10,
                responses.size(),
                responses
        );
    }
}
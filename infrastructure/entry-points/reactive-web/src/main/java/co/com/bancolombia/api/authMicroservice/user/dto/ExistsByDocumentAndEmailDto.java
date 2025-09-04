package co.com.bancolombia.api.authMicroservice.user.dto;

public record ExistsByDocumentAndEmailDto(
        String document,
        String email
) {
}

package walletrest.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Запрос на получение баланса кошелька")
public record BalanceRequestDto(
        @NotNull(message = "Требуется id кошелька")
        @Schema(description = "Уникальный идентификатор кошелька", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID walletId
) {


}

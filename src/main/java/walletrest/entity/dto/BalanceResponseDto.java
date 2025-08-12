package walletrest.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Schema(description = "Ответ с информацией о балансе кошелька")
public record BalanceResponseDto(
        @Schema(description  = "Уникальный идентификатор кошелька", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID walletId,

        @Schema(description = "Текущий баланс кошелька", example = "1250.75")
        BigDecimal balance
) {
}

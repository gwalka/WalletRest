package walletrest.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import walletrest.entity.enums.OperationType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Schema(description = "Запрос на выполнение операции с кошельком")
public record OperationRequest(

        @NotNull(message = "Требуется id кошелька")
        @Schema(description = "Уникальный идентификатор кошелька", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID walletId,

        @NotNull(message = "Выберите тип операции")
        @Schema(description = "Тип операции с кошельком", example = "WITHDRAW")
        OperationType operationType,

        @NotNull(message = "Сумма не может быть пустой")
        @DecimalMin(value = "0.01", inclusive = true, message = "Сумма должна быть положительной")
        @Schema(description = "Сумма операции. Должна быть больше 0", example = "500.00")
        BigDecimal amount
){
}

package walletrest.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record KafkaWithdrawEvent(
        UUID walletId,
        BigDecimal amount
) {
}

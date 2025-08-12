package walletrest.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record KafkaDepositEvent(
        UUID walletId,
        BigDecimal amount
) {
}

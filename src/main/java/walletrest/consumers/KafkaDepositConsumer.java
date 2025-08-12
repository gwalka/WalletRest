package walletrest.consumers;

import walletrest.event.KafkaDepositEvent;
import walletrest.exception.DataValidationException;
import walletrest.exception.WalletNotFoundException;
import walletrest.service.ProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaDepositConsumer {

    private final ProcessingService processingService;

    @KafkaListener(topics = "${spring.kafka.topics.deposit.name}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "depositKafkaListenerContainerFactory")
    public void handleDepositEvent(KafkaDepositEvent event, Acknowledgment ack) {
        if (event == null) {
            ack.acknowledge();
            return;
        }

        try {
            processingService.depositFunds(event);
            ack.acknowledge();
            log.info("Ивент для кошелька {} успешно обработан", event.walletId());

        } catch (WalletNotFoundException | DataValidationException ex) {

            ack.acknowledge();
            log.warn("Бизнес-ошибка при обработке ивента для кошелька {}: {}", event.walletId(), ex.getMessage());

        } catch (Exception ex) {
            log.error("Техническая ошибка при обработке ивента для кошелька {}: {}", event.walletId(), ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}

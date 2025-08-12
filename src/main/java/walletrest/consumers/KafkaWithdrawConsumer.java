package walletrest.consumers;

import walletrest.event.KafkaWithdrawEvent;
import walletrest.exception.DataValidationException;
import walletrest.exception.KafkaHandleException;
import walletrest.exception.WrongOperationException;
import walletrest.service.ProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaWithdrawConsumer {


    private final ProcessingService processingService;

    @KafkaListener(topics = "${spring.kafka.topics.withdraw.name}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "withdrawKafkaListenerContainerFactory")
    public void handleWithdrawEvent(KafkaWithdrawEvent event, Acknowledgment ack) {
        if (event == null) {
            ack.acknowledge();
            return;
        }

        try {
            processingService.withdrawFunds(event);
            ack.acknowledge();
            log.info("ивент для кошелька {} отправлен", event.walletId());
        } catch (WrongOperationException | DataValidationException ex) {
            log.warn("Бизнес ошибка при обработке ивента кошелька {}: {}", event.walletId(), ex.getMessage());
            ack.acknowledge();

        } catch (Exception ex) {
            log.error("Ошибка обработки ивента для кошелька {}: {}", event.walletId(), ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

    }
}

package walletrest.producer;

import walletrest.event.KafkaDepositEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaDepositProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.deposit.name}")
    private String depositTopic;

    public void sendDepositEvent(KafkaDepositEvent event) {
        if (event != null) {
            try {
                kafkaTemplate.send(depositTopic, event.walletId().toString(), event);
                log.info("Ивент для кошелька {} отправлен в топик {}", event.walletId(), depositTopic);
            } catch (Exception e) {
                log.error("Ошибка отправки ивента для кошелька {} в топик {}", event.walletId(), depositTopic);
                throw new RuntimeException(e);
            }
        }
    }

}

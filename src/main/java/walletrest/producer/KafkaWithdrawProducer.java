package walletrest.producer;

import walletrest.event.KafkaWithdrawEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaWithdrawProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.withdraw.name}")
    private String withdrawTopic;

    public void sendWithdrawEvent(KafkaWithdrawEvent event) {
        if (event != null) {
            try {
                kafkaTemplate.send(withdrawTopic, event.walletId().toString(), event);
                log.info("Ивент для кошелька {} отправлен в топик {}", event.walletId(), withdrawTopic);
            } catch (Exception e) {
                log.error("Ошибка отправки ивента для кошелька {} в топик {}", event.walletId(), withdrawTopic);
                throw new RuntimeException(e);
            }
        }
    }
}

package walletrest.config;

import walletrest.event.KafkaDepositEvent;
import walletrest.event.KafkaWithdrawEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumer {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;


    private Map<String, Object> commonProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }


    @Bean
    public ConsumerFactory<String, KafkaDepositEvent> depositConsumerFactory() {
        Map<String, Object> props = commonProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);


        JsonDeserializer<KafkaDepositEvent> deserializer = new JsonDeserializer<>(KafkaDepositEvent.class);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaDepositEvent> depositKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaDepositEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(depositConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }


    @Bean
    public ConsumerFactory<String, KafkaWithdrawEvent> withdrawConsumerFactory() {
        Map<String, Object> props = commonProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<KafkaWithdrawEvent> deserializer = new JsonDeserializer<>(KafkaWithdrawEvent.class);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaWithdrawEvent> withdrawKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaWithdrawEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(withdrawConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}

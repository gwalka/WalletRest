package walletrest.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfiguration {

    @Value("${spring.kafka.topics.deposit.name}")
    private String depositTopicName;
    @Value("${spring.kafka.topics.deposit.partitions}")
    private int depositPartitions;
    @Value("${spring.kafka.topics.deposit.replications}")
    private short depositReplications;

    @Value("${spring.kafka.topics.withdraw.name}")
    private String withdrawTopicName;
    @Value("${spring.kafka.topics.withdraw.partitions}")
    private int withdrawPartitions;
    @Value("${spring.kafka.topics.withdraw.replications}")
    private short withdrawReplications;


    @Bean
    public NewTopic depositTopic() {
        return new NewTopic(depositTopicName, depositPartitions, depositReplications);
    }

    @Bean
    public NewTopic withdrawTopic() {
        return new NewTopic(withdrawTopicName, withdrawPartitions, withdrawReplications);
    }

}

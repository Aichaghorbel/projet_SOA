package com.educampus.academic.config;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Component;

@Component
public class KafkaStartupCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KafkaStartupCheck.class);

    private final KafkaProperties properties;

    public KafkaStartupCheck(KafkaProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, Object> config = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", properties.getBootstrapServers()),
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);

        try (AdminClient admin = AdminClient.create(config)) {
            String clusterId = admin.describeCluster().clusterId().get(10, TimeUnit.SECONDS);
            log.info("Connexion Kafka OK (cluster {})", clusterId);
        }
    }
}
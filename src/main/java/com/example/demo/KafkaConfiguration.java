package com.example.demo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;


import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "kafka.config")

public class KafkaConfiguration {

	private static final String TRUSTSTORE_JKS = "/var/private/ssl/kafka.client.truststore.jks";
	private static final String SASL_PROTOCOL = "SASL_SSL";
	private static final String SCRAM_SHA_256 = "SCRAM-SHA-256";

	private final String jassTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
	private final String prodJaasCfg = String.format(jassTemplate, "", "");
	private final String consJaasCfg = String.format(jassTemplate, "", "");

	private String bootstrapAddress;
	private String topicName;
	private int numPartitions;
	private short replicationFactor;

	public void setBootstrapAddress(final String bootstrapAddress) {
		this.bootstrapAddress = bootstrapAddress;
	}

	public String getBootstrapAddress() {
		return this.bootstrapAddress;
	}

	public int getNumPartitions() {
		return this.numPartitions;
	}

	public String getTopicName() {
		return this.topicName;
	}

	public void setTopicName(final String topicName) {
		this.topicName = topicName;
	}

	public void setNumPartitions(final int numPartitions) {
		this.numPartitions = numPartitions;
	}

	public short getReplicationFactor() {
		return this.replicationFactor;
	}

	public void setReplicationFactor(final short replicationFactor) {
		this.replicationFactor = replicationFactor;
	}
	@Bean
	public ProducerFactory<String, Object> producerFactoryString() {
		Map<String, Object> configProps = new HashMap<>();

		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		configProps.put(ProducerConfig.ACKS_CONFIG, "all");
		configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "cid1");
		configProps.put("sasl.mechanism", SCRAM_SHA_256);
		configProps.put("sasl.jass.config", devJassCfg);
		configProps.put("security.protocol", SASL_PROTOCOL);
		configProps.put("ssl.truststore.location", TRUSTSTORE_JKS);
		configProps.put("ssl.truststore.password", "password");
		configProps.put("ssl.endpoint.identification.algorithm", "");

		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactoryString());
	}

	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
		configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		configProps.put(JsonDeserializer.TRUSTED_PACKAGES, TRUSTED_PACKAGE);
		configProps.put("sasl.mechanism", SCRAM_SHA_256);
		configProps.put("sasl.jass.config", consJaasCfg);
		configProps.put("security.protocol", SASL_PROTOCOL);
		configProps.put("ssl.truststore.location", TRUSTSTORE_JKS);
		configProps.put("ssl.truststore.password", "password");
		configProps.put("ssl.endpoint.identification.algorithm", "");

		return new DefaultKafkaConsumerFactory<>(configProps);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());

		return factory;
	}

}

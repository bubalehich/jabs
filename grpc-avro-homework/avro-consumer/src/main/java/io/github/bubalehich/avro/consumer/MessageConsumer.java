package io.github.bubalehich.avro.consumer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.github.bubalehich.avro.Message;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public final class MessageConsumer {

	private static final String TOPIC = "messages";
	private static final String BOOTSTRAP_SERVERS = "localhost:9092";
	private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";
	private static final int WIRE_HEADER_LENGTH = 5;

	private MessageConsumer() {
	}

	public static void main(String[] args) {
		Map<String, Object> consumerConfig = Map.of(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS,
				ConsumerConfig.GROUP_ID_CONFIG, "message-printer",
				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		Map<String, Object> avroConfig = Map.of(
				AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL,
				KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

		try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerConfig);
				KafkaAvroDeserializer avroDeserializer = new KafkaAvroDeserializer()) {
			avroDeserializer.configure(avroConfig, false);
			Thread mainThread = Thread.currentThread();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				consumer.wakeup();
				try {
					mainThread.join();
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}));

			consumer.subscribe(List.of(TOPIC));
			System.out.printf("Listening to topic '%s'%n", TOPIC);
			while (true) {
				for (ConsumerRecord<String, byte[]> record : consumer.poll(Duration.ofSeconds(1))) {
					Message message = (Message) avroDeserializer.deserialize(record.topic(), record.value());
					print(record, message);
				}
			}
		}
		catch (WakeupException ex) {
			System.out.println("Consumer stopped");
		}
	}

	private static void print(ConsumerRecord<String, byte[]> record, Message message) {
		ByteBuffer payload = ByteBuffer.wrap(record.value());
		byte magicByte = payload.get();
		int schemaId = payload.getInt();
		String header = HexFormat.ofDelimiter(" ").formatHex(record.value(), 0, WIRE_HEADER_LENGTH);
		System.out.printf("offset=%d bytes[0-4]=[%s] magic=%d schemaId=%d -> %s%n", record.offset(), header,
				magicByte, schemaId, message);
	}

}

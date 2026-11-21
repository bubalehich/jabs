package io.github.bubalehich.avro.producer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class MessageProducer {

	private static final String TOPIC = "messages";
	private static final String BOOTSTRAP_SERVERS = "localhost:9092";
	private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";

	private MessageProducer() {
	}

	public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
		String schemaVersion = args.length > 0 ? args[0] : "v1";
		String text = args.length > 1 ? args[1] : "Hello, Avro!";
		Schema schema = loadSchema(schemaVersion);
		GenericRecord message = buildMessage(schema, text);

		Map<String, Object> config = Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS,
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class,
				AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
		try (KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(config)) {
			String key = message.get("id").toString();
			RecordMetadata metadata = producer.send(new ProducerRecord<>(TOPIC, key, message)).get();
			System.out.printf("Sent with schema %s to %s-%d@%d: %s%n", schemaVersion, metadata.topic(),
					metadata.partition(), metadata.offset(), message);
		}
	}

	private static Schema loadSchema(String version) throws IOException {
		String path = "/schemas/message-%s.avsc".formatted(version);
		try (InputStream schema = MessageProducer.class.getResourceAsStream(path)) {
			if (schema == null) {
				throw new IllegalArgumentException("Unknown schema version '%s', expected v1 or v2".formatted(version));
			}
			return new Schema.Parser().parse(schema);
		}
	}

	private static GenericRecord buildMessage(Schema schema, String text) {
		GenericRecordBuilder message = new GenericRecordBuilder(schema)
				.set("id", UUID.randomUUID().toString())
				.set("text", text);
		if (schema.getField("author") != null) {
			message.set("author", "avro-producer").set("sentAt", Instant.now().toEpochMilli());
		}
		return message.build();
	}

}

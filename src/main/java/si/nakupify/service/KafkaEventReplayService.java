package si.nakupify.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import si.nakupify.entity.Event;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
public class KafkaEventReplayService {

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    public KafkaConsumer<String, Event> createConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "skladisce-replay-" + System.currentTimeMillis());
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "si.nakupify.service.deserializer.EventDeserializer");
        props.put("auto.offset.reset", "earliest");

        return new KafkaConsumer<>(props);
    }

    public List<Event> replayEvents(Long offset, int partition) {
        TopicPartition tp = new TopicPartition("events", partition);
        List<Event> events = new ArrayList<>();
        KafkaConsumer<String, Event> consumer = createConsumer();
        consumer.assign(Collections.singletonList(tp));
        consumer.seek(tp, offset + 1);

        while (true) {
            ConsumerRecords<String, Event> records = consumer.poll(Duration.ofMillis(300));

            if (records.isEmpty()) {
                break;
            }

            for (ConsumerRecord<String, Event> r : records) {
                events.add(r.value());
            }
        }

        consumer.close();

        return events;
    }
}

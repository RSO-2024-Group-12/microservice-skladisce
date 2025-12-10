package si.nakupify.service.deserializer;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import si.nakupify.entity.Event;

public class EventDeserializer extends ObjectMapperDeserializer<Event> {
    public EventDeserializer() {
        super(Event.class);
    }
}

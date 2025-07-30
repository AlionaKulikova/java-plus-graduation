package serializer;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserAvroSpecificDeserializer extends BaseAvroDeserializer<UserActionAvro> {
    public UserAvroSpecificDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
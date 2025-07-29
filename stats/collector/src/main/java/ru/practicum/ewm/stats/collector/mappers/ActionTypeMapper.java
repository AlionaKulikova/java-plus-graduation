package ru.practicum.ewm.stats.collector.mappers;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.grpc.stats.action.UserActionMessages;

public class ActionTypeMapper {
    public static ActionTypeAvro toActionTypeAvro(UserActionMessages.ActionTypeProto actionTypeProto) {
        switch (actionTypeProto) {
            case ACTION_LIKE -> {
                return ActionTypeAvro.LIKE;
            }
            case ACTION_VIEW -> {
                return ActionTypeAvro.VIEW;
            }
            case ACTION_REGISTER -> {
                return ActionTypeAvro.REGISTER;
            }
        }
        return null;
    }
}

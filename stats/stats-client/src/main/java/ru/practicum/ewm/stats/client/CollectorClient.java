package ru.practicum.ewm.stats.client;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;

import java.time.Instant;

@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub userActionController;

    public void sendPreviewEvent(long userId, long eventId) {
        userActionController.collectUserAction(getUserActionProto(userId, eventId, ActionTypeProto.ACTION_VIEW));
    }

    public void sendRegistrationEvent(long userId, long eventId) {
        userActionController.collectUserAction(getUserActionProto(userId, eventId, ActionTypeProto.ACTION_REGISTER));
    }

    public void sendLikeEvent(long userId, long eventId) {
        userActionController.collectUserAction(getUserActionProto(userId, eventId, ActionTypeProto.ACTION_LIKE));
    }

    private UserActionProto getUserActionProto(long userId, long eventId, ActionTypeProto actionType) {
        return UserActionProto.newBuilder()
                .setActionType(actionType)
                .setUserId(userId)
                .setEventId(eventId)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build();
    }
}

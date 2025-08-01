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

    public void viewEvent(Long userId, Long eventId) {
        userActionController.collectUserAction(getUserActionProto(userId, eventId, ActionTypeProto.ACTION_VIEW));
    }

    public void addLikeEvent(Long userId, Long eventId) {
        userActionController.collectUserAction(getUserActionProto(userId, eventId, ActionTypeProto.ACTION_LIKE));
    }

    public void registrationInEvent(Long userId, Long eventId) {
        userActionController.collectUserAction(getUserActionProto(userId, eventId, ActionTypeProto.ACTION_REGISTER));
    }

    private UserActionProto getUserActionProto(Long userId, Long eventId, ActionTypeProto actionType) {
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
package ewm.client.grpcclient;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.action.UserActionControllerGrpc;
import ru.practicum.grpc.stats.action.UserActionMessages;

import java.time.Instant;

@Slf4j
@Service
public class CollectorClient {

    private final UserActionControllerGrpc.UserActionControllerBlockingStub blockingStub;

    public CollectorClient(@GrpcClient("collector") UserActionControllerGrpc.UserActionControllerBlockingStub blockingStub) {
        this.blockingStub = blockingStub;

    }

    public void sendUserAction(long userId, long eventId, UserActionMessages.ActionTypeProto actionType) {
        UserActionMessages.UserActionProto userAction = UserActionMessages.UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build();

        try {
            blockingStub.collectUserAction(userAction);
            log.info("Действие пользователя успешно отправлено");
        } catch (StatusRuntimeException e) {
            log.error("Ошибка при вызове gRPC: {}", e.getStatus());
        }
    }
}
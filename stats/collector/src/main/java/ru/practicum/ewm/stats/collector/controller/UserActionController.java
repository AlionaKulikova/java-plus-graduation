package ru.practicum.ewm.stats.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.collector.mappers.ActionTypeMapper;
import ru.practicum.ewm.stats.collector.service.CollectorActionService;
import ru.practicum.grpc.stats.action.UserActionControllerGrpc;
import ru.practicum.grpc.stats.action.UserActionMessages;

import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final CollectorActionService collectorActionService;

    @Override
    public void collectUserAction(UserActionMessages.UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("Получено сообщение grpc {}", request);

        responseObserver.onNext(Empty.getDefaultInstance());//отправляет пустое сообщение клиенту, тем самым подтверждая, что запрос получен и обработан.
        responseObserver.onCompleted();// закрывает поток, говоря клиенту, что ответ полностью отправлен.

        collectorActionService.sendUserAction(new UserActionAvro(request.getUserId(),//формируем сообщение
                request.getEventId(),//id события
                ActionTypeMapper.toActionTypeAvro(request.getActionType()),//тип просмотр/лайк
                Instant.ofEpochSecond(request.getTimestamp().getSeconds(),//время просмотра/лайка
                        request.getTimestamp().getNanos())));
    }
}

package ru.practicum.ewm.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.service.AnalyzerService;
import ru.practicum.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class AnalyzerController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final AnalyzerService analyzerService;

    @Override
    public void getRecommendations(UserPredictionsRequestProto request,
                                   StreamObserver<RecommendedEventProto> responseObserver) {
        handleRequest(() -> analyzerService.getRecommendations(request), responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        handleRequest(() -> analyzerService.getSimilarEvents(request), responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        handleRequest(() -> analyzerService.getInteractionsCount(request), responseObserver);
    }

    private void handleRequest(Supplier<Iterable<RecommendedEventProto>> supplier,
                               StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            for (RecommendedEventProto proto : supplier.get()) {
                responseObserver.onNext(proto);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке gRPC-запроса", e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getLocalizedMessage()).withCause(e)));
        }
    }
}
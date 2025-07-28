package ru.practicum.stats.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {//начало подключения к брокеру агрегатора для получения сообщений от collector
    private final AggregatorSimilarityService aggregatorSimilarityService;
    private final KafkaConsumer<String, UserActionAvro> kafkaConsumer;//консюмер для принятия и десеарилизации сообщения
   //ключ(Long) : зн->ключ(Long):значение(Double)
    /*
    Используется Collections.synchronizedMap для синхронизации доступа к верхнему уровню карты.
Для вложенных карт синхронизация не обеспечена автоматически, нужно учитывать при многопоточном доступе.
    ! существует более безопасной реализации для многопоточного доступа.
     private final Map<Long, Map<Long, Double>> weights = new ConcurrentHashMap<>();* */
    private Map<Long, Map<Long, Double>> weights = Collections.synchronizedMap(new HashMap<>());
    private Map<Long, Map<Long, Double>> sMinMap = Collections.synchronizedMap(new HashMap<>());

    //коэфициенты веса значимости действий пользователя над  событиями
    private static final Double VIEW_WEIGHT = 0.4;//просмотр событий
    private static final Double REGISTER_WEIGHT = 0.8;//????заявка на участие?????
    private static final Double LIKE_WEIGHT = 1.0;//лайк события

    @Value("${kafka.topics.actions}")
    private String topic;//топик, к которому подключаемя для получения сообщений от collector

    public void start() {
        kafkaConsumer.subscribe(Collections.singletonList(topic));//подписываемся на топик

        while (true) {
            /*Чтобы получить сообщения из Kafka, потребитель регулярно вызывает poll.
Таймаут нужен, чтобы poll не блокировал поток бесконечно, а возвращался через заданное время.
Если сообщений нет, poll вернёт пустую коллекцию. kafkaConsumer десеарелизует сообщения*/
            ConsumerRecords<String, UserActionAvro> records = kafkaConsumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, UserActionAvro> record : records) {//перебираем сообщения десеарелизованные
                UserActionAvro userAction = record.value();//получаем значение(объект)
                log.info("Принято сообщение {}", userAction);


                double weight = 0.0;//начальный вес коэфициента значимости
                switch (userAction.getActionType()) {//получаем ти действия
                    case VIEW -> {//если это просмотр события
                        weight = VIEW_WEIGHT;//0,4
                    }
                    case REGISTER -> {//если запрос на участие??
                        weight = REGISTER_WEIGHT;//0.8
                    }
                    case LIKE -> {//если пользователь лайкнул событие
                        weight = LIKE_WEIGHT;//1.0
                    }
                }

                double oldEventAWeight = 0;//обнуляем предыдущий коэфициент значимости
                double newEventAWeight = weight;//присвваеваем новый полученный выше коэфициент значимости

                //работаем с 1-ммэпом веса
                if (weights.get(userAction.getEventId()) == null//если нет ключа с id события
                        //или события и пользователя по id
                        || weights.get(userAction.getEventId()).get(userAction.getUserId()) == null) {
                    //создаем новую мэпу, которую будем вкладывать, как значение в другую мэпу
                    Map<Long, Double> hashMap = Collections.synchronizedMap(new HashMap<>());
                    //тепреь добавляем в 1-ую мэпу веса ключ: idсобытия, зн: мэпа вышесозданная
                    weights.put(userAction.getEventId(), hashMap);
                    //и вкладываем во вложенную мэпу в качестве ключа шв пользователя, а значением будет коэфициет веса
                    //0.0
                    weights.get(userAction.getEventId()).putIfAbsent(userAction.getUserId(), 0.0);
                } else {//если уже есть такие id-шники событий и юзеров в мэпе
                    oldEventAWeight = weights//тогда в старый вес добавляем содержащийся во вложенной мэпе коэфициент
                            .get(userAction.getEventId())
                            .get(userAction.getUserId());

                    //в новый вес мы запишем больший коэфициент из
                    // //содержащегося коэфициента в мэпе и полученным из case(weight)
                    newEventAWeight = Math.max(weights
                            .get(userAction.getEventId())
                            .get(userAction.getUserId()), weight);
                }

                long eventAId = userAction.getEventId();//получаем id события
                if (newEventAWeight > oldEventAWeight) {//новый вес больше, чем предыдущий вес
                    for (Long eventBId : weights.keySet()) {//перебираем ключи первого мэпа(id событий)
                        if (weights.get(eventBId).get(userAction.getUserId()) != null && eventBId != eventAId) {
                            double sMin = getMinWeightSumForAllUsers(eventAId, eventBId);
                            double sAOld = getWeightSumForAllUsers(eventAId);
                            double sBOld = getWeightSumForAllUsers(eventBId);

                            double minOld = Math.min(
                                    oldEventAWeight,
                                    weights.get(eventBId).get(userAction.getUserId()));

                            double minNew = Math.min(
                                    newEventAWeight,
                                    weights.get(eventBId).get(userAction.getUserId()));
                            double deltaMin = minNew - minOld;

                            double sMinNew = sMin + deltaMin;
                            if (deltaMin != 0) {
                                put(eventAId, eventBId, sMinNew);
                            }

                            double deltaWeightA = newEventAWeight
                                    - weights.get(eventAId).get(userAction.getUserId());
                            double sAnew = sAOld + deltaWeightA;

                            double similarity = sMinNew / (Math.sqrt(sAnew) * Math.sqrt(sBOld));

                            EventSimilarityAvro eventSimilarityAvro = new EventSimilarityAvro(
                                    eventAId,
                                    eventBId,
                                    similarity,
                                    userAction.getTimestamp());
                            //в сервис агрегатора отпрвляем готовое сообщение для дальнейшей отправки в кафку
                            aggregatorSimilarityService.sendEventSimilarity(eventSimilarityAvro);
                        }
                    }
                }
                weights.get(eventAId).put(userAction.getUserId(), newEventAWeight);

            }
            /*для синхронного подтверждения (commit) смещений (offsets),
            которые были прочитаны потребителем (consumer).
             Это означает, что потребитель сообщает брокеру Kafka,
             что он успешно обработал все сообщения до текущего смещения,
             и эти смещения можно считать сохранёнными.

             */
            kafkaConsumer.commitSync();
        }
    }

    private Double getMinWeightSumForAllUsers(Long eventAId, Long eventBId) {
        double sum = 0.0;
        for (Long uid : weights.get(eventAId).keySet()) {
            double firstWeight = 0;
            if (weights.get(eventAId).get(uid) != null) {
                firstWeight = weights.get(eventAId).get(uid);
            }
            double secondWeight = 0;
            if (weights.get(eventBId).get(uid) != null) {
                secondWeight = weights.get(eventBId).get(uid);
            }
            sum += Math.min(firstWeight, secondWeight);
        }
        return sum;
    }

    private Double getWeightSumForAllUsers(Long eventAId) {
        Double sum = 0.0;
        for (Long uid : weights.get(eventAId).keySet()) {
            sum += weights.get(eventAId).get(uid);
        }
        return sum;
    }

    public void put(long eventA, long eventB, Double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        sMinMap
                .computeIfAbsent(first, e -> Collections.synchronizedMap(new HashMap<>()))
                .put(second, sum);
    }
}


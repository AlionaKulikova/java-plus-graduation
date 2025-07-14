package ru.practicum.util;

@SuppressWarnings("squid:S1075")
public class PathConstants {
    private PathConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static final String ADMIN = "/admin";
    public static final String CATEGORIES = "/categories";
    public static final String COMPILATIONS = "/compilations";
    public static final String EVENTS = "/events";
    public static final String USERS = "/users";
    public static final String REQUESTS = "/requests";
    public static final String SUBSCRIPTIONS = "/subscriptions";
    public static final String SUBSCRIBERS = "/subscribers";

    public static final String CANCEL = "/cancel";

    public static final String CATEGORY_ID = "/{catId}";
    public static final String COMPILATION_ID = "/{compId}";
    public static final String EVENT_ID = "/{eventId}";
    public static final String USER_ID = "/{userId}";
    public static final String REQUEST_ID = "/{requestId}";
    public static final String SUBSCRIBED_TO_ID = "/{subscribedToId}";

    public static final String ADMIN_CATEGORIES = ADMIN + CATEGORIES; // Категории для админа
    public static final String ADMIN_COMPILATIONS = ADMIN + COMPILATIONS; // Подборки для админа
    public static final String ADMIN_EVENTS = ADMIN + EVENTS; // События для админа
    public static final String ADMIN_USERS = ADMIN + USERS; // Пользователи для админа

    public static final String PATH_TO_USER = USERS + USER_ID; // Базовый путь до конкретного пользователя
    public static final String PATH_TO_SUBSCRIPTION = SUBSCRIPTIONS + SUBSCRIBED_TO_ID; // Путь для подписки или отписки
    public static final String PRIVATE_EVENTS = PATH_TO_USER + EVENTS; // Базовый путь для приватных событий пользователя
    public static final String PRIVATE_EVENT_REQUESTS = PRIVATE_EVENTS + EVENT_ID + REQUESTS; // Запросы пользователя на участие в событии
    public static final String PRIVATE_REQUESTS = PATH_TO_USER + REQUESTS; // Все запросы пользователя
    public static final String PRIVATE_REQUEST_CANCEL = PRIVATE_REQUESTS + REQUEST_ID + CANCEL; // Отмена конкретного запроса
}
package ru.practicum.events.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.model.Category;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String annotation;
    boolean paid;
    String title;

    @Column(name = "event_date")
    LocalDateTime eventDate;

    String description;

    @Column(name = "request_moderation")
    private boolean requestModeration;

    @Column(name = "participant_limit")
    private int participantLimit;

    @Column(name = "published_at")
    private LocalDateTime publishedOn;

    @Column(name = "created_at")
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User initiator;

    @ManyToOne
    @JoinColumn(name = "location_id")
    Location location;

    @Enumerated(EnumType.STRING)
    EventState state;

    @Column(name = "confirmed_requests")
    Integer confirmedRequests = 0;

    @Column(name = "views")
    Long views;

}

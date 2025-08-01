package ru.practicum.ewm.model.similarity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "similarities")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Similarity {
    @EmbeddedId
    SimilarityCompositeKey key;

    double score;

    Instant timestamp;
}
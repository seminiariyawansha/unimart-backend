package lk.ac.kln.unimart_backend.review.dto;

import java.time.Instant;

public record ReviewResponse(
        Long id, Long orderId, Long reviewerId, Long revieweeId,
        Integer rating, String comment, Instant createdAt, Instant updatedAt
) {}
package lk.ac.kln.unimart_backend.review.mapper;

import lk.ac.kln.unimart_backend.review.dto.ReviewResponse;
import lk.ac.kln.unimart_backend.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(), r.getOrder().getId(), r.getReviewer().getId(), r.getReviewee().getId(),
                r.getRating(), r.getComment(), r.getCreatedAt(), r.getUpdatedAt()
        );
    }
}
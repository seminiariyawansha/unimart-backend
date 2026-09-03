package lk.ac.kln.unimart_backend.review.repository;

import lk.ac.kln.unimart_backend.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByOrderId(Long orderId);
    Page<Review> findByRevieweeId(Long revieweeId, Pageable pageable);
}
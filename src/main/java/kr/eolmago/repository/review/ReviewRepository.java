package kr.eolmago.repository.review;

import kr.eolmago.domain.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Review Repository
 * Spring Data JPA 메서드 네이밍 사용 (팀 스타일)
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * 거래 ID로 후기 목록
     */
    List<Review> findByDeal_DealId(Long dealId);
    
    /**
     * 판매자 ID로 후기 목록
     */
    List<Review> findBySeller_UserId(UUID sellerId);
    
    /**
     * 구매자 ID로 후기 목록
     */
    List<Review> findByBuyer_UserId(UUID buyerId);
    
    /**
     * 평점별 후기 목록
     */
    List<Review> findByRating(int rating);
}

package kr.eolmago.service.review;

import kr.eolmago.domain.entity.deal.Deal;
import kr.eolmago.domain.entity.deal.enums.DealStatus;
import kr.eolmago.domain.entity.review.Review;
import kr.eolmago.domain.entity.user.User;
import kr.eolmago.dto.view.review.ReviewResponse;
import kr.eolmago.repository.deal.DealRepository;
import kr.eolmago.repository.review.ReviewRepository;
import kr.eolmago.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Review Service
 * 비즈니스 로직과 검증 담당
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;

    /**
     * 후기 작성
     */
    @Transactional
    public ReviewResponse createReview(
            Long dealId,
            UUID sellerId,
            UUID buyerId,
            int rating,
            String content
    ) {
        // 비즈니스 검증: Entity 존재 확인
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다"));
        User seller = userRepository.findByUserId(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다"));
        User buyer = userRepository.findByUserId(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다"));

        // 비즈니스 검증: 거래 완료 확인
        if (deal.getStatus() != DealStatus.COMPLETED) {
            throw new IllegalStateException("완료된 거래만 후기를 작성할 수 있습니다");
        }

        // 비즈니스 검증: 평점 범위
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 사이여야 합니다");
        }

        // 정적 팩토리 메서드로 생성
        Review review = Review.create(
                deal,
                seller,
                buyer,
                (short) rating,
                content
        );

        Review saved = reviewRepository.save(review);
        return ReviewResponse.from(saved);
    }

    /**
     * 후기 조회
     */
    public ReviewResponse getReview(Long reviewId) {
        Review review = findReviewById(reviewId);
        return ReviewResponse.from(review);
    }

    /**
     * 전체 후기 목록
     */
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 거래별 후기 목록
     */
    public List<ReviewResponse> getReviewsByDeal(Long dealId) {
        return reviewRepository.findByDeal_DealId(dealId).stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 판매자별 후기 목록
     */
    public List<ReviewResponse> getReviewsBySeller(UUID sellerId) {
        return reviewRepository.findBySeller_UserId(sellerId).stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 구매자별 후기 목록
     */
    public List<ReviewResponse> getReviewsByBuyer(UUID buyerId) {
        return reviewRepository.findByBuyer_UserId(buyerId).stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 후기 삭제
     */
    @Transactional
    public void deleteReview(Long reviewId, UUID userId) {
        Review review = findReviewById(reviewId);

        // 비즈니스 검증: 작성자 확인
        if (!review.getSeller().getUserId().equals(userId) &&
            !review.getBuyer().getUserId().equals(userId)) {
            throw new IllegalArgumentException("후기 관련자만 삭제할 수 있습니다");
        }

        reviewRepository.delete(review);
    }

    // ========================================
    // Private Helper
    // ========================================

    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("후기를 찾을 수 없습니다"));
    }
}

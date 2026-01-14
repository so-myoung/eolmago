package kr.eolmago.controller.api.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.eolmago.global.security.CustomUserDetails;
import kr.eolmago.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "리뷰 공통 API", description = "리뷰 공통 기능(삭제/존재여부) API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 삭제", description = "리뷰 작성자(구매자) 또는 판매자만 삭제할 수 있습니다.")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "거래에 대한 리뷰 존재 여부", description = "해당 dealId에 대해 리뷰가 존재하는지 확인합니다.")
    @GetMapping("/deals/{dealId}/exists")
    public ResponseEntity<Map<String, Boolean>> existsReviewForDeal(@PathVariable Long dealId) {
        boolean hasReview = reviewService.existsReviewForDeal(dealId);
        return ResponseEntity.ok(Map.of("hasReview", hasReview));
    }
}

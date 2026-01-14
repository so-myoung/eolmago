package kr.eolmago.controller.api.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.eolmago.dto.api.review.request.ReviewCreateRequest;
import kr.eolmago.global.security.CustomUserDetails;
import kr.eolmago.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Buyer Deal Review", description = "구매자 거래 기반 리뷰 작성 API")
@RestController
@RequestMapping("/api/buyer/deals")
@RequiredArgsConstructor
public class BuyerDealReviewApiController {

    private final ReviewService reviewService;

    @Operation(
            summary = "구매자 거래 리뷰 작성",
            description = "구매 거래 상세(목록/상세)에서 거래(dealId)에 대한 리뷰를 작성합니다."
    )
    @PostMapping("/{dealId}/review")
    public ResponseEntity<Void> createBuyerDealReview(
            @PathVariable Long dealId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReviewCreateRequest request
    ) {
        UUID reviewerId = userDetails.getUserId();
        reviewService.createReview(dealId, reviewerId, request);
        return ResponseEntity.ok().build();
    }
}

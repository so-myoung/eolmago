package kr.eolmago.controller.view.review;

import kr.eolmago.domain.entity.deal.Deal;
import kr.eolmago.domain.entity.user.User;
import kr.eolmago.dto.view.deal.DealResponse;
import kr.eolmago.dto.view.review.ReviewResponse;
import kr.eolmago.repository.deal.DealRepository;
import kr.eolmago.repository.user.UserRepository;
import kr.eolmago.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

/**
 * Review 테스트 Controller
 */
@Controller
@RequestMapping("/test/review")
@RequiredArgsConstructor
public class ReviewTestController {

    private final ReviewService reviewService;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;

    /**
     * Review 테스트 메인 페이지
     */
    @GetMapping
    public String testPage(Model model) {
        List<ReviewResponse> reviews = reviewService.getAllReviews();
        model.addAttribute("reviews", reviews);
        return "pages/review/review-list";
    }

    /**
     * Review 생성 폼
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        // Deal 엔티티 조회 후 DTO로 변환
        List<DealResponse> deals = dealRepository.findAll().stream()
                .map(DealResponse::from)
                .toList();
        
        List<User> users = userRepository.findAll();
        
        model.addAttribute("deals", deals);
        model.addAttribute("users", users);
        return "pages/review/review-create";
    }

    /**
     * Review 생성 처리
     */
    @PostMapping("/create")
    public String create(
            @RequestParam Long dealId,
            @RequestParam UUID sellerId,
            @RequestParam UUID buyerId,
            @RequestParam int rating,
            @RequestParam String content,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ReviewResponse review = reviewService.createReview(
                dealId, sellerId, buyerId, rating, content
            );
            redirectAttributes.addFlashAttribute("message", "후기가 작성되었습니다");
            return "redirect:/test/review/" + review.reviewId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/test/review/create";
        }
    }

    /**
     * Review 상세 조회
     */
    @GetMapping("/{reviewId}")
    public String detail(@PathVariable Long reviewId, Model model) {
        try {
            ReviewResponse review = reviewService.getReview(reviewId);
            model.addAttribute("review", review);
            return "pages/review/review-detail";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/test/review";
        }
    }

    /**
     * Deal별 Review 목록
     */
    @GetMapping("/deal/{dealId}")
    public String listByDeal(@PathVariable Long dealId, Model model) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByDeal(dealId);
            model.addAttribute("reviews", reviews);
            model.addAttribute("dealId", dealId);
            return "pages/review/review-list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/test/review";
        }
    }

    /**
     * Review 삭제
     */
    @PostMapping("/{reviewId}/delete")
    public String delete(
            @PathVariable Long reviewId,
            @RequestParam UUID userId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reviewService.deleteReview(reviewId, userId);
            redirectAttributes.addFlashAttribute("message", "후기가 삭제되었습니다");
            return "redirect:/test/review";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/test/review/" + reviewId;
        }
    }
}

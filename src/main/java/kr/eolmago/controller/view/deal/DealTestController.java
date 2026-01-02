package kr.eolmago.controller.view.deal;

import kr.eolmago.domain.entity.auction.Auction;
import kr.eolmago.domain.entity.deal.enums.DealStatus;
import kr.eolmago.domain.entity.user.User;
import kr.eolmago.dto.view.deal.DealResponse;
import kr.eolmago.repository.auction.AuctionRepository;
import kr.eolmago.repository.user.UserRepository;
import kr.eolmago.service.deal.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

/**
 * Deal 테스트 Controller
 */
@Controller
@RequestMapping("/test/deal")
@RequiredArgsConstructor
public class DealTestController {

    private final DealService dealService;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    /**
     * Deal 테스트 메인 페이지
     */
    @GetMapping
    public String testPage(Model model) {
        List<DealResponse> deals = dealService.getAllDeals();
        model.addAttribute("deals", deals);
        return "pages/deal/deal-list";
    }

    /**
     * Deal 생성 폼
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        List<Auction> auctions = auctionRepository.findAll();
        List<User> users = userRepository.findAll();
        
        model.addAttribute("auctions", auctions);
        model.addAttribute("users", users);
        return "pages/deal/deal-create";
    }

    /**
     * Deal 생성 처리
     */
    @PostMapping("/create")
    public String create(
            @RequestParam UUID auctionId,
            @RequestParam UUID sellerId,
            @RequestParam UUID buyerId,
            @RequestParam Long finalPrice,
            RedirectAttributes redirectAttributes
    ) {
        try {
            DealResponse deal = dealService.createDeal(auctionId, sellerId, buyerId, finalPrice);
            redirectAttributes.addFlashAttribute("message", "거래가 생성되었습니다");
            return "redirect:/test/deal/" + deal.dealId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/test/deal/create";
        }
    }

    /**
     * Deal 상세 조회
     */
    @GetMapping("/{dealId}")
    public String detail(@PathVariable Long dealId, Model model) {
        try {
            DealResponse deal = dealService.getDeal(dealId);
            model.addAttribute("deal", deal);
            return "pages/deal/deal-detail";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/test/deal";
        }
    }

    /**
     * 상태별 거래 목록
     */
    @GetMapping("/status/{status}")
    public String listByStatus(@PathVariable DealStatus status, Model model) {
        List<DealResponse> deals = dealService.getDealsByStatus(status);
        model.addAttribute("deals", deals);
        model.addAttribute("status", status);
        return "pages/deal/deal-list";
    }

    /**
     * 판매자 확인
     */
    @PostMapping("/{dealId}/confirm-seller")
    public String confirmBySeller(
            @PathVariable Long dealId,
            @RequestParam UUID userId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dealService.confirmBySeller(dealId, userId);
            redirectAttributes.addFlashAttribute("message", "판매자 확인이 완료되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + dealId;
    }

    /**
     * 구매자 확인
     */
    @PostMapping("/{dealId}/confirm-buyer")
    public String confirmByBuyer(
            @PathVariable Long dealId,
            @RequestParam UUID userId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dealService.confirmByBuyer(dealId, userId);
            redirectAttributes.addFlashAttribute("message", "구매자 확인이 완료되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + dealId;
    }

    /**
     * 거래 완료
     */
    @PostMapping("/{dealId}/complete")
    public String complete(
            @PathVariable Long dealId,
            @RequestParam UUID userId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dealService.completeDeal(dealId, userId);
            redirectAttributes.addFlashAttribute("message", "거래가 완료되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + dealId;
    }

    /**
     * 거래 종료 (취소)
     */
    @PostMapping("/{dealId}/terminate")
    public String terminate(
            @PathVariable Long dealId,
            @RequestParam UUID userId,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dealService.terminateDeal(dealId, userId, reason);
            redirectAttributes.addFlashAttribute("message", "거래가 종료되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + dealId;
    }

    /**
     * 배송 시작
     */
    @PostMapping("/{dealId}/ship")
    public String ship(
            @PathVariable Long dealId,
            @RequestParam UUID userId,
            @RequestParam String shippingNumber,
            @RequestParam String carrierCode,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dealService.startShipping(dealId, userId, shippingNumber, carrierCode);
            redirectAttributes.addFlashAttribute("message", "배송이 시작되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + dealId;
    }
}

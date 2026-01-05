package kr.eolmago.controller.view.deal;

import kr.eolmago.domain.entity.auction.Auction;
import kr.eolmago.domain.entity.deal.enums.DealStatus;
import kr.eolmago.domain.entity.user.User;
import kr.eolmago.dto.view.deal.DealResponse;
import kr.eolmago.repository.auction.AuctionRepository;
import kr.eolmago.repository.user.UserRepository;
import kr.eolmago.service.deal.DealService;
import kr.eolmago.service.deal.DealPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final DealPdfService pdfService;
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
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        DealResponse deal = dealService.getDeal(id);
        model.addAttribute("deal", deal);
        return "pages/deal/deal-detail";
    }

    /**
     * 상태별 거래 목록
     */
    @GetMapping("/status/{status}")
    public String listByStatus(@PathVariable String status, Model model) {
        try {
            DealStatus dealStatus = DealStatus.valueOf(status);
            List<DealResponse> deals = dealService.getDealsByStatus(dealStatus);
            model.addAttribute("deals", deals);
            model.addAttribute("filterStatus", status);
            return "pages/deal/deal-list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "잘못된 상태값입니다: " + status);
            return "redirect:/test/deal";
        }
    }

    // ========================================
    // 상태 전환 엔드포인트
    // ========================================

    /**
     * 판매자 확인
     */
    @PostMapping("/{id}/confirm-seller")
    public String confirmSeller(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dealService.confirmBySeller(id);
            redirectAttributes.addFlashAttribute("message", "판매자 확인이 완료되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + id;
    }

    /**
     * 구매자 확인
     */
    @PostMapping("/{id}/confirm-buyer")
    public String confirmBuyer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dealService.confirmByBuyer(id);
            redirectAttributes.addFlashAttribute("message", "구매자 확인이 완료되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + id;
    }

    /**
     * 거래 완료
     */
    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dealService.completeDeal(id);
            redirectAttributes.addFlashAttribute("message", "거래가 완료되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + id;
    }

    /**
     * 거래 취소
     */
    @PostMapping("/{id}/terminate")
    public String terminate(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dealService.terminateDeal(id, reason);
            redirectAttributes.addFlashAttribute("message", "거래가 취소되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + id;
    }

    /**
     * 거래 만료
     */
    @PostMapping("/{id}/expire")
    public String expire(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dealService.expireDeal(id);
            redirectAttributes.addFlashAttribute("message", "거래가 만료되었습니다");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/test/deal/" + id;
    }

    // ========================================
    // PDF 다운로드
    // ========================================

    /**
     * 거래확정서 PDF 다운로드
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = pdfService.generateDealConfirmationPdf(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "deal-confirmation-" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

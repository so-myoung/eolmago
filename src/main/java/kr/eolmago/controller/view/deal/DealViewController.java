package kr.eolmago.controller.view.deal;

import kr.eolmago.controller.view.support.SellerViewModelSupport;
import kr.eolmago.dto.api.deal.response.BuyerDealDetailResponse;
import kr.eolmago.dto.api.deal.response.SellerDealDetailResponse;
import kr.eolmago.global.security.CustomUserDetails;
import kr.eolmago.service.deal.BuyerDealService;
import kr.eolmago.service.deal.SellerDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class DealViewController {

    // seller
    private final SellerDealService sellerDealService;
    private final SellerViewModelSupport sellerViewModelSupport;

    // buyer
    private final BuyerDealService buyerDealService;


    @GetMapping("/seller/deals")
    public String sellerDeals(Model model) {
        sellerViewModelSupport.applyCommonModel(model);
        model.addAttribute("pageTitle", "판매 거래 관리");
        model.addAttribute("pageSubtitle", "판매한 상품의 거래 진행 상황을 확인하세요.");
        model.addAttribute("role", "SELLER");
        model.addAttribute("dealListApi", "/api/seller/deals");
        model.addAttribute("dealDetailBasePath", "/seller/deals");

        return "pages/deal/deals";
    }

    @GetMapping("/seller/deals/{dealId}")
    public String sellerDealDetail(@PathVariable Long dealId,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {

        UUID sellerId = userDetails.getUserId();
        SellerDealDetailResponse deal = sellerDealService.getDealDetail(dealId, sellerId);

        sellerViewModelSupport.applyCommonModel(model);
        model.addAttribute("dealId", dealId);
        model.addAttribute("role", "SELLER");
        model.addAttribute("deal", deal);

        return "pages/deal/deal-detail";
    }

    // =========================
    // Buyer
    // =========================
    @GetMapping("/buyer/deals")
    public String buyerDeals(Model model) {

        sellerViewModelSupport.applyCommonModel(model);

        model.addAttribute("pageTitle", "구매 거래 관리");
        model.addAttribute("pageSubtitle", "구매한 상품의 거래 진행 상황을 확인하세요.");
        model.addAttribute("role", "BUYER");
        model.addAttribute("dealListApi", "/api/buyer/deals");
        model.addAttribute("dealDetailBasePath", "/buyer/deals");

        return "pages/deal/deals";
    }

    @GetMapping("/buyer/deals/{dealId}")
    public String buyerDealDetail(@PathVariable Long dealId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {

        UUID buyerId = userDetails.getUserId();
        BuyerDealDetailResponse deal = buyerDealService.getDealDetail(dealId, buyerId);

        model.addAttribute("dealId", dealId);
        model.addAttribute("role", "BUYER");
        model.addAttribute("deal", deal);

        return "pages/deal/deal-detail";
    }
}

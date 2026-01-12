package kr.eolmago.controller.api.deal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.eolmago.dto.api.deal.request.CreateDealFromAuctionRequest;
import kr.eolmago.dto.api.deal.response.DealCreationResponse;
import kr.eolmago.service.deal.DealCreationService;
import kr.eolmago.service.deal.DealPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Deal 생성 API 컨트롤러
 */
@Tag(name = "Deal Creation", description = "거래 생성 API")
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
public class DealCreationApiController {

    private final DealCreationService dealCreationService;
    private final DealPdfService pdfService;

    @Operation(
            summary = "경매 종료 후 거래 생성",
            description = "ENDED_SOLD 상태의 경매로부터 거래를 자동 생성합니다"
    )
    @PostMapping("/from-auction")
    public ResponseEntity<DealCreationResponse> createDealFromAuction(
            @Valid @RequestBody CreateDealFromAuctionRequest request
    ) {
        DealCreationResponse response = dealCreationService.createDealFromAuction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "특정 경매의 거래 생성",
            description = "경로 파라미터로 auctionId를 받아 거래를 생성합니다"
    )
    @PostMapping("/auctions/{auctionId}")
    public ResponseEntity<DealCreationResponse> createDealFromAuctionPath(
            @PathVariable UUID auctionId,
            @Valid @RequestBody CreateDealFromAuctionRequest request
    ) {
        DealCreationResponse response = dealCreationService.createDealFromAuctionPath(auctionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "경매의 거래 생성 여부 확인",
            description = "해당 경매로 이미 거래가 생성되었는지 확인합니다"
    )
    @GetMapping("/check/auction/{auctionId}")
    public ResponseEntity<Boolean> checkDealExists(
            @PathVariable UUID auctionId
    ) {
        boolean exists = dealCreationService.isDealExistsForAuction(auctionId);
        return ResponseEntity.ok(exists);
    }

    // ========================================
    // PDF 다운로드
    // ========================================

    /**
     * 거래확정서 PDF 다운로드
     */
    @Operation(summary = "거래확정서 PDF 다운로드", description = "완료된 거래의 거래확정서를 PDF로 다운로드합니다")
    @GetMapping("/{dealId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long dealId) {
        byte[] pdfBytes = pdfService.generateDealConfirmationPdf(dealId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "deal-confirmation-" + dealId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}

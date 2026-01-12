package kr.eolmago.service.deal;

import kr.eolmago.domain.entity.auction.AuctionImage;
import kr.eolmago.domain.entity.deal.Deal;
import kr.eolmago.dto.api.deal.response.SellerDealListResponse;
import kr.eolmago.global.exception.BusinessException;
import kr.eolmago.global.exception.ErrorCode;
import kr.eolmago.repository.auction.AuctionImageRepository;
import kr.eolmago.repository.deal.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 판매자 거래 조회/처리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerDealService {

    private final DealRepository dealRepository;
    private final AuctionImageRepository auctionImageRepository;

    /**
     * 판매자의 모든 거래 조회
     */
    public SellerDealListResponse getSellerDeals(UUID sellerId) {
        List<Deal> deals = dealRepository.findBySeller_UserId(sellerId);

        List<SellerDealListResponse.DealDto> dealDtos = deals.stream()
                .map(this::toDealDto)
                .toList();

        return new SellerDealListResponse(
                sellerId,
                deals.size(),
                dealDtos
        );
    }

    /**
     * 판매자의 특정 거래 상세 조회 (권한 검증)
     */
    public SellerDealListResponse.DealDto getDealDetail(Long dealId, UUID sellerId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEAL_NOT_FOUND));

        // 권한 검증: 내가 판매자인 거래인지 확인
        if (!deal.getSeller().getUserId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.DEAL_UNAUTHORIZED);
        }

        return toDealDto(deal);
    }

    /**
     * 판매자 측 거래 확정
     * 컨트롤러에서 호출하는 메서드 시그니처를 맞추기 위한 구현.
     */
    @Transactional
    public void confirmDeal(Long dealId, UUID sellerId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEAL_NOT_FOUND));

        // 권한 검증
        if (!deal.getSeller().getUserId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.DEAL_UNAUTHORIZED);
        }

        // 도메인 로직 위임
        deal.confirmBySeller();
    }

    /**
     * Deal 엔티티 → SellerDealListResponse.DealDto 변환
     * - 제목: auctions.title
     * - 금액: deals.final_price
     * - 썸네일: 경매 아이템의 첫 번째 AuctionImage
     */
    private SellerDealListResponse.DealDto toDealDto(Deal deal) {
        String createdAt = deal.getCreatedAt() != null
                ? deal.getCreatedAt().toString()
                : null;

        String auctionTitle = null;
        String thumbnailUrl = null;

        if (deal.getAuction() != null) {
            auctionTitle = deal.getAuction().getTitle();

            if (deal.getAuction().getAuctionItem() != null) {
                List<AuctionImage> images =
                        auctionImageRepository.findByAuctionItemOrderByDisplayOrder(
                                deal.getAuction().getAuctionItem()
                        );

                if (!images.isEmpty()) {
                    thumbnailUrl = images.get(0).getImageUrl();
                }
            }
        }

        return new SellerDealListResponse.DealDto(
                deal.getDealId(),
                deal.getFinalPrice(),
                deal.getStatus().name(),
                createdAt,
                auctionTitle,
                thumbnailUrl
        );
    }
}

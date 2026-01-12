package kr.eolmago.service.deal;

import kr.eolmago.domain.entity.auction.AuctionImage;
import kr.eolmago.domain.entity.deal.Deal;
import kr.eolmago.dto.api.deal.response.BuyerDealListResponse;
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
 * 구매자 거래 조회/처리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerDealService {

    private final DealRepository dealRepository;
    private final AuctionImageRepository auctionImageRepository;

    /**
     * 구매자의 모든 거래 조회
     */
    public BuyerDealListResponse getBuyerDeals(UUID buyerId) {
        List<Deal> deals = dealRepository.findByBuyer_UserId(buyerId);

        List<BuyerDealListResponse.DealDto> dealDtos = deals.stream()
                .map(this::toDealDto)
                .toList();

        return new BuyerDealListResponse(
                buyerId,
                deals.size(),
                dealDtos
        );
    }

    /**
     * 구매자의 특정 거래 상세 조회 (권한 검증)
     */
    public BuyerDealListResponse.DealDto getDealDetail(Long dealId, UUID buyerId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEAL_NOT_FOUND));

        // 권한 검증: 내가 구매자인 거래인지 확인
        if (!deal.getBuyer().getUserId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.DEAL_UNAUTHORIZED);
        }

        return toDealDto(deal);
    }

    /**
     * 구매자 측 거래 확정
     * 컨트롤러에서 호출하는 메서드 시그니처를 맞추기 위한 구현.
     * 실제 상태 변경은 Deal 엔티티의 도메인 메서드에 위임한다.
     */
    @Transactional
    public void confirmDeal(Long dealId, UUID buyerId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEAL_NOT_FOUND));

        // 권한 검증
        if (!deal.getBuyer().getUserId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.DEAL_UNAUTHORIZED);
        }

        // 도메인 로직 위임 (둘 다 확인되면 CONFIRMED 로 전환)
        deal.confirmByBuyer();
    }

    /**
     * Deal 엔티티 → BuyerDealListResponse.DealDto 변환
     * - 제목: auctions.title
     * - 금액: deals.final_price
     * - 썸네일: 경매 아이템의 첫 번째 AuctionImage
     */
    private BuyerDealListResponse.DealDto toDealDto(Deal deal) {
        String createdAt = deal.getCreatedAt() != null
                ? deal.getCreatedAt().toString()
                : null;

        String auctionTitle = null;
        String thumbnailUrl = null;

        if (deal.getAuction() != null) {
            // 경매 제목
            auctionTitle = deal.getAuction().getTitle();

            // 경매 아이템 → 이미지
            if (deal.getAuction().getAuctionItem() != null) {
                List<AuctionImage> images =
                        auctionImageRepository.findByAuctionItemOrderByDisplayOrder(
                                deal.getAuction().getAuctionItem()
                        );

                // 여러 장이면 첫 번째만 사용
                if (!images.isEmpty()) {
                    thumbnailUrl = images.get(0).getImageUrl();
                }
            }
        }

        return new BuyerDealListResponse.DealDto(
                deal.getDealId(),
                deal.getFinalPrice(),
                deal.getStatus().name(),
                createdAt,
                auctionTitle,
                thumbnailUrl
        );
    }
}

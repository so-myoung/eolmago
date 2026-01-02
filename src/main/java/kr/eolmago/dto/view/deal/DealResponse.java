package kr.eolmago.dto.view.deal;

import kr.eolmago.domain.entity.deal.Deal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Deal 응답 DTO
 */
public record DealResponse(
    Long dealId,
    UUID auctionId,
    String auctionTitle,
    UUID sellerId,
    UUID buyerId,
    Long finalPrice,
    String status,
    OffsetDateTime confirmByAt,
    OffsetDateTime sellerConfirmedAt,
    OffsetDateTime buyerConfirmedAt,
    OffsetDateTime confirmedAt,
    OffsetDateTime completedAt,
    OffsetDateTime terminatedAt,
    String terminationReason,
    OffsetDateTime expiredAt,
    OffsetDateTime disputedAt,
    OffsetDateTime shipByAt,
    OffsetDateTime shippedAt,
    String shippingNumber,
    String shippingCarrierCode,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    /**
     * Entity → DTO 변환
     */
    public static DealResponse from(Deal deal) {
        return new DealResponse(
            deal.getDealId(),
            deal.getAuction().getAuctionId(),
            deal.getAuction().getTitle(),
            deal.getSeller().getUserId(),
            deal.getBuyer().getUserId(),
            deal.getFinalPrice(),
            deal.getStatus().name(),
            deal.getConfirmByAt(),
            deal.getSellerConfirmedAt(),
            deal.getBuyerConfirmedAt(),
            deal.getConfirmedAt(),
            deal.getCompletedAt(),
            deal.getTerminatedAt(),
            deal.getTerminationReason(),
            deal.getExpiredAt(),
            deal.getDisputedAt(),
            deal.getShipByAt(),
            deal.getShippedAt(),
            deal.getShippingNumber(),
            deal.getShippingCarrierCode(),
            deal.getCreatedAt(),
            deal.getUpdatedAt()
        );
    }
}

package kr.eolmago.repository.deal;

import kr.eolmago.dto.api.deal.response.DealDetailDto;
import kr.eolmago.dto.api.deal.response.DealPdfDto;

import java.util.Optional;

/**
 * Deal Custom Repository
 */
public interface DealRepositoryCustom {

    // 거래 상세 조회
    Optional<DealDetailDto> findDetailById(Long dealId);
    /**
     * PDF 생성을 위한 Deal 정보 조회
     *
     * Deal + Auction + AuctionItem + User + UserProfile + SocialLogin을 조인하여
     * 한 번의 쿼리로 필요한 모든 데이터 조회
     *
     * @param dealId 거래 ID
     * @return PDF 생성용 DTO
     */
    Optional<DealPdfDto> findPdfDataByDealId(Long dealId);
}

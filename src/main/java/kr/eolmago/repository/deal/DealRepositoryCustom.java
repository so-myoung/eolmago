package kr.eolmago.repository.deal;

import kr.eolmago.domain.entity.deal.Deal;
import kr.eolmago.domain.entity.deal.enums.DealStatus;
import kr.eolmago.dto.api.deal.response.DealDetailDto;
import kr.eolmago.dto.api.deal.response.DealPdfDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealRepositoryCustom {

    // 거래 상세 조회
    Optional<DealDetailDto> findDetailById(Long dealId);

    // PDF 생성을 위한 Deal 정보 조회
    Optional<DealPdfDto> findPdfDataByDealId(Long dealId);

    // 자동 완료 가능한 거래 조회
    List<Deal> findCompletableDeals(OffsetDateTime shippedAtThreshold);

    long countBySellerIdAndStatus(UUID sellerId, DealStatus status);
}

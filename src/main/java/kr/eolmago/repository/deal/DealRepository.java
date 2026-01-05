package kr.eolmago.repository.deal;

import kr.eolmago.domain.entity.deal.Deal;
import kr.eolmago.domain.entity.deal.enums.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Deal Repository
 * Spring Data JPA 메서드 네이밍 사용 (팀 스타일)
 */
@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    
    /**
     * 상태별 거래 목록
     */
    List<Deal> findByStatus(DealStatus status);
    
    /**
     * 판매자 ID로 거래 목록
     */
    List<Deal> findBySeller_UserId(UUID sellerId);
    
    /**
     * 구매자 ID로 거래 목록
     */
    List<Deal> findByBuyer_UserId(UUID buyerId);
    
    /**
     * 만료된 거래 목록
     */
    List<Deal> findByStatusAndConfirmByAtBefore(DealStatus status, OffsetDateTime now);
}

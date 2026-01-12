package kr.eolmago.repository.deal;

import kr.eolmago.domain.entity.deal.Deal;
import kr.eolmago.domain.entity.deal.DealDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealDocumentRepository extends JpaRepository<DealDocument, Long> {

    /**
     * Deal로 문서 조회
     *
     * @param deal Deal 엔티티
     * @return 거래확정서 문서 (Optional)
     */
    Optional<DealDocument> findByDeal(Deal deal);

    /**
     * Deal로 문서 존재 여부 확인
     *
     * @param deal Deal 엔티티
     * @return 존재 여부
     */
    boolean existsByDeal(Deal deal);


}

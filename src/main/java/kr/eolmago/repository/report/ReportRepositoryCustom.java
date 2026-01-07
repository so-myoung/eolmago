package kr.eolmago.repository.report;

import kr.eolmago.domain.entity.auction.Auction;
import kr.eolmago.domain.entity.report.Report;
import kr.eolmago.domain.entity.report.enums.ReportReason;
import kr.eolmago.domain.entity.report.enums.ReportStatus;
import kr.eolmago.domain.entity.user.User;
import kr.eolmago.dto.api.report.request.ReportSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface ReportRepositoryCustom {

    /**
     * 상태와 생성일자로 신고를 조회합니다.
     * 관리자 대시보드에서 미처리 신고를 최신순으로 조회할 때 사용합니다.
     */
    Page<Report> findByStatusOrderByCreatedAt(ReportStatus status, Pageable pageable);

    /**
     * 특정 기간 내 생성된 신고를 조회합니다.
     * 신고 통계 조회 시 사용합니다.
     */
    List<Report> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate);

    /**
     * 신고 사유별로 신고 건수를 집계합니다.
     * 신고 분석 대시보드에서 사용합니다.
     */
    Map<ReportReason, Long> countByReason();

    /**
     * 특정 사용자에 대한 신고 건수를 조회합니다.
     * 사용자가 몇 번이나 신고를 받았는지 확인할 때 사용합니다.
     */
    long countByReportedUser(User reportedUser);

    /**
     * 특정 경매에 대한 신고를 모두 조회합니다.
     * 경매 삭제 전 관련 신고를 확인할 때 사용합니다.
     */
    List<Report> findByAuctionWithDetails(Auction auction);

    /**
     * 동적 조건으로 신고를 검색합니다.
     * 관리자의 고급 검색 기능에서 사용합니다.
     */
    Page<Report> searchReports(ReportSearchCondition condition, Pageable pageable);
}

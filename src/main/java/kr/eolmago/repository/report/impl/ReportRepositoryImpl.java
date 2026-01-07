package kr.eolmago.repository.report.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.eolmago.domain.entity.auction.Auction;
import kr.eolmago.domain.entity.report.QReport;
import kr.eolmago.domain.entity.report.Report;
import kr.eolmago.domain.entity.report.enums.ReportReason;
import kr.eolmago.domain.entity.report.enums.ReportStatus;
import kr.eolmago.domain.entity.report.enums.ReportTargetType;
import kr.eolmago.domain.entity.user.User;
import kr.eolmago.repository.report.ReportRepositoryCustom;
import kr.eolmago.dto.api.report.request.ReportSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Report Repository Custom 구현체
 * QueryDSL을 사용하여 복잡한 동적 쿼리를 처리합니다.
 */
@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QReport report = QReport.report;

    /**
     * 특정 상태의 신고를 생성일자 역순으로 조회합니다.
     * 관리자 대시보드에서 접수된 신고를 최신순으로 조회할 때 사용됩니다.
     *
     * @param status 신고 상태 (PENDING, RESOLVED, REJECTED)
     * @param pageable 페이징 정보
     * @return 상태별 신고 목록 (페이징 적용)
     */
    @Override
    public Page<Report> findByStatusOrderByCreatedAt(ReportStatus status, Pageable pageable) {
        List<Report> content = queryFactory
                .selectFrom(report)
                .where(report.status.eq(status))
                .orderBy(report.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(report)
                .where(report.status.eq(status))
                .fetch()
                .size();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 특정 기간 내 생성된 신고를 조회합니다.
     * 신고 통계, 월별/주별 분석 대시보드에서 사용됩니다.
     *
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 기간 내 생성된 신고 목록 (생성일 역순)
     */
    @Override
    public List<Report> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate) {
        return queryFactory
                .selectFrom(report)
                .where(
                        report.createdAt.goe(startDate),
                        report.createdAt.loe(endDate)
                )
                .orderBy(report.createdAt.desc())
                .fetch();
    }

    /**
     * 신고 사유별 신고 건수를 집계합니다.
     * 신고 분석 대시보드에서 어떤 사유의 신고가 가장 많은지 확인할 때 사용됩니다.
     *
     * @return 사유별 신고 건수 맵 (예: FRAUD_SUSPECT -> 45건)
     */
    @Override
    public Map<ReportReason, Long> countByReason() {
        return queryFactory
                .select(report.reason, report.count())
                .from(report)
                .groupBy(report.reason)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(report.reason),
                        tuple -> tuple.get(report.count())
                ));
    }

    /**
     * 특정 사용자가 받은 신고 총 건수를 조회합니다.
     * 사용자의 신뢰도 점수 계산이나 자동 제재 판단 시 사용됩니다.
     * (예: 신고 3회 이상 시 자동 정지)
     *
     * @param reportedUser 피신고자
     * @return 해당 사용자에 대한 신고 총 건수
     */
    @Override
    public long countByReportedUser(User reportedUser) {
        return queryFactory
                .selectFrom(report)
                .where(report.reportedUser.eq(reportedUser))
                .fetch()
                .size();
    }

    /**
     * 특정 경매에 대한 모든 신고를 관계 데이터와 함께 조회합니다.
     * Fetch Join을 사용하여 N+1 문제를 방지합니다.
     * 경매 삭제 시 관련 신고를 확인하거나, 경매의 신뢰도 평가에 사용됩니다.
     *
     * @param auction 조회 대상 경매
     * @return 해당 경매에 대한 모든 신고 (reporter, reportedUser 즉시 로딩)
     */
    @Override
    public List<Report> findByAuctionWithDetails(Auction auction) {
        return queryFactory
                .selectFrom(report)
                .leftJoin(report.reporter).fetchJoin()
                .leftJoin(report.reportedUser).fetchJoin()
                .where(report.auction.eq(auction))
                .fetch();
    }

    /**
     * 동적 조건으로 신고를 검색합니다.
     * 관리자의 고급 검색 기능에서 여러 조건을 조합하여 신고를 검색할 때 사용됩니다.
     * (예: PENDING 상태 + FRAUD_SUSPECT 사유 + 특정 기간)
     *
     * @param condition 검색 조건 (상태, 사유, 타입, 사용자 ID, 기간 등)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 신고 목록 (페이징 적용)
     */
    @Override
    public Page<Report> searchReports(ReportSearchCondition condition, Pageable pageable) {
        List<Report> content = queryFactory
                .selectFrom(report)
                .where(
                        statusEq(condition.status()),
                        reasonEq(condition.reason()),
                        typeEq(condition.type()),
                        reportedUserEq(condition.reportedUserId()),
                        createdAtBetween(condition.startDate(), condition.endDate())
                )
                .orderBy(report.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(report)
                .where(
                        statusEq(condition.status()),
                        reasonEq(condition.reason()),
                        typeEq(condition.type()),
                        reportedUserEq(condition.reportedUserId()),
                        createdAtBetween(condition.startDate(), condition.endDate())
                )
                .fetch()
                .size();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 상태 조건 헬퍼 메서드
     * null이면 조건을 무시합니다.
     */
    private BooleanExpression statusEq(ReportStatus status) {
        return status != null ? report.status.eq(status) : null;
    }

    /**
     * 사유 조건 헬퍼 메서드
     * null이면 조건을 무시합니다.
     */
    private BooleanExpression reasonEq(ReportReason reason) {
        return reason != null ? report.reason.eq(reason) : null;
    }

    /**
     * 타입 조건 헬퍼 메서드
     * null이면 조건을 무시합니다.
     */
    private BooleanExpression typeEq(ReportTargetType type) {
        return type != null ? report.type.eq(type) : null;
    }

    /**
     * 피신고자 조건 헬퍼 메서드
     * null이면 조건을 무시합니다.
     */
    private BooleanExpression reportedUserEq(UUID reportedUserId) {
        return reportedUserId != null ? report.reportedUser.userId.eq(reportedUserId) : null;
    }

    /**
     * 생성일자 범위 조건 헬퍼 메서드
     * 시작일만 있으면 그 이후, 종료일만 있으면 그 이전, 둘 다 있으면 그 사이
     * null이면 조건을 무시합니다.
     */
    private BooleanExpression createdAtBetween(OffsetDateTime startDate, OffsetDateTime endDate) {
        if (startDate != null && endDate != null) {
            return report.createdAt.between(startDate, endDate);
        } else if (startDate != null) {
            return report.createdAt.goe(startDate);
        } else if (endDate != null) {
            return report.createdAt.loe(endDate);
        }
        return null;
    }
}

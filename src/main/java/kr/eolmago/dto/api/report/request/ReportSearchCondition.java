package kr.eolmago.dto.api.report.request;

import kr.eolmago.domain.entity.report.enums.ReportReason;
import kr.eolmago.domain.entity.report.enums.ReportStatus;
import kr.eolmago.domain.entity.report.enums.ReportTargetType;

import java.time.OffsetDateTime;
import java.util.UUID;

// 관리자 페이지에서 조건에 맞는 신고 목록을 검색하기 위한 필터링 조건
public record ReportSearchCondition(
    ReportStatus status,
    ReportReason reason,
    ReportTargetType type,
    UUID reportedUserId,
    OffsetDateTime startDate,
    OffsetDateTime endDate
) {}

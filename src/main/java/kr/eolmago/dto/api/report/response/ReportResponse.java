package kr.eolmago.dto.api.report.response;

import kr.eolmago.domain.entity.report.Report;
import kr.eolmago.domain.entity.report.enums.ReportAction;
import kr.eolmago.domain.entity.report.enums.ReportReason;
import kr.eolmago.domain.entity.report.enums.ReportStatus;
import kr.eolmago.domain.entity.report.enums.ReportTargetType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 신고 조회 응답 DTO
 */
public record ReportResponse(
    Long reportId,
    UUID reporterId,
    String reporterNickname,
    UUID reportedUserId,
    String reportedUserNickname,
    UUID auctionId,
    ReportTargetType type,
    ReportReason reason,
    String description,
    ReportStatus status,
    ReportAction action,
    String actionMemo,
    OffsetDateTime createdAt,
    OffsetDateTime resolvedAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
            report.getReportId(),
            report.getReporter().getUserId(),
            report.getReporter().getUserProfile().getNickname(),
            report.getReportedUser().getUserId(),
            report.getReportedUser().getUserProfile().getNickname(),
            report.getAuction().getAuctionId(),
            report.getType(),
            report.getReason(),
            report.getDescription(),
            report.getStatus(),
            report.getAction(),
            report.getActionMemo(),
            report.getCreatedAt(),
            report.getResolvedAt()
        );
    }
}

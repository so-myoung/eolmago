package kr.eolmago.dto.api.report.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.eolmago.domain.entity.report.enums.ReportReason;
import kr.eolmago.domain.entity.report.enums.ReportTargetType;

import java.util.UUID;

/**
 * 신고 생성 요청 DTO
 */
public record CreateReportRequest(
    @NotNull(message = "피신고자 ID는 필수입니다")
    UUID reportedUserId,

    @NotNull(message = "경매 ID는 필수입니다")
    UUID auctionId,

    @NotNull(message = "신고 타입은 필수입니다")
    ReportTargetType type,

    @NotNull(message = "신고 사유는 필수입니다")
    ReportReason reason,

    @NotBlank(message = "신고 상세 내용은 필수입니다")
    @Size(min = 10, max = 1000, message = "신고 내용은 10자 이상 1000자 이하여야 합니다")
    String description
) {}

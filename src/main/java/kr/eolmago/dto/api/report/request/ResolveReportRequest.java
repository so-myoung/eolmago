package kr.eolmago.dto.api.report.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.eolmago.domain.entity.report.enums.ReportAction;

/**
 * 신고 처리 요청 DTO
 */
public record ResolveReportRequest(
    @NotNull(message = "조치 유형은 필수입니다")
    ReportAction action,

    @NotBlank(message = "처리 메모는 필수입니다")
    @Size(min = 10, max = 500, message = "처리 메모는 10자 이상 500자 이하여야 합니다")
    String memo
) {}

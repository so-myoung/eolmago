package kr.eolmago.service.report;

import kr.eolmago.domain.entity.auction.Auction;
import kr.eolmago.domain.entity.report.Report;
import kr.eolmago.domain.entity.report.enums.ReportAction;
import kr.eolmago.domain.entity.report.enums.ReportStatus;
import kr.eolmago.domain.entity.user.User;
import kr.eolmago.dto.api.report.request.CreateReportRequest;
import kr.eolmago.dto.api.report.response.ReportResponse;
import kr.eolmago.repository.auction.AuctionRepository;
import kr.eolmago.repository.report.ReportRepository;
import kr.eolmago.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;

    // 신고 접수
    public Long createReport(UUID reporterId, CreateReportRequest request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("신고자를 찾을 수 없습니다"));
        User reportedUser = userRepository.findById(request.reportedUserId())
                .orElseThrow(() -> new IllegalArgumentException("피신고자를 찾을 수 없습니다"));
        Auction auction = auctionRepository.findById(request.auctionId())
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다"));

        Report report = Report.create(
                reporter,
                reportedUser,
                auction,
                request.type(),
                request.reason(),
                request.description()
        );

        return reportRepository.save(report).getReportId();
    }

    // 내가 한 신고 목록 조회
    @Transactional(readOnly = true)
    public Page<ReportResponse> getMyReports(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return reportRepository.findByReporter(user, pageable)
                .map(ReportResponse::from);
    }

    // 신고 상세 조회
    @Transactional(readOnly = true)
    public ReportResponse getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다"));
        return ReportResponse.from(report);
    }

    // 신고 목록 전체 조회 (관리자)
    @Transactional(readOnly = true)
    public Page<ReportResponse> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable)
                .map(ReportResponse::from);
    }

    // 신고 검토 (관리자)
    public void reviewReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다"));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("접수된 신고만 검토할 수 있습니다");
        }

        report.updateStatus(ReportStatus.UNDER_REVIEW);
        reportRepository.save(report);
    }

    // 신고 처리-조치 (관리자)
    public void processReport(Long reportId, ReportAction action, String actionMemo) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다"));

        if (report.getStatus() != ReportStatus.UNDER_REVIEW) {
            throw new IllegalStateException("검토 중인 신고만 처리할 수 있습니다");
        }

        // ToDo User 상태 변경 + UserPenalty
        report.updateStatus(ReportStatus.RESOLVED);
        report.updateResolvedAt(OffsetDateTime.now());
        report.updateAction(action);
        report.UpdateActionMemo(actionMemo);


        reportRepository.save(report);

        // ToDo 신고자에게 처리 완료 알림
    }

    // 신고 기각 (관리자)
    public void rejectReport(Long reportId, String memo) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다"));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("접수된 신고만 기각할 수 있습니다");
        }
        report.updateStatus(ReportStatus.REJECTED);
        report.UpdateActionMemo(memo);
        report.updateResolvedAt(OffsetDateTime.now());
    }
}

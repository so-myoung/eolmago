package kr.eolmago.repository.report;

import kr.eolmago.domain.entity.auction.Auction;
import kr.eolmago.domain.entity.report.Report;
import kr.eolmago.domain.entity.report.enums.ReportStatus;
import kr.eolmago.domain.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {

    Page<Report> findByReporter(User reporter, Pageable pageable);

    Page<Report> findByReportedUser(User reportedUser, Pageable pageable);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    List<Report> findByAuction(Auction auction);
}

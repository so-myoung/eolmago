package kr.eolmago.scheduler;

import kr.eolmago.domain.entity.user.User;
import kr.eolmago.domain.entity.user.UserPenalty;
import kr.eolmago.domain.entity.user.enums.UserStatus;
import kr.eolmago.repository.user.UserPenaltyRepository;
import kr.eolmago.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPenaltyScheduler {

    private final UserPenaltyRepository userPenaltyRepository;
    private final UserRepository userRepository;

    /**
     * 매일 자정(00:00:00)에 실행되어 제재 기간이 만료된 사용자의 상태를 ACTIVE로 변경합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void releaseExpiredPenalties() {
        log.info("Starting scheduled task: releaseExpiredPenalties");

        OffsetDateTime now = OffsetDateTime.now();
        List<UserPenalty> expiredPenalties = userPenaltyRepository.findExpiredPenalties(now);

        for (UserPenalty penalty : expiredPenalties) {
            User user = penalty.getUser();
            // 현재 상태가 SUSPENDED인 경우에만 ACTIVE로 변경 (BANNED 등 다른 상태일 수 있음)
            if (user.getStatus() == UserStatus.SUSPENDED) {
                user.updateStatus(UserStatus.ACTIVE);
                log.info("Released penalty for user: {}", user.getUserId());
            }
        }

        log.info("Finished scheduled task: releaseExpiredPenalties. Released count: {}", expiredPenalties.size());
    }
}

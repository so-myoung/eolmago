package kr.eolmago.repository.user;

import kr.eolmago.domain.entity.user.User;
import kr.eolmago.domain.entity.user.UserPenalty;

import java.time.OffsetDateTime;
import java.util.List;

public interface UserPenaltyRepositoryCustom {
    List<UserPenalty> findExpiredPenalties(OffsetDateTime now);
    boolean existsActivePenalty(User user, OffsetDateTime now);
}

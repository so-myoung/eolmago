package kr.eolmago.repository.user.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.eolmago.domain.entity.user.UserPenalty;
import kr.eolmago.domain.entity.user.enums.PenaltyType;
import kr.eolmago.repository.user.UserPenaltyRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

import static kr.eolmago.domain.entity.user.QUserPenalty.userPenalty;

@Repository
@RequiredArgsConstructor
public class UserPenaltyRepositoryImpl implements UserPenaltyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserPenalty> findExpiredPenalties(OffsetDateTime now) {
        return queryFactory
                .selectFrom(userPenalty)
                .join(userPenalty.user).fetchJoin()
                .where(
                        userPenalty.type.eq(PenaltyType.SUSPENDED),
                        userPenalty.expiresAt.isNotNull(),
                        userPenalty.expiresAt.before(now)
                )
                .fetch();
    }
}

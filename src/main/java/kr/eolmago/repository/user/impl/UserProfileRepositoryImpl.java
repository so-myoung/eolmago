package kr.eolmago.repository.user.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kr.eolmago.domain.entity.user.UserProfile;
import kr.eolmago.repository.user.UserProfileRepositoryCustom;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import static kr.eolmago.domain.entity.user.QUser.user;
import static kr.eolmago.domain.entity.user.QUserProfile.userProfile;

public class UserProfileRepositoryImpl implements UserProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UserProfileRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<UserProfile> findByUserIdWithUser(UUID userId) {
        UserProfile result = queryFactory
                .selectFrom(userProfile)
                .leftJoin(userProfile.user, user).fetchJoin()
                .where(userProfile.user.userId.eq(userId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return queryFactory
                .selectOne()
                .from(userProfile)
                .where(userProfile.nickname.eq(nickname))
                .fetchOne() != null;
    }

    @Override
    public Optional<String> findNicknameByUserId(UUID userId) {
        String nickname = queryFactory
                .select(userProfile.nickname)
                .from(userProfile)
                .leftJoin(userProfile.user, user) // userProfile.user.userId 조건 때문에 조인 유지
                .where(userProfile.user.userId.eq(userId))
                .fetchOne();

        return Optional.ofNullable(nickname);
    }
}

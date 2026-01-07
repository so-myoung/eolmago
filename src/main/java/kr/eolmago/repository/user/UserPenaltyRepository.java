package kr.eolmago.repository.user;

import kr.eolmago.domain.entity.user.UserPenalty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPenaltyRepository extends JpaRepository<UserPenalty, Long>, UserPenaltyRepositoryCustom {

}

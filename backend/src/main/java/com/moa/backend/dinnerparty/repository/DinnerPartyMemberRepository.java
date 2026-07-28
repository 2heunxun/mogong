package com.moa.backend.dinnerparty.repository;

import com.moa.backend.dinnerparty.entity.DinnerPartyMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DinnerPartyMemberRepository extends JpaRepository<DinnerPartyMember, Long> {

    Optional<DinnerPartyMember> findByPartyIdAndUserId(Long partyId, Long userId);

    boolean existsByPartyIdAndUserId(Long partyId, Long userId);

    long countByPartyIdAndStatus(Long partyId, DinnerPartyMember.Status status);

    List<DinnerPartyMember> findByPartyIdAndStatusOrderByJoinedAtAsc(Long partyId, DinnerPartyMember.Status status);

    List<DinnerPartyMember> findByUserIdAndStatus(Long userId, DinnerPartyMember.Status status);

    void deleteByPartyIdAndUserId(Long partyId, Long userId);
}

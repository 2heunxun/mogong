package com.moa.backend.weekendparty.repository;

import com.moa.backend.weekendparty.entity.WeekendPartyMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeekendPartyMemberRepository extends JpaRepository<WeekendPartyMember, Long> {

    Optional<WeekendPartyMember> findByPartyIdAndUserId(Long partyId, Long userId);

    boolean existsByPartyIdAndUserId(Long partyId, Long userId);

    long countByPartyIdAndStatus(Long partyId, WeekendPartyMember.Status status);

    List<WeekendPartyMember> findByPartyIdAndStatusOrderByJoinedAtAsc(Long partyId, WeekendPartyMember.Status status);

    List<WeekendPartyMember> findByUserIdAndStatus(Long userId, WeekendPartyMember.Status status);

    void deleteByPartyIdAndUserId(Long partyId, Long userId);
}

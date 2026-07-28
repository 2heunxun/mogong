package com.moa.backend.weekendparty.repository;

import com.moa.backend.weekendparty.entity.WeekendParty;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WeekendPartyRepository extends JpaRepository<WeekendParty, Long>, JpaSpecificationExecutor<WeekendParty> {

    List<WeekendParty> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}

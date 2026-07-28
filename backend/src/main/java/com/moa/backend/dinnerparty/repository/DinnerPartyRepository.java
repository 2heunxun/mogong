package com.moa.backend.dinnerparty.repository;

import com.moa.backend.dinnerparty.entity.DinnerParty;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DinnerPartyRepository extends JpaRepository<DinnerParty, Long>, JpaSpecificationExecutor<DinnerParty> {

    List<DinnerParty> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}

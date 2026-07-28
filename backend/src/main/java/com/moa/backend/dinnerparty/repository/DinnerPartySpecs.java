package com.moa.backend.dinnerparty.repository;

import com.moa.backend.dinnerparty.entity.DinnerParty;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class DinnerPartySpecs {

    private DinnerPartySpecs() {
    }

    public static Specification<DinnerParty> withFilters(String keyword, DinnerParty.Status status) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            return predicates;
        };
    }
}

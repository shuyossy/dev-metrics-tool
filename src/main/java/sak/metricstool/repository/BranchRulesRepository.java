package sak.metricstool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sak.metricstool.entity.BranchRules;

public interface BranchRulesRepository extends JpaRepository<BranchRules, Long> {
}

package sak.metricstool.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * BRANCH_RULESテーブル
 * ブランチ戦略チェック用のルールを保持します。
 */
@Entity
@Table(name = "branch_rules")
@Data
public class BranchRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "rule_name", length = 50, nullable = false)
    private String ruleName;

    @Column(name = "regex_pattern", length = 200, nullable = false)
    private String regexPattern;

    @Column(name = "parent_rule_type", length = 50)
    private String parentRuleType;
}

-- 智投进化平台数据库初始化脚本
-- 数据库名称: investmind

-- 创建数据库
CREATE DATABASE IF NOT EXISTS investmind CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE investmind;

-- 投资报告表
-- 存储AI生成的原始报告和人工修正版本
CREATE TABLE IF NOT EXISTS `invest_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_date` DATE NOT NULL COMMENT '报告日期',
    `report_type` VARCHAR(32) NOT NULL DEFAULT 'DAILY' COMMENT '报告类型: DAILY-日报, WEEKLY-周报, MONTHLY-月报',
    
    -- AI生成内容
    `ai_content` TEXT COMMENT 'AI生成的原始报告内容(Markdown格式)',
    `ai_generate_time` DATETIME COMMENT 'AI生成时间',
    
    -- 人工修正内容
    `human_content` TEXT COMMENT '人工修正后的报告内容(Markdown格式)',
    `human_correct_time` DATETIME COMMENT '人工修正时间',
    `corrected_by` VARCHAR(64) COMMENT '修正人',
    
    -- 向量化和学习状态
    `is_vectorized` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已向量化: 0-否, 1-是',
    `vectorized_time` DATETIME COMMENT '向量化时间',
    `is_used_for_learning` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已用于模型学习: 0-否, 1-是',
    `learning_time` DATETIME COMMENT '用于学习的时间',
    
    -- 质量评分
    `quality_score` DECIMAL(3,2) DEFAULT NULL COMMENT '报告质量评分(0.00-5.00)',
    
    -- 标签和分类
    `tags` VARCHAR(512) COMMENT '标签,逗号分隔',
    `market_analysis` TEXT COMMENT '市场分析摘要',
    `strategy_advice` TEXT COMMENT '策略建议摘要',
    
    -- 软删除和时间戳
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    INDEX `idx_report_date` (`report_date`),
    INDEX `idx_report_type` (`report_type`),
    INDEX `idx_is_vectorized` (`is_vectorized`),
    INDEX `idx_is_used_for_learning` (`is_used_for_learning`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资报告表';

-- 报告修正记录表
-- 记录每次修正的详细变更
CREATE TABLE IF NOT EXISTS `report_correction_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_id` BIGINT NOT NULL COMMENT '关联的报告ID',
    `field_name` VARCHAR(64) NOT NULL COMMENT '修正的字段名',
    `old_value` TEXT COMMENT '修正前的值',
    `new_value` TEXT COMMENT '修正后的值',
    `corrected_by` VARCHAR(64) COMMENT '修正人',
    `correct_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修正时间',
    `correction_reason` VARCHAR(512) COMMENT '修正原因',
    
    PRIMARY KEY (`id`),
    INDEX `idx_report_id` (`report_id`),
    INDEX `idx_correct_time` (`correct_time`),
    CONSTRAINT `fk_correction_report` FOREIGN KEY (`report_id`) REFERENCES `invest_report`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告修正记录表';

-- 向量索引记录表
-- 记录哪些内容已存入向量数据库
CREATE TABLE IF NOT EXISTS `vector_index_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_id` BIGINT NOT NULL COMMENT '关联的报告ID',
    `content_type` VARCHAR(32) NOT NULL COMMENT '内容类型: FULL_REPORT-完整报告, MARKET_ANALYSIS-市场分析, STRATEGY-策略建议',
    `vector_id` VARCHAR(128) NOT NULL COMMENT '向量数据库中的ID',
    `embedding_model` VARCHAR(64) COMMENT '使用的embedding模型',
    `vectorized_content` TEXT COMMENT '被向量化的内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vector_id` (`vector_id`),
    INDEX `idx_report_id` (`report_id`),
    INDEX `idx_content_type` (`content_type`),
    CONSTRAINT `fk_vector_report` FOREIGN KEY (`report_id`) REFERENCES `invest_report`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='向量索引记录表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(512) COMMENT '配置描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 初始化默认配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('ai.prompt.template', '请生成一份投资分析报告，包含以下内容：\n1. 今日市场概况\n2. 主要板块分析\n3. 投资策略建议\n\n请以专业的投资分析师角度撰写，使用Markdown格式。', 'AI生成报告的提示词模板'),
('ai.max_tokens', '1000', 'AI生成报告的最大token数'),
('ai.temperature', '0.7', 'AI生成报告的temperature参数'),
('report.auto_generate_time', '08:00:00', '每日自动生成报告的时间'),
('vector.top_k', '5', '向量检索返回的最相似结果数量')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- ============================================
-- 第一阶段：多用户账号系统
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '加密密码(BCrypt)',
    `email` VARCHAR(128) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `user_type` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '用户类型: ADMIN/USER',
    `nickname` VARCHAR(64) COMMENT '昵称',
    `avatar` VARCHAR(256) COMMENT '头像URL',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) COMMENT '最后登录IP',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户投资理念配置表
CREATE TABLE IF NOT EXISTS `user_investment_philosophy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',

    -- 结构化标签
    `risk_preference` VARCHAR(32) COMMENT '风险偏好: CONSERVATIVE/MODERATE/AGGRESSIVE',
    `investment_style` VARCHAR(32) COMMENT '投资风格: VALUE/GROWTH/BLEND',
    `investment_horizon` VARCHAR(32) COMMENT '投资期限: SHORT/MEDIUM/LONG',
    `preferred_sectors` VARCHAR(256) COMMENT '偏好行业,逗号分隔',
    `avoid_sectors` VARCHAR(256) COMMENT '回避行业,逗号分隔',

    -- 自由文本
    `philosophy_description` TEXT COMMENT '投资理念详细描述',
    `core_investment_philosophy` TEXT COMMENT '核心投资哲学',
    `stock_selection_criteria` TEXT COMMENT '选股标准',
    `valuation_logic` TEXT COMMENT '估值逻辑',
    `position_management_rules` TEXT COMMENT '仓位管理规则',
    `sell_conditions` TEXT COMMENT '卖出条件',
    `holding_period` VARCHAR(128) COMMENT '持有周期',
    `industry_restrictions` VARCHAR(512) COMMENT '行业限制',
    `strategy_notes` TEXT COMMENT '策略备注',
    `risk_management` TEXT COMMENT '风险管理方法',

    -- AI 学习相关
    `ai_learning_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许AI学习',
    `learning_iterations` INT NOT NULL DEFAULT 0 COMMENT '学习迭代次数',
    `version_no` INT NOT NULL DEFAULT 1 COMMENT '投资理念版本号',

    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    CONSTRAINT `fk_philosophy_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户投资理念配置表';

-- 修改投资报告表，添加用户ID字段（兼容 MySQL 8.0.21）
SET @add_user_id_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'invest_report'
        AND COLUMN_NAME = 'user_id'
    ),
    'SELECT 1',
    'ALTER TABLE `invest_report` ADD COLUMN `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT ''关联用户ID,0为系统公共报告'' AFTER `id`'
  )
);
PREPARE stmt_add_user_id FROM @add_user_id_sql;
EXECUTE stmt_add_user_id;
DEALLOCATE PREPARE stmt_add_user_id;

SET @add_user_id_index_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM INFORMATION_SCHEMA.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'invest_report'
        AND INDEX_NAME = 'idx_user_id'
    ),
    'SELECT 1',
    'ALTER TABLE `invest_report` ADD INDEX `idx_user_id` (`user_id`)'
  )
);
PREPARE stmt_add_user_id_index FROM @add_user_id_index_sql;
EXECUTE stmt_add_user_id_index;
DEALLOCATE PREPARE stmt_add_user_id_index;

-- user_investment_philosophy 新字段兼容补齐（MySQL 8.0.21）
SET @add_core_philo_sql = (
  SELECT IF(
    EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_investment_philosophy' AND COLUMN_NAME = 'core_investment_philosophy'),
    'SELECT 1',
    'ALTER TABLE `user_investment_philosophy` ADD COLUMN `core_investment_philosophy` TEXT COMMENT ''核心投资哲学'' AFTER `philosophy_description`'
  )
);
PREPARE stmt_add_core_philo FROM @add_core_philo_sql;
EXECUTE stmt_add_core_philo;
DEALLOCATE PREPARE stmt_add_core_philo;

SET @add_stock_criteria_sql = (
  SELECT IF(
    EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_investment_philosophy' AND COLUMN_NAME = 'stock_selection_criteria'),
    'SELECT 1',
    'ALTER TABLE `user_investment_philosophy` ADD COLUMN `stock_selection_criteria` TEXT COMMENT ''选股标准'' AFTER `core_investment_philosophy`'
  )
);
PREPARE stmt_add_stock_criteria FROM @add_stock_criteria_sql;
EXECUTE stmt_add_stock_criteria;
DEALLOCATE PREPARE stmt_add_stock_criteria;

SET @add_valuation_logic_sql = (
  SELECT IF(
    EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_investment_philosophy' AND COLUMN_NAME = 'valuation_logic'),
    'SELECT 1',
    'ALTER TABLE `user_investment_philosophy` ADD COLUMN `valuation_logic` TEXT COMMENT ''估值逻辑'' AFTER `stock_selection_criteria`'
  )
);
PREPARE stmt_add_valuation_logic FROM @add_valuation_logic_sql;
EXECUTE stmt_add_valuation_logic;
DEALLOCATE PREPARE stmt_add_valuation_logic;

SET @add_position_rules_sql = (
  SELECT IF(
    EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_investment_philosophy' AND COLUMN_NAME = 'position_management_rules'),
    'SELECT 1',
    'ALTER TABLE `user_investment_philosophy` ADD COLUMN `position_management_rules` TEXT COMMENT ''仓位管理规则'' AFTER `valuation_logic`'
  )
);
PREPARE stmt_add_position_rules FROM @add_position_rules_sql;
EXECUTE stmt_add_position_rules;
DEALLOCATE PREPARE stmt_add_position_rules;

SET @add_sell_conditions_sql = (
  SELECT IF(
    EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_investment_philosophy' AND COLUMN_NAME = 'sell_conditions'),
    'SELECT 1',
    'ALTER TABLE `user_investment_philosophy` ADD COLUMN `sell_conditions` TEXT COMMENT ''卖出条件'' AFTER `position_management_rules`'
  )
);
PREPARE stmt_add_sell_conditions FROM @add_sell_conditions_sql;
EXECUTE stmt_add_sell_conditions;
DEALLOCATE PREPARE stmt_add_sell_conditions;

SET @add_holding_period_sql = (
  SELECT IF(
    EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_investment_philosophy' AND COLUMN_NAME = 'holding_period'),
    'SELECT 1',
    'ALTER TABLE `user_investment_philosophy` ADD COLUMN `holding_period` VARCHAR(128) COMMENT ''持有周期'' AFTER `sell_conditions`'
  )
);
PREPARE stmt_add_holding_period FROM @add_holding_period_sql;
EXECUTE stmt_add_holding_period;
DEALLOCATE PREPARE stmt_add_holding_period;

SET @add_industry_restrict_sql = (
  SELECT IF(
    EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_investment_philosophy' AND COLUMN_NAME = 'industry_restrictions'),
    'SELECT 1',
    'ALTER TABLE `user_investment_philosophy` ADD COLUMN `industry_restrictions` VARCHAR(512) COMMENT ''行业限制'' AFTER `holding_period`'
  )
);
PREPARE stmt_add_industry_restrict FROM @add_industry_restrict_sql;
EXECUTE stmt_add_industry_restrict;
DEALLOCATE PREPARE stmt_add_industry_restrict;

SET @add_version_no_sql = (
  SELECT IF(
    EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_investment_philosophy' AND COLUMN_NAME = 'version_no'),
    'SELECT 1',
    'ALTER TABLE `user_investment_philosophy` ADD COLUMN `version_no` INT NOT NULL DEFAULT 1 COMMENT ''投资理念版本号'' AFTER `learning_iterations`'
  )
);
PREPARE stmt_add_version_no FROM @add_version_no_sql;
EXECUTE stmt_add_version_no;
DEALLOCATE PREPARE stmt_add_version_no;

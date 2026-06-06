-- 试卷表
CREATE TABLE IF NOT EXISTS `exam_paper` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '试卷ID',
    `title` VARCHAR(255) NOT NULL COMMENT '试卷标题',
    `description` TEXT COMMENT '试卷描述/说明',
    `total_score` INT DEFAULT 0 COMMENT '试卷总分',
    `duration` INT DEFAULT 120 COMMENT '考试时长（分钟）',
    `start_time` DATETIME COMMENT '考试开始时间',
    `end_time` DATETIME COMMENT '考试结束时间',
    `publish_status` ENUM('DRAFT', 'PUBLISHED', 'CLOSED') DEFAULT 'DRAFT' COMMENT '发布状态：草稿/已发布/已关闭',
    `answer_visible` ENUM('ALWAYS', 'AFTER_SUBMIT', 'AFTER_GRADED', 'NEVER') DEFAULT 'AFTER_GRADED' COMMENT '答案可见时机：始终可见/提交后可见/批改后可见/不可见',
    `max_attempts` INT DEFAULT 1 COMMENT '允许作答次数，0表示不限',
    `target_classes` VARCHAR(500) COMMENT '发布对象班级（逗号分隔，空表示所有学生）',
    `created_by` BIGINT COMMENT '创建者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`created_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷表';

-- 试卷题目表（题目快照，避免题库修改影响历史试卷）
CREATE TABLE IF NOT EXISTS `exam_paper_question` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `paper_id` BIGINT NOT NULL COMMENT '试卷ID',
    `question_id` BIGINT COMMENT '原始题目ID（可为空，仅作关联参考）',
    `question_order` INT DEFAULT 0 COMMENT '题目顺序',
    `score` INT DEFAULT 0 COMMENT '该题在试卷中的分值',
    `title` VARCHAR(255) NOT NULL COMMENT '题目标题（快照）',
    `description` TEXT NOT NULL COMMENT '题目描述（快照）',
    `requirements` TEXT COMMENT '要求说明（快照）',
    `sample_input` TEXT COMMENT '示例输入（快照）',
    `sample_output` TEXT COMMENT '示例输出（快照）',
    `reference_answer` TEXT COMMENT '参考答案（快照）',
    `difficulty` ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM' COMMENT '难度（快照）',
    `question_type` ENUM('PROGRAMMING', 'SINGLE_CHOICE', 'MULTIPLE_CHOICE') DEFAULT 'PROGRAMMING' COMMENT '题型（快照）',
    `options` TEXT COMMENT '选项JSON（快照）',
    `correct_answer` VARCHAR(255) COMMENT '正确答案（快照）',
    `answer_explanation` TEXT COMMENT '答案解析（快照）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`paper_id`) REFERENCES `exam_paper`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`question_id`) REFERENCES `question`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷题目表';

-- 试卷答卷表（整卷提交记录）
CREATE TABLE IF NOT EXISTS `exam_paper_submission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '答卷ID',
    `paper_id` BIGINT NOT NULL COMMENT '试卷ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `attempt_number` INT DEFAULT 1 COMMENT '第几次作答',
    `total_score` INT DEFAULT 0 COMMENT '总得分',
    `auto_score` INT DEFAULT 0 COMMENT '自动判分得分（选择题）',
    `manual_score` INT DEFAULT 0 COMMENT '人工判分得分（编程题）',
    `status` ENUM('DRAFT', 'SUBMITTED', 'PENDING_GRADING', 'GRADED') DEFAULT 'DRAFT' COMMENT '状态：草稿/已提交/待批改/已批改',
    `submit_time` DATETIME COMMENT '提交时间',
    `graded_by` BIGINT COMMENT '批改老师ID',
    `graded_at` DATETIME COMMENT '批改完成时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`paper_id`) REFERENCES `exam_paper`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`graded_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷答卷表';

-- 试卷答题详情表（每题的作答记录）
CREATE TABLE IF NOT EXISTS `exam_answer` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `submission_id` BIGINT NOT NULL COMMENT '答卷ID',
    `paper_question_id` BIGINT NOT NULL COMMENT '试卷题目ID',
    `answer_code` TEXT COMMENT '答题代码（编程题）',
    `selected_answer` VARCHAR(255) COMMENT '选择题答案',
    `score` INT DEFAULT 0 COMMENT '得分',
    `feedback` TEXT COMMENT '老师评语',
    `is_auto_graded` TINYINT(1) DEFAULT 0 COMMENT '是否已自动判分',
    `is_manual_graded` TINYINT(1) DEFAULT 0 COMMENT '是否已人工判分',
    `graded_by` BIGINT COMMENT '批改老师ID',
    `graded_at` DATETIME COMMENT '批改时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`submission_id`) REFERENCES `exam_paper_submission`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`paper_question_id`) REFERENCES `exam_paper_question`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`graded_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷答题详情表';

-- 创建索引
CREATE INDEX idx_exam_paper_status ON `exam_paper`(`publish_status`);
CREATE INDEX idx_exam_paper_created_by ON `exam_paper`(`created_by`);
CREATE INDEX idx_exam_paper_question_paper ON `exam_paper_question`(`paper_id`);
CREATE INDEX idx_exam_submission_paper ON `exam_paper_submission`(`paper_id`);
CREATE INDEX idx_exam_submission_student ON `exam_paper_submission`(`student_id`);
CREATE INDEX idx_exam_submission_status ON `exam_paper_submission`(`status`);
CREATE INDEX idx_exam_answer_submission ON `exam_answer`(`submission_id`);
CREATE INDEX idx_exam_answer_question ON `exam_answer`(`paper_question_id`);

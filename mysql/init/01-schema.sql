-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名/学号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `role` ENUM('STUDENT', 'TEACHER') NOT NULL DEFAULT 'STUDENT' COMMENT '角色',
    `need_change_pwd` TINYINT(1) DEFAULT 1 COMMENT '是否需要修改密码',
    `class_name` VARCHAR(50) COMMENT '班级',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 题目表
CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    `title` VARCHAR(255) NOT NULL COMMENT '题目标题',
    `description` TEXT NOT NULL COMMENT '题目描述',
    `requirements` TEXT COMMENT '要求说明',
    `sample_input` TEXT COMMENT '示例输入',
    `sample_output` TEXT COMMENT '示例输出',
    `reference_answer` TEXT COMMENT '参考答案',
    `score` INT DEFAULT 10 COMMENT '分值',
    `difficulty` ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM' COMMENT '难度',
    `question_type` ENUM('PROGRAMMING', 'SINGLE_CHOICE', 'MULTIPLE_CHOICE') DEFAULT 'PROGRAMMING' COMMENT '题型：编程题/单选题/多选题',
    `options` TEXT COMMENT '选项（JSON数组格式，如["A.选项1","B.选项2"]）',
    `correct_answer` VARCHAR(255) COMMENT '正确答案（单选如"A"，多选如"A,C"）',
    `answer_explanation` TEXT COMMENT '答案解析/讲解',
    `created_by` BIGINT COMMENT '创建者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`created_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- 学生答题记录表
CREATE TABLE IF NOT EXISTS `submission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `answer_code` TEXT COMMENT '答题代码（编程题使用）',
    `selected_answer` VARCHAR(255) COMMENT '选择题答案（单选如"A"，多选如"A,C"）',
    `score` INT DEFAULT 0 COMMENT '得分',
    `feedback` TEXT COMMENT '老师反馈',
    `status` ENUM('PENDING', 'GRADED') DEFAULT 'PENDING' COMMENT '状态',
    `graded_by` BIGINT COMMENT '批改老师ID',
    `graded_at` DATETIME COMMENT '批改时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`question_id`) REFERENCES `question`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`graded_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交记录表';

-- 创建索引
CREATE INDEX idx_user_role ON `user`(`role`);
CREATE INDEX idx_submission_student ON `submission`(`student_id`);
CREATE INDEX idx_submission_question ON `submission`(`question_id`);
CREATE INDEX idx_submission_status ON `submission`(`status`);

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 插入默认教师账号 (密码: 123456, 使用BCrypt加密)
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `need_change_pwd`) VALUES
('teacher001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张老师', 'TEACHER', 0),
('teacher002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李老师', 'TEACHER', 0);

-- 插入示例学生账号
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `need_change_pwd`, `class_name`) VALUES
('2024001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王小明', 'STUDENT', 1, '计算机2401班'),
('2024002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李小红', 'STUDENT', 1, '计算机2401班'),
('2024003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张小华', 'STUDENT', 1, '计算机2402班');

-- 插入示例题目
INSERT INTO `question` (`title`, `description`, `requirements`, `sample_input`, `sample_output`, `reference_answer`, `score`, `difficulty`, `created_by`) VALUES
('实现单例模式', 
'请编写一个Java类实现单例模式（Singleton Pattern）。',
'1. 确保类只有一个实例\n2. 提供全局访问点\n3. 线程安全\n4. 延迟加载',
'无',
'每次调用getInstance()返回同一个实例',
'public class Singleton {\n    private static volatile Singleton instance;\n    private Singleton() {}\n    public static Singleton getInstance() {\n        if (instance == null) {\n            synchronized (Singleton.class) {\n                if (instance == null) {\n                    instance = new Singleton();\n                }\n            }\n        }\n        return instance;\n    }\n}',
15, 'MEDIUM', 1),

('实现冒泡排序',
'请编写一个Java方法实现冒泡排序算法。',
'1. 方法签名: public static void bubbleSort(int[] arr)\n2. 原地排序，不使用额外数组\n3. 升序排列',
'[5, 3, 8, 4, 2]',
'[2, 3, 4, 5, 8]',
'public static void bubbleSort(int[] arr) {\n    int n = arr.length;\n    for (int i = 0; i < n - 1; i++) {\n        for (int j = 0; j < n - i - 1; j++) {\n            if (arr[j] > arr[j + 1]) {\n                int temp = arr[j];\n                arr[j] = arr[j + 1];\n                arr[j + 1] = temp;\n            }\n        }\n    }\n}',
10, 'EASY', 1),

('实现二叉树遍历',
'请编写Java代码实现二叉树的前序、中序、后序遍历。',
'1. 定义二叉树节点类TreeNode\n2. 实现三种遍历方法\n3. 可以使用递归或迭代方式',
'树结构: 1 -> (2, 3), 2 -> (4, 5)',
'前序: 1,2,4,5,3\n中序: 4,2,5,1,3\n后序: 4,5,2,3,1',
'class TreeNode {\n    int val;\n    TreeNode left, right;\n    TreeNode(int val) { this.val = val; }\n}\n\npublic void preOrder(TreeNode root) {\n    if (root == null) return;\n    System.out.print(root.val + \" \");\n    preOrder(root.left);\n    preOrder(root.right);\n}',
20, 'HARD', 1);

-- 插入示例提交记录
INSERT INTO `submission` (`student_id`, `question_id`, `answer_code`, `score`, `feedback`, `status`, `graded_by`, `graded_at`) VALUES
(3, 1, 'public class Singleton {\n    private static Singleton instance;\n    private Singleton() {}\n    public static Singleton getInstance() {\n        if (instance == null) {\n            instance = new Singleton();\n        }\n        return instance;\n    }\n}', 12, '实现基本正确，但缺少线程安全处理。建议使用双重检查锁定或静态内部类方式。', 'GRADED', 1, NOW()),
(4, 2, 'public static void bubbleSort(int[] arr) {\n    for (int i = 0; i < arr.length; i++) {\n        for (int j = 0; j < arr.length - 1; j++) {\n            if (arr[j] > arr[j+1]) {\n                int temp = arr[j];\n                arr[j] = arr[j+1];\n                arr[j+1] = temp;\n            }\n        }\n    }\n}', 8, '算法逻辑正确，但内层循环可以优化为arr.length-i-1，减少不必要的比较。', 'GRADED', 1, NOW());

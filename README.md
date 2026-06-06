# Java编程题判断系统

一个用于Java主观编程题判断的前后端分离Web系统，支持教师管理学生、导入题目，学生答题和教师批改等功能。

## 🛠 技术栈

- **Frontend**: Vue3 + Vite + Element Plus + Pinia
- **Backend**: Spring Boot 3.2 + MyBatis + Spring Security + JWT
- **Database**: MySQL 8.0

## 🚀 启动指南 (How to Run)

1. 确保 Docker Desktop 已启动
2. 在根目录执行：
   ```bash
   docker compose up --build
   ```
3. 等待容器启动完成（首次构建约3-5分钟）
4. 访问系统

## 🔗 服务地址 (Services)

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8000/api
- **Backend Swagger**: http://localhost:8000/api/swagger-ui.html
- **Database**: localhost:3306

## 🧪 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 教师 | teacher001 | 123456 | 张老师 |
| 教师 | teacher002 | 123456 | 李老师 |
| 学生 | 2024001 | 123456 | 王小明（首次登录需改密码）|
| 学生 | 2024002 | 123456 | 李小红（首次登录需改密码）|
| 学生 | 2024003 | 123456 | 张小华（首次登录需改密码）|

## 📋 功能特性

### 教师端
- ✅ 批量导入学生（Excel文件，学号为账号，初始密码123456）
- ✅ 学生管理（查看、删除）
- ✅ 创建/编辑题目
- ✅ 批量导入题目（Excel文件）
- ✅ 批改学生作业（打分、评语）
- ✅ 查看参考答案

### 学生端
- ✅ 查看题目列表
- ✅ 在线答题
- ✅ 查看提交记录和成绩
- ✅ 首次登录强制修改密码

### 系统特性
- ✅ 响应式布局，支持移动端
- ✅ JWT身份认证
- ✅ 角色权限控制
- ✅ RESTful API
- ✅ Docker一键部署

## 📁 项目结构

```
taskId538/
├── docker-compose.yml       # Docker编排配置
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── components/     # 公共组件
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # Pinia状态管理
│   │   ├── styles/         # 全局样式
│   │   ├── utils/          # 工具函数
│   │   └── views/          # 页面组件
│   ├── Dockerfile
│   └── nginx.conf
├── backend/                 # 后端项目
│   ├── src/main/java/com/exam/
│   │   ├── controller/     # 控制器
│   │   ├── service/        # 服务层
│   │   ├── mapper/         # MyBatis映射
│   │   ├── entity/         # 实体类
│   │   ├── dto/            # 数据传输对象
│   │   ├── config/         # 配置类
│   │   └── util/           # 工具类
│   ├── Dockerfile
│   └── settings.xml        # Maven阿里云镜像配置
└── mysql/
    └── init/               # 数据库初始化脚本
        ├── 01-schema.sql   # 表结构
        └── 02-seed.sql     # 初始数据
```

## 📝 Excel导入格式

### 学生导入模板
| 学号 | 姓名 | 班级 |
|------|------|------|
| 2024001 | 张三 | 计算机2401班 |

### 题目导入模板
| 标题 | 描述 | 要求 | 示例输入 | 示例输出 | 参考答案 | 分值 | 难度 |
|------|------|------|----------|----------|----------|------|------|
| 实现单例模式 | 请编写... | 1. 确保... | 无 | ... | public class... | 15 | MEDIUM |

> 难度可选值：EASY / MEDIUM / HARD

## 🔧 开发说明

### 本地开发（不使用Docker）

**后端**
```bash
cd backend
mvn spring-boot:run
```

**前端**
```bash
cd frontend
npm install
npm run dev
```

### 数据库配置
- Host: localhost
- Port: 3306
- Database: java_exam
- Username: exam_user
- Password: exam123456

## 📌 注意事项

1. 首次启动需要等待数据库初始化完成
2. 学生首次登录会强制要求修改密码
3. 教师可以看到参考答案，学生不可见
4. 文件上传大小限制为10MB

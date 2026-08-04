# AI大学生职业生涯规划智能体平台

> 以AI为驱动的职业生涯规划系统，支持多角色（学生/教师/管理员），集成 DeepSeek 大模型。

## 项目简介

CareerSail 是一款面向大学生的 AI 驱动职业生涯规划智能体平台，通过职业测评、AI 对话、模拟面试、简历优化等功能模块，为用户提供个性化的职业发展建议与能力提升方案。系统支持学生、教师、管理员三种角色，满足教学管理与自主学习的完整场景。

## 技术栈

### 后端

| 层级 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.3.5 |
| 语言 | Java | 17 |
| ORM | MyBatis-Plus | 3.5.7 |
| 安全 | Spring Security + JWT | jjwt 0.12.6 |
| AI引擎 | Spring AI（DeepSeek V4 Pro） | 1.0.0-M6 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | — |
| 向量库 | Chroma / 内存 SimpleVectorStore | — |
| 工具库 | Hutool | 5.8.29 |
| 文档处理 | Apache POI / PDFBox | 5.2.5 / 3.0.3 |
| 构建 | Maven | — |

### 前端

| 层级 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 | — |
| 构建 | Vite | — |
| UI库 | Element Plus | — |
| 状态管理 | Pinia | — |
| 路由 | Vue Router 4 | — |
| HTTP | Axios | — |

### 部署

| 层级 | 技术 |
|------|------|
| 容器化 | Docker + Docker Compose |
| 反向代理 | Nginx |

## 功能模块

系统支持三种角色：学生、教师、管理员，不同角色登录后看到不同功能菜单。

### 学生端

| 菜单 | 子模块 | 功能说明 |
|------|--------|----------|
| 首页 | — | 学生仪表盘，展示测评进度、待办任务、最新通知 |
| 职业探索 | 岗位搜索 | 职业库关键词搜索与分类筛选，查看职业详情 |
| | 薪资洞察 | 各职业薪资数据可视化图表 |
| | 求职日历 | 职业发展事件日历，标记求职关键节点 |
| | 收藏对比 | 职业收藏管理，多职业横向对比 |
| | 学习路径 | AI 根据目标职业生成个性化学习路径和技能差距分析 |
| 能力成长 | 职业测评 | 多类型问卷（MBTI、霍兰德等），逐题作答，AI 自动生成测评报告 |
| | 实训任务 | 查看教师布置的任务，在线提交作业，查看评分 |
| | AI 模拟面试 | 多类型面试模拟，AI 提问 + 智能反馈，历史回顾 |
| | 模拟笔试 | 在线笔试答题，支持多种题型，自动计时提交 |
| | 简历优化 | 上传简历文件，AI 分析并给出优化建议 |
| AI 咨询 | — | 基于 DeepSeek 大模型的流式对话，多轮会话，历史记录管理 |

### 教师端

| 菜单 | 功能说明 |
|------|----------|
| 学情仪表板 | 班级数据概览，学生测评与任务完成统计 |
| 学生管理 | 学生列表查看，Excel 批量导入导出，学生详情 |
| 任务管理 | 创建/编辑/删除实训任务，分配学生，查看提交进度 |
| 报告管理 | 生成/查看学生测评报告，批量导出 |
| 成绩总览 | 班级成绩统计与对比分析 |
| 任务统计 | 实训任务完成率、提交情况统计 |

### 管理员端

| 菜单 | 功能说明 |
|------|----------|
| 管理中心 | 平台核心数据概览（用户数、测评数、活跃度） |
| 用户管理 | 用户列表、角色分配（学生/教师/管理员）、账号启停、重置密码 |
| 职业知识库 | 职业信息增删改查、批量导入、向量索引重建 |
| 实训管理 | 全局实训任务查看与管理 |
| 系统配置 | AI 提示词管理、系统参数动态配置 |

## 项目截图

### 学生端

**登录**

![登录界面](./screenshots/student/登录界面.png)

**首页**

![学生首页](./screenshots/student/学生首页.png)

**职业探索**

![岗位搜索](./screenshots/student/岗位搜索.png)
![薪资洞察](./screenshots/student/薪资洞察.png)
![收藏对比](./screenshots/student/收藏对比.png)
![学习路径](./screenshots/student/学习路径.png)

**能力成长**

![职业测评](./screenshots/student/职业测评.png)
![职业测评报告](./screenshots/student/职业测评报告.png)
![实训任务](./screenshots/student/实训任务.png)
![AI模拟面试](./screenshots/student/模拟面试.png)
![模拟笔试](./screenshots/student/模拟笔试.png)
![简历优化](./screenshots/student/简历优化.png)

**AI 咨询**

![AI咨询对话](./screenshots/student/AI咨询对话.png)

### 教师端

![学情仪表盘](./screenshots/teacher/学情仪表盘.png)
![学生管理](./screenshots/teacher/学生管理.png)
![报告管理](./screenshots/teacher/报告管理.png)
![成绩总览](./screenshots/teacher/成绩总览.png)
![任务统计](./screenshots/teacher/任务统计.png)

### 管理员端

![管理中心](./screenshots/administrator/管理中心.png)
![用户管理](./screenshots/administrator/用户管理.png)
![职业知识库](./screenshots/administrator/职业知识库.png)
![实训任务管理](./screenshots/administrator/实训任务管理.png)
![系统配置](./screenshots/administrator/系统配置.png)

## 快速开始

### 环境要求

- JDK 17+、MySQL 8.0+、Redis、Maven 3.8+、Node.js 18+

### 1. 克隆项目

```bash
git clone https://github.com/Zongzi611/ai-career-platform.git
cd ai-career-platform
```

### 2. 导入数据库

```bash
mysql -u root -p < sql/init.sql
```

### 3. 配置环境变量

```bash
set DEEPSEEK_API_KEY=你的API密钥
```

### 4. 启动后端

```bash
mvn spring-boot:run
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

> 首次启动需要执行 `npm install` 安装依赖，后续启动只需 `npm run dev`。

### 6. 访问

浏览器打开 `http://localhost:5173`

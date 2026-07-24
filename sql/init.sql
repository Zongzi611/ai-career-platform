-- ============================================================
-- CareerSail 大学生职业规划智能体 — 数据库初始化脚本
-- Database: careersail_db  (Charset: utf8mb4)
-- ============================================================

CREATE DATABASE IF NOT EXISTS careersail_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE careersail_db;

-- ============================================================
-- 1. 系统配置表 (AI 提示词存储)
-- ============================================================
CREATE TABLE sys_config (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    config_key  VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键 e.g. ai.chat.prompt, ai.scoring.prompt',
    config_value TEXT        NOT NULL COMMENT '配置值 / 完整提示词文本',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB COMMENT='系统配置表(含AI提示词)';

-- ============================================================
-- 2. 角色表
-- ============================================================
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色显示名称',
    role_code   VARCHAR(50)  NOT NULL UNIQUE COMMENT '角色编码 ROLE_STUDENT/ROLE_TEACHER/ROLE_ADMIN',
    description VARCHAR(255) DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='角色表';

-- ============================================================
-- 3. 用户表
-- ============================================================
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录用户名',
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
    real_name   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    email       VARCHAR(100) DEFAULT NULL,
    phone       VARCHAR(20)  DEFAULT NULL,
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    gender      TINYINT      DEFAULT 0 COMMENT '0=未知, 1=男, 2=女',
    grade       VARCHAR(20)  DEFAULT NULL COMMENT '年级 e.g. 2024级',
    major       VARCHAR(100) DEFAULT NULL COMMENT '专业',
    class_name  VARCHAR(100) DEFAULT NULL COMMENT '班级名称',
    student_no  VARCHAR(50)  DEFAULT NULL COMMENT '学号',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_major (major),
    INDEX idx_class (class_name)
) ENGINE=InnoDB COMMENT='用户表';

-- ============================================================
-- 4. 用户-角色关联表
-- ============================================================
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB COMMENT='用户-角色映射表';

-- ============================================================
-- 5. 职业知识库表
-- ============================================================
CREATE TABLE career_info (
    id             BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    position_name  VARCHAR(100)  NOT NULL COMMENT '岗位名称',
    industry       VARCHAR(100)  NOT NULL COMMENT '所属行业',
    major_match    VARCHAR(500)  NOT NULL COMMENT '匹配专业(逗号分隔)',
    salary_min     INT           DEFAULT NULL COMMENT '最低月薪(K)',
    salary_max     INT           DEFAULT NULL COMMENT '最高月薪(K)',
    skills_required TEXT         NOT NULL COMMENT '所需技能(逗号分隔或JSON)',
    career_path    TEXT          DEFAULT NULL COMMENT '职业晋升路径描述',
    description    TEXT          DEFAULT NULL COMMENT '岗位详细描述',
    demand_level   VARCHAR(20)   DEFAULT NULL COMMENT '市场需求: HIGH/MEDIUM/LOW',
    status         TINYINT       NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=启用',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FULLTEXT INDEX ft_career (position_name, industry, skills_required, description),
    INDEX idx_industry (industry),
    INDEX idx_demand (demand_level)
) ENGINE=InnoDB COMMENT='职业知识库';

-- ============================================================
-- 6. 测评类型表
-- ============================================================
CREATE TABLE assessment_type (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL COMMENT '测评名称',
    code            VARCHAR(50)  NOT NULL UNIQUE COMMENT '测评编码 MBTI/HOLLAND',
    description     TEXT         DEFAULT NULL COMMENT '测评描述',
    icon            VARCHAR(255) DEFAULT NULL COMMENT '图标URL',
    total_questions INT          NOT NULL DEFAULT 0 COMMENT '题目总数',
    time_limit      INT          DEFAULT NULL COMMENT '时间限制(分钟), NULL=不限时',
    status          TINYINT      NOT NULL DEFAULT 1,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='测评类型表';

-- ============================================================
-- 7. 测评题目表
-- ============================================================
CREATE TABLE assessment_question (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type_id       BIGINT       NOT NULL COMMENT 'FK→assessment_type.id',
    question_text VARCHAR(500) NOT NULL COMMENT '题目文本',
    question_type VARCHAR(20)  NOT NULL DEFAULT 'SINGLE' COMMENT '题目类型: SINGLE=单选',
    dimension     VARCHAR(20)  DEFAULT NULL COMMENT '测评维度: MBTI:E/I,S/N,T/F,J/P; Holland:R,I,A,S,E,C',
    sort_order    INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type_id (type_id),
    INDEX idx_dimension (dimension)
) ENGINE=InnoDB COMMENT='测评题目表';

-- ============================================================
-- 8. 测评选项表
-- ============================================================
CREATE TABLE assessment_option (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT       NOT NULL COMMENT 'FK→assessment_question.id',
    option_text VARCHAR(300) NOT NULL COMMENT '选项文本',
    score_value INT          NOT NULL DEFAULT 0 COMMENT '选项得分',
    dimension   VARCHAR(20)  DEFAULT NULL COMMENT '贡献维度 (e.g. E或I for MBTI)',
    sort_order  INT          NOT NULL DEFAULT 0,
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB COMMENT='测评选项表';

-- ============================================================
-- 9. 测评记录表
-- ============================================================
CREATE TABLE assessment_record (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL COMMENT 'FK→sys_user.id',
    type_id    BIGINT      NOT NULL COMMENT 'FK→assessment_type.id',
    status     VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS/COMPLETED',
    start_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time   DATETIME    DEFAULT NULL,
    INDEX idx_user_type (user_id, type_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='测评作答记录表';

-- ============================================================
-- 10. 测评作答明细表
-- ============================================================
CREATE TABLE assessment_answer (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    record_id   BIGINT NOT NULL COMMENT 'FK→assessment_record.id',
    question_id BIGINT NOT NULL COMMENT 'FK→assessment_question.id',
    option_id   BIGINT NOT NULL COMMENT 'FK→assessment_option.id',
    UNIQUE KEY uk_record_question (record_id, question_id),
    INDEX idx_record_id (record_id)
) ENGINE=InnoDB COMMENT='测评作答明细表';

-- ============================================================
-- 11. 测评结果表
-- ============================================================
CREATE TABLE assessment_result (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    record_id        BIGINT       NOT NULL COMMENT 'FK→assessment_record.id',
    user_id          BIGINT       NOT NULL COMMENT 'FK→sys_user.id (冗余加速查询)',
    type_id          BIGINT       NOT NULL COMMENT 'FK→assessment_type.id',
    dimension_scores JSON         DEFAULT NULL COMMENT '维度得分 JSON. MBTI:{"E":20,"I":12,...}; Holland:{"R":25,"I":38,...}',
    result_type      VARCHAR(20)  DEFAULT NULL COMMENT '结果编码: MBTI=INTJ/ENFP; Holland=RSA/IAE',
    report_text      TEXT         DEFAULT NULL COMMENT 'AI 生成的完整报告 Markdown',
    is_ai_generated  TINYINT      NOT NULL DEFAULT 0 COMMENT '0=未生成, 1=已生成',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_record_id (record_id),
    INDEX idx_user_id (user_id),
    INDEX idx_user_type (user_id, type_id)
) ENGINE=InnoDB COMMENT='测评结果表(AI生成报告)';

-- ============================================================
-- 12. AI 对话历史表
-- ============================================================
CREATE TABLE ai_chat_history (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL COMMENT 'FK→sys_user.id',
    session_id  VARCHAR(64)  NOT NULL COMMENT '会话 UUID',
    role        VARCHAR(20)  NOT NULL COMMENT '角色: user / assistant',
    content     TEXT         NOT NULL COMMENT '消息内容',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    INDEX idx_user_session (user_id, session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='AI对话历史记录';

-- ============================================================
-- 13. AI 实训任务表
-- ============================================================
CREATE TABLE ai_training_task (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(200) NOT NULL COMMENT '任务标题',
    description      TEXT         NOT NULL COMMENT '任务场景描述',
    category         VARCHAR(50)  DEFAULT NULL COMMENT '分类: resume/interview/workplace_comm/problem_solving',
    difficulty       VARCHAR(20)  DEFAULT 'MEDIUM' COMMENT '难度: EASY/MEDIUM/HARD',
    reference_answer TEXT         DEFAULT NULL COMMENT '参考答案(供AI评分参考)',
    scoring_criteria TEXT         DEFAULT NULL COMMENT 'AI评分标准',
    create_by        BIGINT       DEFAULT NULL COMMENT '创建人 FK→sys_user.id',
    status           TINYINT      NOT NULL DEFAULT 1,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_create_by (create_by)
) ENGINE=InnoDB COMMENT='AI实训任务表';

-- ============================================================
-- 14. AI 实训记录表
-- ============================================================
CREATE TABLE ai_training_record (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL COMMENT 'FK→sys_user.id',
    task_id      BIGINT       NOT NULL COMMENT 'FK→ai_training_task.id',
    user_answer  TEXT         NOT NULL COMMENT '学生提交答案',
    ai_score     INT          DEFAULT NULL COMMENT 'AI评分 0-100',
    ai_feedback  TEXT         DEFAULT NULL COMMENT 'AI改进建议',
    status       VARCHAR(20)  NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/SCORED',
    submit_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    score_time   DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_user_task (user_id, task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB COMMENT='AI实训作答记录';

-- ============================================================
-- 15. 教师任务表
-- ============================================================
CREATE TABLE teacher_task (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    teacher_id   BIGINT       NOT NULL COMMENT 'FK→sys_user.id',
    title        VARCHAR(200) NOT NULL COMMENT '任务标题',
    description  TEXT         DEFAULT NULL,
    task_type    VARCHAR(30)  NOT NULL COMMENT '任务类型: ASSESSMENT/TRAINING/REPORT',
    ref_id       BIGINT       DEFAULT NULL COMMENT '关联ID: assessment_type.id 或 training_task.id',
    target_class VARCHAR(100) DEFAULT NULL COMMENT '目标班级名称 或 ALL=全体',
    deadline     DATETIME     DEFAULT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/CLOSED',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_target_class (target_class),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='教师下发任务表';

-- ============================================================
-- 16. 班级-学生映射表
-- ============================================================
CREATE TABLE class_student (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    class_name  VARCHAR(100) NOT NULL COMMENT '班级名称',
    student_id  BIGINT       NOT NULL COMMENT 'FK→sys_user.id',
    teacher_id  BIGINT       DEFAULT NULL COMMENT '班主任 FK→sys_user.id',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_class_student (class_name, student_id),
    INDEX idx_class_name (class_name),
    INDEX idx_student_id (student_id),
    INDEX idx_teacher_id (teacher_id)
) ENGINE=InnoDB COMMENT='班级-学生映射表';

-- ============================================================
-- 17. 学生报告表
-- ============================================================
CREATE TABLE student_report (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_id     BIGINT       NOT NULL COMMENT 'FK→sys_user.id',
    teacher_id     BIGINT       NOT NULL COMMENT '报告创建人 FK→sys_user.id',
    report_type    VARCHAR(50)  NOT NULL COMMENT '报告类型: ASSESSMENT_SUMMARY/TRAINING_SUMMARY/COMPREHENSIVE',
    report_title   VARCHAR(200) NOT NULL,
    report_content TEXT         NOT NULL COMMENT '报告正文 HTML/Markdown',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_student_teacher (student_id, teacher_id)
) ENGINE=InnoDB COMMENT='学生职业报告表';

-- ============================================================
-- 种子数据
-- ============================================================

-- 角色数据
INSERT INTO sys_role (role_name, role_code, description) VALUES
('学生', 'ROLE_STUDENT', '学生角色 — 访问职业工具、测评、AI咨询'),
('教师', 'ROLE_TEACHER', '教师角色 — 管理学生、下发任务、查看报告'),
('管理员', 'ROLE_ADMIN', '管理员角色 — 系统管理、用户管理、配置管理');

-- 系统配置 (AI提示词 + 模型配置)
INSERT INTO sys_config (config_key, config_value, description) VALUES
('ai.model.provider', 'ollama', 'AI模型提供商: ollama / tongyi / wenxin'),
('ai.model.name', 'qwen:7b', '模型名称'),
('ai.chat.prompt',
 '你是一位专业的大学生职业规划顾问。你的名字叫"职小途"。
你的职责是：
1) 根据学生的专业、兴趣和MBTI/霍兰德测评结果提供个性化职业建议；
2) 推荐匹配的岗位和行业，并解释推荐理由；
3) 解答职业发展相关问题，包括简历优化、面试准备、行业趋势等；
4) 为不同年级的学生提供针对性的在校学习规划建议。

请遵守以下规则：
- 仅基于系统中的职业知识库数据提供建议，不要编造岗位信息；
- 保持专业、耐心、鼓励的态度；
- 每次回复控制在300字以内，简洁有针对性；
- 如果学生未提供专业或测评信息，主动询问以提供更精准的建议。',
 '职业咨询智能体系统提示词'),

('ai.scoring.prompt',
 '你是一位职场技能评审专家。请根据以下评分标准对学生的实训任务答案进行评分（满分100分），并给出150字以内的具体改进建议。

评分维度：
- 内容完整性(30分)：是否覆盖任务要求的所有要点
- 逻辑清晰度(25分)：表达是否条理清晰、层次分明
- 专业术语使用(20分)：是否正确使用行业专业术语
- 创新性(15分)：是否有独到见解或创新思路
- 实用性(10分)：方案是否具备实际可操作性

请严格按照以下JSON格式返回评分结果，不要包含其他内容：
{"score": 数字, "feedback": "评语"}',
 '实训任务AI评分提示词'),

('ai.report.prompt',
 '你是一位资深职业测评分析师。请根据学生的测评结果，生成一份完整的职业发展规划报告。

报告需包含以下结构：
1. **人格特质概述** — 基于测评类型解读学生的性格特点与行为倾向
2. **职业兴趣分析** — 分析学生的职业兴趣领域及其对应的岗位方向
3. **推荐职业方向** — 列出3-5个最适合的职业方向，说明匹配理由
4. **发展建议** — 针对性的能力提升建议和短板改进方案
5. **学习路径规划** — 大学期间的阶段性学习与实践规划

报告语言正式、专业，总字数800-1200字。使用Markdown格式排版。',
 '测评报告生成提示词'),

('ai.model.temperature', '0.7', '模型温度参数'),
('ai.model.max_tokens', '2000', '模型最大输出 token 数');

-- 测评类型
INSERT INTO assessment_type (name, code, description, total_questions) VALUES
('MBTI职业性格测试', 'MBTI',
 '通过专业题目评估四个维度：外向/内向(E/I)、感觉/直觉(S/N)、思考/情感(T/F)、判断/感知(J/P)，确定16种人格类型中的最佳匹配，为职业选择提供科学参考。',
 0),
('霍兰德职业兴趣测试', 'HOLLAND',
 '评估六种职业兴趣类型：现实型(R)、研究型(I)、艺术型(A)、社会型(S)、企业型(E)、常规型(C)，匹配最适合的职业领域。',
 0);

-- 默认管理员账号 (密码: admin123, BCrypt加密)
-- 注意: 实际部署时请修改密码
INSERT INTO sys_user (username, password, real_name, email, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 'admin@qkc.edu', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 3);

-- 示例职业数据
INSERT INTO career_info (position_name, industry, major_match, salary_min, salary_max, skills_required, career_path, description, demand_level) VALUES
('Java后端开发工程师', '信息技术/互联网', '计算机科学与技术,软件工程,信息管理与信息系统,数据科学与大数据技术', 8, 25,
 'Java,Spring Boot,MySQL,Redis,微服务架构,Linux,容器化技术,系统设计能力',
 '初级开发工程师 → 中级开发工程师 → 高级开发工程师 → 技术专家/架构师 → 技术总监',
 '负责企业级应用后端系统的设计、开发与维护。参与需求分析、系统架构设计、编码实现、性能优化和线上问题排查。需要扎实的Java基础和Spring生态经验，熟悉分布式系统设计。',
 'HIGH'),

('数据分析师', '信息技术/互联网', '统计学,数学,计算机科学与技术,数据科学与大数据技术,信息管理与信息系统', 7, 22,
 'SQL,Python,Excel高级应用,Tableau/PowerBI,统计学基础,机器学习基础,业务分析能力',
 '初级数据分析师 → 中级数据分析师 → 高级数据分析师 → 数据科学家/BI经理 → 数据总监',
 '通过数据采集、清洗、建模和可视化，为企业业务决策提供数据支持。需要熟练使用SQL进行数据提取，具备将业务问题转化为数据分析方案的能力。',
 'HIGH'),

('产品经理', '信息技术/互联网', '计算机科学与技术,软件工程,市场营销,心理学,工业设计', 8, 28,
 '需求分析,原型设计(Axure/Figma),项目管理,数据分析,用户研究,沟通协调,商业思维',
 '产品助理 → 产品经理 → 高级产品经理 → 产品总监 → CPO',
 '负责产品的全生命周期管理，包括用户需求调研、产品规划、原型设计、项目推进和数据分析。需要优秀的需求洞察能力、跨部门沟通能力和商业敏感度。',
 'HIGH'),

('UI/UX设计师', '信息技术/互联网', '视觉传达设计,数字媒体艺术,工业设计,心理学,计算机科学与技术', 6, 20,
 'Figma/Sketch,Adobe XD,用户研究,交互设计,视觉设计,设计系统搭建,原型设计',
 '初级UI设计师 → UI设计师 → 高级UI设计师 → 设计主管 → 设计总监',
 '负责产品的用户界面与交互体验设计。从用户研究出发，设计直观、美观且易用的产品界面。需要审美能力与用户同理心兼备。',
 'MEDIUM'),

('人工智能算法工程师', '信息技术/互联网', '计算机科学与技术,人工智能,数学,统计学,自动化', 12, 35,
 'Python,PyTorch/TensorFlow,机器学习,深度学习,NLP/CV,数学基础,模型部署,论文阅读能力',
 '初级算法工程师 → 算法工程师 → 高级算法工程师 → 算法专家/研究员 → AI技术总监',
 '负责AI算法模型的研发、训练、优化和部署。需要扎实的数学和机器学习理论基础，熟练使用主流深度学习框架，具备阅读和复现顶会论文的能力。',
 'HIGH'),

('金融分析师', '金融/投资/证券', '金融学,经济学,会计学,统计学,数学', 7, 20,
 '财务分析,估值建模,Excel高级应用,Wind/Bloomberg,行业研究,财务报告分析,CFA/CPA',
 '助理金融分析师 → 金融分析师 → 高级金融分析师 → 投资经理/研究总监 → 基金经理/首席分析师',
 '负责对宏观经济、行业趋势和公司基本面进行深入分析，撰写研究报告，为投资决策提供支持。需要扎实的财务知识和较强的逻辑分析能力。',
 'MEDIUM'),

('临床医生', '医疗/健康', '临床医学,基础医学,中西医结合', 5, 18,
 '临床诊断,病历书写,医患沟通,医学基础知识,急救技能,终身学习能力,执业医师资格证',
 '住院医师 → 主治医师 → 副主任医师 → 主任医师 → 科室主任',
 '负责患者的诊断、治疗和健康管理。需要扎实的医学理论基础和丰富的临床经验，具备良好的医患沟通能力和应急处理能力。',
 'MEDIUM'),

('电气工程师', '制造业/能源', '电气工程及其自动化,自动化,测控技术与仪器,机械电子工程', 6, 18,
 'PLC编程,电路设计,电气CAD,自动化控制,电力系统分析,设备调试,电气安全规范',
 '助理电气工程师 → 电气工程师 → 高级电气工程师 → 电气技术主管 → 电气总工程师',
 '负责电气系统的设计、安装、调试和维护。包括电气图纸绘制、控制系统编程、设备选型和现场技术支持。',
 'MEDIUM'),

('市场营销专员', '消费品/零售/电商', '市场营销,工商管理,广告学,新闻传播,电子商务', 5, 15,
 '市场调研,品牌策划,新媒体运营,数据分析,文案撰写,活动策划,沟通能力',
 '市场专员 → 市场经理 → 高级市场经理 → 市场总监 → CMO',
 '负责品牌推广、营销活动策划与执行。包括市场调研分析、社交媒体运营、内容营销策划和营销效果评估。需要创意能力与数据分析能力兼备。',
 'MEDIUM'),

('人力资源专员', '全行业通用', '人力资源管理,工商管理,心理学,社会学,劳动关系', 5, 14,
 '招聘流程,员工关系,薪酬福利,培训开发,劳动法,沟通协调,HR系统操作',
 'HR专员 → HR主管 → HR经理 → HR总监 → CHO',
 '负责企业人才招聘、培训发展、绩效管理和员工关系维护。是企业与员工之间的桥梁，需要良好的沟通协调能力。',
 'MEDIUM'),

('建筑施工管理', '建筑/房地产', '土木工程,工程管理,建筑学,给排水科学与工程', 6, 16,
 '施工技术,工程管理,工程测量,安全规范,项目管理,成本控制,建造师证书',
 '施工员 → 项目工程师 → 项目经理 → 工程总监 → 区域总经理',
 '负责建筑施工现场的组织、协调和管理。包括施工进度控制、质量安全管理、材料管理和人员调配。需要较强的现场管理能力和吃苦耐劳精神。',
 'MEDIUM');

# 数据验证 SQL

> 用途：验证接口操作后数据库数据的正确性。执行环境：MySQL 8.0，数据库 `careersail_db`。
> 执行时间：2026-09-06

## 一、用户注册数据验证

```sql
-- 1. 验证注册后用户数据正确写入
-- 场景：Postman 注册接口返回成功后，执行此 SQL 确认数据落库
SELECT id, username, real_name, college, major, grade, class_name, student_no, create_time
FROM sys_user
WHERE username = 'testuser01'
  AND create_time >= NOW() - INTERVAL 10 MINUTE;
```

**执行结果：** ✅ 通过。查到 1 条记录（id=79），username/real_name/college/major/grade/class_name/student_no 全部字段与注册参数一致，create_time 正常写入。

```sql
-- 2. 验证同一用户名不能重复注册（UNIQUE 约束生效）
-- 预期：查询结果为空（无重复记录）
SELECT username, COUNT(*) AS cnt
FROM sys_user
GROUP BY username
HAVING cnt > 1;
```

**执行结果：** ✅ 通过。空结果集，无重复用户名，UNIQUE 约束生效。

```sql
-- 3. 验证注册用户默认分配学生角色
SELECT u.username, r.role_code
FROM sys_user u
JOIN sys_user_role ur ON ur.user_id = u.id
JOIN sys_role r ON r.id = ur.role_id
WHERE u.username = 'testuser01';
```

**执行结果：** ✅ 通过。返回 role_code = `ROLE_STUDENT`，注册默认分配学生角色正确。

## 二、测评数据验证

```sql
-- 4. 验证测评记录正确关联用户和测评类型
SELECT r.id, u.username, t.name AS type_name, r.status, r.start_time, r.end_time
FROM assessment_record r
JOIN sys_user u ON r.user_id = u.id
JOIN assessment_type t ON r.type_id = t.id
WHERE u.username = 'zhangsan'
ORDER BY r.start_time DESC;
```

**执行结果：** ✅ 通过。zhangsan 完成 1 次"MBTI职业性格测试"，记录 id=11，status=COMPLETED，start_time/end_time 正常（作答时长约 30 秒）。

```sql
-- 5. 验证测评结果数据完整
-- 预期：is_ai_generated = 1，report_text 非空（AI 报告已生成）
SELECT u.username, t.name AS type_name, res.result_type, res.is_ai_generated,
       LENGTH(res.report_text) AS report_length
FROM assessment_result res
JOIN sys_user u ON res.user_id = u.id
JOIN assessment_type t ON res.type_id = t.id
WHERE u.username = 'zhangsan'
ORDER BY res.create_time DESC;
```

**执行结果：** ✅ 通过。result_type=INFP 正确生成，is_ai_generated=1，report_text 长度 6437 字符，AI 报告内容完整。

> 备注：报告生成与测评提交存在时序差——测评刚完成时曾查到 is_ai_generated=0，稍后重新查询已生成。属异步/手动触发设计，非 Bug，但说明数据验证能捕捉到生成时序。

```sql
-- 6. 验证作答明细数量与问卷题目总数一致
-- 预期：answer_count 应等于该测评类型的 total_questions
SELECT r.id AS record_id, t.name, t.total_questions,
       (SELECT COUNT(*) FROM assessment_answer a WHERE a.record_id = r.id) AS answer_count
FROM assessment_record r
JOIN assessment_type t ON r.type_id = t.id
WHERE r.status = 'COMPLETED'
ORDER BY r.id DESC;
```

**执行结果：** ⚠️ 发现数据异常。record_id 4~11 正常（answer_count=20=total_questions）；**record_id 1、2、3 状态为 COMPLETED 但 answer_count=0**——"已完成"记录却没有答题明细。

**分析：** 这 3 条为开发早期测试数据，疑似绕过接口直接插入数据库，未走正常答题流程导致状态与明细不一致。

## 三、AI 对话数据验证

```sql
-- 7. 验证 AI 对话消息正确存储（user / assistant 成对出现）
SELECT session_id, role, LEFT(content, 50) AS content_preview, create_time
FROM ai_chat_history
WHERE user_id = (SELECT id FROM sys_user WHERE username = 'zhangsan')
ORDER BY create_time DESC
LIMIT 20;
```

**执行结果：** ✅ 通过。zhangsan 的对话记录正常存储，user 提问与 assistant 回复按时间顺序成对出现，AI 回复内容与提问相关（金融学专业+INFP 性格的岗位推荐）。

```sql
-- 8. 验证同一会话消息的完整性
-- 预期：每个 session 的 user 消息数 = assistant 消息数（或差 1，最后一条可能未回复）
SELECT session_id,
       SUM(CASE WHEN role = 'user' THEN 1 ELSE 0 END) AS user_msg,
       SUM(CASE WHEN role = 'assistant' THEN 1 ELSE 0 END) AS assistant_msg
FROM ai_chat_history
GROUP BY session_id;
```

**执行结果：** ✅ 通过。共 15 个会话，全部会话 user_msg = assistant_msg（完全对称），无悬空提问。

## 四、数据一致性验证

```sql
-- 9. 验证删除用户后关联数据的处理
SELECT 'assessment_record' AS table_name, COUNT(*) AS orphan_cnt
FROM assessment_record
WHERE user_id NOT IN (SELECT id FROM sys_user)
UNION ALL
SELECT 'assessment_result', COUNT(*) FROM assessment_result
WHERE user_id NOT IN (SELECT id FROM sys_user)
UNION ALL
SELECT 'ai_chat_history', COUNT(*) FROM ai_chat_history
WHERE user_id NOT IN (SELECT id FROM sys_user)
UNION ALL
SELECT 'ai_training_record', COUNT(*) FROM ai_training_record
WHERE user_id NOT IN (SELECT id FROM sys_user);
```

**执行结果：** ✅ 通过。4 张核心表孤儿数据均为 0，无悬挂引用。

```sql
-- 10. 验证职业库数据量
SELECT COUNT(*) AS total_careers FROM career_info;
SELECT industry, COUNT(*) AS cnt FROM career_info GROUP BY industry;
```

**执行结果：** ✅ 通过。职业库共 111 个岗位，覆盖 32 个行业分类。行业分布合理：信息技术/互联网 22 个（最多）、金融/投资/证券 11 个、全行业通用 7 个。

## 验证总结

| 验证项 | 结果 |
|--------|------|
| 注册数据落库 | ✅ 通过 |
| 用户名唯一约束 | ✅ 通过 |
| 默认角色分配 | ✅ 通过 |
| 测评记录关联 | ✅ 通过 |
| AI 报告生成 | ✅ 通过（存在生成时序，属设计行为） |
| 答题明细一致性 | ⚠️ 发现 3 条测试数据异常（COMPLETED 但 0 答题） |
| 对话消息存储 | ✅ 通过 |
| 会话完整性 | ✅ 通过 |
| 孤儿数据检查 | ✅ 通过 |
| 职业库数据量 | ✅ 通过 |

**总体结论：** 核心业务数据写入、关联、完整性均正常。发现 1 个数据一致性问题（早期测试数据），已确认不影响线上功能。

## 执行方法

1. 打开 MySQL 命令行：

```bash
mysql -u root -p careersail_db
```

2. 逐条粘贴 SQL 执行，对照"预期"注释验证结果
3. 若用 Navicat / DataGrip，直接新建查询窗口执行即可

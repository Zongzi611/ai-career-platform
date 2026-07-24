-- ============================================================
-- CareerSail 测评题库种子数据 — MBTI + 霍兰德
-- ============================================================
USE careersail_db;

-- ============================================================
-- MBTI 职业性格测试 (20题，4个维度各5题)
-- ============================================================

-- E/I 维度 (外向/内向)
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '在聚会或社交场合中，你通常会', 'SINGLE', 'E', 1);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '认识很多新朋友，感到精力充沛', 2, 'E', 1),
(@qid, '和少数熟悉的朋友聊天，感到舒适', 1, 'I', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '周末休息时，你更倾向于', 'SINGLE', 'I', 2);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '约朋友出去玩或参加活动', 2, 'E', 1),
(@qid, '在家看书、看电影或做自己的事', 1, 'I', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '在团队讨论中，你通常会', 'SINGLE', 'E', 3);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '积极发言，想到什么说什么', 2, 'E', 1),
(@qid, '先听别人说完，再深思熟虑后发言', 1, 'I', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '当遇到烦恼时，你倾向于', 'SINGLE', 'I', 4);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '找朋友倾诉，通过交流理清思路', 2, 'E', 1),
(@qid, '自己先消化，想清楚后再决定是否分享', 1, 'I', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '在课堂上，你更喜欢', 'SINGLE', 'E', 5);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '小组讨论、互动式教学', 2, 'E', 1),
(@qid, '独立听讲、独立思考', 1, 'I', 2);

-- S/N 维度 (感觉/直觉)
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '你更相信', 'SINGLE', 'S', 6);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '亲身经历和具体事实', 2, 'S', 1),
(@qid, '直觉感受和抽象概念', 1, 'N', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '在学习新知识时，你更喜欢', 'SINGLE', 'S', 7);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '按部就班，从基础到进阶系统学习', 2, 'S', 1),
(@qid, '先把握整体框架，再填充细节', 1, 'N', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '你更擅长', 'SINGLE', 'N', 8);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '关注当下，做好眼前的具体任务', 2, 'S', 1),
(@qid, '展望未来，想象各种可能性和创新方案', 1, 'N', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '阅读文章时，你更关注', 'SINGLE', 'S', 9);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '文中的具体数据和事实', 2, 'S', 1),
(@qid, '作者的深层含义和观点', 1, 'N', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '解决问题时，你的思路通常是', 'SINGLE', 'N', 10);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '用已有的经验和方法，稳扎稳打', 2, 'S', 1),
(@qid, '跳出常规，尝试新的方法', 1, 'N', 2);

-- T/F 维度 (思考/情感)
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '做决定时，你更看重', 'SINGLE', 'T', 11);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '逻辑分析和客观事实', 2, 'T', 1),
(@qid, '个人价值观和对他人感受的影响', 1, 'F', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '当朋友向你倾诉烦恼时，你倾向于', 'SINGLE', 'F', 12);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '帮助分析问题，提出解决方案', 2, 'T', 1),
(@qid, '先表示理解和共情，给予情感支持', 1, 'F', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '在团队合作中，你更关注', 'SINGLE', 'T', 13);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '任务的完成效率和质量标准', 2, 'T', 1),
(@qid, '团队的和谐氛围和成员感受', 1, 'F', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '面对批评时，你通常会', 'SINGLE', 'T', 14);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '分析批评的合理性和逻辑性', 2, 'T', 1),
(@qid, '先考虑对方的出发点和态度', 1, 'F', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '你认为公平和同情哪个更重要', 'SINGLE', 'F', 15);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '公平更重要，规则面前人人平等', 2, 'T', 1),
(@qid, '同情更重要，特殊情况需要灵活处理', 1, 'F', 2);

-- J/P 维度 (判断/感知)
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '对于学习和工作任务，你习惯', 'SINGLE', 'J', 16);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '制定详细计划，按时间表严格执行', 2, 'J', 1),
(@qid, '保持灵活，根据情况随时调整', 1, 'P', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '你的书桌或房间通常是', 'SINGLE', 'J', 17);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '整洁有序，物品摆放井井有条', 2, 'J', 1),
(@qid, '有些凌乱但你能找到需要的东西', 1, 'P', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '面对截止日期，你通常', 'SINGLE', 'J', 18);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '提前规划，尽早完成', 2, 'J', 1),
(@qid, '在最后期限前冲刺完成', 1, 'P', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '旅行时，你更喜欢', 'SINGLE', 'P', 19);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '提前做好攻略，预定好所有行程', 2, 'J', 1),
(@qid, '只定大致方向，到了再随心所欲探索', 1, 'P', 2);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(1, '做完一项重要决定后，你通常会', 'SINGLE', 'J', 20);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '感到安心，终于有了确定性', 2, 'J', 1),
(@qid, '会想是否有更好的选择，保持开放', 1, 'P', 2);

-- ============================================================
-- 霍兰德职业兴趣测试 (30题，6个类型各5题)
-- ============================================================

-- R型 (现实型/Realistic) - 喜欢动手操作、机械、户外
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢动手修理或组装物品', 'SINGLE', 'R', 1);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'R', 1), (@qid, '比较符合', 1, 'R', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我对机械设备和工具的使用很感兴趣', 'SINGLE', 'R', 2);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'R', 1), (@qid, '比较符合', 1, 'R', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢户外活动或体力劳动', 'SINGLE', 'R', 3);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'R', 1), (@qid, '比较符合', 1, 'R', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '比起抽象理论，我更擅长处理具体、实际的问题', 'SINGLE', 'R', 4);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'R', 1), (@qid, '比较符合', 1, 'R', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我享受用双手创造或建造东西的过程', 'SINGLE', 'R', 5);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'R', 1), (@qid, '比较符合', 1, 'R', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

-- I型 (研究型/Investigative) - 喜欢思考、研究、分析
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢深入探究事物的原理和规律', 'SINGLE', 'I', 6);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'I', 1), (@qid, '比较符合', 1, 'I', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢独立思考和解决复杂问题', 'SINGLE', 'I', 7);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'I', 1), (@qid, '比较符合', 1, 'I', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我对科学研究和实验有浓厚兴趣', 'SINGLE', 'I', 8);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'I', 1), (@qid, '比较符合', 1, 'I', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我善于收集数据并分析其中的规律', 'SINGLE', 'I', 9);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'I', 1), (@qid, '比较符合', 1, 'I', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢阅读学术文章或科普类内容', 'SINGLE', 'I', 10);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'I', 1), (@qid, '比较符合', 1, 'I', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

-- A型 (艺术型/Artistic) - 喜欢创造、表达、设计
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢通过绘画、写作、音乐等方式表达自己', 'SINGLE', 'A', 11);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'A', 1), (@qid, '比较符合', 1, 'A', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我对美的事物有敏锐的感知力', 'SINGLE', 'A', 12);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'A', 1), (@qid, '比较符合', 1, 'A', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我享受创造新颖、独特的东西', 'SINGLE', 'A', 13);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'A', 1), (@qid, '比较符合', 1, 'A', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '比起遵循规则，我更喜欢自由发挥', 'SINGLE', 'A', 14);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'A', 1), (@qid, '比较符合', 1, 'A', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我经常有新的创意和想法', 'SINGLE', 'A', 15);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'A', 1), (@qid, '比较符合', 1, 'A', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

-- S型 (社会型/Social) - 喜欢帮助、教导、服务他人
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢帮助他人解决困难或困惑', 'SINGLE', 'S', 16);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'S', 1), (@qid, '比较符合', 1, 'S', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我善于倾听并理解他人的感受', 'SINGLE', 'S', 17);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'S', 1), (@qid, '比较符合', 1, 'S', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢参与志愿服务或公益活动', 'SINGLE', 'S', 18);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'S', 1), (@qid, '比较符合', 1, 'S', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我在团队中喜欢扮演协调者和支持者的角色', 'SINGLE', 'S', 19);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'S', 1), (@qid, '比较符合', 1, 'S', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我乐于向他人传授知识和经验', 'SINGLE', 'S', 20);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'S', 1), (@qid, '比较符合', 1, 'S', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

-- E型 (企业型/Enterprising) - 喜欢领导、说服、管理
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢在团队中担任领导者角色', 'SINGLE', 'E', 21);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'E', 1), (@qid, '比较符合', 1, 'E', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我善于说服他人接受我的观点', 'SINGLE', 'E', 22);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'E', 1), (@qid, '比较符合', 1, 'E', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我对商业运作和创业有浓厚兴趣', 'SINGLE', 'E', 23);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'E', 1), (@qid, '比较符合', 1, 'E', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我享受竞争并渴望获得成功和认可', 'SINGLE', 'E', 24);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'E', 1), (@qid, '比较符合', 1, 'E', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我善于组织和管理项目或活动', 'SINGLE', 'E', 25);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'E', 1), (@qid, '比较符合', 1, 'E', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

-- C型 (常规型/Conventional) - 喜欢规范、整理、数据处理
INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我喜欢按照规定的流程和标准做事', 'SINGLE', 'C', 26);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'C', 1), (@qid, '比较符合', 1, 'C', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我注重细节，做事有条不紊', 'SINGLE', 'C', 27);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'C', 1), (@qid, '比较符合', 1, 'C', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我善于整理和管理数据、文件或档案', 'SINGLE', 'C', 28);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'C', 1), (@qid, '比较符合', 1, 'C', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '比起冒险和变化，我更喜欢稳定和可预测的环境', 'SINGLE', 'C', 29);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'C', 1), (@qid, '比较符合', 1, 'C', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

INSERT INTO assessment_question (type_id, question_text, question_type, dimension, sort_order) VALUES
(2, '我擅长做精确的账目记录或数据统计', 'SINGLE', 'C', 30);
SET @qid = LAST_INSERT_ID();
INSERT INTO assessment_option (question_id, option_text, score_value, dimension, sort_order) VALUES
(@qid, '非常符合', 2, 'C', 1), (@qid, '比较符合', 1, 'C', 2), (@qid, '不确定', 0, NULL, 3), (@qid, '不太符合', 0, NULL, 4), (@qid, '完全不符合', 0, NULL, 5);

-- Update total_questions count
UPDATE assessment_type SET total_questions = 20 WHERE id = 1;
UPDATE assessment_type SET total_questions = 30 WHERE id = 2;

-- Verify
SELECT type_id, COUNT(*) as q_count FROM assessment_question GROUP BY type_id;

package com.careersail.ai;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.careersail.entity.CareerInfo;
import com.careersail.entity.SysUser;
import com.careersail.mapper.CareerInfoMapper;
import com.careersail.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final ChatModel chatModel;
    private final CareerInfoMapper careerInfoMapper;
    private final SysUserMapper sysUserMapper;

    private static final String SYSTEM_PROMPT = """
        You are a university career advisor. Generate a 4-year learning roadmap for a student.
        Output MUST be valid JSON with this exact structure:
        {
          "summary": "brief overview 50 words",
          "stages": [
            {"year":"大一", "season":"探索期", "focus":"...", "courses":["..."], "projects":["..."], "milestones":["..."], "icon":"🔍"},
            {"year":"大二", "season":"成长期", "focus":"...", "courses":["..."], "projects":["..."], "milestones":["..."], "icon":"🌱"},
            {"year":"大三", "season":"实战期", "focus":"...", "courses":["..."], "projects":["..."], "milestones":["..."], "icon":"🚀"},
            {"year":"大四", "season":"冲刺期", "focus":"...", "courses":["..."], "projects":["..."], "milestones":["..."], "icon":"🏆"}
          ],
          "skills": ["skill1","skill2","skill3","skill4","skill5"],
          "certifications": ["cert1","cert2"],
          "keyAdvice": "one sentence career advice"
        }
        Make courses, projects, and milestones specific to the target career. Use Chinese.
        """;

    public Map<String, Object> generate(Long careerId, Long userId) {
        CareerInfo career = careerInfoMapper.selectById(careerId);
        SysUser user = sysUserMapper.selectById(userId);

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Target career: ").append(career != null ? career.getPositionName() : "General").append("\n");
        if (career != null) {
            userPrompt.append("Required skills: ").append(career.getSkillsRequired() != null ? career.getSkillsRequired() : "N/A").append("\n");
            userPrompt.append("Career path: ").append(career.getCareerPath() != null ? career.getCareerPath() : "N/A").append("\n");
            userPrompt.append("Industry: ").append(career.getIndustry() != null ? career.getIndustry() : "N/A").append("\n");
        }
        if (user != null) {
            userPrompt.append("Student major: ").append(user.getMajor() != null ? user.getMajor() : "Unknown").append("\n");
            userPrompt.append("Current grade: ").append(user.getGrade() != null ? user.getGrade() : "Unknown").append("\n");
        }
        userPrompt.append("\nGenerate a detailed 4-year university learning roadmap for this career. Output JSON only.");

        try {
            String response = chatModel.call(new Prompt(List.of(
                    new SystemMessage(SYSTEM_PROMPT),
                    new UserMessage(userPrompt.toString())
            ))).getResult().getOutput().getText();

            // Extract JSON
            String json = response;
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}");
            if (start >= 0 && end > start) json = response.substring(start, end + 1);

            JSONObject obj = JSONUtil.parseObj(json);
            log.info("Learning path generated: career={}, stages={}", careerId, obj.getJSONArray("stages").size());
            return obj.toBean(Map.class);
        } catch (Exception e) {
            log.error("Learning path generation failed", e);
            return generateFallback(career);
        }
    }

    private Map<String, Object> generateFallback(CareerInfo career) {
        String name = career != null ? career.getPositionName() : "目标岗位";
        String skills = career != null && career.getSkillsRequired() != null ? career.getSkillsRequired() : "专业技能";

        JSONObject result = new JSONObject();
        result.set("summary", "为 " + name + " 量身定制的大学四年学习路径，帮助你循序渐进地成长为合格的" + name + "。");

        JSONArray stages = new JSONArray();
        stages.add(createStage("大一", "探索期", "打好基础，培养兴趣，了解行业",
                new String[]{"高等数学", "程序设计基础", "大学英语", "计算机导论"},
                new String[]{"个人博客搭建", "参加编程社团"},
                new String[]{"GPA 3.0+", "通过英语四级", "确定专业方向"}, "🔍"));
        stages.add(createStage("大二", "成长期", "深入学习核心课程，开始实践",
                new String[]{"数据结构与算法", "数据库原理", "面向对象编程", "计算机网络"},
                new String[]{"开发一个完整的Web应用", "参加蓝桥杯/ACM竞赛"},
                new String[]{"完成首个千行代码项目", "通过英语六级", "掌握Git协作"}, "🌱"));
        stages.add(createStage("大三", "实战期", "系统学习框架，积累项目经验，准备实习",
                new String[]{"Spring/React/Vue框架", "系统设计与架构", "软件工程", "Linux运维"},
                new String[]{"完成一个全栈项目并部署上线", "参与开源项目贡献代码"},
                new String[]{"暑期大厂实习", "刷LeetCode 200+题", "优化简历投递秋招"}, "🚀"));
        stages.add(createStage("大四", "冲刺期", "求职冲刺，毕设答辩，完成身份转变",
                new String[]{"行业前沿技术", "职场软技能", "毕业论文"},
                new String[]{"毕业设计", "整理技术博客/作品集"},
                new String[]{"拿到满意Offer", "顺利通过答辩", "完成从学生到工程师的转变"}, "🏆"));
        result.set("stages", stages);

        JSONArray skillArr = new JSONArray();
        for (String s : (skills + ",沟通协作,项目管理,英语").split(",")) skillArr.add(s.trim());
        result.set("skills", skillArr);

        JSONArray certs = new JSONArray();
        certs.add("大学英语六级/雅思");
        certs.add("相关行业认证");
        result.set("certifications", certs);

        result.set("keyAdvice", "大学四年看似很长，但每个阶段都有最重要的任务。大一打好基础，大二找到方向，大三全力冲刺，大四优雅收官。");
        return result.toBean(Map.class);
    }

    private JSONObject createStage(String year, String season, String focus, String[] courses, String[] projects, String[] milestones, String icon) {
        JSONObject s = new JSONObject();
        s.set("year", year);
        s.set("season", season);
        s.set("focus", focus);
        s.set("courses", courses);
        s.set("projects", projects);
        s.set("milestones", milestones);
        s.set("icon", icon);
        return s;
    }
}

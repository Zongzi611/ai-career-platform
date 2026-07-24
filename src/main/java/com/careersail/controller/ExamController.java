package com.careersail.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.careersail.common.Result;
import com.careersail.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final SysUserMapper sysUserMapper;
    private final Environment env;

    private final RestClient restClient = RestClient.builder()
            .requestFactory(clientHttpRequestFactory())
            .build();

    private static SimpleClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30_000);   // 连接超时 30s
        factory.setReadTimeout(300_000);     // 读取超时 5min（20 题需要较长时间）
        return factory;
    }

    /**
     * 直接 HTTP 调用 DeepSeek API，绕过 Spring AI 兼容层
     */
    private String callDeepSeekApi(String userPrompt) {
        String apiBaseUrl = env.getProperty("spring.ai.openai.base-url", "https://api.deepseek.com/v1");
        String apiKey = env.getProperty("spring.ai.openai.api-key", "");
        String modelName = env.getProperty("spring.ai.openai.chat.options.model", "deepseek-v4-pro");

        log.info("Calling DeepSeek API: url={}, model={}, keyLen={}",
                apiBaseUrl + "/chat/completions", modelName, apiKey.length());

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4000);

        String response = restClient.post()
                .uri(apiBaseUrl + "/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        // 解析 OpenAI 兼容返回格式: choices[0].message.content
        JSONObject respJson = JSONUtil.parseObj(response);
        return respJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getStr("content");
    }

    /**
     * 调试端点：直接测试 DeepSeek API 连通性
     */
    @GetMapping("/ping")
    public Result<?> ping() {
        StringBuilder sb = new StringBuilder();
        sb.append("env=").append(env != null).append("|");
        try {
            String url = env.getProperty("spring.ai.openai.base-url", "N/A");
            String key = env.getProperty("spring.ai.openai.api-key", "N/A");
            String model = env.getProperty("spring.ai.openai.chat.options.model", "N/A");
            sb.append("url=").append(url).append("|model=").append(model).append("|keyLen=").append(key != null ? key.length() : 0);
            return Result.ok(Map.of("debug", sb.toString()));
        } catch (Exception e) {
            sb.append("ERROR:").append(e.getClass().getSimpleName()).append(":").append(e.getMessage());
            return Result.ok(Map.of("debug", sb.toString()));
        }
    }

    @PostMapping("/start")
    public Result<?> start(@RequestBody Map<String, String> body) {
        String position = body.getOrDefault("targetPosition", "通用技术岗");
        String type = body.getOrDefault("examType", "technical");

        String prompt = "你是技术笔试出题官。为" + position + "岗位出20道" + (type.equals("technical")?"技术":"综合") + "笔试题，覆盖该岗位核心知识点，难度由浅入深。每道题包含：题目、4个选项(A/B/C/D)、正确答案、解析。\n"
                + "返回JSON数组：[{\"question\":\"...\",\"options\":[\"A. ...\",\"B. ...\",\"C. ...\",\"D. ...\"],\"answer\":\"A\",\"explanation\":\"...\"}]，共20道。";

        try {
            log.info("AI exam generation started: position={}, type={}", position, type);
            String resp = callDeepSeekApi(prompt);
            log.info("AI exam response received, length={}, preview={}",
                    resp.length(), resp.substring(0, Math.min(200, resp.length())));

            int startIdx = resp.indexOf("[");
            int endIdx = resp.lastIndexOf("]");
            if (startIdx < 0 || endIdx <= startIdx) {
                log.warn("AI returned non-JSON-array format, falling back. Full response:\n{}", resp);
                return Result.ok(Map.of("questions", generateFallback(position), "totalQuestions", 20));
            }

            String json = resp.substring(startIdx, endIdx + 1);
            return Result.ok(Map.of("questions", JSONUtil.parseArray(json), "totalQuestions", 20));
        } catch (Exception e) {
            log.error("AI exam generation FAILED, using fallback questions. Error type={}, message={}",
                    e.getClass().getSimpleName(), e.getMessage(), e);
            return Result.ok(Map.of("questions", generateFallback(position), "totalQuestions", 20));
        }
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Map<String, Object> body) {
        List<Map<String, String>> answers = (List<Map<String, String>>) body.get("answers");
        int correct = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, String> a : answers) {
            boolean right = a.get("userAnswer").equals(a.get("correctAnswer"));
            if (right) correct++;
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("question", a.get("question"));
            r.put("userAnswer", a.get("userAnswer"));
            r.put("correctAnswer", a.get("correctAnswer"));
            r.put("explanation", a.get("explanation"));
            r.put("correct", right);
            results.add(r);
        }
        int score = Math.round((float) correct / answers.size() * 100);
        return Result.ok(Map.of("score", score, "correct", correct, "total", answers.size(), "results", results));
    }

    private List<Map<String, Object>> generateFallback(String position) {
        return List.of(
            // Java基础
            Map.of("question","Java中HashMap的底层实现原理是？","options",List.of("A. 数组","B. 链表","C. 数组+链表/红黑树","D. 二叉树"),"answer","C","explanation","HashMap采用数组+链表+红黑树结构，当链表长度超过8且数组长度>=64时转为红黑树。"),
            Map.of("question","在Java中，以下哪个关键字用于实现接口？","options",List.of("A. extends","B. implements","C. abstract","D. interface"),"answer","B","explanation","implements关键字用于类实现接口，extends用于继承类或接口，abstract用于定义抽象类/方法。"),
            Map.of("question","以下哪项是Java中的函数式接口？","options",List.of("A. Serializable","B. Runnable","C. Iterable","D. Cloneable"),"answer","B","explanation","Runnable接口只有一个抽象方法run()，是典型的函数式接口，可用Lambda表达式简化。"),
            Map.of("question","Java垃圾回收中，以下哪个区域属于堆内存？","options",List.of("A. 程序计数器","B. 虚拟机栈","C. 年轻代(Eden区)","D. 本地方法栈"),"answer","C","explanation","年轻代(Young Generation)属于堆内存，包括Eden区和两个Survivor区，是GC的主要工作区域。"),
            // 数据结构与算法
            Map.of("question","时间复杂度O(n log n)的排序算法是？","options",List.of("A. 冒泡排序","B. 快速排序(平均)","C. 插入排序","D. 选择排序"),"answer","B","explanation","快速排序的平均时间复杂度为O(n log n)，冒泡/插入/选择排序均为O(n²)。堆排序和归并排序也是O(n log n)。"),
            Map.of("question","二分查找的前提条件是？","options",List.of("A. 数据无序","B. 数据有序","C. 数据存储在链表中","D. 数据量小于100"),"answer","B","explanation","二分查找要求数据有序排列（通常为升序），通过每次折半缩小查找范围，时间复杂度为O(log n)。"),
            Map.of("question","以下哪种数据结构是FIFO（先进先出）？","options",List.of("A. 栈 (Stack)","B. 队列 (Queue)","C. 堆 (Heap)","D. 哈希表"),"answer","B","explanation","队列是FIFO结构，先进入的元素先出队；栈是LIFO（后进先出）；堆是完全二叉树；哈希表是键值对。"),
            Map.of("question","红黑树是一种？","options",List.of("A. 线性表","B. 自平衡二叉查找树","C. 哈希表","D. 图"),"answer","B","explanation","红黑树是自平衡二叉查找树，通过颜色约束保证最坏情况下O(log n)的查找/插入/删除性能，用于TreeMap和TreeSet。"),
            // 数据库
            Map.of("question","SQL注入攻击可以通过以下哪种方式有效防范？","options",List.of("A. 使用字符串拼接SQL","B. 参数化查询(PreparedStatement)","C. 增加数据库索引","D. 使用HTTP POST方法"),"answer","B","explanation","参数化查询将SQL结构与参数值分离，从源头阻止恶意代码注入，是最可靠的SQL注入防御手段。"),
            Map.of("question","数据库事务的ACID特性中，'I'代表什么？","options",List.of("A. 原子性 (Atomicity)","B. 一致性 (Consistency)","C. 隔离性 (Isolation)","D. 持久性 (Durability)"),"answer","C","explanation","ACID：Atomicity(原子性)、Consistency(一致性)、Isolation(隔离性)、Durability(持久性)。I代表Isolation。"),
            Map.of("question","在MySQL中，以下哪个索引类型使用B+树结构？","options",List.of("A. HASH索引","B. FULLTEXT索引","C. InnoDB主键索引","D. SPATIAL索引"),"answer","C","explanation","InnoDB存储引擎的主键索引（聚簇索引）使用B+树结构，叶子节点存储完整行数据，支持范围查询效率高。"),
            Map.of("question","数据库左连接(LEFT JOIN)的作用是？","options",List.of("A. 只返回两表匹配的行","B. 返回左表全部行+右表匹配的行","C. 返回右表全部行+左表匹配的行","D. 返回两表全部行"),"answer","B","explanation","LEFT JOIN返回左表中所有行，即使右表中没有匹配记录（右表字段以NULL填充）；RIGHT JOIN则相反。"),
            // 计算机网络
            Map.of("question","TCP三次握手中，第三次握手的主要作用是？","options",List.of("A. 建立物理连接","B. 协商加密算法","C. 防止已失效的连接请求到达服务端","D. 分配端口号"),"answer","C","explanation","第三次握手防止客户端发出的旧连接请求报文突然到达服务器端，避免服务器建立错误的连接。"),
            Map.of("question","HTTP状态码301和302的区别是？","options",List.of("A. 301永久重定向/302临时重定向","B. 301临时重定向/302永久重定向","C. 都是永久重定向","D. 都是客户端错误"),"answer","A","explanation","301(Moved Permanently)表示资源永久迁移，浏览器会缓存；302(Found)表示临时重定向，浏览器每次都会重新请求原URL。"),
            Map.of("question","HTTPS相比HTTP增加的安全性主要依赖什么技术？","options",List.of("A. Base64编码","B. SSL/TLS加密","C. JWT认证","D. OAuth2授权"),"answer","B","explanation","HTTPS = HTTP + SSL/TLS，通过TLS协议实现数据加密传输、服务器身份认证和数据完整性校验。"),
            Map.of("question","DNS协议使用的传输层端口是？","options",List.of("A. TCP 80","B. UDP 53","C. TCP 443","D. UDP 8080"),"answer","B","explanation","DNS默认使用UDP 53端口进行域名解析查询，对于超过512字节的响应会切换为TCP 53。"),
            // 操作系统与Linux
            Map.of("question","进程和线程的主要区别是？","options",List.of("A. 线程是资源分配的基本单位","B. 进程间不共享内存空间","C. 线程不能并发执行","D. 进程比线程切换开销小"),"answer","B","explanation","每个进程拥有独立的内存地址空间；同一进程的线程共享堆内存但各自拥有栈，切换开销更小。"),
            Map.of("question","Linux中，查看系统内存使用情况的命令是？","options",List.of("A. df -h","B. top / free -h","C. ls -la","D. ps aux | grep mem"),"answer","B","explanation","free -h查看内存总量和已用量；top实时显示进程资源占用；df -h查看磁盘空间；ps aux查看进程列表。"),
            Map.of("question","死锁的四个必要条件不包括以下哪项？","options",List.of("A. 互斥条件","B. 请求与保持","C. 资源充足","D. 循环等待"),"answer","C","explanation","死锁四条件：互斥、请求与保持、不可剥夺、循环等待。资源充足则不会发生死锁，破坏任一条件即可预防死锁。"),
            // 设计模式与架构
            Map.of("question","单例模式的核心特点是？","options",List.of("A. 一个类可以创建多个实例","B. 一个类只有一个实例并提供全局访问点","C. 创建对象时不使用new关键字","D. 每个线程持有一个独立实例"),"answer","B","explanation","单例模式(Singleton)确保类在整个应用中只有一个实例，并提供全局访问点，常用实现方式有饿汉式、懒汉式、枚举等。")
        );
    }
}

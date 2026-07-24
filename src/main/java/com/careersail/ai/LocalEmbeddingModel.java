package com.careersail.ai;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义 EmbeddingModel — 对接本地 Python sentence-transformers 服务
 * 一切运行在 E 盘，不依赖 Ollama / 外部 API
 */
@Slf4j
@Component
public class LocalEmbeddingModel extends AbstractEmbeddingModel {

    private final RestClient restClient = RestClient.create();

    private static final String EMBEDDING_URL = "http://127.0.0.1:11435/api/embeddings";

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> inputs = request.getInstructions();
        List<Embedding> embeddings = new ArrayList<>();

        for (String input : inputs) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("input", input);

            try {
                String resp = restClient.post()
                        .uri(EMBEDDING_URL)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .retrieve()
                        .body(String.class);

                JSONObject json = JSONUtil.parseObj(resp);
                var data = json.getJSONArray("data");
                if (data != null && !data.isEmpty()) {
                    var emb = data.getJSONObject(0).getJSONArray("embedding");
                    float[] vec = new float[emb.size()];
                    for (int i = 0; i < emb.size(); i++) {
                        vec[i] = emb.getFloat(i);
                    }
                    embeddings.add(new Embedding(vec, 0));
                }
            } catch (Exception e) {
                log.warn("Local embedding failed for input '{}': {}", input.substring(0, Math.min(50, input.length())), e.getMessage());
                // 返回零向量作为 fallback
                embeddings.add(new Embedding(new float[384], 0));
            }
        }

        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        EmbeddingRequest req = new EmbeddingRequest(List.of(document.getFormattedContent()), null);
        EmbeddingResponse resp = call(req);
        return resp.getResults().get(0).getOutput();
    }

    @Override
    public int dimensions() {
        return 384; // all-MiniLM-L6-v2 输出 384 维
    }
}

package com.careersail.ai;

import com.careersail.entity.CareerInfo;
import com.careersail.vo.CareerVO;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final VectorStore vectorStore;

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void addDocument(CareerInfo career) {
        Document doc = buildDocument(career);
        vectorStore.add(List.of(doc));
    }

    @Async
    public void addDocumentAsync(CareerInfo career) {
        try {
            addDocument(career);
        } catch (Exception e) {
            // Chroma may not be available — log and continue
            System.err.println("Chroma add failed (async): " + e.getMessage());
        }
    }

    @Async
    public void updateDocumentAsync(CareerInfo career) {
        try {
            deleteDocument(career.getId());
            addDocument(career);
        } catch (Exception e) {
            System.err.println("Chroma update failed (async): " + e.getMessage());
        }
    }

    @Async
    public void deleteDocumentAsync(Long careerId) {
        try {
            deleteDocument(careerId);
        } catch (Exception e) {
            System.err.println("Chroma delete failed (async): " + e.getMessage());
        }
    }

    public void deleteDocument(Long careerId) {
        vectorStore.delete(List.of("career_" + careerId));
    }

    public List<CareerVO> searchSimilar(String query, int topK) {
        try {
            List<Document> docs = vectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(topK).build());
            return docs.stream().map(doc -> {
                CareerVO vo = new CareerVO();
                vo.setId(Long.valueOf(doc.getMetadata().get("careerId").toString()));
                vo.setPositionName(doc.getMetadata().getOrDefault("positionName", "").toString());
                vo.setIndustry(doc.getMetadata().getOrDefault("industry", "").toString());
                vo.setMajorMatch(doc.getMetadata().getOrDefault("majorMatch", "").toString());
                vo.setSkillsRequired(doc.getMetadata().getOrDefault("skillsRequired", "").toString());
                vo.setDescription(doc.getFormattedContent());
                vo.setSimilarityScore(
                        doc.getMetadata().containsKey("distance")
                                ? 1.0 - Double.parseDouble(doc.getMetadata().get("distance").toString())
                                : null);
                return vo;
            }).toList();
        } catch (Exception e) {
            System.err.println("Chroma search failed, vector results unavailable: " + e.getMessage());
            return List.of();
        }
    }

    public void reIndexAll(List<CareerInfo> all) {
        try {
            // Delete all existing and rebuild
            List<Document> docs = all.stream().map(this::buildDocument).toList();
            vectorStore.add(docs);
        } catch (Exception e) {
            System.err.println("Chroma reindex failed: " + e.getMessage());
        }
    }

    private Document buildDocument(CareerInfo career) {
        String content = String.format(
                "岗位名称: %s\n行业: %s\n匹配专业: %s\n所需技能: %s\n岗位描述: %s\n职业发展路径: %s",
                career.getPositionName(),
                career.getIndustry(),
                career.getMajorMatch(),
                career.getSkillsRequired(),
                career.getDescription() != null ? career.getDescription() : "",
                career.getCareerPath() != null ? career.getCareerPath() : ""
        );
        return new Document(content, Map.of(
                "careerId", career.getId(),
                "positionName", career.getPositionName(),
                "industry", career.getIndustry(),
                "majorMatch", career.getMajorMatch(),
                "skillsRequired", career.getSkillsRequired()
        ));
    }
}

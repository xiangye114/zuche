
package com.zuche.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zuche.entity.KnowledgeBase;
import com.zuche.mapper.KnowledgeBaseMapper;
import com.zuche.service.RAGService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RAGServiceImpl implements RAGService {

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public List<KnowledgeBase> retrieve(String query, int topK) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getStatus, 1);
        
        List<KnowledgeBase> allKnowledge = knowledgeBaseMapper.selectList(wrapper);
        
        return allKnowledge.stream()
                .filter(kb -> isRelevant(kb, query))
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .limit(topK)
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean isRelevant(KnowledgeBase kb, String query) {
        if (kb.getQuestion() == null || query == null) {
            return false;
        }
        
        String lowerQuery = query.toLowerCase().replaceAll("[\\p{Punct}]", "");
        String lowerQuestion = kb.getQuestion().toLowerCase().replaceAll("[\\p{Punct}]", "");
        
        if (lowerQuery.contains(lowerQuestion) || lowerQuestion.contains(lowerQuery)) {
            return true;
        }
        
        String[] queryWords = lowerQuery.split("\\s+");
        String[] questionWords = lowerQuestion.split("\\s+");
        
        for (String queryWord : queryWords) {
            if (queryWord.length() < 2) continue;
            for (String questionWord : questionWords) {
                if (queryWord.equals(questionWord) || questionWord.contains(queryWord) || queryWord.contains(questionWord)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public String buildRagContext(String query) {
        List<KnowledgeBase> relevantKnowledge = retrieve(query, 3);
        
        if (relevantKnowledge.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("参考信息：\n");
        for (int i = 0; i < relevantKnowledge.size(); i++) {
            KnowledgeBase kb = relevantKnowledge.get(i);
            context.append((i + 1)).append(". ")
                   .append("问题：").append(kb.getQuestion()).append("\n")
                   .append("回答：").append(kb.getAnswer()).append("\n\n");
        }
        
        return context.toString();
    }
}

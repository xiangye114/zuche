
package com.zuche.service;

import com.zuche.entity.KnowledgeBase;

import java.util.List;

public interface RAGService {
    List<KnowledgeBase> retrieve(String query, int topK);
    String buildRagContext(String query);
}

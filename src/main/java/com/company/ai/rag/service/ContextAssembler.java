package com.company.ai.rag.service;

import com.company.ai.rag.service.retrieval.RetrievedChunk;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContextAssembler {

    public String assemble(List<RetrievedChunk> chunks) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk chunk = chunks.get(i);
            builder.append("资料")
                    .append(i + 1)
                    .append(":\n")
                    .append("来源文件: ")
                    .append(chunk.getFileName())
                    .append("\n")
                    .append("标题路径: ")
                    .append(chunk.getTitlePath())
                    .append("\n")
                    .append("内容: ")
                    .append(chunk.getContent())
                    .append("\n\n");
        }
        return builder.toString();
    }
}

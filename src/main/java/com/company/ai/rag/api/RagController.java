package com.company.ai.rag.api;

import com.company.ai.rag.api.dto.AskRequest;
import com.company.ai.rag.api.dto.AskResponse;
import com.company.ai.rag.service.RagQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagQueryService ragQueryService;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(@Valid @RequestBody AskRequest request) {
        AskResponse response = ragQueryService.ask(request);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", response
        ));
    }
}

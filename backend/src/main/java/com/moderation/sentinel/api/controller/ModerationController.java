package com.moderation.sentinel.api.controller;

import com.moderation.sentinel.model.ModerationResponse;
import com.moderation.sentinel.service.moderation.ModerationService;
import com.moderation.sentinel.util.algorithm.trie.Trie;
import com.moderation.sentinel.util.algorithm.trie.TrieInitializer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/moderation")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(TrieInitializer initializer) {
        Map<String, Trie> tries = new HashMap<>();
        tries.put("offensive", initializer.getOffensiveTrie());
        tries.put("safe", initializer.getSafeTrie());
        this.moderationService = new ModerationService(tries);
    }

    @GetMapping("/")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Connection Established");
    }

    @GetMapping("/text")
    public ResponseEntity<String> errorMessage() {
        return ResponseEntity.badRequest().body("Invalid method invoked - GET api/v1/moderation/text");
    }

    @PostMapping("/text")
    public ResponseEntity<ModerationResponse> moderateText(@RequestBody Map<String, String> payload) {
        String input = payload.getOrDefault("text", "");
        ModerationResponse result = moderationService.analyze(input);
        return ResponseEntity.ok(result);
    }
}

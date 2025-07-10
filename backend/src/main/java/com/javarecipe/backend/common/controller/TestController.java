package com.javarecipe.backend.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public ResponseEntity<Map<String, String>> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Java Recipe API is running successfully!");
        response.put("status", "ok");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/devtools")
    public ResponseEntity<Map<String, String>> testDevTools() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "DevTools automatic restart is working!");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/auto-restart")
    public ResponseEntity<Map<String, Object>> testAutoRestart() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This endpoint was added to test automatic restart");
        response.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("osName", System.getProperty("os.name"));
        
        return ResponseEntity.ok(response);
    }
} 
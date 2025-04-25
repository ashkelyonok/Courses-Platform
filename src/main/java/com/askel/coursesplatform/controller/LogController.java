package com.askel.coursesplatform.controller;

import com.askel.coursesplatform.model.entity.LogObj;
import com.askel.coursesplatform.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Tag(name = "Log API", description = "Operations with .log file")
public class LogController {

    private final LogService logService;

    @PostMapping("/create")
    @Operation(
            summary = "Create log file asynchronously",
            description = "Starts log file generation and returns an ID"
    )
    public ResponseEntity<Long> createLogFile(@RequestParam String date) {
        Long id = logService.createLogAsync(date);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/status/{id}")
    @Operation(
            summary = "Get log generation status",
            description = "Returns the current state of log file generation"
    )
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable Long id) {
        LogObj logObject = logService.getStatus(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", logObject.getStatus());
        if (logObject.getErrorMessage() != null) {
            response.put("error", logObject.getErrorMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{id}")
    @Operation(
            summary = "Download generated log file",
            description = "Download the generated log file by ID"
    )
    public ResponseEntity<Resource> getLogFileById(@PathVariable Long id) throws IOException {
        return logService.downloadCreatedLogs(id);
    }
}

package com.askel.coursesplatform.controller;

import com.askel.coursesplatform.service.CounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
@Tag(name = "Visit tracking", description = "Visit counter operations")
public class CounterController {

    private final CounterService counterService;

    @GetMapping("/count")
    @Operation(summary = "Get visit count", description = "Returns request stats per URI")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(counterService.getStats());
    }
}

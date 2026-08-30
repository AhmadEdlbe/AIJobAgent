package com.aijobagent.controller;

import com.aijobagent.dto.ApplicationDto;
import com.aijobagent.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service){ this.service=service; }

    @GetMapping
    public ResponseEntity<List<ApplicationDto>> all(@RequestParam(required=false) String status){
        if(status!=null) return ResponseEntity.ok(service.getByStatus(status));
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDto> get(@PathVariable String id){
        return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApplicationDto> create(@RequestBody Map<String,String> body){
        String jobId = body.get("jobId");
        String status = body.getOrDefault("status","SAVED");
        return ResponseEntity.ok(service.create(jobId,status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationDto> updateStatus(@PathVariable String id, @RequestBody Map<String,String> body){
        String status = body.get("status");
        return service.updateStatus(id,status).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

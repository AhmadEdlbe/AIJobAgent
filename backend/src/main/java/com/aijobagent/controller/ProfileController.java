package com.aijobagent.controller;

import com.aijobagent.dto.UserProfileDto;
import com.aijobagent.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service){ this.service=service; }

    @GetMapping
    public ResponseEntity<UserProfileDto> get(){
        return service.get()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(new UserProfileDto("singleton","","","","","","","", List.of(), List.of(), List.of(), null)));
    }

    @PutMapping
    public ResponseEntity<UserProfileDto> put(@Valid @RequestBody UserProfileDto dto){
        return ResponseEntity.ok(service.upsert(dto));
    }

    @PostMapping("/resume")
    public ResponseEntity<UserProfileDto> uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        var current = service.get().orElse(new UserProfileDto("singleton","","","","","","","", List.of(), List.of(), List.of(), null));
        String text = new String(file.getBytes()); // simplistic - for PDF use Apache Tika (add if needed)
        if(file.getOriginalFilename()!=null && file.getOriginalFilename().endsWith(".pdf")){
            // Keep as is; TODO add PDF text extraction with PDFBox
            text = "[PDF binary - text extraction not enabled] " + text.substring(0, Math.min(1000, text.length()));
        }
        var updated = new UserProfileDto(current.id(), current.fullName(), current.email(), current.phoneNumber(), current.linkedInUrl(), current.gitHubUrl(), text, file.getOriginalFilename(), current.preferredCountries(), current.preferredJobTitles(), current.skills(), null);
        return ResponseEntity.ok(service.upsert(updated));
    }
}

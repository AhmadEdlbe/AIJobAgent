package com.aijobagent.controller;

import com.aijobagent.dto.JobDto;
import com.aijobagent.dto.ScanRequest;
import com.aijobagent.entity.JobEntity;
import com.aijobagent.repository.JobRepository;
import com.aijobagent.service.JobSearchService;
import com.aijobagent.service.MappingService;
import com.aijobagent.service.ProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {
    private final JobRepository jobRepo;
    private final JobSearchService searchService;
    private final ProfileService profileService;
    private final MappingService mapper;

    public JobController(JobRepository jobRepo, JobSearchService searchService, ProfileService profileService, MappingService mapper){
        this.jobRepo=jobRepo; this.searchService=searchService; this.profileService=profileService; this.mapper=mapper;
    }

    @GetMapping
    public ResponseEntity<Page<JobDto>> list(
            @RequestParam(required=false) String q,
            @RequestParam(required=false) String workMode,
            @RequestParam(required=false) String seniority,
            @RequestParam(required=false) String source,
            @RequestParam(defaultValue="0") int minMatch,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size
    ){
        JobEntity.WorkMode wm=null; JobEntity.Seniority sen=null; JobEntity.JobSource src=null;
        try{ if(workMode!=null) wm=JobEntity.WorkMode.valueOf(workMode); }catch(Exception e){}
        try{ if(seniority!=null) sen=JobEntity.Seniority.valueOf(seniority); }catch(Exception e){}
        try{ if(source!=null) src=JobEntity.JobSource.valueOf(source); }catch(Exception e){}
        var pg = jobRepo.search(q, wm, sen, src, minMatch, PageRequest.of(page,size, Sort.by("matchPercentage").descending()));
        return ResponseEntity.ok(pg.map(mapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDto> get(@PathVariable String id){
        return jobRepo.findById(id).map(mapper::toDto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/high-match")
    public ResponseEntity<List<JobDto>> highMatch(){
        return ResponseEntity.ok(jobRepo.findByMatchPercentageGreaterThanEqualOrderByMatchPercentageDesc(75).stream().map(mapper::toDto).toList());
    }

    @PostMapping("/scan")
    public ResponseEntity<List<JobDto>> scan(@RequestBody(required=false) ScanRequest req){
        var profile = profileService.get().orElse(null);
        ScanRequest effective = req;
        if(effective==null && profile!=null){
            effective = new ScanRequest(null, profile.preferredCountries(), profile.preferredJobTitles(), profile.skills());
        }
        var result = searchService.scan(effective, profile);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<JobDto> favorite(@PathVariable String id, @RequestParam boolean fav){
        var opt = jobRepo.findById(id);
        if(opt.isEmpty()) return ResponseEntity.notFound().build();
        var e = opt.get(); e.setFavorite(fav); jobRepo.save(e);
        return ResponseEntity.ok(mapper.toDto(e));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id){
        jobRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

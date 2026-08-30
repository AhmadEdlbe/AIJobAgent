package com.aijobagent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyScanScheduler {
    private static final Logger log = LoggerFactory.getLogger(DailyScanScheduler.class);
    private final JobSearchService jobSearchService;
    private final ProfileService profileService;

    public DailyScanScheduler(JobSearchService jobSearchService, ProfileService profileService){
        this.jobSearchService=jobSearchService; this.profileService=profileService;
    }

    // Every day at 9am
    @Scheduled(cron = "0 0 9 * * *")
    public void dailyScan(){
        log.info("Starting daily job scan");
        try{
            var profile = profileService.get().orElse(null);
            var req = profile!=null? new com.aijobagent.dto.ScanRequest(null, profile.preferredCountries(), profile.preferredJobTitles(), profile.skills()): new com.aijobagent.dto.ScanRequest(null, null, null, null);
            var jobs = jobSearchService.scan(req, profile);
            long high = jobs.stream().filter(j-> j.matchPercentage()>=75).count();
            log.info("Daily scan completed: {} jobs, {} high match", jobs.size(), high);
            // TODO: send push via FCM if high>0
        }catch(Exception e){
            log.error("Daily scan failed {}", e.getMessage(), e);
        }
    }
}

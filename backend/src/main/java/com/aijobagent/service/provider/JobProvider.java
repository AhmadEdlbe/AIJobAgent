package com.aijobagent.service.provider;

import com.aijobagent.dto.JobDto;
import com.aijobagent.dto.ScanRequest;
import java.util.List;

public interface JobProvider {
    List<JobDto> fetch(ScanRequest req) throws Exception;
    String sourceName();
}

package com.aijobagent.repository;

import com.aijobagent.entity.InterviewPrepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InterviewPrepRepository extends JpaRepository<InterviewPrepEntity, String> {
    Optional<InterviewPrepEntity> findByJobId(String jobId);
}

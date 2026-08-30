package com.aijobagent.repository;

import com.aijobagent.entity.CoverLetterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CoverLetterRepository extends JpaRepository<CoverLetterEntity, String> {
    Optional<CoverLetterEntity> findByJobId(String jobId);
}

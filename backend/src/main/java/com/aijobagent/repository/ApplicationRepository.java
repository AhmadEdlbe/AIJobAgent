package com.aijobagent.repository;

import com.aijobagent.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, String> {
    List<ApplicationEntity> findByStatus(ApplicationEntity.ApplicationStatus status);
    Optional<ApplicationEntity> findByJobId(String jobId);
    long countByStatus(ApplicationEntity.ApplicationStatus status);
    long countByStatusIn(List<ApplicationEntity.ApplicationStatus> statuses);
}

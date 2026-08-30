package com.aijobagent.repository;

import com.aijobagent.entity.JobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, String> {
    List<JobEntity> findByMatchPercentageGreaterThanEqualOrderByMatchPercentageDesc(int pct);
    List<JobEntity> findBySource(JobEntity.JobSource source);
    long countByMatchPercentageGreaterThanEqual(int pct);

    @Query("SELECT j FROM JobEntity j WHERE " +
            "(:query IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%',:query,'%')) OR LOWER(j.company) LIKE LOWER(CONCAT('%',:query,'%'))) " +
            "AND (:workMode IS NULL OR j.workMode = :workMode) " +
            "AND (:seniority IS NULL OR j.seniority = :seniority) " +
            "AND (:source IS NULL OR j.source = :source) " +
            "AND (j.matchPercentage >= :minMatch)")
    Page<JobEntity> search(@Param("query") String query,
                           @Param("workMode") JobEntity.WorkMode workMode,
                           @Param("seniority") JobEntity.Seniority seniority,
                           @Param("source") JobEntity.JobSource source,
                           @Param("minMatch") int minMatch,
                           Pageable pageable);
}

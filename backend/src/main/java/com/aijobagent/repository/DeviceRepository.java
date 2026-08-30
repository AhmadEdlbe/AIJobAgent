package com.aijobagent.repository;

import com.aijobagent.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {
    Optional<DeviceEntity> findByApiKey(String apiKey);
}

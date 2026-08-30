package com.aijobagent.service;

import com.aijobagent.dto.UserProfileDto;
import com.aijobagent.entity.UserProfileEntity;
import com.aijobagent.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService {
    private final UserProfileRepository repo;
    private final MappingService mapper;

    public ProfileService(UserProfileRepository repo, MappingService mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Optional<UserProfileDto> get() {
        return repo.findById("singleton").map(mapper::toDto);
    }

    @Transactional
    public UserProfileDto upsert(UserProfileDto dto){
        UserProfileEntity e = mapper.toEntity(dto);
        e.setId("singleton");
        // if exists keep createdAt
        repo.findById("singleton").ifPresent(existing -> e.setCreatedAt(existing.getCreatedAt()));
        UserProfileEntity saved = repo.save(e);
        return mapper.toDto(saved);
    }
}

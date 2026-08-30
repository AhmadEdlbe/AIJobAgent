package com.aijobagent.controller;

import com.aijobagent.dto.AuthDtos.*;
import com.aijobagent.entity.DeviceEntity;
import com.aijobagent.repository.DeviceRepository;
import com.aijobagent.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final DeviceRepository deviceRepo;
    private final JwtTokenProvider jwt;

    public AuthController(DeviceRepository deviceRepo, JwtTokenProvider jwt){ this.deviceRepo=deviceRepo; this.jwt=jwt; }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterDeviceRequest req){
        String deviceId = req.deviceId()!=null? req.deviceId(): UUID.randomUUID().toString();
        var device = deviceRepo.findById(deviceId).orElseGet(() -> {
            DeviceEntity d = new DeviceEntity();
            d.setDeviceId(deviceId);
            d.setDeviceName(req.deviceName());
            d.setApiKey(UUID.randomUUID().toString());
            d.setLastSeen(Instant.now());
            return deviceRepo.save(d);
        });
        device.setLastSeen(Instant.now());
        deviceRepo.save(device);
        String access = jwt.generateAccessToken(deviceId);
        String refresh = jwt.generateRefreshToken(deviceId);
        return ResponseEntity.ok(new TokenResponse(access,refresh,deviceId, jwt.getExpirationMs()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest req){
        if(!jwt.validateToken(req.refreshToken())) return ResponseEntity.status(401).build();
        String deviceId = jwt.getDeviceIdFromToken(req.refreshToken());
        String access = jwt.generateAccessToken(deviceId);
        String refresh = jwt.generateRefreshToken(deviceId);
        return ResponseEntity.ok(new TokenResponse(access,refresh,deviceId, jwt.getExpirationMs()));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health(){ return ResponseEntity.ok("ok"); }
}

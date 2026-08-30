package com.aijobagent.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

/**
 * Single-device auth: deviceId bound to app installation.
 * Used for JWT issuance without traditional registration.
 */
@Entity
@Table(name = "devices")
public class DeviceEntity {
    @Id
    private String deviceId; // UUID generated on first app launch

    private String deviceName;
    @Column(unique = true)
    private String apiKey; // alternative to JWT for single user
    @CreationTimestamp
    private Instant createdAt;
    private Instant lastSeen;

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
}

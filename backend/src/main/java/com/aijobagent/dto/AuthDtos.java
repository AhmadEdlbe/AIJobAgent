package com.aijobagent.dto;

public class AuthDtos {
    public record RegisterDeviceRequest(String deviceId, String deviceName) {}
    public record TokenResponse(String accessToken, String refreshToken, String deviceId, long expiresIn) {}
    public record RefreshRequest(String refreshToken) {}
    public record ApiErrorResponse(long timestamp, int status, String error, String message, String path) {}
}

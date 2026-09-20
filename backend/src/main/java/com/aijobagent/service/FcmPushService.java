package com.aijobagent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * FCM push stub. When firebase-admin SDK added, implement real send.
 * For now logs; DailyScanScheduler can call this when high-match found.
 */
@Service
public class FcmPushService {
    private static final Logger log = LoggerFactory.getLogger(FcmPushService.class);

    @Value("${app.fcm.enabled:false}")
    private boolean enabled;

    @Value("${app.fcm.server-key:}")
    private String serverKey;

    public void sendHighMatchPush(String deviceToken, int count) {
        if (!enabled || deviceToken == null || serverKey == null || serverKey.isBlank()) {
            log.info("FCM disabled or no token, would send push: {} high-match to {}", count, deviceToken);
            return;
        }
        // TODO: integrate firebase-admin:
        // FirebaseMessaging.getInstance().send(Message.builder().setToken(deviceToken).putData("count", String.valueOf(count)).setNotification(...).build());
        log.info("FCM push sent: {} high-match jobs", count);
    }
}

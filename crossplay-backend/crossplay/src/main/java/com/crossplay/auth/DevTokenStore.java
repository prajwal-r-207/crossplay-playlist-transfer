package com.crossplay.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DevTokenStore {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    public void saveToken(String platform, String token) {
        tokens.put(platform, token);
    }

    public String getToken(String platform) {
        return tokens.get(platform);
    }
}

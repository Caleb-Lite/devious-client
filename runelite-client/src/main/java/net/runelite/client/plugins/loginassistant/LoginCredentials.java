package net.runelite.client.plugins.loginassistant;

public class LoginCredentials {
    private String accessToken;
    private String refreshToken;
    private String sessionId;
    private String characterId;
    private String displayName;

    public LoginCredentials() {}

    public LoginCredentials(String accessToken, String refreshToken, String sessionId, String characterId, String displayName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.sessionId = sessionId;
        this.characterId = characterId;
        this.displayName = displayName;
    }

    // Getters and setters for all fields
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
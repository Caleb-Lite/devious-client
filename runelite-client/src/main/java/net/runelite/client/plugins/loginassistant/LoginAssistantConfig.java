package net.runelite.client.plugins.loginassistant;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("loginassistant")
public interface LoginAssistantConfig extends Config {

    @ConfigItem(
            keyName = "redirectUri",
            name = "Redirect URI",
            description = "The redirect URI for the OAuth2 login process"
    )
    default String redirectUri() {
        return "https://secure.runescape.com/m=weblogin/launcher-redirect";
    }

    @ConfigItem(
            keyName = "clientId",
            name = "Client ID",
            description = "The client ID for the OAuth2 login process"
    )
    default String clientId() {
        return "com_jagex_auth_desktop_launcher";
    }

    @ConfigItem(
            keyName = "scope",
            name = "Scope",
            description = "The scope for the OAuth2 login process"
    )
    default String scope() {
        return "openid offline gamesso.token.create user.profile.read";
    }

    @ConfigItem(
            keyName = "tokenEndpoint",
            name = "Token Endpoint",
            description = "The endpoint to exchange code for tokens"
    )
    default String tokenEndpoint() {
        return "https://account.jagex.com/oauth2/token";
    }

    @ConfigItem(
            keyName = "sessionEndpoint",
            name = "Session Endpoint",
            description = "The endpoint to create a game session"
    )
    default String sessionEndpoint() {
        return "https://auth.jagex.com/game-session/v1/sessions";
    }
}

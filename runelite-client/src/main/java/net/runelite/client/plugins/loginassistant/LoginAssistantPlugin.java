package net.runelite.client.plugins.loginassistant;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import javax.inject.Inject;
import java.awt.image.BufferedImage;

@PluginDescriptor(
        name = "Login Assistant",
        description = "Helps in logging into Jagex accounts via OAuth2",
        tags = {"login", "jagex", "oauth2"}
)
public class LoginAssistantPlugin extends Plugin {
    @Inject
    private ClientToolbar clientToolbar;

    private NavigationButton navButton;
    private LoginAssistantPanel panel;

    @Override
    protected void startUp() throws Exception {
        panel = injector.getInstance(LoginAssistantPanel.class);

        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Login Assistant")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navButton);
    }

    @Provides
    LoginAssistantConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LoginAssistantConfig.class);
    }
}

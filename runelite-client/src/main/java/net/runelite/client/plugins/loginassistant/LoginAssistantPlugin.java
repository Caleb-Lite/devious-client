package net.runelite.client.plugins.loginassistant;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import lombok.Getter;

@PluginDescriptor(
        name = "Login Assistant",
        description = "Manages multiple account logins",
        tags = {"login", "account", "credentials"}
)
public class LoginAssistantPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private LoginAssistantConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    private LoginAssistantPanel panel;
    private NavigationButton navButton;

    @Getter
    private final Map<String, LoginCredentials> accountCredentials = new HashMap<>();

    @Override
    protected void startUp() {
        panel = injector.getInstance(LoginAssistantPanel.class);
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "login_icon.png");
        navButton = NavigationButton.builder()
                .tooltip("Login Assistant")
                .icon(icon)
                .priority(10)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
        loadAccountCredentials();
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
    }

    @Provides
    LoginAssistantConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LoginAssistantConfig.class);
    }

    public void promptForNewAccount() {
        String accountName = JOptionPane.showInputDialog("Enter account name:");
        if (accountName != null && !accountName.isEmpty()) {
            String accessToken = JOptionPane.showInputDialog("Enter access token:");
            String refreshToken = JOptionPane.showInputDialog("Enter refresh token:");
            String sessionId = JOptionPane.showInputDialog("Enter session ID:");
            String characterId = JOptionPane.showInputDialog("Enter character ID:");
            String displayName = JOptionPane.showInputDialog("Enter display name:");

            LoginCredentials credentials = new LoginCredentials(accessToken, refreshToken, sessionId, characterId, displayName);
            accountCredentials.put(accountName, credentials);
            saveAccountCredentials();
            panel.refreshPanel();
        }
    }

    public void removeAccount(String accountName) {
        accountCredentials.remove(accountName);
        saveAccountCredentials();
    }

    private void loadAccountCredentials() {
        String savedAccounts = config.savedAccounts();
        if (savedAccounts != null && !savedAccounts.isEmpty()) {
            String[] accounts = savedAccounts.split(";");
            for (String account : accounts) {
                String[] parts = account.split(":");
                if (parts.length == 6) {
                    String accountName = parts[0];
                    String accessToken = parts[1];
                    String refreshToken = parts[2];
                    String sessionId = parts[3];
                    String characterId = parts[4];
                    String displayName = parts[5];
                    accountCredentials.put(accountName, new LoginCredentials(accessToken, refreshToken, sessionId, characterId, displayName));
                }
            }
        }
    }

    private void saveAccountCredentials() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, LoginCredentials> entry : accountCredentials.entrySet()) {
            LoginCredentials cred = entry.getValue();
            sb.append(entry.getKey()).append(":")
                    .append(cred.getAccessToken()).append(":")
                    .append(cred.getRefreshToken()).append(":")
                    .append(cred.getSessionId()).append(":")
                    .append(cred.getCharacterId()).append(":")
                    .append(cred.getDisplayName()).append(";");
        }
        config.setSavedAccounts(sb.toString());
    }

    public String[] getAccounts() {
        return accountCredentials.keySet().toArray(new String[0]);
    }

    public LoginCredentials getCredentials(String accountName) {
        return accountCredentials.get(accountName);
    }

    public void performLogin() {
        // Implement any additional login actions here if needed
    }
}
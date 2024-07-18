package net.runelite.client.plugins.loginassistant;

import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.Properties;

public class LoginAssistantPanel extends PluginPanel {
    private final LoginAssistantConfig config;
    private final LoginAssistantPlugin plugin;
    private final Client client;

    private JComboBox<String> accountSelector;
    private JButton loginButton;
    private JButton addAccountButton;
    private JButton removeAccountButton;

    @Inject
    private LoginAssistantPanel(LoginAssistantConfig config, LoginAssistantPlugin plugin, Client client) {
        super(false);
        this.config = config;
        this.plugin = plugin;
        this.client = client;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel northPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        northPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        accountSelector = new JComboBox<>();
        updateAccountSelector();

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> loginSelectedAccount());

        addAccountButton = new JButton("Add Account");
        addAccountButton.addActionListener(e -> plugin.promptForNewAccount());

        removeAccountButton = new JButton("Remove Account");
        removeAccountButton.addActionListener(e -> removeSelectedAccount());

        northPanel.add(accountSelector);
        northPanel.add(loginButton);
        northPanel.add(addAccountButton);
        northPanel.add(removeAccountButton);

        add(northPanel, BorderLayout.NORTH);
    }

    private void updateAccountSelector() {
        accountSelector.removeAllItems();
        for (String account : plugin.getAccounts()) {
            accountSelector.addItem(account);
        }
    }

    private void loginSelectedAccount() {
        String selectedAccount = (String) accountSelector.getSelectedItem();
        if (selectedAccount != null) {
            LoginCredentials credentials = plugin.getCredentials(selectedAccount);
            if (credentials != null) {
                try {
                    // Set access token
                    Method setAccessTokenMethod = client.getClass().getMethod("setAccessToken", String.class);
                    setAccessTokenMethod.invoke(client, credentials.getAccessToken());

                    // Set refresh token
                    Method setRefreshTokenMethod = client.getClass().getMethod("setRefreshToken", String.class);
                    setRefreshTokenMethod.invoke(client, credentials.getRefreshToken());

                    // Set session ID
                    Method setSessionIdMethod = client.getClass().getMethod("setSessionId", String.class);
                    setSessionIdMethod.invoke(client, credentials.getSessionId());

                    // Set character ID
                    Method setCharacterIdMethod = client.getClass().getMethod("setCharacterId", String.class);
                    setCharacterIdMethod.invoke(client, credentials.getCharacterId());

                    // Set display name
                    Method setDisplayNameMethod = client.getClass().getMethod("setDisplayName", String.class);
                    setDisplayNameMethod.invoke(client, credentials.getDisplayName());

                    // Trigger onRefreshToken to save credentials
                    Method onRefreshTokenMethod = client.getClass().getMethod("onRefreshToken", int.class);
                    onRefreshTokenMethod.invoke(null, 0); // Note: This is a static method, so we pass null as the first argument

                    // Perform any additional login actions if needed
                    plugin.performLogin();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle the exception (e.g., show an error message to the user)
                }
            }
        }
    }

    private void removeSelectedAccount() {
        String selectedAccount = (String) accountSelector.getSelectedItem();
        if (selectedAccount != null) {
            plugin.removeAccount(selectedAccount);
            updateAccountSelector();
        }
    }

    public void refreshPanel() {
        updateAccountSelector();
    }
}
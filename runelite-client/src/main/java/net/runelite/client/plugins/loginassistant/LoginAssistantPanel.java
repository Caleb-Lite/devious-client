package net.runelite.client.plugins.loginassistant;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.rs.ClientLoader;
import net.runelite.client.ui.PluginPanel;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginAssistantPanel extends PluginPanel {
    private List<JSONObject> accounts;
    private JComboBox<String> accountDropdown;

    private String displayName;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    public LoginAssistantPanel() {
        System.out.println("DEBUG: Initializing LoginAssistantPanel");
        setLayout(new BorderLayout());

        JButton fetchAccountsButton = new JButton("Fetch Accounts");
        accountDropdown = new JComboBox<>();
        JButton loadAccountButton = new JButton("Load Account");

        JPanel dropdownPanel = new JPanel(new FlowLayout());
        dropdownPanel.add(accountDropdown);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(fetchAccountsButton);
        buttonPanel.add(loadAccountButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(dropdownPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.NORTH);

        fetchAccountsButton.addActionListener(e -> {
            try {
                String sessionId = loadSessionIdFromFile();
                if (sessionId != null) {
                    fetchAccounts(sessionId);
                } else {
                    System.out.println("DEBUG: Session ID not found in file");
                }
            } catch (Exception ex) {
                System.out.println("DEBUG: Error fetching accounts: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        loadAccountButton.addActionListener(e -> {
            try {
                loadSelectedAccount();
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
        });

        System.out.println("DEBUG: LoginAssistantPanel initialization complete");
    }

    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.isEmpty() && displayName.charAt(0) != '#') {
            this.displayName = displayName;
        } else {
            this.displayName = "";
        }
    }

    private void loadSelectedAccount() throws JSONException {
        System.out.println("DEBUG: Load Account button clicked");
        int selectedIndex = accountDropdown.getSelectedIndex();
        if (selectedIndex != -1 && accounts != null && !accounts.isEmpty()) {
            JSONObject selectedAccount = accounts.get(selectedIndex);
            String characterId = selectedAccount.getString("accountId");
            String displayName = selectedAccount.getString("displayName");

            // Load the session ID from the sessionid.json file
            String sessionId = loadSessionIdFromFile();

            if (sessionId != null) {
                System.out.println("DEBUG: Selected account - Session ID: " + sessionId +
                        ", Character ID: " + characterId + ", Display Name: " + displayName);

                // Set the environment variables
                System.setProperty("JX_SESSION_ID", sessionId);
                System.setProperty("JX_CHARACTER_ID", characterId);
                System.setProperty("JX_DISPLAY_NAME", displayName);

                // Update the credentials.properties file
                updateCredentialsFile(characterId, sessionId, displayName);

                // Reinitialize the client
                clientThread.invoke(() -> {
                    try {
                        System.out.println("DEBUG: Starting client reinitialization");

                        // Log out the current user if logged in
                        if (client.getGameState() != GameState.LOGIN_SCREEN) {
                            System.out.println("DEBUG: Logging out current user");
                            client.setGameState(GameState.LOGIN_SCREEN);
                        }

                        // Set the Jagex login index
                        LoginIndexSetter.setJagexLoginIndex(client);
                        System.out.println("DEBUG: Jagex login index set");

                        // Clear any cached user data
                        client.setUsername("");
                        System.out.println("DEBUG: Cleared cached username");

                        // Set GameState to STARTING
                        client.setGameState(GameState.STARTING);
                        System.out.println("DEBUG: Setting GameState to STARTING");

                        // Set the display name directly
                        setDisplayName(displayName);
                        System.out.println("DEBUG: Display name set to " + displayName);

                        System.out.println("DEBUG: Client reinitialization complete");
                    } catch (Exception e) {
                        System.out.println("DEBUG: Error during client reinitialization: " + e.getMessage());
                        e.printStackTrace();
                    }
                });

                System.out.println("DEBUG: Account loaded and client reinitialization requested");
            } else {
                System.out.println("DEBUG: Session ID not found in file");
            }
        }
    }

    private int getLoginIndex(Object clientInstance) {
        try {
            // Access the Client class
            Class<?> clientClass = clientInstance.getClass();

            // Get the loginIndex field
            Field loginIndexField = clientClass.getDeclaredField("loginIndex");

            // Make the field accessible
            loginIndexField.setAccessible(true);

            // Get the value of the loginIndex field
            return loginIndexField.getInt(clientInstance);

        } catch (NoSuchFieldException e) {
            System.out.println("The 'loginIndex' field was not found in the Client class.");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.out.println("Unable to access the 'loginIndex' field.");
            e.printStackTrace();
        }
        return -1; // Return -1 if unable to retrieve the login index
    }

    private void updateCredentialsFile(String characterId, String sessionId, String displayName) {
        File credentialsFile = new File(RuneLite.OPENOSRS, "credentials.properties");
        Properties properties = new Properties();

        if (credentialsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(credentialsFile)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        properties.setProperty("JX_CHARACTER_ID", characterId);
        properties.setProperty("JX_SESSION_ID", sessionId);
        properties.setProperty("JX_DISPLAY_NAME", displayName);

        try (FileOutputStream fos = new FileOutputStream(credentialsFile)) {
            properties.store(fos, "Do not share this file with anyone");
            System.out.println("DEBUG: Updated credentials.properties file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadSessionIdFromFile() {
        System.out.println("DEBUG: Loading session ID from file");
        try {
            File file = new File(RuneLite.RUNELITE_DIR, "sessionid.json");
            System.out.println("DEBUG: File path: " + file.getAbsolutePath());
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();

                JSONObject jsonData = new JSONObject(content.toString());
                System.out.println("DEBUG: JSON data loaded: " + jsonData.toString());
                String sessionId = jsonData.getString("sessionId");
                System.out.println("DEBUG: Session ID: " + sessionId);
                return sessionId;
            } else {
                System.out.println("DEBUG: sessionid.json file does not exist");
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error loading session ID from file: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void updateAccountDropdown() throws JSONException {
        accountDropdown.removeAllItems();
        for (JSONObject account : accounts) {
            String displayName = account.getString("displayName");
            accountDropdown.addItem(displayName);
        }
    }

    private void fetchAccounts(String sessionId) throws Exception {
        System.out.println("DEBUG: Fetching accounts from API");
        URL url = new URL("https://auth.jagex.com/game-session/v1/accounts");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + sessionId);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println("DEBUG: API response: " + response.toString());

        JSONArray accountsArray = new JSONArray(response.toString());
        accounts = new ArrayList<>();
        for (int i = 0; i < accountsArray.length(); i++) {
            JSONObject account = accountsArray.getJSONObject(i);
            accounts.add(account);
            String characterId = account.getString("accountId");
            String displayName = account.getString("displayName");
            System.out.println("DEBUG: Account - Character ID: " + characterId + ", Display Name: " + displayName);
        }
        System.out.println("DEBUG: Fetched " + accounts.size() + " accounts from API");
        updateAccountDropdown();
    }
}
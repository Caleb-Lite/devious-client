package net.runelite.client.plugins.loginassistant;

import net.runelite.api.Client;

public class LoginIndexSetter {

    public static void setJagexLoginIndex(Object clientInstance) {
        setLoginIndex(clientInstance, 10);
    }

    public static void setLegacyLoginIndex(Object clientInstance) {
        setLoginIndex(clientInstance, 2);
    }

    public static int getLoginIndex(Object clientInstance) {
        try {
            // Cast the clientInstance to the Client interface
            Client client = (Client) clientInstance;

            // Use the getLoginIndex() method directly
            return client.getLoginIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if unable to retrieve the login index
    }

    private static void setLoginIndex(Object clientInstance, int index) {

    }
}
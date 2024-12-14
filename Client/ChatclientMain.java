package Client;

import javax.swing.*;

public class ChatclientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI());
    }
}

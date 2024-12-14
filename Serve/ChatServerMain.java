package Serve;

import javax.swing.*;

public class ChatServerMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatServerGUI());
    }
}

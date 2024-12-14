package Serve;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatServerGUI extends JFrame {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private JTextPane chatLogPane;
    private SimpleAttributeSet nameAttribute;

    public ChatServerGUI() {
        setTitle("多人聊天服务器");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 初始化聊天记录显示区域为JTextPane，方便设置文本样式
        chatLogPane = new JTextPane();
        chatLogPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatLogPane);
        add(scrollPane, BorderLayout.CENTER);

        // 设置名字显示的文本属性（这里设置为红色）
        nameAttribute = new SimpleAttributeSet();
        StyleConstants.setForeground(nameAttribute, Color.RED);

        // 启动服务器按钮
        JButton startServerButton = new JButton("启动服务器");
        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // 创建服务器套接字，监听指定端口（这里使用8888端口，可按需修改）
                    serverSocket = new ServerSocket(8888);
                    appendToChatLog("服务器已启动，正在监听客户端连接...");

                    // 开启线程监听客户端连接
                    Thread acceptThread = new Thread(() -> {
                        while (true) {
                            try {
                                Socket clientSocket = serverSocket.accept();
                                ClientHandler clientHandler = new ClientHandler(clientSocket);
                                clients.add(clientHandler);
                                clientHandler.start();
                                appendToChatLog("用户 [" + clientSocket.getInetAddress().getHostAddress() + "] 已连接");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    acceptThread.start();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startServerButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // 辅助方法，用于向聊天记录显示区域添加文本，并处理名字颜色显示
    private void appendToChatLog(String text) {
        StyledDocument doc = chatLogPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // 处理单个客户端连接的内部类
    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // 读取客户端发送过来的用户名
                String userName = in.readLine();
                // 通知其他客户端有新用户加入，设置用户名颜色为红色
                broadcast("用户 [" + userName + "] 加入了聊天室");
                String message;
                while ((message = in.readLine()) != null) {
                    // 可以添加一些格式判断，比如确保消息不是空等
                    if (!message.isEmpty()) {
                        // 广播客户端发送的消息，带上用户名且设置用户名颜色为红色
                        broadcast(userName + ": " + message);
                    }
                }
                clients.remove(this);
                broadcast("用户 [" + userName + "] 离开了聊天室");
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
            appendToChatLog(message);
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
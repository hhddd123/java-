package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatClientGUI extends JFrame {
    private JTextPane chatLogPane;
    private JTextField inputField;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private SimpleAttributeSet nameAttribute;
    private String userName;

    public ChatClientGUI() {
        setTitle("多人聊天客户端");
        setSize(400, 300);
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

        // 底部输入面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // 输入消息文本框
        inputField = new JTextField(20);
        bottomPanel.add(inputField);

        // 发送消息按钮
        JButton sendButton = new JButton("发送");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = inputField.getText().trim();
                if (!input.isEmpty()) {
                    out.println(input);
                    inputField.setText("");
                }
            }
        });
        bottomPanel.add(sendButton);

        // 为输入框添加键盘事件监听器，用于监听回车键按下事件
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String input = inputField.getText().trim();
                    if (!input.isEmpty()) {
                        out.println(input);
                        inputField.setText("");
                    }
                }
            }
        });

        add(bottomPanel, BorderLayout.SOUTH);

        // 弹出对话框让用户输入名字
        userName = JOptionPane.showInputDialog("请输入您的名字");
        if (userName == null) {
            userName = "匿名";
        }

        try {
            // 连接服务器（这里假设服务器运行在本地，端口是8888）
            socket = new Socket("111.161.122.206", 20679);
            appendToChatLog("已连接到服务器");

            // 发送用户名到服务器
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(userName);

            // 获取输入输出流
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 启动线程接收服务器转发的消息
            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine())!= null) {
                        appendToChatLog(message);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            receiveThread.start();

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "无法连接到服务器，请检查服务器是否已启动以及网络设置！", "连接错误", JOptionPane.ERROR_MESSAGE);
        }

        setVisible(true);
    }

    // 辅助方法，用于向聊天记录显示区域添加文本，并处理名字颜色显示
    private void appendToChatLog(String text) {
        StyledDocument doc = chatLogPane.getStyledDocument();
        try {
            int index = text.indexOf(":");
            if (index > 0) {
                String namePart = text.substring(0, index);
                String msgPart = text.substring(index + 1);
                doc.insertString(doc.getLength(), namePart, nameAttribute);
                doc.insertString(doc.getLength(), ": " + msgPart + "\n", null);
            } else {
                doc.insertString(doc.getLength(), text + "\n", null);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * S-DES加解密GUI界面（修复组件引用错误，确保可运行）
 */
public class SDesGUI extends JFrame {
    // 基础加解密组件
    private JTextField plaintextField;
    private JTextField keyField;
    private JTextArea resultArea;

    // 暴力破解组件
    private JTextField bfPlaintextField;
    private JTextField bfCiphertextField;
    private JTextArea bfResultArea;

    // 按钮组件（全局引用，用于绑定事件）
    private JButton encryptBtn;
    private JButton decryptBtn;
    private JButton asciiEncryptBtn;
    private JButton asciiDecryptBtn;
    private JButton bruteForceBtn;

    public SDesGUI() {
        initUI();
    }

    /**
     * 初始化GUI界面
     */
    private void initUI() {
        // 窗口基本设置
        setTitle("S-DES 加解密程序");
        setSize(650, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        // 主面板（GridBagLayout布局）
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // -------------------------- 基础加解密区域 --------------------------
        // 明文标签与输入框
        JLabel plaintextLabel = new JLabel("明文 (8bit二进制/ASCII):");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(plaintextLabel, gbc);

        plaintextField = new JTextField(45);
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(plaintextField, gbc);

        // 密钥标签与输入框
        JLabel keyLabel = new JLabel("密钥 (10bit二进制):");
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(keyLabel, gbc);

        keyField = new JTextField(45);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(keyField, gbc);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel();
        encryptBtn = new JButton("二进制加密");
        decryptBtn = new JButton("二进制解密");
        asciiEncryptBtn = new JButton("ASCII加密");
        asciiDecryptBtn = new JButton("ASCII解密");
        bruteForceBtn = new JButton("暴力破解");

        buttonPanel.add(encryptBtn);
        buttonPanel.add(decryptBtn);
        buttonPanel.add(asciiEncryptBtn);
        buttonPanel.add(asciiDecryptBtn);
        buttonPanel.add(bruteForceBtn);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        // 结果显示区
        JLabel resultLabel = new JLabel("结果:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        mainPanel.add(resultLabel, gbc);

        resultArea = new JTextArea(6, 45);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(resultScroll, gbc);

        // -------------------------- 暴力破解区域 --------------------------
        JLabel bfTitleLabel = new JLabel("=== 暴力破解（明密文对需为8bit二进制）===");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(bfTitleLabel, gbc);

        // 明文对输入
        JLabel bfPlaintextLabel = new JLabel("明文对:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        mainPanel.add(bfPlaintextLabel, gbc);

        bfPlaintextField = new JTextField(45);
        gbc.gridx = 1;
        gbc.gridy = 5;
        mainPanel.add(bfPlaintextField, gbc);

        // 密文对输入
        JLabel bfCiphertextLabel = new JLabel("密文对:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(bfCiphertextLabel, gbc);

        bfCiphertextField = new JTextField(45);
        gbc.gridx = 1;
        gbc.gridy = 6;
        mainPanel.add(bfCiphertextField, gbc);

        // 破解结果显示
        JLabel bfResultLabel = new JLabel("破解结果:");
        gbc.gridx = 0;
        gbc.gridy = 7;
        mainPanel.add(bfResultLabel, gbc);

        bfResultArea = new JTextArea(4, 45);
        bfResultArea.setEditable(false);
        JScrollPane bfResultScroll = new JScrollPane(bfResultArea);
        gbc.gridx = 1;
        gbc.gridy = 7;
        mainPanel.add(bfResultScroll, gbc);

        // 添加主面板到窗口
        add(mainPanel);

        // 绑定按钮事件
        bindEvents();
    }

    /**
     * 绑定按钮点击事件（直接使用组件引用，避免getComponent()错误）
     */
    private void bindEvents() {
        encryptBtn.addActionListener(e -> handleBinaryOperation(true));
        decryptBtn.addActionListener(e -> handleBinaryOperation(false));
        asciiEncryptBtn.addActionListener(e -> handleAsciiOperation(true));
        asciiDecryptBtn.addActionListener(e -> handleAsciiOperation(false));
        bruteForceBtn.addActionListener(e -> handleBruteForce());
    }

    /**
     * 处理二进制加解密
     */
    private void handleBinaryOperation(boolean isEncrypt) {
        String inputStr = plaintextField.getText().trim();
        String keyStr = keyField.getText().trim();
        resultArea.setText("");

        // 输入校验
        if (inputStr.length() != 8) {
            resultArea.setText("❌ 错误：二进制输入必须为8位");
            return;
        }
        if (keyStr.length() != 10) {
            resultArea.setText("❌ 错误：密钥必须为10位二进制");
            return;
        }

        try {
            int[] inputArray = SDesUtil.binaryStrToIntArray(inputStr);
            int[] keyArray = SDesUtil.binaryStrToIntArray(keyStr);
            if(isEncrypt){
                int[] resultArray = SDesUtil.encrypt(inputArray, keyArray);
                String resultStr = SDesUtil.intArrayToBinaryStr(resultArray);
                resultArea.setText( "✅ 密文: " + resultStr);
                System.out.println(resultStr);
            }
            else{
                int[] resultArray = SDesUtil.decrypt(inputArray, keyArray);
                String resultStr = SDesUtil.intArrayToBinaryStr(resultArray);
                resultArea.setText("✅ 明文: " + resultStr);
                System.out.println(resultStr);
            };
        } catch (IllegalArgumentException ex) {
            resultArea.setText("❌ 错误：" + ex.getMessage());
        }
    }

    /**
     * 处理ASCII加解密
     */
    private void handleAsciiOperation(boolean isEncrypt) {
        String inputStr = plaintextField.getText().trim();
        String keyStr = keyField.getText().trim();
        resultArea.setText("");

        if (keyStr.length() != 10) {
            resultArea.setText("❌ 错误：密钥必须为10位二进制");
            return;
        }

        try {
            int[] keyArray = SDesUtil.binaryStrToIntArray(keyStr);
            StringBuilder binaryResult = new StringBuilder();
            String inputBinary = SDesUtil.asciiToBinary(inputStr);

            if (!isEncrypt && inputBinary.length() % 8 != 0) {
                resultArea.setText("❌ 错误：密文对应的二进制长度不是8的倍数");
                return;
            }

            // 逐字节处理
            for (int i = 0; i < inputBinary.length(); i += 8) {
                String byteStr = inputBinary.substring(i, i + 8);
                int[] byteArray = SDesUtil.binaryStrToIntArray(byteStr);
                int[] processed = isEncrypt ?
                        SDesUtil.encrypt(byteArray, keyArray) :
                        SDesUtil.decrypt(byteArray, keyArray);
                binaryResult.append(SDesUtil.intArrayToBinaryStr(processed));
            }

            String asciiResult = SDesUtil.binaryToAscii(binaryResult.toString());
            resultArea.setText(isEncrypt ? "✅ ASCII加密结果: " : "✅ ASCII解密结果: " + asciiResult);
        } catch (IllegalArgumentException ex) {
            resultArea.setText("❌ 错误：" + ex.getMessage());
        }
    }

    /**
     * 处理暴力破解
     */
    private void handleBruteForce() {
        String plaintextStr = bfPlaintextField.getText().trim();
        String ciphertextStr = bfCiphertextField.getText().trim();
        bfResultArea.setText("");

        if (plaintextStr.length() != 8 || ciphertextStr.length() != 8) {
            bfResultArea.setText("❌ 错误：明密文对必须为8位二进制");
            return;
        }

        try {
            int[] plaintextArray = SDesUtil.binaryStrToIntArray(plaintextStr);
            int[] targetCipherArray = SDesUtil.binaryStrToIntArray(ciphertextStr);

            long startTime = System.currentTimeMillis();
            List<String> foundKeys = new ArrayList<>();

            // 多线程破解（4线程）
            int threadCount = 4;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                int start = i * 256;
                int end = (i + 1) * 256;

                executor.submit(() -> {
                    for (int keyInt = start; keyInt < end; keyInt++) {
                        String keyStr = String.format("%10s", Integer.toBinaryString(keyInt)).replace(' ', '0');
                        int[] keyArray = SDesUtil.binaryStrToIntArray(keyStr);

                        if (Arrays.equals(SDesUtil.encrypt(plaintextArray, keyArray), targetCipherArray)) {
                            synchronized (foundKeys) {
                                foundKeys.add(keyStr);
                            }
                        }
                    }
                    latch.countDown();
                });
            }

            // 等待线程完成并更新UI
            new Thread(() -> {
                try {
                    latch.await();
                    executor.shutdown();
                    long endTime = System.currentTimeMillis();
                    double timeTaken = (endTime - startTime) / 1000.0;

                    StringBuilder resultSb = new StringBuilder();
                    resultSb.append("✅ 破解完成！耗时: ").append(String.format("%.3f", timeTaken)).append(" 秒\n");
                    if (foundKeys.isEmpty()) {
                        resultSb.append("❌ 未找到匹配的密钥");
                    } else {
                        resultSb.append("🔑 找到的密钥（共").append(foundKeys.size()).append("个）: ").append(foundKeys);
                    }

                    SwingUtilities.invokeLater(() -> bfResultArea.setText(resultSb.toString()));
                } catch (InterruptedException e) {
                    SwingUtilities.invokeLater(() -> bfResultArea.setText("❌ 破解中断：" + e.getMessage()));
                }
            }).start();

        } catch (IllegalArgumentException ex) {
            bfResultArea.setText("❌ 错误：" + ex.getMessage());
        }
    }

    /**
     * 主方法：启动程序
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SDesGUI().setVisible(true));
    }
}

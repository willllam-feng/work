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
     * 初始化GUI界面（响应式布局，组件可随窗口大小自适应）
     */
    private void initUI() {
        // 窗口基本设置
        setTitle("S-DES 加解密程序");
        setSize(650, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        // 主面板（GridBagLayout布局，支持响应式调整）
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL; // 默认水平填充
        gbc.weightx = 1.0; // 所有组件默认分配水平额外空间

        // -------------------------- 基础加解密区域 --------------------------
        // 明文标签与输入框
        JLabel plaintextLabel = new JLabel("明文或密文 (8bit二进制/ASCII):");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 0; // 标签不分配垂直额外空间
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
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
        gbc.fill = GridBagConstraints.BOTH; // 结果区域同时水平和垂直填充
        gbc.weighty = 1.0; // 分配垂直方向额外空间
        mainPanel.add(resultScroll, gbc);

        // 重置权重和填充方式，避免影响后续组件
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

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
        gbc.fill = GridBagConstraints.BOTH; // 破解结果区域同时水平和垂直填充
        gbc.weighty = 1.0; // 分配垂直方向额外空间
        mainPanel.add(bfResultScroll, gbc);

        // 添加主面板到窗口，并设置主面板可滚动（防止内容溢出）
        JScrollPane mainScroll = new JScrollPane(mainPanel);
        mainScroll.setBorder(null); // 移除边框
        add(mainScroll);

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
     * 处理ASCII字符串的加密或解密操作
     * @param isEncrypt true表示加密，false表示解密
     */
    private void handleAsciiOperation(boolean isEncrypt) {
        // 清空结果区域
        resultArea.setText("");

        // 获取输入和密钥
        String inputStr = plaintextField.getText().trim();
        String keyStr = keyField.getText().trim();

        // 1. 验证密钥合法性
        if (!isValidKey(keyStr)) {
            resultArea.setText("❌ 错误：密钥必须为10位二进制数字（仅包含0和1）");
            return;
        }

        try {
            // 2. 密钥转换：字符串 -> 整数数组（用于S-DES算法）
            int[] keyArray = SDesUtil.binaryStrToIntArray(keyStr);

            // 3. 输入转换：ASCII字符串 -> 二进制字符串
            String inputBinary;
            if (isEncrypt) {
                // 加密：ASCII明文 -> 二进制
                inputBinary = SDesUtil.asciiToBinary(inputStr);
                // 验证加密输入的二进制长度（理论上ASCII转换后一定是8的倍数，此处为防御性检查）
                if (inputBinary.length() % 8 != 0) {
                    resultArea.setText("❌ 错误：明文转换后二进制长度异常");
                    return;
                }
            } else {
                // 解密：密文（ASCII字符对应的二进制）
                inputBinary = SDesUtil.asciiToBinary(inputStr);
                // 解密必须保证二进制长度是8的倍数（每个加密块为8位）
                if (inputBinary.length() % 8 != 0) {
                    resultArea.setText("❌ 错误：密文对应的二进制长度必须是8的倍数");
                    return;
                }
            }

            // 4. 逐8位块处理（S-DES算法处理单位为8位）
            StringBuilder binaryResult = new StringBuilder();
            for (int i = 0; i < inputBinary.length(); i += 8) {
                // 截取8位二进制块
                String byteStr = inputBinary.substring(i, i + 8);
                // 转换为整数数组
                int[] byteArray = SDesUtil.binaryStrToIntArray(byteStr);
                // 加密/解密处理
                int[] processedArray = isEncrypt
                        ? SDesUtil.encrypt(byteArray, keyArray)
                        : SDesUtil.decrypt(byteArray, keyArray);
                // 转换回二进制字符串并拼接
                binaryResult.append(SDesUtil.intArrayToBinaryStr(processedArray));
            }

            // 5. 结果转换：二进制 -> ASCII字符串
            String asciiResult = SDesUtil.binaryToAscii(binaryResult.toString());

            // 6. 显示结果（修复三元运算符拼接问题）
            String resultPrefix = isEncrypt ? "✅ ASCII加密结果: " : "✅ ASCII解密结果: ";
            resultArea.setText(resultPrefix + asciiResult);

        } catch (IllegalArgumentException e) {
            // 处理二进制转换、密钥解析等已知异常
            resultArea.setText("❌ 错误：" + e.getMessage());
        } catch (Exception e) {
            // 处理其他未知异常
            resultArea.setText("❌ 操作失败：" + e.getMessage());
        }
    }

    /**
     * 验证密钥是否合法（10位且仅包含0和1）
     * @param key 待验证的密钥字符串
     * @return 合法返回true，否则false
     */
    private boolean isValidKey(String key) {
        // 长度必须为10
        if (key.length() != 10) {
            return false;
        }
        // 仅包含0和1
        for (char c : key.toCharArray()) {
            if (c != '0' && c != '1') {
                return false;
            }
        }
        return true;
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

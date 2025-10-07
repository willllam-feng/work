import java.util.Arrays;

/**
 * SDesUtil算法测试类：逐模块验证密钥扩展、轮函数、加密、解密的正确性
 */
public class SDesTest {
    public static void main(String[] args) {
        System.out.println("=== 1. 测试密钥扩展（generateSubkeys）===");
        testSubkeyGeneration();

        System.out.println("\n=== 2. 测试轮函数F（functionF）===");
        testFunctionF();

        System.out.println("\n=== 3. 测试加密算法（encrypt）===");
        testEncryption();

        System.out.println("\n=== 4. 测试解密算法（decrypt，验证可逆性）===");
        testDecryption();

        System.out.println("\n=== 5. 测试ASCII扩展功能 ===");
        testAsciiConversion();
    }

    /**
     * 测试1：密钥扩展（10位→k1、k2）
     * 输入：0000000000 → 预期k1：10100100，k2：01010010
     */
    private static void testSubkeyGeneration() {
        String keyStr = "0000000000";
        int[] keyArray = SDesUtil.binaryStrToIntArray(keyStr);
        int[][] subkeys = SDesUtil.generateSubkeys(keyArray);

        String k1Str = SDesUtil.intArrayToBinaryStr(subkeys[0]);
        String k2Str = SDesUtil.intArrayToBinaryStr(subkeys[1]);

        System.out.println("输入密钥：" + keyStr);
        System.out.println("生成k1：" + k1Str + "（预期：10100100）");
        System.out.println("生成k2：" + k2Str + "（预期：01010010）");

        // 校验结果
        if (k1Str.equals("10100100") && k2Str.equals("01010010")) {
            System.out.println("✅ 密钥扩展测试通过！");
        } else {
            System.out.println("❌ 密钥扩展测试失败！");
        }
    }

    /**
     * 测试2：轮函数F（4位数据 + 8位子密钥→4位输出）
     * 输入：数据=0000（右半部分），子密钥k1=10100100 → 预期输出：1001
     */
    private static void testFunctionF() {
        String right4Str = "0000";
        String subkey8Str = "10100100"; // 测试1生成的k1
        int[] right4Array = SDesUtil.binaryStrToIntArray(right4Str);
        int[] subkey8Array = SDesUtil.binaryStrToIntArray(subkey8Str);

        int[] fResult = SDesUtil.functionF(right4Array, subkey8Array);
        String fResultStr = SDesUtil.intArrayToBinaryStr(fResult);

        System.out.println("输入数据（4位）：" + right4Str);
        System.out.println("输入子密钥（8位）：" + subkey8Str);
        System.out.println("轮函数输出：" + fResultStr + "（预期：1001）");

        if (fResultStr.equals("1001")) {
            System.out.println("✅ 轮函数F测试通过！");
        } else {
            System.out.println("❌ 轮函数F测试失败！");
        }
    }

    /**
     * 测试3：加密算法（8位明文→8位密文）
     * 用例1：明文00000000 + 密钥0000000000 → 预期密文10010111
     * 用例2：明文11111111 + 密钥0000000000 → 预期密文01101000
     */
    private static void testEncryption() {
        // 用例1：基础测试
        String plain1Str = "00000000";
        String keyStr = "0000000000";
        int[] plain1Array = SDesUtil.binaryStrToIntArray(plain1Str);
        int[] keyArray = SDesUtil.binaryStrToIntArray(keyStr);

        // 打印加密中间步骤（辅助定位错误）
        System.out.println("=== 用例1：明文=" + plain1Str + "，密钥=" + keyStr);
        int[] ipResult = SDesUtil.permutate(plain1Array, SDesUtil.IP);
        System.out.println("IP置换后：" + SDesUtil.intArrayToBinaryStr(ipResult));

        int[][] subkeys = SDesUtil.generateSubkeys(keyArray);
        System.out.println("子密钥k1：" + SDesUtil.intArrayToBinaryStr(subkeys[0]));
        System.out.println("子密钥k2：" + SDesUtil.intArrayToBinaryStr(subkeys[1]));

        // 执行加密
        int[] cipher1Array = SDesUtil.encrypt(plain1Array, keyArray);
        String cipher1Str = SDesUtil.intArrayToBinaryStr(cipher1Array);
        System.out.println("加密结果：" + cipher1Str + "（预期：10010111）");

        // 用例2：全1明文测试
        String plain2Str = "11111111";
        int[] plain2Array = SDesUtil.binaryStrToIntArray(plain2Str);
        int[] cipher2Array = SDesUtil.encrypt(plain2Array, keyArray);
        String cipher2Str = SDesUtil.intArrayToBinaryStr(cipher2Array);
        System.out.println("\n=== 用例2：明文=" + plain2Str + "，密钥=" + keyStr);
        System.out.println("加密结果：" + cipher2Str + "（预期：01101000）");

        // 校验结果
        if (cipher1Str.equals("10010111") && cipher2Str.equals("01101000")) {
            System.out.println("✅ 加密算法测试通过！");
        } else {
            System.out.println("❌ 加密算法测试失败！");
        }
    }

    /**
     * 测试4：解密算法（验证可逆性）
     * 输入：密文10010111 + 密钥0000000000 → 预期明文00000000
     */
    private static void testDecryption() {
        String cipherStr = "10010111"; // 测试3的密文
        String keyStr = "0000000000";
        int[] cipherArray = SDesUtil.binaryStrToIntArray(cipherStr);
        int[] keyArray = SDesUtil.binaryStrToIntArray(keyStr);

        int[] plainArray = SDesUtil.decrypt(cipherArray, keyArray);
        String plainStr = SDesUtil.intArrayToBinaryStr(plainArray);

        System.out.println("输入密文：" + cipherStr);
        System.out.println("输入密钥：" + keyStr);
        System.out.println("解密结果：" + plainStr + "（预期：00000000）");

        if (plainStr.equals("00000000")) {
            System.out.println("✅ 解密算法（可逆性）测试通过！");
        } else {
            System.out.println("❌ 解密算法测试失败！");
        }
    }

    /**
     * 测试5：ASCII扩展功能（字符串→二进制→字符串）
     */
    private static void testAsciiConversion() {
        String asciiStr = "A"; // ASCII码65 → 二进制01000001
        String binaryExpected = "01000001";

        // ASCII→二进制
        String binaryActual = SDesUtil.asciiToBinary(asciiStr);
        System.out.println("ASCII字符串：" + asciiStr);
        System.out.println("转二进制：" + binaryActual + "（预期：" + binaryExpected + "）");

        // 二进制→ASCII
        String asciiActual = SDesUtil.binaryToAscii(binaryActual);
        System.out.println("二进制转回ASCII：" + asciiActual + "（预期：" + asciiStr + "）");

        if (binaryActual.equals(binaryExpected) && asciiActual.equals(asciiStr)) {
            System.out.println("✅ ASCII扩展功能测试通过！");
        } else {
            System.out.println("❌ ASCII扩展功能测试失败！");
        }
    }
}

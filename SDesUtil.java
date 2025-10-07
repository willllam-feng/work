import java.util.Arrays;

/**
 * S-DES算法工具类，封装密钥扩展、置换、轮函数、加解密等核心逻辑
 */
public class SDesUtil {
    // --------------------- 置换盒与S-Box定义--------------------------
    /** 10位密钥置换盒P10 */
    public static final int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    /** 8位子密钥置换盒P8 */
    public static final int[] P8 = {6, 3, 7, 4, 8, 5, 10, 9};
    /** 初始置换盒IP */
    public static final int[] IP = {2, 6, 3, 1, 4, 8, 5, 7};
    /** 最终置换盒IP-1 */
    public static final int[] IP_INVERSE = {4, 1, 3, 5, 7, 2, 8, 6};
    /** 轮函数扩展盒EP-Box（4位→8位） */
    public static final int[] EP_BOX = {4, 1, 2, 3, 2, 3, 4, 1};
    /** S-Box1 */
    public static final int[][] S_BOX1 = {
            {1, 0, 3, 2},
            {3, 2, 1, 0},
            {0, 2, 1, 3},
            {3, 1, 0, 2}
    };
    /** S-Box2 */
    public static final int[][] S_BOX2 = {
            {0, 1, 2, 3},
            {2, 3, 1, 0},
            {3, 0, 1, 2},
            {2, 1, 0, 3}
    };
    /** 轮函数置换盒SP-Box */
    public static final int[] SP_BOX = {2, 4, 3, 1};

    // -------------------------- 基础工具方法 --------------------------
    /**
     * 二进制字符串转int数组（如"1010"→[1,0,1,0]）
     * @param binaryStr 仅含0/1的二进制字符串
     * @return 对应的int数组
     * @throws IllegalArgumentException 输入含非0/1字符时抛出
     */
    public static int[] binaryStrToIntArray(String binaryStr) throws IllegalArgumentException {
        if (!binaryStr.matches("[01]+")) {
            throw new IllegalArgumentException("二进制字符串只能包含0和1");
        }
        int[] array = new int[binaryStr.length()];
        for (int i = 0; i < binaryStr.length(); i++) {
            array[i] = binaryStr.charAt(i) - '0';
        }
        return array;
    }

    /**
     * int数组转二进制字符串（如[1,0,1,0]→"1010"）
     * @param array 仅含0/1的int数组
     * @return 对应的二进制字符串
     */
    public static String intArrayToBinaryStr(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int bit : array) {
            sb.append(bit);
        }
        return sb.toString();
    }

    /**
     * 合并两个int数组（用于拼接左右半部分密钥/数据）
     * @param a 第一个数组
     * @param b 第二个数组
     * @return 合并后的新数组
     */
    private static int[] mergeArrays(int[] a, int[] b) {
        int[] result = new int[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    // -------------------------- 算法核心方法 --------------------------
    /**
     * 执行置换操作
     */
    public static int[] permutate(int[] input, int[] permutationTable) {
        int[] result = new int[permutationTable.length];
        for (int i = 0; i < permutationTable.length; i++) {
            // 置换表为1-based，转换为input的0-based索引
            result[i] = input[permutationTable[i] - 1];
        }
        return result;
    }

    /**
     * 循环左移操作（对n位数据左移指定次数）
     */
    public static int[] leftShift(int[] input, int shifts) {
        int length = input.length;
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            // 循环左移：(当前索引+移位次数) mod 数组长度
            result[i] = input[(i + shifts) % length];
        }
        return result;
    }

    /**
     * 密钥扩展：10位密钥生成2个8位子密钥k1、k2
     * @param key10 10位密钥的int数组
     * @return 二维数组，[0]为k1，[1]为k2
     */
    public static int[][] generateSubkeys(int[] key10) {
        // 步骤1：10位密钥经P10置换
        int[] p10Result = permutate(key10, P10);
        // 步骤2：拆分为左右各5位
        int[] left = Arrays.copyOfRange(p10Result, 0, 5);
        int[] right = Arrays.copyOfRange(p10Result, 5, 10);

        // 生成k1：左右各左移1位 → 合并 → P8置换
        int[] leftShift1 = leftShift(left, 1);
        int[] rightShift1 = leftShift(right, 1);
        int[] k1Pre = mergeArrays(leftShift1, rightShift1);
        int[] k1 = permutate(k1Pre, P8);

        // 生成k2：在k1基础上左右各再左移1位（累计2位）→ 合并 → P8置换
        int[] leftShift2 = leftShift(leftShift1, 1);
        int[] rightShift2 = leftShift(rightShift1, 1);
        int[] k2Pre = mergeArrays(leftShift2, rightShift2);
        int[] k2 = permutate(k2Pre, P8);

        return new int[][]{k1, k2};
    }

    /**
     * 轮函数F：4位数据与8位子密钥作用，输出4位结果
     * @param right4 4位右半部分数据
     * @param subkey8 8位子密钥
     * @return 4位输出结果
     */
    public static int[] functionF(int[] right4, int[] subkey8) {
        // 步骤1：4位→8位扩展（EP-Box）
        int[] epResult = permutate(right4, EP_BOX);
        // 步骤2：与子密钥异或
        int[] xorResult = new int[8];
        for (int i = 0; i < 8; i++) {
            xorResult[i] = epResult[i] ^ subkey8[i];
        }
        // 步骤3：拆分为左右4位，分别进入S-Box1和S-Box2
        int[] s1Input = Arrays.copyOfRange(xorResult, 0, 4);
        int[] s2Input = Arrays.copyOfRange(xorResult, 4, 8);

        // 处理S-Box1：4位→2位
        int s1Row = s1Input[0] * 2 + s1Input[3]; // 行：第1、4位
        int s1Col = s1Input[1] * 2 + s1Input[2]; // 列：第2、3位
        int s1Value = S_BOX1[s1Row][s1Col];
        int[] s1Binary = new int[2];
        s1Binary[0] = (s1Value >> 1) & 1; // 高位
        s1Binary[1] = s1Value & 1;       // 低位

        // 处理S-Box2：4位→2位
        int s2Row = s2Input[0] * 2 + s2Input[3];
        int s2Col = s2Input[1] * 2 + s2Input[2];
        int s2Value = S_BOX2[s2Row][s2Col];
        int[] s2Binary = new int[2];
        s2Binary[0] = (s2Value >> 1) & 1;
        s2Binary[1] = s2Value & 1;

        // 步骤4：合并S-Box结果（4位）→ SP-Box置换
        int[] sCombined = mergeArrays(s1Binary, s2Binary);
        return permutate(sCombined, SP_BOX);
    }

    /**
     * 加密：8位明文→8位密文
     * @param plaintext8 8位明文的int数组
     * @param key10 10位密钥的int数组
     * @return 8位密文的int数组
     */
    public static int[] encrypt(int[] plaintext8, int[] key10) {
        // 步骤1：初始置换IP
        int[] ipResult = permutate(plaintext8, IP);
        // 步骤2：拆分为左右各4位
        int[] left = Arrays.copyOfRange(ipResult, 0, 4);
        int[] right = Arrays.copyOfRange(ipResult, 4, 8);
        // 步骤3：生成子密钥k1、k2
        int[][] subkeys = generateSubkeys(key10);
        int[] k1 = subkeys[0];
        int[] k2 = subkeys[1];

        // 步骤4：第一轮迭代fk1（左=左⊕F(右,k1)，右不变）
        int[] f1Result = functionF(right, k1);
        int[] newLeft1 = new int[4];
        for (int i = 0; i < 4; i++) {
            newLeft1[i] = left[i] ^ f1Result[i];
        }

        // 步骤5：SW交换（左右互换）
        int[] swLeft = right;
        int[] swRight = newLeft1;

        // 步骤6：第二轮迭代fk2（左=左⊕F(右,k2)，右不变）
        int[] f2Result = functionF(swRight, k2);
        int[] newLeft2 = new int[4];
        for (int i = 0; i < 4; i++) {
            newLeft2[i] = swLeft[i] ^ f2Result[i];
        }

        // 步骤7：合并左右→最终置换IP-1
        int[] preIpInverse = mergeArrays(newLeft2, swRight);
        return permutate(preIpInverse, IP_INVERSE);
    }

    /**
     * 解密：8位密文→8位明文（子密钥顺序与加密相反：k2→k1）
     * @param ciphertext8 8位密文的int数组
     * @param key10 10位密钥的int数组
     * @return 8位明文的int数组
     */
    public static int[] decrypt(int[] ciphertext8, int[] key10) {
        // 步骤1：初始置换IP（与加密相同）
        int[] ipResult = permutate(ciphertext8, IP);
        // 步骤2：拆分为左右各4位
        int[] left = Arrays.copyOfRange(ipResult, 0, 4);
        int[] right = Arrays.copyOfRange(ipResult, 4, 8);
        // 步骤3：生成子密钥k1、k2
        int[][] subkeys = generateSubkeys(key10);
        int[] k1 = subkeys[0];
        int[] k2 = subkeys[1];

        // 步骤4：第一轮迭代fk2（解密先用k2）
        int[] f1Result = functionF(right, k2);
        int[] newLeft1 = new int[4];
        for (int i = 0; i < 4; i++) {
            newLeft1[i] = left[i] ^ f1Result[i];
        }

        // 步骤5：SW交换
        int[] swLeft = right;
        int[] swRight = newLeft1;

        // 步骤6：第二轮迭代fk1（再用k1）
        int[] f2Result = functionF(swRight, k1);
        int[] newLeft2 = new int[4];
        for (int i = 0; i < 4; i++) {
            newLeft2[i] = swLeft[i] ^ f2Result[i];
        }

        // 步骤7：合并左右→最终置换IP-1
        int[] preIpInverse = mergeArrays(newLeft2, swRight);
        return permutate(preIpInverse, IP_INVERSE);
    }

    // -------------------------- ASCII扩展功能方法 --------------------------
    /**
     * ASCII字符串转8位分组二进制字符串（如"A"→"01000001"）
     * @param asciiStr 输入ASCII字符串
     * @return 连续的8位分组二进制字符串
     */
    public static String asciiToBinary(String asciiStr) {
        StringBuilder binarySb = new StringBuilder();
        for (char c : asciiStr.toCharArray()) {
            // 补0至8位
            String binary = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            binarySb.append(binary);
        }
        return binarySb.toString();
    }

    /**
     * 8位分组二进制字符串转ASCII字符串
     * @param binaryStr 连续的8位分组二进制字符串
     * @return 对应的ASCII字符串（可能为乱码）
     * @throws IllegalArgumentException 二进制长度不是8的倍数时抛出
     */
    public static String binaryToAscii(String binaryStr) throws IllegalArgumentException {
        if (binaryStr.length() % 8 != 0) {
            throw new IllegalArgumentException("二进制字符串长度必须是8的倍数");
        }
        StringBuilder asciiSb = new StringBuilder();
        for (int i = 0; i < binaryStr.length(); i += 8) {
            String byteStr = binaryStr.substring(i, i + 8);
            int value = Integer.parseInt(byteStr, 2);
            asciiSb.append((char) value);
        }
        return asciiSb.toString();
    }
}
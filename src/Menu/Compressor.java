package Menu;

/**
 * Compressor - Doing RLE coding and decoding.
 */
public class Compressor {

    private static final int[][] ZIGZAG_ORDER = new int[][]{
        {0, 1, 5, 6, 14, 15, 27, 28},
        {2, 4, 7, 13, 16, 26, 29, 42},
        {3, 8, 12, 17, 25, 30, 41, 43},
        {9, 11, 18, 24, 31, 40, 44, 53},
        {10, 19, 23, 32, 39, 45, 52, 54},
        {20, 22, 33, 38, 46, 51, 55, 60},
        {21, 34, 37, 47, 50, 56, 59, 61},
        {35, 36, 48, 49, 57, 58, 62, 63}
    };
    private static final int[][] FANOUT_ORDER = new int[][]{
        {0, 1, 4, 8, 13, 19, 26, 34,},
        {2, 3, 6, 11, 17, 24, 32, 41,},
        {5, 7, 10, 15, 22, 30, 39, 47,},
        {9, 12, 16, 21, 28, 37, 45, 52,},
        {14, 18, 23, 29, 36, 43, 50, 56,},
        {20, 25, 31, 38, 44, 49, 54, 59,},
        {27, 33, 40, 46, 51, 55, 58, 61,},
        {35, 42, 48, 53, 57, 60, 62, 63,}
    };

    public static void toArray(int[][] srcMat, int[] dstArr) {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                dstArr[FANOUT_ORDER[i][j]] = srcMat[i][j];
    }

    public static void toMatrix(int[] srcArr, int[][] dstMat) {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                dstMat[i][j] = srcArr[FANOUT_ORDER[i][j]];
    }

    public static int toRLE(int[] src, byte[] dest, int startAtBits) {
        int minBits[] = new int[64]; // 以此元素为结尾的序列的最小长度
        int maxBits[] = new int[64]; // 以此元素为结尾的组中元素的最大位宽
        int lastGroupSize[] = new int[64]; // 以此元素为结尾的组的大小
        int bitWidth[] = new int[64]; // 每个元素的位宽
        int lastValIdx = 63; // 最后一个不为0的数的下标

        for (; lastValIdx >= 0; lastValIdx--) // 删除0后缀
            if (src[lastValIdx] != 0)
                break;
        for (int i = 0; i <= lastValIdx; i++) { // 动态规划求最优分组
            bitWidth[i] = getBitWidth(src[i]);
            maxBits[i] = bitWidth[i];
            minBits[i] = (i == 0 ? 0 : minBits[i - 1]) + maxBits[i];
            lastGroupSize[i] = 1;
            for (int j = 1; j <= Math.min(i, 14); j++) { // 遍历最后一组的大小
                maxBits[i] = Math.max(maxBits[i], bitWidth[i - j]);
                int segBits = ((i == j) ? 0 : minBits[i - j - 1])
                        + maxBits[i] * (j + 1);
                if (minBits[i] > segBits) {
                    minBits[i] = segBits;
                    lastGroupSize[i] = j + 1;
                }
            }
            minBits[i] += 8;
        }
        for (int i = lastValIdx, j; i >= 0; i -= lastGroupSize[i]) { // 回溯分组
            j = i - lastGroupSize[i] + 1;
            lastGroupSize[j] = lastGroupSize[i];
            maxBits[j] = maxBits[i];
        }
        int bitCnt = 0;
        for (int i = 0; i <= lastValIdx;) { // 逐组写入
            setBits(lastGroupSize[i], 4, dest, startAtBits + bitCnt);
            setBits(maxBits[i], 4, dest, startAtBits + bitCnt + 4);
            bitCnt += 8;
            if (maxBits[i] > 1) { // 重复0特判
                for (int j = 0; j < lastGroupSize[i]; j++) {
                    setBits(src[i + j], maxBits[i], dest, startAtBits + bitCnt);
                    bitCnt += maxBits[i];
                }
            }
            i += lastGroupSize[i];
        }
        if (lastValIdx < 63) { // 尾部0特判
            setBits(0, 4, dest, startAtBits + bitCnt);
            bitCnt += 4;
        }
        return bitCnt;
    }

    public static int reRLE(byte[] src, int startAtBits, int[] dest) {
        int bitCnt = 0;
        int destIdx = 0;
        while (destIdx < 64) {
            int groupSize = getBits(src, startAtBits + bitCnt, 4);
            bitCnt += 4;
            if (groupSize == 0)
                break;
            int bitWidth = getBits(src, startAtBits + bitCnt, 4);
            bitCnt += 4;
            for (int i = 0; i < groupSize; i++) {
                if (bitWidth == 1) // 重复0特判
                    dest[destIdx++] = 0;
                else {
                    dest[destIdx++] = signedExtend(
                            getBits(src, startAtBits + bitCnt, bitWidth),
                            bitWidth);
                    bitCnt += bitWidth;
                }
            }
        }
        for (; destIdx < 64; destIdx++) // 尾部0特判
            dest[destIdx] = 0;
        return bitCnt;
    }

    private static int getBits(byte[] src, int startAtBits, int bitCnt) {
        int byteIdx = startAtBits / 8;
        int bitIdx = startAtBits % 8;
        int result = 0;
        for (int i = 0; i < bitCnt; i++) {
            if ((src[byteIdx] & (1 << bitIdx)) != 0)
                result |= (1 << i);
            byteIdx += bitIdx == 7 ? 1 : 0;
            bitIdx = bitIdx == 7 ? 0 : bitIdx + 1;
        }
        return result;
    }

    private static void setBits(int src, int bitCnt, byte[] dest,
            int startAtBits) {
        int byteIdx = startAtBits / 8;
        int bitIdx = startAtBits % 8;
        for (int i = 0; i < bitCnt; i++) {
            dest[byteIdx] &= (byte) (~(1 << bitIdx));
            if ((src & (1 << i)) != 0)
                dest[byteIdx] |= (byte) (1 << bitIdx);
            byteIdx += bitIdx == 7 ? 1 : 0;
            bitIdx = bitIdx == 7 ? 0 : bitIdx + 1;
        }
    }

    private static int signedExtend(int src, int bitCnt) {
        if ((src & (1 << (bitCnt - 1))) != 0)
            return src | ((-1) << bitCnt);
        return src;
    }

    private static int getBitWidth(int a) {
        if (a < 0)
            a = -a - 1;
        if (a == 0)
            return 1;
        return (int) (Math.log(a) / Math.log(2)) + 2;
    }
}

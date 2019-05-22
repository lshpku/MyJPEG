package Menu;

/**
 *
 * @author Cocity
 */
public class Compressor {

    public static int[] toArray(int[][] matrix) { // zigzag transpose
        int array[] = new int[64];
        int arrayIdx = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j <= i; j++)
                array[arrayIdx++] = matrix[i - j][j];
            i++;
            for (int j = 0; j <= i; j++)
                array[arrayIdx++] = matrix[j][i - j];
        }
        for (int i = 1; i < 8; i++) {
            for (int j = 0; j < 8 - i; j++)
                array[arrayIdx++] = matrix[7 - j][i + j];
            i++;
            for (int j = 0; j < 8 - i; j++)
                array[arrayIdx++] = matrix[i + j][7 - j];
        }
        return array;
    }

    public static int[][] toMatrix(int[] array) { // zigzag transpose
        int matrix[][] = new int[8][8];
        int arrayIdx = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j <= i; j++)
                matrix[i - j][j] = array[arrayIdx++];
            i++;
            for (int j = 0; j <= i; j++)
                matrix[j][i - j] = array[arrayIdx++];
        }
        for (int i = 1; i < 8; i++) {
            for (int j = 0; j < 8 - i; j++)
                matrix[7 - j][i + j] = array[arrayIdx++];
            i++;
            for (int j = 0; j < 8 - i; j++)
                matrix[i + j][7 - j] = array[arrayIdx++];
        }
        return matrix;
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

    public static int getBitWidth(int a) {
        if (a < 0)
            a = -a - 1;
        if (a == 0)
            return 1;
        return (int) (Math.log(a) / Math.log(2)) + 2;
    }
}

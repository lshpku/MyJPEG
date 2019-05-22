package Menu;

/**
 * DCTConverter - Doing DCT coding and decoding.
 */
public class DCTConverter {

    private static final double PI = Math.PI;
    private static final double SQRT_2_HALF = Math.sqrt(2) / 2;

    public static int[][] toDCT(int[][] org) { // DCT coding
        int dct[][] = new int[8][8];
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                double sum = 0;
                for (int x = 0; x < 8; x++)
                    for (int y = 0; y < 8; y++)
                        sum += (org[x][y] - 128)
                                * Math.cos((2 * x + 1) * u * PI / 16)
                                * Math.cos((2 * y + 1) * v * PI / 16);
                dct[u][v] = getClosestInt(
                        sum * C(u) * C(v) / 4 / quantiFactor(u, v));
            }
        }
        return dct;
    }

    public static int[][] reDCT(int[][] dct) { // DCT decoding
        double reDct[][] = new double[8][8];
        int reOrg[][] = new int[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                reDct[i][j] = dct[i][j] * quantiFactor(i, j);
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                double sum = 0;
                for (int u = 0; u < 8; u++)
                    for (int v = 0; v < 8; v++)
                        sum += C(u) * C(v) * reDct[u][v]
                                * Math.cos((2 * x + 1) * u * PI / 16)
                                * Math.cos((2 * y + 1) * v * PI / 16);
                reOrg[x][y] = limitInByte(getClosestInt((sum / 4) + 128));
            }
        }
        return reOrg;
    }

    private static double C(int c) {
        return c > 0 ? 1.0 : SQRT_2_HALF;
    }

    private static int limitInByte(int a) {
        return a <= 255 ? (a >= 0 ? a : 0) : 255;
    }

    private static int getClosestInt(double a) {
        if (a > 0) {
            int lower = (int) a;
            return a - lower >= 0.5 ? lower + 1 : lower;
        }
        int upper = (int) a;
        return upper - a > 0.5 ? upper - 1 : upper;
    }

    private static final int KERNEL_HALF_QUANTI[] = new int[]{
        16, 11, 10, 16, 24, 40, 51, 61,
        12, 12, 14, 19, 26, 58, 60, 55,
        14, 13, 16, 24, 40, 57, 69, 56,
        14, 17, 22, 29, 51, 87, 80, 62,
        18, 22, 37, 56, 68, 109, 103, 77,
        24, 35, 55, 64, 81, 104, 113, 92,
        49, 64, 78, 87, 103, 121, 120, 101,
        72, 92, 95, 98, 112, 100, 103, 99
    };
    private static final int KERNEL_SUPERFINE[] = new int[]{
        1, 1, 1, 1, 1, 2, 3, 3,
        1, 1, 1, 1, 1, 3, 3, 3,
        1, 1, 1, 1, 2, 3, 3, 3,
        1, 1, 1, 1, 2, 4, 4, 3,
        1, 1, 3, 4, 4, 6, 6, 4,
        1, 2, 3, 3, 4, 5, 6, 5,
        2, 3, 4, 4, 5, 6, 6, 5,
        5, 5, 5, 5, 5, 5, 5, 5
    };
    private static int quantiFactor(int i, int j) {
        //return KERNEL_HALF_QUANTI[i * 8 + j];
        return KERNEL_SUPERFINE[i * 8 + j];
    }
}

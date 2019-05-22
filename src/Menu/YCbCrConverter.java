package Menu;

/**
 * YCbCrConverter - Color-space converter between RGB and YCbCr.
 */
public class YCbCrConverter {

    private static final int R_MASK = 0xFF0000;
    private static final int G_MASK = 0x00FF00;
    private static final int B_MASK = 0x0000FF;
    
    private int h_16, w_16; // image size (16-aligned)
    private int[][] Y; // luminance matrix
    private int[][] Cb, Cr; // chrominance matrices (half size of Y)
    private int[][] RGB; // RGB matrices

    /* Class constructors */
    YCbCrConverter(int[][] inputRGB) {
        setRGB(inputRGB);
    }
    YCbCrConverter(int[][] inputY, int[][] inputCb, int[][] inputCr) {
        setYCbCr(inputY, inputCb, inputCr);
    }

    /* Build color space from RGB */
    private void setRGB(int[][] inputRGB) {
        int height = inputRGB.length; // get size info.
        int width = inputRGB[0].length;
        h_16 = alignTo16(height);
        w_16 = alignTo16(width);

        RGB = new int[h_16][w_16]; // allocate matrices
        Y = new int[h_16][w_16];
        Cb = new int[h_16 / 2][w_16 / 2];
        Cr = new int[h_16 / 2][w_16 / 2];
        int[][] tempCb = new int[h_16][w_16];
        int[][] tempCr = new int[h_16][w_16];

        for (int i = 0; i < height; i++) { // doing convertion
            for (int j = 0; j < width; j++) {
                RGB[i][j] = inputRGB[i][j];
                int r = (R_MASK & inputRGB[i][j]) >> 16;
                int g = (G_MASK & inputRGB[i][j]) >> 8;
                int b = B_MASK & inputRGB[i][j];
                Y[i][j] = (int) (0.299 * r + 0.587 * g + 0.144 * b);
                tempCb[i][j] = 128 - (int) (0.1687 * r + 0.3313 * g - 0.5 * b);
                tempCr[i][j] = 128 + (int) (0.5 * r - 0.4187 * g - 0.0813 * b);
            }
        }

        for (int i = 0; i < height; i += 2) { // compress CbCr matrices
            for (int j = 0; j < width; j += 2) {
                Cb[i / 2][j / 2] = (tempCb[i][j] + tempCb[i + 1][j]
                        + tempCb[i][j + 1] + tempCb[i + 1][j + 1]) / 4;
                Cr[i / 2][j / 2] = (tempCr[i][j] + tempCr[i + 1][j]
                        + tempCr[i][j + 1] + tempCr[i + 1][j + 1]) / 4;
            }
        }
    }

    /* Build color space from YCbCr */
    private void setYCbCr(int[][] inputY, int[][] inputCb, int[][] inputCr) {
        h_16 = inputY.length; // get size info.
        w_16 = inputY[0].length;
        RGB = new int[h_16][w_16]; // allocate matrices
        Y = new int[h_16][w_16];
        Cb = new int[h_16 / 2][w_16 / 2];
        Cr = new int[h_16 / 2][w_16 / 2];

        for (int i = 0; i < h_16 / 2; i++) { // get copies of CbCr matrices
            for (int j = 0; j < w_16 / 2; j++) {
                Cb[i][j] = inputCb[i][j];
                Cr[i][j] = inputCr[i][j];
            }
        }

        for (int i = 0; i < h_16; i++) { // doing convertion
            for (int j = 0; j < w_16; j++) {
                int y = Y[i][j] = inputY[i][j]; // get a copy of Y matrix
                int cb = Cb[i / 2][j / 2];
                int cr = Cr[i / 2][j / 2];
                int r = limitInByte(y + 1.402 * (cr - 128));
                int g = limitInByte(y - 0.344 * (cb - 128) - 0.714 * (cr - 128));
                int b = limitInByte(y + 1.772 * (cb - 128));
                RGB[i][j] = (r << 16) + (g << 8) + b;
            }
        }
    }

    int getY(int y, int x) {
        return Y[y][x];
    }
    int getCb(int y, int x) {
        return Cb[y][x];
    }
    int getCr(int y, int x) {
        return Cr[y][x];
    }
    int getRGB(int y, int x) {
        return RGB[y][x];
    }

    private static int alignTo16(int size) {
        int remainder = size % 16;
        if (remainder == 0)
            return size;
        return size - remainder + 16;
    }

    private static int limitInByte(double d) {
        int a;
        if (d > 0) {
            int lower = (int) d;
            a = d - lower >= 0.5 ? lower + 1 : lower;
        } else {
            int upper = (int) d;
            a = upper - d > 0.5 ? upper - 1 : upper;
        }
        return a <= 255 ? (a >= 0 ? a : 0) : 255;
    }
}

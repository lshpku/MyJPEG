package Menu;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Wu4Image - Container of Wu4 file.
 */
public class Wu4Image {

    private static final int CHECK_CODE = 0x347557;

    private int height, width; // size of the original image
    private int[][] RGB; // image represented in RGB matrix
    private byte[] sourceFile; // image represent in compressed bytes

    /**
     * setSourceFile - Build content from Wu4 file.
     */
    public void setSourceFile(byte[] srcFile) {
        sourceFile = new byte[srcFile.length]; // get a copy of sourceFile
        System.arraycopy(srcFile, 0, sourceFile, 0, sourceFile.length);

        if (readInt(sourceFile, 0) != CHECK_CODE) // parse header
            return;
        width = readInt(sourceFile, 4);
        height = readInt(sourceFile, 8);
        int w_16 = alignTo16(width);
        int h_16 = alignTo16(height);

        int[] decArr = new int[64]; // decompressed array
        int[][] dctMat = new int[8][8]; // DCT matrix
        int[][] recMat = new int[8][8]; // original matrix
        int bitsOffset = 96; // index of sourceFile

        int[][] Y = new int[h_16][w_16]; // get Y matrix
        for (int i = 0; i < h_16; i += 8) {
            for (int j = 0; j < w_16; j += 8) {
                bitsOffset += Compressor.reRLE(sourceFile, bitsOffset, decArr);
                Compressor.toMatrix(decArr, dctMat);
                DCTConverter.reDCT(dctMat, recMat);
                for (int u = 0; u < 8; u++)
                    for (int v = 0; v < 8; v++)
                        Y[i + u][j + v] = recMat[u][v];
            }
        }

        int[][] Cb = new int[h_16 / 2][w_16 / 2]; // get Cb matrix
        for (int i = 0; i < h_16 / 2; i += 8) {
            for (int j = 0; j < w_16 / 2; j += 8) {
                bitsOffset += Compressor.reRLE(sourceFile, bitsOffset, decArr);
                Compressor.toMatrix(decArr, dctMat);
                DCTConverter.reDCT(dctMat, recMat);
                for (int u = 0; u < 8; u++)
                    for (int v = 0; v < 8; v++)
                        Cb[i + u][j + v] = recMat[u][v];
            }
        }

        int[][] Cr = new int[h_16 / 2][w_16 / 2]; // get Cr matrix
        for (int i = 0; i < h_16 / 2; i += 8) {
            for (int j = 0; j < w_16 / 2; j += 8) {
                bitsOffset += Compressor.reRLE(sourceFile, bitsOffset, decArr);
                Compressor.toMatrix(decArr, dctMat);
                DCTConverter.reDCT(dctMat, recMat);
                for (int u = 0; u < 8; u++)
                    for (int v = 0; v < 8; v++)
                        Cr[i + u][j + v] = recMat[u][v];
            }
        }

        YCbCrConverter rgb = new YCbCrConverter(Y, Cb, Cr); // get RBG matrix

        RGB = new int[height][width]; // get fined RBG matrix
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                RGB[i][j] = rgb.getRGB(i, j);
    }

    /**
     * setSourceFile - Build content from RGB matrix.
     */
    public void setRGB(int[][] inputRGB) {
        height = inputRGB.length; // get size info.
        width = inputRGB[0].length;
        int h_16 = alignTo16(height);
        int w_16 = alignTo16(width);

        RGB = new int[height][width]; // get a copy of RGB matrix
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                RGB[i][j] = inputRGB[i][j];

        YCbCrConverter ycc = new YCbCrConverter(RGB); // get YCbCr matrices

        int[][] orgMat = new int[8][8]; // original matrix
        int[][] dctMat = new int[8][8]; // DCT matrix
        int[] uncArr = new int[64]; // uncompressed array
        byte[] bitsArray = new byte[h_16 * w_16 * 6]; // temporary storage
        int bitsOffset = 96; // index of bitsArray;

        writeInt(bitsArray, 0, CHECK_CODE); // store header
        writeInt(bitsArray, 4, width);
        writeInt(bitsArray, 8, height);

        for (int i = 0; i < h_16; i += 8) { // store Y matrix
            for (int j = 0; j < w_16; j += 8) {
                for (int u = 0; u < 8; u++)
                    for (int v = 0; v < 8; v++)
                        orgMat[u][v] = ycc.getY(i + u, j + v);
                DCTConverter.toDCT(orgMat, dctMat);
                Compressor.toArray(dctMat, uncArr);
                bitsOffset += Compressor.toRLE(uncArr, bitsArray, bitsOffset);
            }
        }

        for (int i = 0; i < h_16 / 2; i += 8) { // store Cb matrix
            for (int j = 0; j < w_16 / 2; j += 8) {
                for (int u = 0; u < 8; u++)
                    for (int v = 0; v < 8; v++)
                        orgMat[u][v] = ycc.getCb(i + u, j + v);
                DCTConverter.toDCT(orgMat, dctMat);
                Compressor.toArray(dctMat, uncArr);
                bitsOffset += Compressor.toRLE(uncArr, bitsArray, bitsOffset);
            }
        }

        for (int i = 0; i < h_16 / 2; i += 8) { // store Cr matrix
            for (int j = 0; j < w_16 / 2; j += 8) {
                for (int u = 0; u < 8; u++)
                    for (int v = 0; v < 8; v++)
                        orgMat[u][v] = ycc.getCr(i + u, j + v);
                DCTConverter.toDCT(orgMat, dctMat);
                Compressor.toArray(dctMat, uncArr);
                bitsOffset += Compressor.toRLE(uncArr, bitsArray, bitsOffset);
            }
        }

        int srcLength = (bitsOffset + 7) / 8; // get fined sourceFile
        sourceFile = new byte[srcLength];
        for (int i = 0; i < srcLength; i++)
            sourceFile[i] = bitsArray[i];
    }

    public byte[] getSourceFile() {
        return sourceFile;
    }

    public int[][] getRGB() {
        return RGB;
    }

    private static int alignTo16(int size) {
        int remainder = size % 16;
        if (remainder == 0)
            return size;
        return size - remainder + 16;
    }

    private static int readInt(byte b[], int offset) {
        return (b[offset] & 0xFF) + ((b[offset + 1] << 8) & 0xFF00)
                + ((b[offset + 2] << 16) & 0xFF0000) + (b[offset + 3] << 24);
    }
    private static void writeInt(byte b[], int offset, int value) {
        b[offset] = (byte) (value & 0xFF);
        b[offset + 1] = (byte) ((value >> 8) & 0xFF);
        b[offset + 2] = (byte) ((value >> 16) & 0xFF);
        b[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }
}

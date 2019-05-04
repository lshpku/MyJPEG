/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Menu;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 *
 * @author Cocity
 */
public class DCTTest extends JFrame {

    final double PI = 3.14159265;
    final double MS = 0.70710678;
    BufferedImage img = null, im2 = null;

    public static void main(String[] args) {
        DCTTest d = new DCTTest();
        d.parseImage();
        d.display();
    }

    public void parseImage() {
        try {
            img = ImageIO.read(new File("src\\Image\\test.bmp"));
        } catch (IOException ex) {
        }
        im2 = new BufferedImage(img.getWidth(), img.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        int org[][] = new int[8][8];
        for (int x = 0; x < img.getWidth(); x += 8) {
            for (int y = 0; y < img.getHeight(); y += 8) {
                for (int i = 0; i < 8; i++)
                    for (int j = 0; j < 8; j++) {
                        int rgb = img.getRGB(x + i, y + j);
                        org[i][j] = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF)
                                + (rgb & 0xFF)) / 3;
                        img.setRGB(x + i, y + j, org[i][j] * 0x10101);
                    }
                int dct[][] = DCTConverter.toDCT(org);
                int rec[][] = DCTConverter.reDCT(dct);
                for (int i = 0; i < 8; i++)
                    for (int j = 0; j < 8; j++)
                        im2.setRGB(x + i, y + j, rec[i][j] * 0x10101);
            }
        }

        display();
    }

    public void display() {
        setLayout(null);
        setBounds(300, 100, 1600, 960);
        getContentPane().setBackground(Color.white);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        repaint();
    }

    public void paint(Graphics g) {
        int zoom = 3;
        g.drawImage(img, 32, 64,
                img.getWidth() * zoom, img.getHeight() * zoom, this);
        g.drawImage(im2, 40 + img.getWidth() * zoom, 64,
                img.getWidth() * zoom, img.getHeight() * zoom, this);
    }
}

class DCTConverter {

    final static double PI = 3.14159265;
    final static double MS = 0.70710678;

    public static int[][] toDCT(int[][] org) {
        int dct[][] = new int[8][8];
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                double sum = 0;
                for (int x = 0; x < 8; x++)
                    for (int y = 0; y < 8; y++)
                        sum += (org[x][y] - 128) * cos((2 * x + 1) * u * PI / 16)
                                * cos((2 * y + 1) * v * PI / 16);
                dct[u][v] = (int) (sum * C(u) * C(v) / 4);
            }
        }
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                dct[i][j] /= kernel(i, j);
        return dct;
    }

    public static int[][] reDCT(int[][] dct) {
        int rec[][] = new int[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                dct[i][j] *= kernel(i, j);
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                double sum = 0;
                for (int u = 0; u < 8; u++)
                    for (int v = 0; v < 8; v++)
                        sum += C(u) * C(v) * dct[u][v]
                                * cos((2 * x + 1) * u * PI / 16)
                                * cos((2 * y + 1) * v * PI / 16);
                rec[x][y] = (int) (sum / 4) + 128;
                if (rec[x][y] > 255)
                    rec[x][y] = 255;
                if (rec[x][y] < 0)
                    rec[x][y] = 0;
            }
        }
        return rec;
    }

    public static double cos(double a) {
        return Math.cos(a);
    }

    public static double C(int c) {
        return c > 0 ? 1.0 : 0.70710678;
    }

    public static int kernel(int i, int j) {
        int ker[] = new int[]{
            1, 1, 2, 2, 16, 32, 64, 64,
            1, 1, 2, 2, 16, 32, 64, 64,
            2, 2, 2, 2, 16, 32, 64, 64,
            2, 2, 2, 2, 16, 32, 64, 64,
            16, 16, 16, 16, 16, 32, 64, 64,
            32, 32, 32, 32, 32, 32, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64
        };
        //int r = (int) Math.sqrt(i * i + j * j);
        return ker[i * 8 + j];
        //return 1 << (r / 3);
    }
}

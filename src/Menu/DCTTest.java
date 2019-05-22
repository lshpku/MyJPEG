package Menu;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class DCTTest extends JFrame {

    BufferedImage im1 = null, im2 = null;

    public static void main(String[] args) {
        DCTTest d = new DCTTest();
        try {
            d.im1 = ImageIO.read(new File("src\\Image\\lena.bmp"));
        } catch (IOException ex) {
        }

        int[][] rgb = new int[d.im1.getHeight()][d.im1.getWidth()];
        for (int i = 0; i < rgb.length; i++)
            for (int j = 0; j < rgb[i].length; j++)
                rgb[i][j] = d.im1.getRGB(j, i);

        Wu4Image w1 = new Wu4Image();
        w1.setRGB(rgb);
        byte[] b1 = w1.getSourceFile();
        System.out.println(b1.length / 1024);
        Wu4Image w2 = new Wu4Image();
        w2.setSourceFile(b1);
        rgb = w2.getRGB();

        d.im2 = new BufferedImage(d.im1.getWidth(), d.im1.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < rgb.length; i++)
            for (int j = 0; j < rgb[i].length; j++)
                d.im2.setRGB(j, i, rgb[i][j]);

        d.display();
    }

    public void display() {
        setLayout(null);
        setBounds(300, 100, 1920, 1080);
        getContentPane().setBackground(Color.white);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        repaint();
    }

    public void paint(Graphics g) {
        int zoom = 2;
        g.drawImage(im1, 32, 64,
                im1.getWidth() * zoom, im1.getHeight() * zoom, this);
        g.drawImage(im2, 40 + im2.getWidth() * zoom, 64,
                im2.getWidth() * zoom, im2.getHeight() * zoom, this);
    }
}

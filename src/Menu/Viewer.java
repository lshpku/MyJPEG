package Menu;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;

/**
 * Viewer - Viewer and converter for Wu4 file.
 */
public class Viewer extends JFrame {

    private JButton btnOpen, btnSaveBmp, btnSaveWu4; // 文件操作按钮
    private FileDialog openDia, saveDia; // 文件操作对话框
    private File file; // 当前打开文件
    private BufferedImage image;
    private final int buttonX = 24, buttonY = 24;

    Viewer() {
        super("Wu4 Viewer");
        init();
    }

    public static void main(String[] args) {
        new Viewer();
    }

    /* 图形用户界面组件初始化 */
    public void init() {

        /* 设置窗体布局 */
        setLayout(null);
        setBounds(300, 100, 960, 640);
        getContentPane().setBackground(Color.white);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        /* 添加文件操作对话框 */
        openDia = new FileDialog(this, "打开", FileDialog.LOAD);
        saveDia = new FileDialog(this, "保存", FileDialog.SAVE);

        /* 添加文件操作按钮 */
        btnOpen = new JButton(new ImageIcon("src\\Image\\OpenFile.png"));
        btnOpen.setBounds(buttonX, buttonY, 272, 48);
        btnOpen.addActionListener(new FileOpener());
        add(btnOpen);
        btnSaveBmp = new JButton(new ImageIcon("src\\Image\\SaveFileBmp.png"));
        btnSaveBmp.setBounds(buttonX, buttonY + 64, 128, 48);
        btnSaveBmp.addActionListener(new FileSaver());
        add(btnSaveBmp);
        btnSaveWu4 = new JButton(new ImageIcon("src\\Image\\SaveFileWu4.png"));
        btnSaveWu4.setBounds(buttonX + 144, buttonY + 64, 128, 48);
        btnSaveWu4.addActionListener(new FileSaver());
        add(btnSaveWu4);

        setVisible(true);// 设置窗体可见
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (image == null)
            return;

        /* 绘制图片缩略图 */
        int width = image.getWidth(), height = image.getHeight();
        float zoom = Math.min((float) width / 400, (float) height / 300);
        width = (int) (width / zoom);
        height = (int) (height / zoom);
        g.drawImage(image, 630 - width / 2, 350 - height / 2,
                width, height, this);

        /* 输出图片信息 */
        g.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        g.drawString("width: " + width, buttonX + 32, buttonY + 400);
        g.drawString("height: " + height, buttonX + 32, buttonY + 432);
    }

    private class FileOpener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            openDia.setVisible(true);//显示打开文件对话框

            String dirpath = openDia.getDirectory(); //获取打开文件路径
            String fileName = openDia.getFile(); //获取打开文件名称
            if (dirpath == null || fileName == null) // 若获取失败则返回
                return;

            file = new File(dirpath, fileName); // 打开文件

            try { // 尝试读文件
                if (fileName.endsWith(".wu4"))
                    image = null;
                else
                    image = ImageIO.read(file);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            repaint();
        }
    }

    private class FileSaver implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (file == null) {
                saveDia.setVisible(true); //显示保存文件对话框
                String dirpath = saveDia.getDirectory(); //获取保存文件路径
                String fileName = saveDia.getFile(); //获取保存文件名称
                if (dirpath == null || fileName == null) // 若获取失败则返回
                    return;
                else
                    file = new File(dirpath, fileName); // 新建一个文件
            }
            try { // 尝试写入文件
                BufferedWriter bufw = new BufferedWriter(new FileWriter(file));
                String text = "I love wu4";
                bufw.write(text); //将获取文本内容写入到字符输出流
                bufw.close(); //关闭文件
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}

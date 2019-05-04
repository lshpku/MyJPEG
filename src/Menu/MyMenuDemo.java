package Menu;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MyMenuDemo {

    private Frame f;// 定义窗体
    private MenuBar bar;// 定义菜单栏
    private TextArea ta;
    private Menu fileMenu;// 定义"文件"和"子菜单"菜单
    private MenuItem openItem, saveItem, closeItem;// 定义条目“退出”和“子条目”菜单项

    private FileDialog openDia, saveDia;// 定义“打开、保存”对话框
    private File file;//定义文件

    MyMenuDemo() {
        init();
    }

    /* 图形用户界面组件初始化 */
    public void init() {
        f = new Frame("my window");// 创建窗体对象
        f.setBounds(300, 100, 650, 600);// 设置窗体位置和大小

        bar = new MenuBar();// 创建菜单栏
        ta = new TextArea();// 创建文本域

        fileMenu = new Menu("文件");// 创建“文件”菜单

        openItem = new MenuItem("打开");// 创建“打开"菜单项
        saveItem = new MenuItem("保存");// 创建“保存"菜单项
        closeItem = new MenuItem("退出");// 创建“退出"菜单项

        fileMenu.add(openItem);// 将“打开”菜单项添加到“文件”菜单上
        fileMenu.add(saveItem);// 将“保存”菜单项添加到“文件”菜单上
        fileMenu.add(closeItem);// 将“退出”菜单项添加到“文件”菜单上

        bar.add(fileMenu);// 将文件添加到菜单栏上

        f.setMenuBar(bar);// 将此窗体的菜单栏设置为指定的菜单栏。

        openDia = new FileDialog(f, "打开", FileDialog.LOAD);
        saveDia = new FileDialog(f, "保存", FileDialog.SAVE);

        f.add(ta);// 将文本域添加到窗体内
        myEvent();// 加载事件处理

        f.setVisible(true);// 设置窗体可见

    }

    private void myEvent() {

        // 打开菜单项监听
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                openDia.setVisible(true);//显示打开文件对话框

                String dirpath = openDia.getDirectory();//获取打开文件路径并保存到字符串中。
                String fileName = openDia.getFile();//获取打开文件名称并保存到字符串中

                if (dirpath == null || fileName == null)//判断路径和文件是否为空
                    return;
                else
                    ta.setText(null);//文件不为空，清空原来文件内容。
                file = new File(dirpath, fileName);//创建新的路径和名称

                try {
                    BufferedReader bufr = new BufferedReader(new FileReader(file));//尝试从文件中读东西
                    String line = null;//变量字符串初始化为空
                    while ((line = bufr.readLine()) != null) {
                        ta.append(line + "\r\n");//显示每一行内容
                    }
                    bufr.close();//关闭文件
                } catch (FileNotFoundException e1) {
                    // 抛出文件路径找不到异常
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // 抛出IO异常
                    e1.printStackTrace();
                }

            }

        });

        // 保存菜单项监听
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (file == null) {
                    saveDia.setVisible(true);//显示保存文件对话框
                    String dirpath = saveDia.getDirectory();//获取保存文件路径并保存到字符串中。
                    String fileName = saveDia.getFile();////获取打保存文件名称并保存到字符串中

                    if (dirpath == null || fileName == null)//判断路径和文件是否为空
                        return;//空操作
                    else
                        file = new File(dirpath, fileName);//文件不为空，新建一个路径和名称
                }
                try {
                    BufferedWriter bufw = new BufferedWriter(new FileWriter(file));

                    String text = ta.getText();//获取文本内容
                    bufw.write(text);//将获取文本内容写入到字符输出流

                    bufw.close();//关闭文件
                } catch (IOException e1) {
                    //抛出IO异常
                    e1.printStackTrace();
                }

            }

        });

        // 退出菜单项监听
        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }

        });

        // 窗体关闭监听
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);

            }

        });
    }

    public static void main(String[] args) {
        new MyMenuDemo();
    }
}

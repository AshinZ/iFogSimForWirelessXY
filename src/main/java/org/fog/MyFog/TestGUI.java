package org.fog.MyFog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;


/**
 * Created by Z_HAO on 2020/2/23
 */
public class TestGUI {
    //定义一些变量
    private int x = 1000;
    private int y = 800;
    private int fontSize = 20;
    private Color bgColor = new Color(255,255,255);
    private String[] fontsName = {"宋体"};

    public static void main(String []args) throws Exception {
        TestGUI gui = new TestGUI();
        BufferedImage bufferedImage = gui.getImage();
        File file = new File("C:\\Users\\Z_HAO\\Desktop\\Test.png");
        ImageIO.write(bufferedImage , "png" , file);
    }

    //获取随机的字体
    private String getFont(){
        return fontsName[0];
    }

    //获取颜色
    private Color getColor(){
        return new Color(0 , 0 , 0);
    }

    //获取一张验证码图片
    public BufferedImage getImage(){
        return addCharAndLine();
    }

    //设置缓冲区
    private BufferedImage getBufferedImage(){
        BufferedImage bi = new BufferedImage(x,y,BufferedImage.TYPE_INT_RGB);
        Graphics2D pen = (Graphics2D)bi.getGraphics();
        pen.setColor(this.bgColor);
        pen.fillRect(0 , 0 , x , y);
        return bi;
    }

    //给缓冲区添加字符串,添加干扰线
    private BufferedImage addCharAndLine(){
        BufferedImage bi = getBufferedImage();
        Graphics2D pen = (Graphics2D)bi.getGraphics();
        String font = getFont();
        pen.setColor(getColor());
        pen.setFont(new Font(font,Font.PLAIN,fontSize));
        pen.drawLine(0 , y - 1 , x , y - 1);
        pen.drawLine(0 , y , 0 , 0);
        for(int i=0;i<10000;i++){//这里加了四个字符
            MyRandom random = new MyRandom(0.01);
            pen.drawString("·" , (float)random.getDoubleRandom() , (float)(random.getRandom() * 800));
        }
		return bi;
    }
}

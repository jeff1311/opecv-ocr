package com.ljf.opencvocr;

import com.alibaba.fastjson.JSONObject;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 图片工具类
 * @author ljf
 * @since 2019-03-14
 */
public class OCRUtil {

    public static JSONObject findContours(Mat srcDilate,Mat src){
        String resultStr = "";
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(srcDilate, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<MatOfPoint> sortMat = sortMat(contours);
        //清空文件夹
//        Util.clearFiles(Constants.disk + "/ocr/test/binary");

        Mat img = new Mat(src.rows(), src.cols(), CvType.CV_8UC1,new Scalar(255, 255, 255));
//        Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/" + new Date().getTime() + ".jpg"),img);

        for(int i = 0;i < sortMat.size();i ++){
            Rect rect = Imgproc.boundingRect(sortMat.get(i));

            //画出矩形
//            int x = rect.x;
//            int y = rect.y;
//            int w = x + rect.width;
//            int h = y + rect.height;
//            Point point1 = new Point(x, y);
//            Point point2 = new Point(w, h);
//            Scalar scalar = new Scalar(255, 0, 255);
//            Imgproc.rectangle(src,point1,point2,scalar);

            if(rect.area() > 8000){
                Mat r = new Mat(src, rect);

                //灰度化
                Imgproc.cvtColor(r,r,Imgproc.COLOR_BGR2GRAY);
                //二值化（自适应）
                int blockSize = 41;
                int constValue = 30;
                Imgproc.adaptiveThreshold(r, r, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, constValue);
                //去除面积小于20像素的区域
                clean(r,20);

                Mat roi = new Mat(img, rect);
                for(int x = 0;x < roi.rows();x ++){
                    for(int y = 0;y < roi.cols();y ++){
                        double[] data = r.get(x,y);
                        roi.put(x, y, data);
                    }
                }

            }
        }

        Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/binary/" + new Date().getTime() + ".jpg"),img);

        resultStr = resultStr + ocr2(img);

        System.out.println(resultStr);

        return filterOcrInfo(resultStr);
    }

    public static JSONObject filterOcrInfo(String ocrInfo){
        ocrInfo = filter(ocrInfo);
        System.out.println(ocrInfo);

        JSONObject result = new JSONObject();

        String[] array = ocrInfo.split("\n");

        for(int i = 0;i < array.length;i ++){
            String text = array[i];
            if(i == 0){
                String name = text;
                if(name.contains("名")){
                    int index = name.indexOf("名");
                    name = name.substring(index + 1);
                }else{
                    name = name.replace("\n","");
                }
                name = name.replace("-","");
                result.put("name",nameFilter(filter(name)));
            }
            if(i == 1){
                String nation = text;
                nation = nation.replace(" ","");
                nation = nation.replace("汊","汉");
                nation = nation.replace("况","汉");
                for(String n : Constants.NATIONS){
                    if(nation.contains(n)){
                        result.put("nation",n);
                        break;
                    }
                }
            }
            if(i == 3){
                String address = text;
                address = address.replace(" ","");
                int aIndex = address.indexOf("址");
                if(aIndex != -1){
                    address = address.substring(aIndex + 1);
                }
                result.put("address",filter(address));
            }

            if(i > 4){
                String idCode = idCodeFilter(text);
                if(!"".equals(idCode)){
                    result.put("gender",parseGender(idCode));
                    String year = idCode.substring(6,10);
                    String month = idCode.substring(10,12);
                    String day = idCode.substring(12,14);
                    result.put("year",year);
                    int m = Integer.parseInt(month.substring(0,1));
                    int d = Integer.parseInt(day.substring(0,1));
                    result.put("month",m == 0 ? month.toCharArray()[1] : month);
                    result.put("day",d == 0 ? day.toCharArray()[1] : day);
                    result.put("idCode",idCode.replace("x","X"));
                }
            }

        }

        return result;
    }

    //取出身份证号
    public static String idCodeFilter(String text){
        String temp = text;
        temp = temp.replace(" ", "").
                replace("o", "0").
                replace("O", "0").
                replace("l", "1").
                replace("]", "1").
                replace("】", "1").
                replace("?", "7").
                replace("了", "7").
                replace("B", "8").
                replace("《","");
        String code = "";
        char[] textArray = temp.toCharArray();
        for(char c : textArray){
            //是否为数字
            boolean isDigit = Character.isDigit(c);
            //后面是否为连续
            if((isDigit || String.valueOf(c).toLowerCase().equals("x")) && code.length() < 18){
                code += c;
                if(code.length() == 18){
                    break;
                }
            }else{
                code = "";
            }
        }
        return code;
    }

    //根据身份证号获取性别
    public static String parseGender(String idCode){
        String gender = "";
        String s = idCode.substring(16,17);
        int i = Integer.parseInt(s);
        if(i % 2 == 0){
            gender = "女";
        }else{
            gender = "男";
        }
        return gender;
    }

    //过滤特殊字符
    public static String filter(String text){
        String s = " 《》『』（）()|[]】\"〕′_＿ˇ`~!@#$%^&*+={}':;＇,.<>＜＞\\＼/?～！＃￥％…＆＊＋｛｝‘；：”“’。，、？";
        char[] sArray = s.toCharArray();
        for(char c : sArray){
            text = text.replace(String.valueOf(c),"");
        }
        return text;
    }

    //名字只能是中文，过滤英文字母和数字
    public static String nameFilter(String name){
        char[] chars = name.toCharArray();
        for(char c : chars){
            if ((c > 'A' && c < 'Z') || (c > 'a' && c < 'z') || Character.isDigit(c)) {
                name = name.replace(String.valueOf(c),"");
            }
        }
        name = name.replace("-","");
        return name.trim();
    }

    /**
     * 排序
     * @param contours
     * @return
     */
    public static List<MatOfPoint> sortMat(ArrayList<MatOfPoint> contours){
        for(int a = 0;a < contours.size() - 1;a ++){
            for(int b = 0;b < contours.size() - a - 1;b ++){
                Rect rect1 = Imgproc.boundingRect(contours.get(b + 1));
                Rect rect2 = Imgproc.boundingRect(contours.get(b));
                if(sort(rect1,rect2)){
                    MatOfPoint temp = contours.get(b);
                    contours.set(b, contours.get(b + 1));
                    contours.set(b + 1, temp);
                }
            }
        }
        return contours;
    }

    /**
     * 排序规则
     * @param a
     * @param b
     * @return
     */
    public static boolean sort(Rect a,Rect b){
        if(a.y < b.y){
            return true;
        }else if(a.y == b.y){
            return (a.x < b.x);
        }else{
            return false;
        }
    }


    public static int isBlack(int colorInt, int whiteThreshold) {
        final Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= whiteThreshold) {
            return 1;
        }
        return 0;
    }

    /**
     * Mat转换成BufferedImage
     *
     * @param matrix
     *            要转换的Mat
     * @param fileExtension
     *            格式为 ".jpg", ".png", etc
     * @return
     */
    public static BufferedImage Mat2BufImg (Mat matrix, String fileExtension) {
        // convert the matrix into a matrix of bytes appropriate for
        // this file extension
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExtension, matrix, mob);
        // convert the "matrix of bytes" into a byte array
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }

    /**
     * BufferedImage转换成Mat
     *
     * @param original
     *            要转换的BufferedImage
     * @param imgType
     *            bufferedImage的类型 如 BufferedImage.TYPE_3BYTE_BGR
     * @param matType
     *            转换成mat的type 如 CvType.CV_8UC3
     */
    public static Mat BufImg2Mat (BufferedImage original, int imgType, int matType) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() != imgType) {

            // Create a buffered image
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), imgType);

            // Draw the image onto the new buffer
            Graphics2D g = image.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(original, 0, 0, null);
            } finally {
                g.dispose();
            }
        }
        DataBufferByte dbi =(DataBufferByte)original.getRaster().getDataBuffer();
        byte[] pixels = dbi.getData();
        Mat mat = Mat.eye(original.getHeight(), original.getWidth(), matType);
        mat.put(0, 0, pixels);
        return mat;
    }

    public static String ocr(Mat src){
        ITesseract instance = new Tesseract();
        //设置训练库的位置
        String classPath = Util.getClassPath();
        String dataPath = classPath + "tessdata";
        instance.setDatapath(dataPath);
        instance.setLanguage("chi_sim");//chi_sim eng
        String result = null;
        try {
            //灰度化
            Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);
            //二值化（自适应）
            int blockSize = 41;
            int constValue = 30;
            Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, constValue);
            //过滤杂纹
//            Imgproc.medianBlur(src, src,3);

            //去除面积小于20像素的区域
            clean(src,20);

            Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/binary/" + new Date().getTime() + ".jpg"),src);
            BufferedImage binary = Mat2BufImg(src, ".jpg");

            result =  instance.doOCR(binary);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }

    public static String ocr2(Mat src){
//    	File file = new File(path);
        ITesseract instance = new Tesseract();
        //设置训练库的位置
        String classPath = Util.getClassPath();
        String dataPath = classPath + "tessdata";
        instance.setDatapath(dataPath);
        instance.setLanguage("chi_sim");//chi_sim eng
        String result = null;
        BufferedImage binary = Mat2BufImg(src, ".jpg");
        try {
            result =  instance.doOCR(binary);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }


    /**
     * inputStream转换为字节数组
     * @param input
     * @return
     */
    public static byte[] toByteArray(InputStream input) {
        if (input == null) {
            return null;
        }
        ByteArrayOutputStream output = null;
        byte[] result = null;
        try {
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 100];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            result = output.toByteArray();
            if (output != null) {
                output.close();
            }
        } catch (Exception e) {}
        return result;
    }

    /**
     * 字节数组转换为BufferedImage
     * @param imagedata
     * @return
     */
    public static BufferedImage toBufferedImage(byte[] imagedata) {
        Image image = Toolkit.getDefaultToolkit().createImage(imagedata);
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        image = new ImageIcon(image).getImage();
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
        }
        if (bimage == null) {
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        Graphics g = bimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }

    public static void clean(Mat src,int size){
        //轮廓检测
        Mat temp = src.clone();
        Imgproc.Canny(temp, temp, 20, 60);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(temp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //去除面积小于N像素的区域
        for(int j = 0;j < contours.size();j ++){
            Rect br = Imgproc.boundingRect(contours.get(j));
            if(br.area() <= size){
                Mat r = new Mat(src, br);
                for(int x = 0;x < r.rows();x ++){
                    for(int y = 0;y < r.cols();y ++){
                        double[] data = {255};
                        r.put(x, y, data);
                    }
                }
            }
        }
    }

    public static void window(String title,Mat mat){
        new ShowImage(title,mat);
    }

}

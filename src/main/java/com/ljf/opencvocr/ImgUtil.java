package com.ljf.opencvocr;

import com.alibaba.fastjson.JSONObject;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 图片工具栏
 * @author ljf
 * @since 2019-03-14
 */
public class ImgUtil {

    private static Logger logger = LoggerFactory.getLogger("ImgUtil");
    
//    static{
//		//载入本地库
//    	String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
//        System.load(opencvLib);
//	}

	public static void autoCrop(String imgPath){
		
		//原图
		Mat src = Imgcodecs.imread(imgPath);
		//原图（灰）
		Mat srcGray = src.clone();
		Imgproc.cvtColor(srcGray, srcGray, Imgproc.COLOR_BGR2GRAY);
        //高斯滤波，降噪
        Imgproc.GaussianBlur(srcGray, srcGray, new Size(3, 3), 0);
        //轮廓检测
        Imgproc.Canny(srcGray, srcGray, 100, 100);
        //轮廓提取
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //找出最大轮廓
        double maxArea = 0;
        //最大轮廓索引
        int maxIndex = 0;
        for(int i = 0;i < contours.size();i ++){
        	Rect br = Imgproc.boundingRect(contours.get(i));
        	if(maxArea < br.area()){
        		maxArea = br.area();
        		maxIndex = i;
        	}
        }
        //最大轮廓
        Rect maxContour = Imgproc.boundingRect(contours.get(maxIndex));
        Mat srcImg = new Mat(src, maxContour);
        Mat tmpImg = new Mat();
        srcImg.copyTo(tmpImg);
        Imgcodecs.imwrite("E:/ocr/test/" + new Date().getTime() + ".jpg", tmpImg);
        Imgcodecs.imwrite("E:/ocr/test/" + new Date().getTime() + ".jpg", srcGray);
	}
	
	/**
	 * 灰化
	 * @param srcPath
	 * @return	
	 */
	public static Mat gray(String srcPath){
		Mat srcGray = Imgcodecs.imread(srcPath, Imgcodecs.IMREAD_GRAYSCALE);
		Mat dst = new Mat();
		Imgproc.cvtColor(srcGray, dst, Imgproc.THRESH_OTSU);//使用OTSU界定输入图像
//		Imgcodecs.imwrite(dstPath, dst);
		return dst;
	}
	
	/**
	 * 二值化
	 * @param src
	 * @param gray
	 * @return
	 */
	public static Mat binarize(Mat src,Mat gray){
		Mat m = new Mat();
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
		Mat dst = new Mat();
		Imgproc.threshold(gray, dst, 100, 255, 4);//去水印
		Imgproc.threshold(dst, m, 0, 255, 0);//二值化
		Imgproc.GaussianBlur(m, m, new Size(3, 3), 0);
//		Imgcodecs.imwrite("H:/opencv/test-binary.jpg", m);
		return m;
	}
	
	/**
	 * 膨胀
	 * @param src
	 * @return
	 */
	public static Mat dilate(Mat src,int threshold){
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size(3,3));     //使用3*3交叉内核
		Mat dilated = new Mat();
		Imgproc.dilate(src, dilated, kernel, new Point(-1, -1), threshold);   //以这个内核为中心膨胀8倍
		//具体的内核大小和膨胀倍数根据实际情况而定，只要确保所有字符都粘在一起即可
//		Imgcodecs.imwrite("H:/opencv/test-dilate.jpg", dilated);
		return dilated;
	}
	
	public static JSONObject findContours(Mat srcDilate,Mat src){
	    JSONObject result = new JSONObject();
	    String resultStr = "";
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(srcDilate, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		List<MatOfPoint> sortMat = sortMat(contours);
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

            Mat srcImg = new Mat(src, rect);
            Mat tmpImg = new Mat();
            srcImg.copyTo(tmpImg);
            String storagePath = "E:/ocr/test/block/" + i + ".jpg";
            Imgcodecs.imwrite(storagePath, tmpImg);
            System.out.println(rect.area());
            if(rect.area() > 10000){
                resultStr = resultStr + ocr(storagePath) + "<br>";
//                resultStr = resultStr.replace("\n","<br>");
            }
        }

        System.out.println(resultStr);

        String storagePath = "E:/ocr/test/src.jpg";
        Imgcodecs.imwrite(storagePath, src);

        //筛选有用信息
        String[] resultArray = resultStr.split("<br>");

        for(int i = 0;i < resultArray.length;i ++){
            String text = resultArray[i];
            if(i == 0){
                String name = text;
                if(name.contains("名")){
                    int index = name.indexOf("名");
                    name = name.substring(index + 1);
                    int nIndex = name.indexOf("\n");
                    if(nIndex != -1){
                        name = name.substring(0,nIndex);
                    }
                }else{
                    if(name.split("\n").length > 2){
                        int nIndex = name.indexOf("\n");
                        if(nIndex != -1){
                            name = name.substring(0,nIndex);
                        }
                    }else{
                        name = name.replace("\n","");
                    }
                }
                result.put("name",nameFilter(filter(name)));
            }
            if(text.contains("族")){
                String nationality = text;
                int index = nationality.indexOf("族");
                nationality = nationality.substring(index + 1);
                int nIndex = nationality.indexOf("\n");
                if(nIndex != -1){
                    nationality = nationality.substring(0,nIndex);
                }
                nationality = nationality.replace("汊","汉");
                nationality = nationality.replace("灰","汉");
                nationality = nationality.replace("况","汉");
                result.put("nationality",filter(nationality));
            }
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
                result.put("idCode",idCode);
            }
            if((text.contains("省") || text.contains("市") ||
                    text.contains("区") || text.contains("县") ||
                    text.contains("乡") || text.contains("镇")) && text.length() > 12){
                String address = text;
                address = address.replace(" ","");
                int aIndex = 0;
                if(address.indexOf("住址") == -1){
                    aIndex = address.indexOf("址");
                }else{
                    aIndex = address.indexOf("住址");
                }
                if(aIndex != -1){
                    address = address.substring(aIndex + 1);
                }
                String[] addressArray = address.split("\n");
                address = address.replace("\n","");
                if(addressArray[0].contains("住") || addressArray[0].contains("址")){
                    addressArray[0] = addressArray[0].replace("住","");
                    addressArray[0] = addressArray[0].replace("址","");
                }
                if(addressArray.length >= 3){
                    address = addressArray[0] + addressArray[1] + addressArray[2];
                }else{
                    address = addressArray[0] + addressArray[1];
                }
                result.put("address",filter(address));
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
                replace("B", "8");
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
        String s = " （）()|]】\"〕′_＿ˇ`~!@#$%^&*+={}':;＇,.<>＜＞\\＼/?～！＃￥％…＆＊＋｛｝‘；：”“’。，、？";
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
        return name;
    }

	/**
	 * 排序
	 * @param contours
	 * @return
	 */
	public static List<MatOfPoint> sortMat(ArrayList<MatOfPoint> contours){
		for(int a = 0;a < contours.size();a ++){
			Rect rect1 = Imgproc.boundingRect(contours.get(a));
			for(int b = 0;b < contours.size();b ++){
				Rect rect2 = Imgproc.boundingRect(contours.get(b));
				if(sort(rect1,rect2)){
					MatOfPoint temp = contours.get(a);
					contours.set(a, contours.get(b));
					contours.set(b, temp);
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
	
	/**
	 * 二值化（动态）
	 * @param src
	 * @param dst
	 */
	public static Mat binarization(String src, Mat dst) {
		Mat img = Imgcodecs.imread(src);
		Imgproc.cvtColor(img, dst, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 10);
//        Imgcodecs.imwrite("H:/opencv/test-binary.jpg", dst);
        return dst;
    }
	
	public static boolean isBlack(int colorInt)  
    {  
        Color color = new Color(colorInt);  
        if (color.getRed() + color.getGreen() + color.getBlue() <= 300)  
        {  
            return true;  
        }  
        return false;  
    }  

    public static boolean isWhite(int colorInt)  
    {  
        Color color = new Color(colorInt);  
        if (color.getRed() + color.getGreen() + color.getBlue() > 300)  
        {  
            return true;  
        }  
        return false;  
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
    
    /**
     * 图片压缩
     * @param oldFile
     * @param newFile
     * @param width
     * @param height
     * @return
     */
    public static int zipImgAuto(InputStream oldFile, File newFile, int width, int height) {
        try {

            Image srcFile = ImageIO.read(oldFile);
            int w = srcFile.getWidth(null);
            int h = srcFile.getHeight(null);
            double ratio;
            if(width > 0){
                ratio = width / (double) w;
                height = (int) (h * ratio);
            }else{
                if(height > 0){
                    ratio = height / (double) h;
                    width = (int) (w * ratio);
                }
            }

            String srcImgPath = newFile.getAbsoluteFile().toString();

            String suffix = srcImgPath.substring(srcImgPath.lastIndexOf(".") + 1,srcImgPath.length());

            BufferedImage buffImg = null;
            if(suffix.equals("png")){
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }else{
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D graphics = buffImg.createGraphics();
            graphics.setBackground(new Color(255,255,255));
            graphics.setColor(new Color(255,255,255));
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(srcFile.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

            ImageIO.write(buffImg, suffix, new File(srcImgPath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return height;
    }

    /**
     * 图片裁剪
     * @param filePath
     * @param x
     * @param y
     * @param w
     * @param h
     * @param binaryFlag
     * @return
     */
    public static BufferedImage cropImage(String filePath, int x, int y, int w, int h,boolean binaryFlag){
        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("jpg");
        ImageReader imgReader = it.next();
        imgReader.setInput(iis);
        ImageReadParam par = imgReader.getDefaultReadParam();
        par.setSourceRegion(new Rectangle(x, y, w, h));
        BufferedImage bi = null;
        try {
            bi = imgReader.read(0, par);
            //是否二值化
            if(binaryFlag){
                bi = binary(bi,bi);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi;
    }

    /**
     * 二值化
     * @param src
     * @param dest
     * @return
     */
    public static BufferedImage binary(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();

        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];

        getRGB(src, 0, 0, width, height, inPixels);
        int index = 0;
        int means = getThreshold(inPixels, height, width);
        for (int row = 0; row < height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for (int col = 0; col < width; col++) {
                index = row * width + col;
                ta = (inPixels[index] >> 24) & 0xff;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
                if (tr > means) {
                    tr = tg = tb = 255;//白
                } else {
                    tr = tg = tb = 0;//黑
                }
                outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
            }
        }
        setRGB(dest, 0, 0, width, height, outPixels);
        return dest;
    }

    private static int getThreshold(int[] inPixels, int h, int w) {
        int iniThreshold = 120;
        int finalThreshold = 0;
        int temp[] = new int[inPixels.length];
        for (int index = 0; index < inPixels.length; index++) {
            temp[index] = (inPixels[index] >> 16) & 0xff;
        }
        List<Integer> sub1 = new ArrayList<Integer>();
        List<Integer> sub2 = new ArrayList<Integer>();
        int means1 = 0, means2 = 0;
        while (finalThreshold != iniThreshold) {
            finalThreshold = iniThreshold;
            for (int i = 0; i < temp.length; i++) {
                if (temp[i] <= iniThreshold) {
                    sub1.add(temp[i]);
                } else {
                    sub2.add(temp[i]);
                }
            }
            means1 = getMeans(sub1);
            means2 = getMeans(sub2);
            sub1.clear();
            sub2.clear();
            iniThreshold = (means1 + means2) / 2;
        }
        finalThreshold -= 15;
        return finalThreshold;
    }

    private static int getMeans(List<Integer> data) {
        int result = 0;
        int size = data.size();
        for (Integer i : data) {
            result += i;
        }
        if(result != 0 && size != 0){        	
        	return (result / size);
        }else{
        	return 0;
        }
    }

    public static void setRGB(BufferedImage image, int x, int y, int w,int h, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB){
            image.getRaster().setDataElements(x, y, w, h, pixels);
        }else{
            image.setRGB(x, y, w, h, pixels, 0, w);
        }
    }

    public static void getRGB(BufferedImage image, int x, int y, int w,int h, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB){
            image.getRaster().getDataElements(x, y, w, h, pixels);
        }else{
            image.getRGB(x, y, w, h, pixels, 0, w);
        }
    }

    public static String ocr(String path){
//    	File file = new File(path);
        ITesseract instance = new Tesseract();
        //设置训练库的位置
        String classPath = Util.getClassPath();
        String dataPath = classPath + "tessdata";
        instance.setDatapath(dataPath);
        instance.setLanguage("chi_sim");//chi_sim eng
        String result = null;
        try {
            //读取图像
            Mat src = Imgcodecs.imread(path);
            //灰度图
            Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);
            //二值化（自适应）
            int blockSize = 41;
            int constValue = 30;
            Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, constValue);
            //过滤杂纹
//            Imgproc.medianBlur(src, src,3);

            //轮廓检测
            Mat temp = src.clone();
            Imgproc.Canny(temp, temp, 20, 60);
            ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(temp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            //去除面积小于20像素的区域
            for(int j = 0;j < contours.size();j ++){
                Rect br = Imgproc.boundingRect(contours.get(j));
                if(br.area() < 20){
                    Mat r = new Mat(src, br);
                    for(int x = 0;x < r.rows();x ++){
                        for(int y = 0;y < r.cols();y ++){
                            double[] data = {255};
                            r.put(x, y, data);
                        }
                    }
                }
            }

            Imgcodecs.imwrite("E:/ocr/test/binary/" + new Date().getTime() + ".jpg",src);
            BufferedImage binary = Mat2BufImg(src, ".jpg");

//        	BufferedImage src = ImageIO.read(file);
//        	BufferedImage binary = binary(src, src);
//        	ImageIO.write(src, "jpg", new File("E:/ocr/test/binary/" + new Date().getTime() + ".jpg"));

            result =  instance.doOCR(binary);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
        System.out.println(result);
        return result;
    }
    
    public static String ocr(BufferedImage img){
        ITesseract instance = new Tesseract();
        String classPath = Util.getClassPath();
        String dataPath = classPath + "ocr/tessdata";
        logger.info("Tesseract-OCR tessdata:{}",dataPath);
        instance.setDatapath(dataPath);
        instance.setLanguage("chi_sim");//chi_sim eng
        String result = null;
        try {
            result =  instance.doOCR(img);
            logger.info("OCR result:{}",result);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String ocr(File img){
        ITesseract instance = new Tesseract();
        String classPath = Util.getClassPath();
        String dataPath = classPath + "ocr/tessdata";
        logger.info("Tesseract-OCR tessdata:{}",dataPath);
        instance.setDatapath(dataPath);
        instance.setLanguage("chi_sim");//chi_sim eng
        String result = null;
        try {
            result =  instance.doOCR(img);
            logger.info("OCR result:{}",result);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
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

    public static void window(String title,Mat mat){
    	new ShowImage(title,mat);
    }
    
}

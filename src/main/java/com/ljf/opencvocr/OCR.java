package com.ljf.opencvocr;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.alibaba.fastjson.JSONObject;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * 图片文字识别
 * @author ljf
 * @since 2019-03-17
 */
public class OCR {

	static {
		//载入opencv库
		String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
		System.load(opencvLib);
	}

	public static JSONObject execute(String imgPath, boolean show){
		//读取图像
		//根据人脸识别裁剪身份证以内的区域
		Map<String, Mat> crop = Face.idcardCrop(imgPath,false);
		Mat src = crop.get("crop");
		Mat key = crop.get("key");
		//灰度化
		Mat gray = key;
		Imgproc.cvtColor(gray, gray, Imgproc.COLOR_BGR2GRAY);
		//二值化（自适应）
		int blockSize = 25;
		int threshold = 35;
		Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, blockSize, threshold);
		
		//过滤杂纹
		Imgproc.medianBlur(gray, gray,3);
		//保存图片（测试）
		Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/" + new Date().getTime() + ".jpg"), gray);
		
		//膨胀（白色膨胀）
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));//使用3*3交叉内核
		Imgproc.dilate(gray, gray, kernel, new Point(-1, -1), 20);//以这个内核为中心膨胀N倍
		//保存图片（测试）
		Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/" + new Date().getTime() + ".jpg"), gray);
		
		//腐蚀（黑色膨胀）
		Mat kernel3 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));//使用3*3交叉内核
		Imgproc.erode(gray, gray, kernel3, new Point(-1, -1), 10);
		//保存图片（测试）
		Imgcodecs.imwrite(Util.mkDirs(Util.mkDirs(Constants.disk + "/ocr/test/" + new Date().getTime() + ".jpg")), gray);
		
		//查找轮廓
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //创建一个白色图片作为背景
        Mat img = new Mat(src.rows(), src.cols(), CvType.CV_8UC1,new Scalar(255, 255, 255));
//        Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/" + new Date().getTime() + ".jpg"),img);

        for(int i = 0;i < contours.size();i ++){
            Rect rect = Imgproc.boundingRect(contours.get(i));

            //画出矩形
//            int x = rect.x;
//            int y = rect.y;
//            int w = x + rect.width;
//            int h = y + rect.height;
//            Point point1 = new Point(x, y);
//            Point point2 = new Point(w, h);
//            Scalar scalar = new Scalar(255, 0, 255);
//            Imgproc.rectangle(src,point1,point2,scalar);

            if(rect.area() > 1500){
                Mat r = new Mat(src, rect);

                //灰度化
                Imgproc.cvtColor(r,r,Imgproc.COLOR_BGR2GRAY);
                //二值化（自适应）
                Imgproc.adaptiveThreshold(r, r, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 41, 30);
                //去除面积小于20像素的区域
                clean(r,20);
                //过滤杂纹
//        	    Imgproc.medianBlur(r, r,3);

                Mat roi = new Mat(img, rect);
                for(int x = 0;x < roi.rows();x ++){
                    for(int y = 0;y < roi.cols();y ++){
                        double[] data = r.get(x,y);
                        roi.put(x, y, data);
                    }
                }

            }
        }

        Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/final.jpg"),img);

        //OCR
        ITesseract instance = new Tesseract();
        //设置训练库的位置
        String dataPath = Util.getClassPath() + "tessdata";
        instance.setDatapath(dataPath);
        instance.setLanguage("chi_sim");//chi_sim eng
        String result = null;
        BufferedImage binary = Mat2BufImg(img, ".jpg");
        try {
            result =  instance.doOCR(binary);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        
		return IdCardUtil.filterOcrInfo(result);
	}

	/**
     * Mat转换成BufferedImage
     * @param matrix 要转换的Mat
     * @param fileExtension 格式为 ".jpg", ".png", etc
     * @return
     */
    public static BufferedImage Mat2BufImg(Mat matrix, String fileExtension) {
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
     * @param original 要转换的BufferedImage
     * @param imgType bufferedImage的类型 如 BufferedImage.TYPE_3BYTE_BGR
     * @param matType 转换成mat的type 如 CvType.CV_8UC3
     */
    public static Mat BufImg2Mat(BufferedImage original, int imgType, int matType) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }
        if (original.getType() != imgType) {
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), imgType);
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
	
}
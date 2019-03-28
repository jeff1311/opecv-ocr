package com.ljf.opencvocr;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * 图片文字识别
 * @author ljf
 * @since 2019-03-17
 */
public class OCR {

	static {
		//载入本地库
		String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
		System.load(opencvLib);
	}

	public static String ocr(String imgPath,boolean show){
		//读取图像
		//Mat src = Imgcodecs.imread(imgPath);
		//根据人脸识别裁剪身份证以内的区域
		Mat src = Face.idcardCrop(imgPath,true);
//		int width = 1200;
//		int height = width * src.height() / src.width();
//		Size size = new Size(width, height);
//		Imgproc.resize(src, src, size);
//		if(show){
//			ImgUtil.window(src);
//		}
		//灰度
		Mat gray = src.clone();
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
//		if(show){
//			ImgUtil.window(gray);
//		}
		//二值化（自适应）
		int blockSize = 25;
		int constValue = 80;
		Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, blockSize, constValue);
		//过滤杂纹
//	    Imgproc.medianBlur(gray, gray,3);
//	    Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size(3,3));//使用3*3交叉内核
//	    Imgproc.erode(gray, gray, kernel2, new Point(-1, -1), 1);
//	    //膨胀
//	    Mat kerne3 = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size(3,3));//使用3*3交叉内核
//		Imgproc.dilate(gray, gray, kerne3, new Point(-1, -1), 1);//以这个内核为中心膨胀8倍
		if(show){
			ImgUtil.window(gray);
		}
		Mat binary = gray.clone();
		//二值图像反色
		Core.bitwise_not(binary, binary);
		if(show){
			ImgUtil.window(binary);
		}
		//二值化
//		int blockSize2 = 25;
//		int constValue2 = 40;
//		Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, blockSize2, constValue2);
//		if(show){
//			ImgUtil.window(gray);
//		}
		//膨胀
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size(3,3));//使用3*3交叉内核
		Imgproc.dilate(gray, gray, kernel, new Point(-1, -1), 30);//以这个内核为中心膨胀8倍
		if(show){
			ImgUtil.window(gray);
		}
		String result = ImgUtil.findContours(gray,src);
		return result;
	}

	public static void main(String[] args) {
		String imgPath = "E:/ocr/3.jpg";
		ocr(imgPath,true);
	}

}
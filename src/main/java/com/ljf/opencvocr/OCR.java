package com.ljf.opencvocr;

import com.alibaba.fastjson.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Date;

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
		Mat src = Face.idcardCrop(imgPath,true);
		//灰度化
		Mat gray = src.clone();
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
		//二值化（自适应）
		int blockSize = 25;
		int constValue = 50;
		Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, blockSize, constValue);
		//保存图片（测试）
		Imgcodecs.imwrite(Util.mkDirs("D:/ocr/test/" + new Date().getTime() + ".jpg"), gray);
		//过滤杂纹
	    Imgproc.medianBlur(gray, gray,3);
//		Mat binary = gray.clone();
//		//二值图像反色
//		Core.bitwise_not(binary, binary);
//		//保存图片（测试）
//		Imgcodecs.imwrite(Util.mkDirs("D:/ocr/test/" + new Date().getTime() + ".jpg"), binary);
		//膨胀（白色膨胀）
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));//使用3*3交叉内核
		Imgproc.dilate(gray, gray, kernel, new Point(-1, -1), 28);//以这个内核为中心膨胀N倍
		//腐蚀（黑色膨胀）
//		Imgproc.erode(gray, gray, kernel, new Point(-1, -1), 15);
		//保存图片（测试）
		Imgcodecs.imwrite(Util.mkDirs(Util.mkDirs("D:/ocr/test/" + new Date().getTime() + ".jpg")), gray);
		JSONObject result = ImgUtil.findContours(gray,src);
		return result;
	}

}
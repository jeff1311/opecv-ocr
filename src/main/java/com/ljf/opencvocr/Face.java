package com.ljf.opencvocr;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.Date;

/**
 * 面部识别
 * @author ljf
 * @since 2019-03-27
 */
public class Face {

	static {
		//载入本地库
		String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
		System.load(opencvLib);
	}

	/**
	 * 身份证正面裁剪（根据人脸识别）
	 * @param imgPath
	 */
	public static Mat idcardCrop(String imgPath,boolean test){
		// 1 读取OpenCV自带的人脸识别特征XML文件
		//OpenCV 图像识别库一般位于 opencv\sources\data 下面
		String faceXmlPath = Util.getClassPath() + "/opencv/xml/haarcascade_frontalface_alt.xml";
		CascadeClassifier facebook = new CascadeClassifier(faceXmlPath);
		// 2 读取测试图片
		Mat image = Imgcodecs.imread(imgPath);
		// 3 固定尺寸
		int width = 1200;
		int height = width * image.height() / image.width();
		Size size = new Size(width, height);
		Imgproc.resize(image, image, size);
		// 4 特征匹配
		MatOfRect face = new MatOfRect();
		facebook.detectMultiScale(image, face);
		// 5 匹配 Rect 矩阵 数组
		Rect[] rects = face.toArray();
		System.out.println("匹配到 " + rects.length + " 个人脸");
		// 6 为识别到的人脸画一个圈
		int maxIndex = 0;
		if(rects.length > 1){
			double maxArea = 0d;
			for(int i = 0;i < rects.length;i ++){
				if(maxArea < rects[i].area()){
					maxArea = rects[i].area();
					maxIndex = i;
				}
			}
		}
		Rect faceRect = rects[maxIndex];
		int x = (int) (faceRect.x - faceRect.width * 2.8);
		int y = (int) (faceRect.y - faceRect.height / 1.7);
		int w = (int) (x + faceRect.width * 4.2);
		int h = (int) (y + faceRect.height * 2.6);
		Point point1 = new Point(x, y);
		Point point2 = new Point(w, h);
		Rect rect = new Rect(point1,point2);
		Mat crop = new Mat(image, rect);
		// 7 把头像区域置为白色
		//face
		int x1 = (int) (faceRect.x - faceRect.width / 3.5);
		int y1 = (int) (faceRect.y - faceRect.height / 2);
		int w1 = (int) (x1 + faceRect.width * 1.6);
		int h1 = (int) (y1 + faceRect.height * 2.0);
		Point point3 = new Point(x1, y1);
		Point point4 = new Point(w1, h1);
		Rect f = new Rect(point3,point4);
		Mat mask = new Mat(image, f);
		//遍历roi区域像素，变为白色
		for( int i = 0; i < mask.rows(); ++i){
			for( int j = 0; j < mask.cols(); ++j ){
				double[] data = mask.get(0,0);
				mask.put(i,j,data);
			}
		}

		if(test){
			String storagePath = "E:/ocr/faceRect/crop/" + new Date().getTime() + ".jpg";
			Imgcodecs.imwrite(storagePath, crop);
			// 7 保存图片
			String filename = "E:/ocr/faceRect/" + new Date().getTime() + ".jpg";
			Imgcodecs.imwrite(filename, image);
		}
		return crop;
	}

	/**
	 * 人脸识别检测
	 * @param imgPath
	 */
	public static void detect(String imgPath){
		// 1 读取OpenCV自带的人脸识别特征XML文件
		//OpenCV 图像识别库一般位于 opencv\sources\data 下面
		String faceXmlPath = Util.getClassPath() + "/opencv/haarcascade_frontalface_alt.xml";
		CascadeClassifier facebook = new CascadeClassifier(faceXmlPath);
		// 2 读取测试图片
		Mat image = Imgcodecs.imread(imgPath);
		// 3 固定尺寸
		int width = 1200;
		int height = width * image.height() / image.width();
		Size size = new Size(width, height);
		Imgproc.resize(image, image, size);
		// 4 特征匹配
		MatOfRect face = new MatOfRect();
		facebook.detectMultiScale(image, face);
		// 5 匹配 Rect 矩阵 数组
		Rect[] rects = face.toArray();
		System.out.println("匹配到 " + rects.length + " 个人脸");
		// 6 为每张识别到的人脸画一个圈
		for (int i = 0; i < rects.length; i++) {
			int x = rects[i].x;
			int y = rects[i].y;
			int w = x + rects[i].width;
			int h = y + rects[i].height;
			Point point1 = new Point(x, y);
			Point point2 = new Point(w, h);
			Scalar scalar = new Scalar(0, 255, 0);
			Imgproc.rectangle(image, point1, point2, scalar);
		}
		// 7 保存图片
		String filename = "E:/ocr/face/" + new Date().getTime() + ".jpg";
		Imgcodecs.imwrite(filename, image);
	}

}

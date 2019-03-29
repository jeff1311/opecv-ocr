package com.ljf.opencvocr;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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
		Rect[] faces = autoRotate(image, facebook);
		// 6 为识别到的人脸画一个圈
		int maxIndex = 0;
		if(faces.length > 1){
			double maxArea = 0d;
			for(int i = 0;i < faces.length;i ++){
				if(maxArea < faces[i].area()){
					maxArea = faces[i].area();
					maxIndex = i;
				}
			}
		}
		Rect faceRect = faces[maxIndex];
		int x = (int) (faceRect.x - faceRect.width * 2.8);
		int y = (int) (faceRect.y - faceRect.height / 1.7);
		int w = (int) (x + faceRect.width * 4.2);
		int h = (int) (y + faceRect.height * 2.6);
		Point point1 = new Point(x, y);
		Point point2 = new Point(w, h);
		Rect rect = new Rect(point1,point2);
		Mat crop = new Mat(image, rect);
		// 7 把人像区域置为白色
		//（方案1）根据人脸检测得到的矩形位置算出大概人像区域
		int x1 = (int) (faceRect.x - faceRect.width / 3.5);
		int y1 = (int) (faceRect.y - faceRect.height / 2);
		int w1 = (int) (x1 + faceRect.width * 1.6);
		int h1 = (int) (y1 + faceRect.height * 2.0);
		Point point3 = new Point(x1, y1);
		Point point4 = new Point(w1, h1);
		Rect f = new Rect(point3,point4);
		Mat mask = new Mat(image, f);
		//（方案2）先根据人脸识别裁剪身份证区域，然后再轮廓提取，遍历最大矩形区域像素，颜色改为白色
		//待测试。。。

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
	public static String detect(String imgPath,String outputPath){
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

		Rect[] faces = autoRotate(image, facebook);

		// 6 为每张识别到的人脸画一个圈
		for (int i = 0; i < faces.length; i++) {
			int x = faces[i].x;
			int y = faces[i].y;
			int w = x + faces[i].width;
			int h = y + faces[i].height;
			Point point1 = new Point(x, y);
			Point point2 = new Point(w, h);
			Scalar scalar = new Scalar(0, 255, 0);
			Imgproc.rectangle(image, point1, point2, scalar);
		}
		// 7 保存图片
		String fileName = new Date().getTime() + ".jpg";
		Imgcodecs.imwrite(outputPath + fileName, image);
		return fileName;
	}

	public static Rect[] autoRotate(Mat src,CascadeClassifier facebook){
		// 4 特征匹配
		MatOfRect face = new MatOfRect();

		Rect[] faces = null;
		boolean hasface = false;
		int times = 0;
		while(!hasface && times < 3){
			times ++;
			if(times == 2){
				// 向左旋转90度
				rotateLeft(src);
			}else if(times == 3){
				// 向右旋转180度
				rotateRight(src);
				rotateRight(src);
			}
			facebook.detectMultiScale(src, face);
			// 5 匹配 Rect 矩阵 数组
			faces = face.toArray();
			System.out.println("匹配到 " + faces.length + " 个人脸");
			if(faces.length > 0){
				double maxArea = 0d;
				for(Rect f : faces){
					System.out.println(f.area());
					if(maxArea < f.area()){
						maxArea = f.area();
					}
				}
				if(maxArea > 10000){
					hasface = true;
				}
			}
		}
		return faces;
	}

	/**
	 * 图像整体向左旋转90度
	 * @param src Mat
	 * @return 旋转后的Mat
	 */
	public static Mat rotateLeft(Mat src) {
		// 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
		Core.transpose(src, src);
		// flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
		// flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
		// flipCode = -1 此函数关于原点对称
		Core.flip(src, src, 0);
		return src;
	}

	/**
	 * 图像整体向右旋转90度
	 * @param src Mat
	 * @return 旋转后的Mat
	 */
	public static Mat rotateRight(Mat src) {
		// 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
		Core.transpose(src, src);
		// flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
		// flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
		// flipCode = -1 此函数关于原点对称
		Core.flip(src, src, 1);
		return src;
	}

}

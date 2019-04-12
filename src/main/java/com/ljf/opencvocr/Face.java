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
		//OpenCV 图像识别库一般位于 opencv\sources\data 下面
		// 1 读取OpenCV自带的人脸识别特征XML文件
		String faceXmlPath = Util.getClassPath() + "/opencv/xml/haarcascade_frontalface_alt.xml";
		CascadeClassifier facebook = new CascadeClassifier(faceXmlPath);
		// 2 读取测试图片
		Mat image = Imgcodecs.imread(imgPath);

		// 3 修改尺寸
//		Size size = null;
//		if(image.width() > image.height()){
//			if(image.width() > 2000){
//				int width = 2000;
//				int height = width * image.height() / image.width();
//				size = new Size(width, height);
//				Imgproc.resize(image, image, size);
//			}
//		}else{
//			if(image.height() > 2000){
//				int height = 2000;
//				int width = height * image.width() / image.height();
//				size = new Size(width, height);
//				Imgproc.resize(image, image, size);
//			}
//		}

		// 4 特征匹配
		Rect[] faces = autoRotate(image, facebook);
		// 5 算出身份证区域并裁图
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

		// 6 把人像区域置为白色
		// 根据人脸检测得到的矩形位置算出大概人像区域
		int x1 = (int) (faceRect.x - faceRect.width / 3.8);
		int y1 = (int) (faceRect.y - faceRect.height / 1.8);
		int w1 = image.width();
		int h1 = (int) (y1 + faceRect.height * 2.1);
		Point point3 = new Point(x1, y1);
		Point point4 = new Point(w1, h1);
		Rect f = new Rect(point3,point4);
		Mat mask = new Mat(image, f);

		// 遍历roi区域像素，变为白色
		for(int i = 0; i < mask.rows();i ++){
			for( int j = 0; j < mask.cols();j ++){
				double[] data = mask.get(0,0);
				mask.put(i,j,data);
			}
		}

		int x0 = (int) (faceRect.x - faceRect.width * 2.8);
		int y0 = (int) (faceRect.y - faceRect.height / 1.7);
		int w0 = (int) (x0 + faceRect.width * 4.2);
		int h0 = (int) (y0 + faceRect.height * 2.7);
		Point point1 = new Point(x0, y0);
		Point point2 = new Point(w0, h0);
		Rect rect = new Rect(point1,point2);
		Mat crop = new Mat(image, rect);


		//遍历裁剪部分像素，只保留黑色
//		for(int x = 0;x < crop.rows();x ++){
//			for(int y = 0;y < crop.cols();y ++){
//				double[] white = {255,255,255};
//				double[] data = crop.get(x, y);
//				if(data[0] > 100 && data[1] > 100 && data[2] > 100){
//					crop.put(x,y,white);
//				}
//			}
//		}

		// 7 修改尺寸
		int width = 1200;
		int height = width * crop.height() / crop.width();
		Size cropSize = new Size(width, height);
		Imgproc.resize(crop, crop, cropSize);

		// 保存图片
		if(test){
			String storagePath = "E:/ocr/faceRect/crop/" + new Date().getTime() + ".jpg";
			Imgcodecs.imwrite(storagePath, crop);

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
		// OpenCV 图像识别库一般位于 opencv\sources\data 下面
		String faceXmlPath = Util.getClassPath() + "/opencv/xml/haarcascade_frontalface_alt.xml";
		CascadeClassifier facebook = new CascadeClassifier(faceXmlPath);
		// 2 读取测试图片
		Mat image = Imgcodecs.imread(imgPath);
		// 3 修改尺寸
		Size size = null;
        if(image.width() > image.height()){        	
        	int width = 1200;
        	int height = width * image.height() / image.width();
        	size = new Size(width, height);
        }else{
        	int height = 1200;
        	int width = height * image.width() / image.height();
        	size = new Size(width, height);
        }
		Imgproc.resize(image, image, size);
		// 4 特征匹配
		Rect[] faces = autoRotate(image, facebook);

		// 5 为每张识别到的人脸画一个圈
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
		// 6 保存图片
		String fileName = new Date().getTime() + ".jpg";
		Imgcodecs.imwrite(outputPath + fileName, image);
		return fileName;
	}

	/**
	 * 特征匹配&自动旋转
	 * @param src
	 * @param facebook
	 * @return
	 */
	public static Rect[] autoRotate(Mat src,CascadeClassifier facebook){
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
			// 匹配 Rect 矩阵 数组
			faces = face.toArray();
			System.out.println("匹配到 " + faces.length + " 个人脸");
			if(faces.length > 0){
				double maxArea = 0d;
				for(Rect f : faces){
//					int x = f.x;
//					int y = f.y;
//					int w = x + f.width;
//					int h = y + f.height;
//					Point point1 = new Point(x, y);
//					Point point2 = new Point(w, h);
//					Scalar scalar = new Scalar(0, 255, 0);
//					Imgproc.rectangle(src, point1, point2, scalar);
//					Imgcodecs.imwrite("E:/ocr/test/face.jpg", src);

					System.out.println(f.area());
					if(maxArea < f.area()){
						maxArea = f.area();
					}
				}
				if(maxArea > 40000 && maxArea < 200000){
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

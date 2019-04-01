package com.ljf.opencvocr;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Test3 {
	
	public static void main(String[] args) {
		String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
        System.load(opencvLib);
        String srcPath = "H:/opencv/sfz/2.jpg";
		
        Mat src = Imgcodecs.imread(srcPath);
        Size size = null;
        if(src.width() > src.height()){        	
        	int width = 800;
        	int height = width * src.height() / src.width();
        	size = new Size(width, height);
        }else{
        	int height = 800;
        	int width = height * src.width() / src.height();
        	size = new Size(width, height);
        }
		Imgproc.resize(src, src, size);
		
		Mat dst = src.clone();
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY);
        // 高斯模糊，主要用于降噪
//        Imgproc.GaussianBlur(dst, dst, new Size(3, 3), 10);
        Imgproc.GaussianBlur(dst, dst, new Size(3,3), 3, 3);
        
        //二值化（自适应）
  		int blockSize = 25;
  		int constValue = 5;//ADAPTIVE_THRESH_GAUSSIAN_C
  		Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, blockSize, constValue);
  		ImgUtil.window(dst);
  		// 膨胀，连接边缘
//  		Imgproc.dilate(dst, dst, new Mat(), new Point(-1,-1), 1, 1, new Scalar(1));
//  		ImgUtil.window(dst);
        //轮廓检测
//        Imgproc.Canny(dst, dst, 20, 90, 3, false);
//        ImgUtil.window(dst);
        
        //轮廓提取
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy1 = new Mat();
        Imgproc.findContours(dst, contours, hierarchy1, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        for(int i = 0;i < contours.size();i ++){   
        	MatOfPoint points = contours.get(i);
        	Imgproc.convexHull(points, new MatOfInt());
        }
        ImgUtil.window(dst);
        
        //去除面积小于100像素的区域
//        for(int i = 0;i < contours.size();i ++){
//        	Rect br = Imgproc.boundingRect(contours.get(i));
//        	if(br.area() < 2000){
//        		Mat r = new Mat(dst, br);
//        		for(int x = 0;x < r.rows();x ++){
//        			for(int y = 0;y < r.cols();y ++){
//        				double[] data = {0};
//        				r.put(x, y, data);
//        			}
//        		}
//        	}
//        }
//        ImgUtil.window(dst);
        
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
      	
      	Rect maxRect = Imgproc.boundingRect(contours.get(maxIndex));
      	int x = maxRect.x;
		int y = maxRect.y;
		int w = x + maxRect.width;
		int h = y + maxRect.height;
		Point point1 = new Point(x, y);
		Point point2 = new Point(w, h);
		Scalar scalar = new Scalar(255, 0, 255);
		Imgproc.rectangle(src,point1,point2,scalar);
        ImgUtil.window(src);
        
	}
	
}

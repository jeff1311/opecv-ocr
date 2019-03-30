package com.ljf.opencvocr;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Test {

    public static void main(String[] args){
        String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
        System.load(opencvLib);
        String srcPath = "H:/opencv/sfz/f.jpg";
		
        Mat src = Imgcodecs.imread(srcPath);
        Size size = null;
        if(src.width() > src.height()){        	
        	int width = 1200;
        	int height = width * src.height() / src.width();
        	size = new Size(width, height);
        }else{
        	int height = 1200;
        	int width = height * src.width() / src.height();
        	size = new Size(width, height);
        }
		Imgproc.resize(src, src, size);
		
        Mat dst = src.clone();
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY);
        // 高斯模糊，主要用于降噪
        Imgproc.GaussianBlur(dst, dst, new Size(3, 3), 0);
        //GaussianBlur图
        ImgUtil.window(dst);
        // 二值化图，主要将灰色部分转成白色，使内容为黑色
        Imgproc.threshold(dst, dst, 150, 255, Imgproc.THRESH_BINARY);
        
        //二值化（自适应）
//  		int blockSize = 25;
//  		int constValue = 50;
//  		Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, blockSize, constValue);
//  		//二值图像反色
//		Core.bitwise_not(dst, dst);
  		
        //threshold图
        ImgUtil.window(dst);
        // 中值滤波，同样用于降噪
//        Imgproc.medianBlur(dst, dst, 3);
//        //medianBlur图
//        ImgUtil.window(dst);
        // 腐蚀操作，主要将内容部分向高亮部分腐蚀，使得内容连接，方便最终区域选取
//        Imgproc.erode(dst, dst, new Mat(15, 15, CvType.CV_8UC1));
//        //erode图
//        ImgUtil.window(dst);
        
        //腐蚀
  		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size(3,3));//使用3*3交叉内核
  		Imgproc.erode(dst, dst, kernel, new Point(-1, -1), 20);//以这个内核为中心膨胀N倍
  		ImgUtil.window(dst);
  		
        //轮廓提取
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        
        for(int i = 0;i < contours.size();i ++){
        	Rect br = Imgproc.boundingRect(contours.get(i));
        	if(br.area() > 2000){        		
        		int x = br.x;
        		int y = br.y;
        		int w = x + br.width;
        		int h = y + br.height;
        		Point point1 = new Point(x + 10, y + 10);
        		Point point2 = new Point(w - 10, h - 10);
        		Scalar scalar = new Scalar(255, 0, 255);
        		Imgproc.rectangle(src,point1,point2,scalar);
        	}
        }
        
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
        Mat src2 = new Mat(src, maxRect);
        
        //轮廓图
        ImgUtil.window(src2);
        
    }

}

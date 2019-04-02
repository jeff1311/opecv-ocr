package com.ljf.opencvocr;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Test3 {
	
	public static void main(String[] args) {
		String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
        System.load(opencvLib);
        String srcPath = "E:/ocr/8.jpg";
		
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
		//灰度
		Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY);
		//二值化（自适应）
  		int blockSize = 25;
  		int constValue = 50;
  		Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, blockSize, constValue);
		ImgUtil.window("自适应二值化",dst);
		// 中值滤波，同样用于降噪
//        Imgproc.medianBlur(dst, dst, 3);
//		ImgUtil.window("中值滤波",dst);
		//腐蚀
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));//使用3*3交叉内核
		Imgproc.dilate(dst, dst, kernel, new Point(-1, -1), 10);//以这个内核为中心膨胀N倍
		ImgUtil.window("腐蚀",dst);

		//掩膜锐化算法
		//x(x,y) = 5 * x(x,y) - [x(x - 1,y) + x(x + 1,y) + x(x,y - 1) + x(x,y + 1)]
//		Mat kernal = new Mat(3,3,CvType.CV_32FC1, new Scalar(0));
//		kernal.put(0, 1, -1);
//		kernal.put(1, 0, -1);
//		kernal.put(1, 1, 5);
//		kernal.put(1, 2, -1);
//		kernal.put(2, 1, -1);
//		Imgproc.filter2D(src, src, src.depth(), kernal);
//		ImgUtil.window(src);

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
//        double maxArea = 0;
//      	//最大轮廓索引
//      	int maxIndex = 0;
//      	for(int i = 0;i < contours.size();i ++){
//      		Rect br = Imgproc.boundingRect(contours.get(i));
//      		if(maxArea < br.area()){
//      			maxArea = br.area();
//      			maxIndex = i;
//      		}
//      	}
      	
//      	Rect maxRect = Imgproc.boundingRect(contours.get(maxIndex));
//      	int x = maxRect.x;
//		int y = maxRect.y;
//		int w = x + maxRect.width;
//		int h = y + maxRect.height;
//		Point point1 = new Point(x, y);
//		Point point2 = new Point(w, h);
//		Scalar scalar = new Scalar(255, 0, 255);
//		Imgproc.rectangle(src,point1,point2,scalar);
//        ImgUtil.window(src);
        
	}

}

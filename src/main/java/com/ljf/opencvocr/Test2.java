package com.ljf.opencvocr;

import java.util.Date;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Test2 {

	public static void main(String[] args) {
		String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
        System.load(opencvLib);
        String srcPath = "H:/opencv/sfz/2.jpg";
		
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
		
		double[] white = {255,255,255};
		for(int x = 0;x < src.rows();x ++){
			for(int y = 0;y < src.cols();y ++){
				double[] data = src.get(x, y);
				if(data[0] < 100 && data[1] < 100 && data[2] < 100){
					
				}else{
					src.put(x, y, white);
				}
			}
		}
		
		String storagePath = "E:/ocr/test/" + new Date().getTime() + ".jpg";
		Imgcodecs.imwrite(storagePath, src);
		ImgUtil.ocr(storagePath);
		ImgUtil.window(src);
		
	}
	
}

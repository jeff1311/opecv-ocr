package com.ljf.opencvocr;

import org.opencv.core.Mat;

public class Test {

    public static void main(String[] args){
        String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
        System.load(opencvLib);
        String srcPath = "H:/opencv/sfz/z.jpg";
		Mat gray = ImgUtil.gray(srcPath);
//		ImgUtil.window(gray);
		Mat binary = ImgUtil.binarize(gray, gray);
//		ImgUtil.window(binary);
//		Mat binary = ImgUtil.binarization(srcPath, gray);
		Mat dilate = ImgUtil.dilate(binary,30);
//		ImgUtil.window(dilate);
//		ImgUtil.findContours(dilate,srcPath);
    }

}

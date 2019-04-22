package com.ljf.opencvocr;

import com.alibaba.fastjson.JSONObject;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Date;

/**
 * 图片文字识别
 * @author ljf
 * @since 2019-03-17
 */
public class OCR2 {

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
		int constValue = 30;
		Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, constValue);

        Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/" + new Date().getTime() + ".jpg"), gray);

        //轮廓检测
        Mat temp = gray.clone();
        Imgproc.Canny(temp, temp, 20, 60);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(temp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //去除面积小于20像素的区域
        for(int j = 0;j < contours.size();j ++){
            Rect br = Imgproc.boundingRect(contours.get(j));
            if(br.area() < 20){
                Mat r = new Mat(gray, br);
                for(int x = 0;x < r.rows();x ++){
                    for(int y = 0;y < r.cols();y ++){
                        double[] data = {255};
                        r.put(x, y, data);
                    }
                }
            }
        }

        String img = Constants.disk + "/ocr/test/" + new Date().getTime() + ".jpg";
		//保存图片（测试）
		Imgcodecs.imwrite(Util.mkDirs(img), gray);
		//过滤杂纹
	    Imgproc.medianBlur(gray, gray,3);

	    ImgUtil.ocr2(gray);

		return null;
	}

}
package com.ljf.opencvocr.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.*;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OCRServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;

	private List<BufferedImage> images = null;

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Util.cleanFiles(Constants.disk + "/ocr/test");
        Model uploadInfo = Upload.getInfo(req);
        Map<String, String> params = uploadInfo.getParams();
        String test = params.get("test");
        images = uploadInfo.getImages();

        JSONArray ocrInfo = new JSONArray();
        for(BufferedImage image : images){
            JSONObject info = OCR.execute(image,true);
            System.out.println(info);
            ocrInfo.add(info);
        }

        JSONObject json = new JSONObject();
        json.put("code", 200);
        json.put("ocrInfo", ocrInfo);
        Util.returnInfo(resp, json);
    }

}

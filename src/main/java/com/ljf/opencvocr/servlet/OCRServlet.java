package com.ljf.opencvocr.servlet;

import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
//资料：https://blog.csdn.net/ysc6688/article/category/2913009
public class OCRServlet extends HttpServlet {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Util.clearFiles(Constants.disk + "/ocr/test");
        Model uploadInfo = Upload.getInfo(req);
        Map<String, String> params = uploadInfo.getParams();
        String storagePath = params.get("storagePath");
        String storageName = params.get("storageName");
        BufferedImage img = uploadInfo.getImg();
        String tempPath = new Date().getTime() + ".jpg";
        String srcImg = Constants.disk + "/ocr" + storagePath;
        ImageIO.write(img, "jpg", Util.mkFile(srcImg + tempPath));
        JSONObject ocrInfo = OCR.execute(srcImg + tempPath,false);
//        JSONObject ocrInfo = OCR2.execute(srcImg + tempPath,false);
        System.out.println(ocrInfo);
        JSONObject json = new JSONObject();
        json.put("code", 200);
        json.put("ocrInfo", ocrInfo);
        json.put("tempPath", tempPath);
        json.put("storageName", storageName);
        json.put("baseImgPath", "/files" + storagePath);
        Util.returnInfo(resp, json);
    }

}

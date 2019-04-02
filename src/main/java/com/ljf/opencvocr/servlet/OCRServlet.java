package com.ljf.opencvocr.servlet;

import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.Model;
import com.ljf.opencvocr.OCR;
import com.ljf.opencvocr.Upload;
import com.ljf.opencvocr.Util;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
import java.util.*;
//资料：https://blog.csdn.net/ysc6688/article/category/2913009
public class OCRServlet extends HttpServlet {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Model uploadInfo = Upload.getInfo(req);
        Map<String, String> params = uploadInfo.getParams();
        String storagePath = params.get("storagePath");
        String storageName = params.get("storageName");
        BufferedImage img = uploadInfo.getImg();
        String tempPath = new Date().getTime() + ".jpg";
        ImageIO.write(img, "jpg", new File("E:/ocr/" + storagePath + tempPath));
        String ocrtext = OCR.execute("E:/ocr/" + storagePath + tempPath,false);
        System.out.println(ocrtext);
        JSONObject json = new JSONObject();
        json.put("code", 200);
        json.put("ocrtext", ocrtext);
        json.put("tempPath", tempPath);
        json.put("storageName", storageName);
        json.put("baseImgPath", "/files" + storagePath);
        Util.returnInfo(resp, json);
    }

}

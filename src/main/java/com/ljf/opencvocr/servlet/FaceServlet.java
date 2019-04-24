package com.ljf.opencvocr.servlet;

import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.Face;
import com.ljf.opencvocr.Model;
import com.ljf.opencvocr.Upload;
import com.ljf.opencvocr.Util;
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

public class FaceServlet extends HttpServlet {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Model upload = Upload.getInfo(req);
//        Map<String, String> params = upload.getParams();
//        String storagePath = params.get("storagePath");
//        String storageName = params.get("storageName");
//        BufferedImage img = upload.getImg();
//        String outputPath = "E:/ocr/face/";
//        String imgName = new Date().getTime() + ".jpg";
//        ImageIO.write(img,"jpg",new File(outputPath + imgName));
//        String imgPath = Face.detect(outputPath + imgName,outputPath);
//        JSONObject json = new JSONObject();
//        json.put("code", 200);
//        json.put("imgPath", "/img/" + imgPath);
//        Util.returnInfo(resp,json);
    }

}

package com.ljf.opencvocr.servlet;

import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OCRServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;

	private List<BufferedImage> images = null;

	private static List<JSONObject> ocrInfo = new ArrayList<JSONObject>();

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Util.cleanFiles(Constants.disk + "/ocr/test");
        Model uploadInfo = Upload.getInfo(req);
        Map<String, String> params = uploadInfo.getParams();
        String test = params.get("test");
        images = uploadInfo.getImages();

        if(ocrInfo.size() != 0){
            ocrInfo.clear();
        }

        //线程池
        int corePoolSize = images.size() <= 4 ? images.size() : 4;//线程池的基本大小
        int maximumPoolSize = 4;//线程池中允许的最大线程数
        int keepAliveTime = 10;//空闲线程等待新任务的最长时间（秒）
        LinkedBlockingQueue<Runnable> runnables = new LinkedBlockingQueue<>();//队列
        ExecutorService executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,keepAliveTime, TimeUnit.SECONDS, runnables);

        //每个线程的任务
        List<BufferedImage> task1 = new ArrayList<BufferedImage>();
        List<BufferedImage> task2 = new ArrayList<BufferedImage>();
        List<BufferedImage> task3 = new ArrayList<BufferedImage>();
        List<BufferedImage> task4 = new ArrayList<BufferedImage>();

        //任务分配
        for(int i = 0;i < images.size();i ++){
            BufferedImage image = images.get(i);
            int n = i % 4;
            switch(n){
                case 0:
                    task1.add(image);
                    break;
                case 1:
                    task2.add(image);
                    break;
                case 2:
                    task3.add(image);
                    break;
                case 3:
                    task4.add(image);
                    break;
            }
        }

        //创建ocr对象
        OcrThread ocr1 = new OcrThread(task1,ocrInfo);
        OcrThread ocr2 = new OcrThread(task2,ocrInfo);
        OcrThread ocr3 = new OcrThread(task3,ocrInfo);
        OcrThread ocr4 = new OcrThread(task4,ocrInfo);

        //开启线程执行
        executorService.execute(ocr1);
        executorService.execute(ocr2);
        executorService.execute(ocr3);
        executorService.execute(ocr4);

        //识别完毕后返回数据
        int maxTime = 10 * 60;//超时时间设为一分钟
        int time = 0;
        while(++ time < maxTime){
            try {
                //每0.1秒循环一次，降低循环次数
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(ocrInfo.size());
            if(ocrInfo.size() == images.size()){
                //跳出循环
                break;
            }
        }
        //关闭线程池
        executorService.shutdown();

        JSONObject json = new JSONObject();
        json.put("code", 200);
        json.put("ocrInfo", ocrInfo);
        Util.returnInfo(resp, json);
    }

}

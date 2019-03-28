package com.ljf.opencvocr;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Util {

	public static void returnInfo(HttpServletResponse response,JSONObject json){
		response.setContentType("text/html;charset=utf-8");
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
			pw.print(json.toJSONString());
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(pw != null){
				pw.close();
			}
		}
	}
	
	/**获取classpath*/
	public static String getClassPath() {
		String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String rootPath = "";
        //windows
        if ("\\".equals(File.separator)) {
            rootPath = classPath.substring(1);
        }
        //linux
        if ("/".equals(File.separator)) {
        	rootPath = classPath;
        }
        return rootPath;
    }
	
}

package com.ljf.opencvocr;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Upload {

	private static Logger logger = LoggerFactory.getLogger("util");

	public static Model getInfo(HttpServletRequest request){
		// 从页面中拿取数据，因为上传页的编码格式跟一般的不同，使用的是enctype="multipart/form-data"
		// form提交采用multipart/form-data,无法采用req.getParameter()取得数据
		Map<String,String> params = new HashMap<String,String>();
		BufferedImage bi = null;
		// 判断上传表单是否为multipart/form-data类型
		if (ServletFileUpload.isMultipartContent(request)) {

			try {

				// DiskFileItemFactory：创建 FileItem 对象的工厂，在这个工厂类中可以配置内存缓冲区大小和存放临时文件的目录。
				// 在接收上传文件数据时，会将内容保存到内存缓存区中，如果文件内容超过了 DiskFileItemFactory 指定的缓冲区的大小，
				// 那么文件将被保存到磁盘上，存储为 DiskFileItemFactory 指定目录中的临时文件。
				// 等文件数据都接收完毕后，ServletUpload再从文件中将数据写入到上传文件目录下的文件中
				// 1.创建DiskFileItemFactory对象，设置缓冲区大小和临时文件目录
				DiskFileItemFactory factory = new DiskFileItemFactory();
				// 文件缓冲文件夹是否存在，不存在则创建
				File tempFolder = new File( "E:/ocr/temp");
				if(!tempFolder.exists()){
					tempFolder.mkdirs();
				}
				factory.setRepository(tempFolder);
				// 文件大于1MB则先存到磁盘临时文件夹
				factory.setSizeThreshold(1 * 1024 * 1024);

				// 2.创建ServletFileUpload对象，并设置上传文件的大小限制。ServletFileUpload：负责处理上传的文件数据，并将每部分的数据封装成一到 FileItem 对象中。
				ServletFileUpload sfu = new ServletFileUpload(factory);
				sfu.setSizeMax(5 * 1024 * 1024);//以byte为单位,不能超过5M(1024B = 1KB,1024KB = 1MB)
				sfu.setHeaderEncoding("utf-8");

				// 3.调用ServletFileUpload.parseRequest方法解析request对象，得到一个保存了所有上传内容的List对象。
				@SuppressWarnings("unchecked")
				List<FileItem> fileItemList = sfu.parseRequest(request);
				// 4.遍历list，每迭代一个FileItem对象，调用其isFormField方法判断是否是上传文件
				String storagePath = "";
				Iterator<FileItem> fileItems1 = fileItemList.iterator();
				while (fileItems1.hasNext()) {
					FileItem fileItem = fileItems1.next();
					if (fileItem.isFormField()) {// 普通表单元素
						if (fileItem.isFormField()){
							String name = fileItem.getFieldName();// name属性值
							String value = fileItem.getString("utf-8");// name对应的value值
							if("storagePath".equals(name)){
								storagePath = value;
							}
							logger.info(name + " = " + value);
							params.put(name, value);
						}
					}
				}

				Iterator<FileItem> fileItems2 = fileItemList.iterator();
				while (fileItems2.hasNext()) {
					FileItem fileItem = fileItems2.next();
					if (!fileItem.isFormField()) {// 文件

						// 文件名称
						String fileName = fileItem.getName();
						logger.info("原文件名：" + fileName);

						// 新文件名,18位随机数
						int random1 = (int)((Math.random() * 9 + 1) * 100000000);
						int random2 = (int)((Math.random() * 9 + 1) * 100000000);
						String random = String.valueOf(random1) + String.valueOf(random2);
						String newFileName = random + ".jpg";
						params.put("storageName", newFileName);
						logger.info("新文件名：" + newFileName);
//						String baseSavePath = Constants.getFileUploadPath() + storagePath + newFileName;

						// 5.保存路径是否存在，不存在则创建
						File saveFolder = new File("E:/ocr/storage/" + storagePath);
						if(!saveFolder.exists()){
							saveFolder.mkdirs();
						}

						// 6.写入文件
//						File newFile = new File(baseSavePath);

//						bi = ImageIO.read(fileItem.getInputStream());//图片泛红
						byte[] byteArray = ImgUtil.toByteArray(fileItem.getInputStream());
						bi = ImgUtil.toBufferedImage(byteArray);

						// 7.删除临时文件
						fileItem.delete();

					}
				}

			} catch (FileUploadException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return new Model(params, bi);
	}

}

package domain;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
@Data
public class Webuploader {
	//uploader自带参数
	private MultipartFile file;
	private String chunks;//总分片量
	private String chunk;//当前分片
	private String name;//文件名称
	private String size;//文件大小
	private String lastModifiedDate;//最后修改时间
	private String type;//文件类型
	private String id;//文件id
	private String guid;
	//自定义参数
	String fileMd5;
	String rate;
	 
}
 
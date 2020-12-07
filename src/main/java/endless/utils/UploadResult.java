package endless.utils;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class UploadResult {
	
	private boolean success = true;
	
	private String message = "上传成功";
	
	private List<Map<String,Object>> urlAndSize = Lists.newArrayList();
	
	private List<String> fileURLs = Lists.newArrayList();
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getFileURLs() {
		return fileURLs;
	}

	public void setFileURLs(List<String> fileURLs) {
		this.fileURLs = fileURLs;
	} 
	
	public List<Map<String, Object>> getUrlAndSize() {
		return urlAndSize;
	}

	public void setUrlAndSize(List<Map<String, Object>> urlAndSize) {
		this.urlAndSize = urlAndSize; 
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	
}

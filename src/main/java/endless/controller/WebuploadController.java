package endless.controller;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.crypto.SecureUtil;
import domain.Webuploader;
import endless.conf.ConfUtil;
import endless.utils.ExtResult;
import endless.utils.IdCardCodeUtils;
import endless.utils.LocalUploadUtils;
import endless.utils.UploadResult;
import endless.utils.WorkId;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import net.coobird.thumbnailator.Thumbnails;


/**
 * @ClassName: WebuploadAction
 * @Description: TODO( webupload上传插件所需方法 )
 * @date 2017年12月13日 上午10:53:52
 */
@RestController
@RequestMapping("/api/webupload/")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WebuploadController {

	public final transient Logger log = LoggerFactory.getLogger(WebuploadController.class);
	
	/**
	 * 为每个用户单独创建了一个自己的上传文件的文件夹
	 * 
	 * @Title: uploader
	 * @Description: TODO( 百度上传插件webuploader服务端接受 )
	 * @param request
	 * @param uploader
	 * @return
	 * @date 2017年12月15日 下午3:05:01
	 */ 
	@PostMapping(value = "upload")
    @ApiOperation( value = "文件上传", httpMethod = "POST",notes="")
    @ApiResponse(code = 200, message = "success", response = ExtResult.class)
	public ExtResult<String> uploader(HttpServletRequest request, Webuploader uploader) { 
		String chunk = uploader.getChunk() == null ? "0":uploader.getChunk() ;  
		try { 
    		String localPath= ConfUtil.upLoadTemp +File.separator+ uploader.getGuid() +File.separator;
            UploadResult re = LocalUploadUtils.uploadFile(request,chunk,localPath, 10240);
            if(re.isSuccess()){
               return ExtResult.ok();
            } 
            return ExtResult.fail();
		} catch (Exception e) {
			e.printStackTrace();
			return ExtResult.fail_msg("服务出现异常");
		}
	}

	@PostMapping(value = "multipleUpload")
    @ApiOperation( value = "文件上传", httpMethod = "POST",notes="")
    @ApiResponse(code = 200, message = "success", response = ExtResult.class)
	public ExtResult<String> multipleUpload(HttpServletRequest request, Webuploader uploader) { 
		String chunk = uploader.getChunk() == null ? "0":uploader.getChunk() ;  
		try { 
    		String localPath=  ConfUtil.upLoadTemp +File.separator+ uploader.getGuid() +File.separator+  SecureUtil.md5(uploader.getName());
            UploadResult re = LocalUploadUtils.uploadFile(request,chunk,localPath, 10240);
            if(re.isSuccess()){
               return ExtResult.ok();
            } 
            return ExtResult.fail();
		} catch (Exception e) {
			e.printStackTrace();
			return ExtResult.fail_msg("服务出现异常");
		}
	}
	@PostMapping(value = "checkChunks")
    @ApiOperation( value = "验证切片是否已经上传或者是否完整", httpMethod = "POST",notes="")
    @ApiResponse(code = 200, message = "success", response = ExtResult.class)
	public ExtResult<String> checkChunks(HttpServletRequest request, Webuploader uploader) {
		// 获取当前用户
		try { 
			 
			String jindu = uploader.getRate();
			if ("".equals(jindu)) {
				jindu = "0";
			}
			String localPath=  ConfUtil.upLoadTemp +File.separator+ uploader.getGuid() +File.separator+  SecureUtil.md5(uploader.getName());
    		String[] fileNames = FileUtil.listFiles(new File(localPath));
    		if(fileNames.length>0 && fileNames.length == new Integer(uploader.getChunks())){
    			 return ExtResult.ok();
    		}
    		return ExtResult.fail(); 
		} catch (Exception e) {
			e.printStackTrace();
			return ExtResult.fail_msg("服务出现异常");
		} 
	} 
 
	@PostMapping(value = "mixChunks")
    @ApiOperation( value = "将所有的切片合成", httpMethod = "POST",notes="")
    @ApiResponse(code = 200, message = "success", response = ExtResult.class)
	public ExtResult<String>  mixChunks(HttpServletRequest request, Webuploader uploader) throws Exception {
		 
		 
		String localPath=  ConfUtil.upLoadTemp +File.separator+ uploader.getGuid();
		
		 
		File temp = Files.createTempFile(WorkId.sortUID()+"", ".png").toFile(); 
		
		try(FileOutputStream destTempfos = new FileOutputStream(temp, true);){ 
				
				String[] fileNames = FileUtil.listFiles(new File(localPath));
				List<String> sortNames = Stream.of(fileNames).sorted((a, b) -> a.compareTo(b)).collect(Collectors.toList());
				for (String name : sortNames) { 
					FileUtils.copyFile(new File(localPath+ File.separator + name), destTempfos);
				}
				FileUtils.deleteDirectory(new File(localPath+File.separator)); 
				Thumbnails.of(temp).size(290, 384).toFile(temp); 
				String code = IdCardCodeUtils.idCard(temp.getAbsolutePath());
	    		System.out.println(code);
	    		if(IdcardUtil.isValidCard(code)){
	    			return ExtResult.ok(code);
	    		}else{
	    			return ExtResult.fail_msg(code+"不是正确的身份证号码，请重新识别");
	    		}  
		} catch (Exception e) {
			e.printStackTrace();
			return ExtResult.fail_msg("服务出现异常");
		}
	}
 
	@PostMapping(value = "multipleMixChunks")
    @ApiOperation( value = "将所有的切片合成", httpMethod = "POST",notes="")
    @ApiResponse(code = 200, message = "success", response = ExtResult.class)
	public ExtResult<String>  multipleMixChunks(HttpServletRequest request, Webuploader uploader) throws Exception {
		 
		 
		String localPath=  ConfUtil.upLoadTemp +File.separator+ uploader.getGuid() +File.separator+  SecureUtil.md5(uploader.getName());
		
		String[] dirNames = FileUtil.listFiles(new File( ConfUtil.upLoadTemp +File.separator+ uploader.getGuid()));
		for(String dirName : dirNames){
			File temp = Files.createTempFile(WorkId.sortUID()+"", ".png").toFile();  
			try(FileOutputStream destTempfos = new FileOutputStream(temp, true);){  
				String[] fileNames = FileUtil.listFiles(new File(localPath));
				List<String> sortNames = Stream.of(fileNames).sorted((a, b) -> a.compareTo(b)).collect(Collectors.toList());
				for (String name : sortNames) { 
					FileUtils.copyFile(new File(localPath+ File.separator + name), destTempfos);
				}
				FileUtils.deleteDirectory(new File(localPath+File.separator));
				return ExtResult.ok();
			} catch (Exception e) {
				e.printStackTrace();
				return ExtResult.fail_msg("服务出现异常");
			}
		} 
		return ExtResult.ok();
	}
 
	
	
	@PostMapping(value = "getProgress")
    @ApiOperation( value = "获取当前文件是否上传，上传进度是多少", httpMethod = "POST",notes="")
    @ApiResponse(code = 200, message = "success", response = ExtResult.class)
	public ExtResult<String> getProgress(HttpServletRequest request, Webuploader uploader) {
		 
	 
		String rate = "0";
		Date now = new Date();
		 
		if (StringUtils.isNotBlank(uploader.getName())) {
			try {
				 
				String localPath=  ConfUtil.upLoadTemp + uploader.getGuid();
	    		String[] fileNames = FileUtil.listFiles(new File(localPath));
				if (fileNames.length == 0) {
					rate = "0";
				}
			} catch (Exception e) {
				rate = "0";
			}
		}
		return ExtResult.ok();
	}
 
	/**
	 * 有多线程问题
	 * @param request
	 * @param uploader
	 * @return
	 */
	@PostMapping(value = "upload2")
    @ApiOperation( value = "webuploader上传方法，不支持断点，若断点，相当于重新上传", httpMethod = "POST",notes="")
    @ApiResponse(code = 200, message = "success", response = ExtResult.class)
	public ExtResult<String> upload2(HttpServletRequest request, Webuploader uploader) {
		 
		String chunks = uploader.getChunks();
		String chunk = uploader.getChunk();
		String name = uploader.getName(); 
		try {   
			// 判断上传的文件是否被分片
			if (StringUtils.isBlank(chunk)) {
				String newName = WorkId.sortUID() +"."+ name.split("\\.")[1];
				String localPath=  ConfUtil.upLoadTemp;
	            UploadResult re = LocalUploadUtils.uploadFile(request,newName,localPath, 10240);
	            if(re.isSuccess()){
	               return ExtResult.ok();
	            } 
	            return ExtResult.fail();
			}
			
			String localPath=  ConfUtil.upLoadTemp + uploader.getGuid();
            UploadResult re = LocalUploadUtils.uploadFile(request,chunk,localPath, 10240); 
			 
			// 是否全部上传完成
			// 所有分片都存在才说明整个文件上传完成
			boolean uploadDone = true; 
			 
    		String[] fileNames = FileUtil.listFiles(new File(localPath));
    		if(fileNames.length < new Integer(chunks)){ 
    			uploadDone = false;
    			return ExtResult.fail_msg("");
    		} 
			 
			// 所有分片文件都上传完成
			// 将所有分片文件合并到一个文件中
			if (uploadDone) {
				synchronized (this) { 
					String[] hasFiles = FileUtil.listFiles(new File(localPath));
		    		if(hasFiles.length < new Integer(chunks)){  
		    			return ExtResult.ok();
		    		} 
					String newName = WorkId.sortUID() +"."+ name.split("\\.")[1];
					File destTempFile = new File( ConfUtil.upLoadTemp,newName);// ??now.getTime()+name大文件是否需要重新命名
					
					try(FileOutputStream destTempfos = new FileOutputStream(destTempFile, true);){ 
							
					 
							List<String> sortNames = Stream.of(hasFiles).sorted((a, b) -> a.compareTo(b)).collect(Collectors.toList());
							for (String tempName : sortNames) { 
								FileUtils.copyFile(new File(localPath+ File.separator + tempName), destTempfos);
							}
							//FileUtils.deleteDirectory(new File(localPath+File.separator));
							return ExtResult.ok();
							
					} catch (Exception e) {
						e.printStackTrace();
						return ExtResult.fail_msg("服务出现异常");
					}
				}
			}
			return ExtResult.ok();
			 
			 
		} catch (Exception e) {
			this.log.error("上传失败", e);
			return ExtResult.fail_msg("上传失败");
		}
	}
}
 
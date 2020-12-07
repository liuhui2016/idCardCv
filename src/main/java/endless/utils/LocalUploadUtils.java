package endless.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

 



/**
 * 
 */
public class LocalUploadUtils {
   
    private static final Map<String, String> EXT_MAP;
    
    static{
        EXT_MAP = new HashMap<String, String>();
        EXT_MAP.put("image", "gif,jpg,jpeg,png,bmp");
        EXT_MAP.put("flash", "swf,flv");
        EXT_MAP.put("media", "swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb,mp4");
        EXT_MAP.put("file", "doc,docx,pdf,PDF,xls,xlsx,ppt,htm,html,txt,zip,rar,gz,bz2");
    }
   
    public static UploadResult uploadFlash(HttpServletRequest request,String fileName,String saveDir,Integer maxSize){
        
        return upload(request,fileName,"flash",saveDir,maxSize);
    } 
     
    public static UploadResult uploadImage(HttpServletRequest request,String fileName,String saveDir,Integer maxSize){
        
        return upload(request,fileName,"image",saveDir,maxSize);
    }
    
    public static UploadResult uploadMedia(HttpServletRequest request,String fileName,String saveDir,Integer maxSize){
        
        return upload(request,fileName,"media",saveDir,maxSize);
    } 
   
    public static UploadResult uploadFile(HttpServletRequest request,String fileName,String saveDir,Integer maxSize){
        
        return upload(request,fileName,"file",saveDir,maxSize);
    }
    //创建文件保存文件夹
    private static void createFileDir(String savePath){
        File uploadDir = new File(savePath);
        if(!uploadDir.exists()){
            uploadDir.mkdirs();
        }
    }
    
    private static UploadResult upload(HttpServletRequest request,String fileName,String fileType,String basePath,Integer maxSize){
    	// 文件保存目录url
    	
    	String savePath = createSavePath(basePath);
    	String saveUrl  = createSaveURLByBase(savePath);
    	//文件保存物理路径
    	//String savePath = request.getSession().getServletContext().getRealPath("") + saveUrl;
    	 
    	//创建文件保存文件夹
    	createFileDir(savePath);
    	
    	UploadResult result = new UploadResult();
        result.setSuccess(false);
     
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
         
        if(multipartResolver.isMultipart(request)){
 
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
            Iterator<String> iter = multiRequest.getFileNames();
            while(iter.hasNext()){
                MultipartFile file = multiRequest.getFile(iter.next());
                if(file != null){ 
                    if(!fileName.trim().equals("")){
                        try {
                        	if(maxSize!=null && file.getSize()>maxSize*1024){
                        		throw new MaxUploadSizeExceededException(maxSize*1024);
                        	}
                        	//fileName = recreateFileName(getFileExt(fileName, fileType));
                        	//fileName = getFileExt1(fileName, fileType);

                            File localFile = new File(savePath,fileName); 
                            inputStreamToFile(file.getInputStream(), localFile);
                         
                        } catch (IOException e) {
                            e.printStackTrace();
                            result.setMessage("文件上传失败");
                            result.setSuccess(false);
                            result.getFileURLs().clear();
                            break;
                        } catch (MaxUploadSizeExceededException e){
                            e.printStackTrace();
                        	result.setMessage("上传文件大小超过限制");
                        	result.setSuccess(false);
                        	result.getFileURLs().clear();
                        	break;
                        } catch (UnsupportedFileExtensionException e){
                            e.printStackTrace();
                        	result.setMessage("上传文件扩展名是不允许的扩展名。\n只允许" + EXT_MAP.get(fileType) + "格式");
                        	result.setSuccess(false);
                        	result.getFileURLs().clear();
                        	break;
                        }catch (Exception e) {
                            e.printStackTrace();
                            result.setMessage("文件上传失败");
                            result.setSuccess(false);
                            result.getFileURLs().clear();
                            break;
                        }
                        result.getFileURLs().add(saveUrl+"/"+fileName);
                        String[] fileNames = file.getOriginalFilename().split("\\.");
                        result.setMessage(fileNames[0]);
                        result.setSuccess(true);
                    }
                }
            }
        }
        return result;
        
    }
    //获取流文件
    private static void inputStreamToFile(InputStream inss, File file) {
        try(OutputStream os = new FileOutputStream(file);InputStream ins = inss){

            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
   
    private static String recreateFileName(String ext)throws Exception {
        return  WorkId.sortUID() + "." + ext;
        /*SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return df.format(new Date()) + "_" + new Random().nextInt(10000000) + "." + ext;*/
    } 
    
    public static String createSaveURLByBase(String basePath){
    	basePath = basePath.split(":")[1];
    	basePath = basePath.replaceAll("\\\\", "/"); 
        return basePath ;
    }
    public static String createSavePath(String basePath){ 
    	basePath = basePath.endsWith(File.separator) ? basePath : basePath + File.separator;
        return basePath ;//+  new SimpleDateFormat("yyyyMMdd").format(new Date());
    }
    
    public static String createSaveURL(String basePath){ 
    	basePath = basePath.startsWith("/") ? basePath : "/"+basePath;
    	basePath = basePath.endsWith("/") ? basePath : basePath + "/";
        return basePath +  new SimpleDateFormat("yyyyMMdd").format(new Date());
    }
     
     
}

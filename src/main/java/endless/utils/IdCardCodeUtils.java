package endless.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point2d;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;

import com.google.common.base.CharMatcher;
import com.nbsl.cv.utils.CoreFunc;
import com.nbsl.cv.utils.OCRUtil;
import com.nbsl.cv.utils.OpencvUtil;

import endless.conf.ConfUtil;
import net.coobird.thumbnailator.Thumbnails;

public class IdCardCodeUtils {  

	/**
	 * 身份证号码识别
	 */
	public static String idCard(String imagePath) throws Exception {
		Mat mat = opencv_imgcodecs.imread(imagePath); // 原图
		return toCard(mat);

	}

	//页面中号码所在位置
	public static float x1 = 0.35f;
	public static float x2 = 1.0f;
	public static float y1 = 0.75f;
	public static float y2 = 0.91f;

	public static String toCard(Mat mat) throws IOException {
		// 1获取指定身份证号码区域
		List<Point2d> list = Stream
				.of(new Point2d(mat.cols() * x1, mat.rows() * y1), new Point2d(mat.cols() * x1, mat.rows() * y1),
						new Point2d(mat.cols() * x2, mat.rows() * y2), new Point2d(mat.cols() * x2, mat.rows() * y2))
				.collect(Collectors.toList());
		//2裁剪身份证号码区域图片
		Mat card = OpencvUtil.shear(mat, list);
		//3裁剪数字区域
		card = detectTextArea(card);  
		if(card == null){
			return "";
		}
		//5转为bufferImge
		if(ConfUtil.show){
			opencv_imgcodecs.imwrite(ConfUtil.stepLocal + File.separator + "card.png", card);
		} 
		BufferedImage nameBuffer = OpencvUtil.Mat2BufImg(card, ".png");
		//6使用tess4j识别
		if(ConfUtil.show){
			Thumbnails.of(nameBuffer).size(nameBuffer.getWidth(), nameBuffer.getHeight()).toFile(ConfUtil.stepLocal + File.separator +"cardImg.png");
		}
		String nameStr = OCRUtil.getImageMessage(nameBuffer, "chi_sim", false);
		String code = "";
		System.out.println("识别后："+nameStr);
		if (StringUtils.isNotBlank(nameStr)) {
			nameStr = nameStr.replace("\n", "");
			String codeX = CharMatcher.DIGIT.removeFrom(nameStr);
			code = CharMatcher.DIGIT.retainFrom(nameStr)
					+ (StringUtils.isNotBlank(codeX) ? ("X".equalsIgnoreCase(codeX.substring(0, 1)) ? "X" : "") : "");
		}
		System.out.println("处理后："+code);
		return code;
	}

	private static Mat detectTextArea(Mat src) {
		
		Mat srcMat = src.clone(); 
		Mat grayMat = new Mat(); 
		//1、图片处理
		//图片转灰度图
		opencv_imgproc.cvtColor(srcMat, grayMat, opencv_imgproc.COLOR_RGB2GRAY); 
		 
		//为了进行图片选择 
		/*
		 * reSrc = OpencvUtil.binary(grayMat);
		 * opencv_imgproc.medianBlur(grayMat, reSrc, 1);	
		opencv_imgcodecs.imwrite(saveStepFile + "medianBlur.jpg", reSrc); 
		opencv_imgproc.adaptiveThreshold(reSrc, reSrc, 255, opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
							// THRESH_BINARY 白底黑字
				opencv_imgproc.THRESH_BINARY_INV, 3, 10); */
	 
		
		// 高斯模糊 的原理(周边像素的平均值+正态分布的权重)
		opencv_imgproc.GaussianBlur(grayMat, grayMat, new Size(7, 7), 0, 0, opencv_core.BORDER_DEFAULT); 
		// 因为边缘部分的像素值是与旁边像素明显有区别的，所以对图片局部求极值，就可以得到整幅图片的边缘信息了
		grayMat = CoreFunc.Sobel(grayMat);
		 
		if(ConfUtil.show){
			opencv_imgcodecs.imwrite(ConfUtil.stepLocal + File.separator + "Sobel.jpg", grayMat);
		}
		

		opencv_imgproc.threshold(grayMat, grayMat, 0, 255, opencv_imgproc.THRESH_OTSU + opencv_imgproc.THRESH_BINARY);
		opencv_imgproc.medianBlur(grayMat, grayMat, 13);

		if(ConfUtil.show){
			opencv_imgcodecs.imwrite(ConfUtil.stepLocal + File.separator + "grayMat.jpg", grayMat);
		}
		 
		// 使用闭操作。对图像进行闭操作以后，可以看到车牌区域被连接成一个矩形装的区域。
		Mat element = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(5, 3));
		opencv_imgproc.morphologyEx(grayMat, grayMat, opencv_imgproc.MORPH_CLOSE, element);
 
		
		if(ConfUtil.show){
			opencv_imgcodecs.imwrite(ConfUtil.stepLocal + File.separator + "MORPH_CLOSE.jpg", grayMat);
		}
	 
		//轮廓提取  
		MatVector contoursList = new MatVector();
		Mat hierarchy = new Mat();
		opencv_imgproc.findContours(grayMat, contoursList, hierarchy, opencv_imgproc.RETR_EXTERNAL,
				opencv_imgproc.CHAIN_APPROX_SIMPLE); 
		
		Rect rect = null;
		//CvBox2D box = null;
		
		int minWidth = grayMat.cols() / 2;
		for(long i=0,total = contoursList.size();i<total;i++){
			rect = opencv_imgproc.boundingRect(contoursList.get(i)); 
			if(rect.width() < minWidth){ 
				rect = null;
				continue;
			} 
			//获取旋转角度
			/*box = opencv_imgproc.cvMinAreaRect2(opencv_core.cvMat(contoursList.get(i))); */
			
			break;
		} 
		
		if (rect != null) { 
			int x = rect.x();
			int y = rect.y();
			int w = rect.width();
			int h = rect.height();
			rect.x(tryXy(x,2));
			rect.y(tryXy(y,2));
			rect.width(w + tryValue(grayMat.cols(),x+w,3));
			rect.height(h + tryValue(grayMat.rows(),y+h,3));
			
			//根据方块旋转，颜色翻转
			/*grayMat = OpencvUtil.rotate3(new Mat(reSrc,rect), box.angle());
			opencv_core.bitwise_not(grayMat,grayMat);*/ 
		 
			return new Mat(srcMat,rect);
		}
		return null;
	} 
	
	private static int tryXy(int x,int down){
		int endV = x - down;
		while(endV < 0){
			endV++;
		} 
		return endV;
	}
	
	private static int tryValue(int maxL,int oldL,int addL){
		int endV = 0;
		do{
			endV = addL;
			addL--;
		}while(maxL < oldL + addL);
		
		return endV;
	}
	
}

package com.nbsl.idcard;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_ml.SVM;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.springframework.util.ResourceUtils;

import com.nbsl.cv.utils.CoreFunc;
import com.nbsl.cv.utils.OCRUtil;
import com.nbsl.cv.utils.OpencvUtil;

public class IdCardCvUtils {

	public static CHAR_SVM svmTrain;	
	//训练后的向量文件
	public static String svmXml;
	//临时文件保存路径
	public static String saveStepFile = "F:/face/";
	public static boolean saveStep = true;

	public static void main(String[] args) throws Exception {

		String text = IdCardCvUtils.getIdCardCode(ResourceUtils.getFile("classpath:test/5.jpg").getAbsolutePath());
		System.out.println(text);
	}
	/**
	 * 选择指定颜色
	 * @throws FileNotFoundException
	 */
	public static void selectColor() throws FileNotFoundException {
		Mat img = opencv_imgcodecs.imread(ResourceUtils.getFile("classpath:test/5.jpg").getAbsolutePath()); 
		Mat imgHSV = new Mat(img.size(), img.type()); 
		// RGB->HSV
		opencv_imgproc.cvtColor(img, imgHSV, opencv_imgproc.COLOR_BGR2HSV);
		opencv_imgcodecs.imwrite(saveStepFile + "HSV.jpg", imgHSV);

		Mat mask = new Mat();
		opencv_core.inRange(imgHSV, new Mat(new Scalar(0, 0, 0, 1)), new Mat(new Scalar(180, 255, 46, 1)), mask);
		opencv_imgcodecs.imwrite(saveStepFile + "HSV1.jpg", mask);
	}
	
	public static String getIdCardCode(String imagePath) throws IOException {  
		Mat rgbMat = opencv_imgcodecs.imread(imagePath); // 原图
		//截取身份证区域，并校正旋转角度
		Mat begin=rgbMat.clone();
		rgbMat = OpencvUtil.houghLinesP(begin,rgbMat);
		Rect rect = detectTextArea(rgbMat);
		String text = getCharText(rgbMat, rect);
		return text;
	}

	private static String getCharText(Mat srcMat, Rect rect) throws IOException {
		if (svmTrain == null) {
			svmTrain = new CHAR_SVM();
			SVM svm = SVM.load(ResourceUtils.getFile("classpath:model/svm.xml").getAbsolutePath());
			svmTrain.setSvm(svm);
		}
		Mat effective = new Mat(); // 身份证位置
		Mat charsGrayMat = new Mat();
		Mat hierarchy = new Mat();
		srcMat.copyTo(effective);

		Mat charsMat = new Mat(effective, rect);

		opencv_imgproc.cvtColor(charsMat, charsGrayMat, opencv_imgproc.COLOR_RGB2GRAY);// 灰度化

		opencv_imgproc.GaussianBlur(charsGrayMat, charsGrayMat, new Size(3, 3), 0, 0, opencv_core.BORDER_DEFAULT);

		double thresholdValue = CoreFunc.otsu(charsGrayMat) - 25;// 减50是去掉文字周边燥点
																	// 阴影覆盖;

		opencv_imgproc.threshold(charsGrayMat, charsGrayMat, thresholdValue, 255, opencv_imgproc.THRESH_BINARY_INV);
		opencv_imgproc.medianBlur(charsGrayMat, charsGrayMat, 3);

		// opencv_imgcodecs.imwrite("temp/charsMat.jpg", charsGrayMat);
		opencv_imgcodecs.imwrite(saveStepFile +"charsMat.jpg", charsGrayMat);  
		

		MatVector charContours = new MatVector();
		opencv_imgproc.findContours(charsGrayMat, charContours, hierarchy, opencv_imgproc.RETR_EXTERNAL,
				opencv_imgproc.CHAIN_APPROX_NONE);

		Vector<Rect> vecRect = new Vector<Rect>();

		for (int k = 0; k < charContours.size(); k++) {
			Rect mr = opencv_imgproc.boundingRect(charContours.get(k));
			if (verifySizes(mr)) {
				vecRect.add(mr);
			}

		}

		Vector<Rect> sortedRect = CoreFunc.SortRect(vecRect);
		int x = 0;
		StringBuffer idcar = new StringBuffer();
		for (Rect rectSor : sortedRect) {
			Mat specMat = new Mat(charsGrayMat, rectSor);
			specMat = preprocessChar(specMat);
			// opencv_imgcodecs.imwrite("temp/debug_specMat" + x + ".jpg",
			// specMat);
			opencv_imgcodecs.imwrite(saveStepFile +"debug_specMat" + x + ".jpg", specMat);
			x++; 
			String charText = svmTrain.svmFind(specMat);
			idcar.append(charText); 
			
		}
		return idcar.toString();

	}

	private static Rect detectTextArea(Mat srcMat) {

		Mat grayMat = new Mat(); // 灰度图

		opencv_imgproc.cvtColor(srcMat, grayMat, opencv_imgproc.COLOR_RGB2GRAY);// 灰度化
		// 高斯模糊 的原理(周边像素的平均值+正态分布的权重
		opencv_imgproc.GaussianBlur(grayMat, grayMat, new Size(3, 3), 0, 0, opencv_core.BORDER_DEFAULT);
		// 因为边缘部分的像素值是与旁边像素明显有区别的，所以对图片局部求极值，就可以得到整幅图片的边缘信息了
		grayMat = CoreFunc.Sobel(grayMat);
		// opencv_imgcodecs.imwrite("temp/Sobel.jpg", grayMat);
		opencv_imgcodecs.imwrite(saveStepFile +"Sobel.jpg", grayMat);

		opencv_imgproc.threshold(grayMat, grayMat, 90, 255, opencv_imgproc.THRESH_OTSU + opencv_imgproc.THRESH_BINARY);
		opencv_imgproc.medianBlur(grayMat, grayMat, 3);

		// opencv_imgcodecs.imwrite("temp/grayMat.jpg", grayMat);
		opencv_imgcodecs.imwrite(saveStepFile +"grayMat.jpg", grayMat);

		// 使用闭操作。对图像进行闭操作以后，可以看到车牌区域被连接成一个矩形装的区域。
		Rect rect = null;
		for (int step = 20; step < 60;) {
			// System.out.println(step);
			Mat element = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(step, 3));
			opencv_imgproc.morphologyEx(grayMat, grayMat, opencv_imgproc.MORPH_CLOSE, element);

			// opencv_imgcodecs.imwrite("temp/MORPH_CLOSE.jpg", grayMat);
			opencv_imgcodecs.imwrite(saveStepFile +"MORPH_CLOSE.jpg", grayMat);

			/**
			 * 轮廓提取()
			 */
			MatVector contoursList = new MatVector();
			Mat hierarchy = new Mat();
			opencv_imgproc.findContours(grayMat, contoursList, hierarchy, opencv_imgproc.RETR_EXTERNAL,
					opencv_imgproc.CHAIN_APPROX_SIMPLE);

			for (int i = 0; i < contoursList.size(); i++) {
				Rect tempRect = opencv_imgproc.boundingRect(contoursList.get(i));
				if (grayMat.arrayHeight() / tempRect.height() < 5) {
					continue;
				}
				int r = tempRect.width() / tempRect.height();
				if (r < 1) {
					r = tempRect.height() / tempRect.width();
				}
				if (tempRect.width() > 10 && tempRect.height() > 10 && grayMat.arrayWidth() != tempRect.width()
						&& r > 10 && r < 20) {
					if (rect == null) {
						rect = tempRect;
						continue;
					}

					if (tempRect.y() > rect.y()) {
						rect = tempRect;
					}
				}
			}
			if (rect != null) {
				// opencv_imgcodecs.imwrite("temp/rectMat.jpg", new
				// Mat(grayMat,rect));
				opencv_imgcodecs.imwrite(saveStepFile +"rectMat.jpg", new Mat(grayMat, rect));
				return rect;
			}
			step = step + 5;
		}
		return rect;

	}

	/**
	 * 字符预处理: 统一每个字符的大小
	 * 
	 * @param in
	 * @return
	 */
	public static Mat preprocessChar(Mat in) {
		int h = in.rows();
		int w = in.cols();
		int charSize = 20;
		Mat transformMat = Mat.eye(2, 3, opencv_core.CV_32F).asMat();
		int m = Math.max(w, h);
		FloatIndexer transIndex = transformMat.createIndexer();
		transIndex.put(0, 2, ((m - w) / 2f));
		transIndex.put(1, 2, ((m - h) / 2f));

		Mat warpImage = new Mat(m, m, in.type());

		opencv_imgproc.warpAffine(in, warpImage, transformMat, warpImage.size(), opencv_imgproc.INTER_LINEAR,
				opencv_core.BORDER_CONSTANT, new Scalar(0));

		Mat out = new Mat();
		opencv_imgproc.resize(warpImage, out, new Size(charSize, charSize));

		return out;
	}

	public static boolean verifySizes(Rect mr) {
		if (mr.size().height() < 5 || mr.size().width() < 5) {
			return false;
		}
		return true;
	}

}

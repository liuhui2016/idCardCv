package com.nbsl.cv.utils;

import static java.lang.StrictMath.abs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Point2d;
import org.bytedeco.javacpp.opencv_core.Point2f;
import org.bytedeco.javacpp.opencv_core.MatExpr;
import org.bytedeco.javacpp.opencv_core.PointVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.RotatedRect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
/**
 * Created by Administrator on 2016/12/7.
 */
public class CannyUtils {

 
    
    
    
    
    public static  Mat  canny( Mat inImg ) {
    
        if ( inImg.empty()) {
            System.out.println("Please check the path of input image!");
            return null;
        }
    	Mat srcImg = new Mat();
    	 inImg.copyTo(srcImg);
        final int imgRows = srcImg.rows();
        final int imgCols = srcImg.cols();

        // Step1: Denoise
        opencv_imgproc.GaussianBlur(srcImg, srcImg, new Size(3, 3), opencv_core.BORDER_DEFAULT);

        // Step2: Convert to gray
        Mat grayImg = Mat.zeros(imgRows, imgCols, opencv_core.CV_8UC1).asMat();
        if (srcImg.channels() == 3) {
            opencv_imgproc.cvtColor(srcImg, grayImg, opencv_imgproc.COLOR_BGR2GRAY);
        }
        opencv_imgproc.medianBlur(grayImg, grayImg, 3);

        // Step3: Binary
        int maskRoiX = (int)(imgCols/12.0);
        int maskRoiY = (int)(imgRows/8.0);
        int maskRoiW = (int)(10/12.0*imgCols);
        int maskRoiH = (int)(6/8.0*imgRows);
        Rect maskRoi = new Rect(maskRoiX, maskRoiY, maskRoiW, maskRoiH);
        Mat maskSrc = new Mat(grayImg, maskRoi);

       
        Map<String, Double> paramMap = FindAdaptiveThreshold(maskSrc, 3, 0.80);
        double thCannyLow = paramMap.get("cannyLowTh");
        double thCannyHigh = paramMap.get("cannyHighTh"); 

        Mat maskImg = Mat.zeros(imgRows, imgCols, opencv_core.CV_8UC1).asMat();
        opencv_imgproc.Canny(grayImg, maskImg, thCannyLow, thCannyHigh, 3, false);

        System.out.println("Canny threshold lowth = " + thCannyLow + "\thighth = " + thCannyHigh);
        return maskImg;
    }

    



    /**
     * Find thresholds for Canny detector.
     * @param src input image.
     * @param aperture_size the window size for Canny detector.
     * @param PercentOfPixelsNotEdges the precision of pixels which not belong to edge.
     * @return 
     */
    public  static Map<String, Double> FindAdaptiveThreshold(Mat src, int aperture_size, double PercentOfPixelsNotEdges)
    {
        Mat dx = new Mat(src.rows(), src.cols(), opencv_core.CV_16SC1);
        Mat dy = new Mat(src.rows(), src.cols(), opencv_core.CV_16SC1);
        opencv_imgproc.Sobel(src, dx, opencv_core.CV_16S, 1, 0, aperture_size, 1, 0, opencv_core.BORDER_DEFAULT);
        opencv_imgproc.Sobel(src, dy, opencv_core.CV_16S, 0, 1, aperture_size, 1, 0, opencv_core.BORDER_DEFAULT);
       return  _FindApdaptiveThreshold(dx, dy, PercentOfPixelsNotEdges);
    }


    /**
     *  Find thresholds for Canny detector (core function).
     * @param dx gradient of x orientation.
     * @param dy gradient of y orientation.
     * @param PercentOfPixelsNotEdges the precision of pixels which not belong to edge.
     */
    private static Map<String,Double>  _FindApdaptiveThreshold(Mat dx, Mat dy, double PercentOfPixelsNotEdges)
    {
    	Map<String,Double> resultMap = new HashMap<String,Double>();
    	 
    	     double m_cannyLowTh;  /* !< the lower threshold for Canny. */
    	      double m_cannyHighTh; /* !< the higher threshold for Canyy. */

        int i, j;
        Size size = dx.size();
        Mat imge = Mat.zeros(size, opencv_core.CV_32FC1).asMat();
        DoubleIndexer imgeIndex = imge.createIndexer();
        // Compute the strong of edge and store the result in image
        DoubleIndexer dxIndex = dx.createIndexer();
        DoubleIndexer dyIndex = dy.createIndexer();
        double maxv = 0.0, data;
        for (i = 0; i < size.height(); i++) {
            for (j = 0; j < size.width(); j++) {
                /*data = abs(dxIndex.get(i, j)[0]) + abs(dyIndex.get(i, j)[0]);*/
                data = abs(dxIndex.get(i, j)) + abs(dyIndex.get(i, j));
                imgeIndex.put(i, j, data);
                maxv = maxv < data ? data : maxv;
            }
        }
        if (0.0 == maxv) {
            m_cannyLowTh = 0.0;
            m_cannyHighTh = 0.0;
            resultMap.put("cannyLowTh", m_cannyLowTh);
            resultMap.put("cannyHighTh", m_cannyHighTh);
            return resultMap;
        }

        // Compute histogram
        int histSize = 256;
        histSize = histSize > (int)maxv ? (int)maxv : histSize;
        IntPointer hist_size = new IntPointer(histSize);
        FloatPointer ranges = new FloatPointer(0, (float) maxv);
        IntPointer channels = new IntPointer(0);
        // Compute hist
        Mat hist = new Mat();
        List<Mat> images = new ArrayList<>();
        images.add(imge);
        opencv_imgproc.calcHist(images.subList(0, 1).get(0),1, channels, new Mat(), hist,1, hist_size, ranges,true, false);

        double sum = 0.0;
        int icount = hist.rows();
        DoubleIndexer histIndex = hist.createIndexer();
        double total = size.height() * size.width() * PercentOfPixelsNotEdges;
        for (i = 0; i < icount; i++) {
            sum += histIndex.get(i, 0);
            if (sum > total) {
                break;
            }
        }
        // Compute high and low threshold of Canny
        m_cannyLowTh = (i + 1) * maxv / histSize;
        if(0.0 == m_cannyLowTh) {
            m_cannyHighTh = 0.0;
        } else {
            m_cannyHighTh = 2.5 * m_cannyLowTh; 
            if (m_cannyHighTh > 255.0) {
                m_cannyHighTh = 255.0;
            }
        }
        resultMap.put("cannyLowTh", m_cannyLowTh);
        resultMap.put("cannyHighTh", m_cannyHighTh);
        return resultMap;
    }
}
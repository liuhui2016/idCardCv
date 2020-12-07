package com.nbsl.cv.utils;

import java.util.Vector;
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
 * 核心
 * @author eguid
 * 
 */
public class CoreFunc {
    

    public enum Direction {
        UNKNOWN, VERTICAL, HORIZONTAL
    }

    
    /**
	 * Get the Sobel Mat of input image!
	 * 
	 * @param image
	 *            The input image.
	 * @return The Sobel Mat image of input image.
	 */
	public static Mat Sobel(Mat image) {
		if (image.empty()) {
			System.out.println("Please check the input image!");
			return image;
		}

		Mat gray = image.clone();
		if (3 == gray.channels()) {
			opencv_imgproc.cvtColor(gray, gray, opencv_imgproc.COLOR_BGR2GRAY);
		}

		Mat grad = new Mat();
		Mat grad_x = new Mat();
		Mat grad_y = new Mat();
		Mat abs_grad_x = new Mat();
		Mat abs_grad_y = new Mat();
		final int scharr_scale = 1;
		final int scharr_delta = 0;
		final int scharr_ddpeth = opencv_core.CV_16S;
		opencv_imgproc.Sobel(gray, grad_x, scharr_ddpeth, 1, 0, 3, scharr_scale, scharr_delta, opencv_core.BORDER_DEFAULT);
		opencv_imgproc.Sobel(gray, grad_y, scharr_ddpeth, 0, 1, 3, scharr_scale, scharr_delta, opencv_core.BORDER_DEFAULT);
		opencv_core.convertScaleAbs(grad_x, abs_grad_x);
		opencv_core.convertScaleAbs(grad_y, abs_grad_y);
		opencv_core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad);

		return grad;
	}
    
     public static  double otsu(Mat image) {
    	int threshold=0;
    	double maxVariance = 0;
    	double w0=0,w1=0;//前景与背景像素点所占比例
    	double u0=0,u1=0;//前景与背景像素值平均灰度
    	double[] histogram=new double[256];
    	double Num=image.cols()*image.rows();
    	UByteRawIndexer imageIndex = image.createIndexer();
    	//统计256个bin，每个bin像素的个数
    	for(int i=0;i<image.rows();i++){
    	    for(int j=0;j<image.cols();j++){
    	    	double record = imageIndex.get(i, j);
    			histogram[(int)record]++; //cout<<"Histogram[data[i*image.step+j]]++:;"<<histogram[int(*p++)]++<<endl; 
    		}
    	}
    	//前景像素统计
    	for(int i=0;i<255;i++){
    		w0=0;
    		w1=0;
    		u0=0;
    		u1=0;
    		for(int j=0;j<=i;j++){
    			w0=w0+histogram[j];//以i为阈值，统计前景像素个数
    			u0=u0+j*histogram[j];//以i为阈值，统计前景像素灰度总和
    		}
    		w0=w0/Num;u0=u0/w0;

    	//背景像素统计
    		for(int j=i+1;j<=255;j++){
    			w1=w1+histogram[j];//以i为阈值，统计前景像素个数
    			u1=u1+j*histogram[j];//以i为阈值，统计前景像素灰度总和
    		}
    		w1=w1/Num;u1=u1/w1;
    		double variance=w0*w1*(u1-u0)*(u1-u0); //当前类间方差计算
    		 if(variance > maxVariance)     
            {      
                maxVariance = variance;      
                threshold = i;      
            }
    	}
    	//cout<<"threshold:"<<threshold<<endl;
    	return threshold;
    	}

    
    public static float[] projectedHistogram(final Mat img, Direction direction) {
        int sz = 0;
        switch (direction) {
        case HORIZONTAL:
            sz = img.rows();
            break;
        case VERTICAL:
            sz = img.cols();
            break;
        default:
            break;
        }
        // 统计这一行或一列中，非零元素的个数，并保存到nonZeroMat中
        float[] nonZeroMat = new float[sz];
		opencv_core.extractChannel(img, img, 0);
        for (int j = 0; j < sz; j++) {
            Mat data = (direction == Direction.HORIZONTAL) ? img.row(j) : img.col(j);
            int count = opencv_core.countNonZero(data);
            nonZeroMat[j] = count;
        }

        // Normalize histogram
        float max = 0;
        for (int j = 0; j < nonZeroMat.length; ++j) {
            max = Math.max(max, nonZeroMat[j]);
        }

        if (max > 0) {
            for (int j = 0; j < nonZeroMat.length; ++j) {
                nonZeroMat[j] /= max;
            }
        }

        return nonZeroMat;
    }
	/**
	 * 将Rect按位置从左到右进行排序
	 * 
	 * @param vecRect
	 * @return
	 */
	public static Vector<Rect> SortRect(final Vector<Rect> vecRect) {
		Vector<Rect> out = new Vector<Rect>();
		Vector<Integer> orderIndex = new Vector<Integer>();
		Vector<Integer> xpositions = new Vector<Integer>();
		for (int i = 0; i < vecRect.size(); ++i) {
			orderIndex.add(i);
			xpositions.add(vecRect.get(i).x());
		}

		float min = xpositions.get(0);
		int minIdx;
		for (int i = 0; i < xpositions.size(); ++i) {
			min = xpositions.get(i);
			minIdx = i;
			for (int j = i; j < xpositions.size(); ++j) {
				if (xpositions.get(j) < min) {
					min = xpositions.get(j);
					minIdx = j;
				}
			}
			int aux_i = orderIndex.get(i);
			int aux_min = orderIndex.get(minIdx);
			orderIndex.remove(i);
			orderIndex.insertElementAt(aux_min, i);
			orderIndex.remove(minIdx);
			orderIndex.insertElementAt(aux_i, minIdx);

			float aux_xi = xpositions.get(i);
			float aux_xmin = xpositions.get(minIdx);
			xpositions.remove(i);
			xpositions.insertElementAt((int) aux_xmin, i);
			xpositions.remove(minIdx);
			xpositions.insertElementAt((int) aux_xi, minIdx);
		}

		for (int i = 0; i < orderIndex.size(); i++)
			out.add(vecRect.get(orderIndex.get(i)));

		return out;
	}

    /**
     * Assign values to feature
     * <p>
     * 样本特征为水平、垂直直方图和低分辨率图像所组成的矢量
     * 
     * @param in
     * @param sizeData - 低分辨率图像size = sizeData*sizeData, 可以为0
     * @return
     */
    public static Mat features(final Mat in, final int sizeData) {
        float[] vhist = projectedHistogram(in, Direction.VERTICAL);
        float[] hhist = projectedHistogram(in, Direction.HORIZONTAL);
        Mat lowData = new Mat();
        if (sizeData > 0) {
            opencv_imgproc.resize(in, lowData, new Size(sizeData, sizeData));
        }

        int numCols = vhist.length + hhist.length + lowData.cols() * lowData.rows();
        Mat out = Mat.zeros(1, numCols, opencv_core.CV_32F).asMat();
		FloatIndexer outIndex = out.createIndexer();
		UByteRawIndexer lowIndex = lowData.createIndexer();
        //FloatIndexer idx = out.createIndexer();
        int j = 0;
        for (int i = 0; i < vhist.length; ++i, ++j) {
			outIndex.put(0, j, vhist[i]);
           // idx.put(0, j, vhist[i]);
        }
        for (int i = 0; i < hhist.length; ++i, ++j) {
			outIndex.put(0, j, hhist[i]);
           // idx.put(0, j, hhist[i]);
        }
        for (int x = 0; x < lowData.cols(); x++) {
            for (int y = 0; y < lowData.rows(); y++, ++j) {
              //  float val = lowData.ptr(x, y).get() & 0xFF;
            	float val = (float)(lowIndex.get(x, y));
               // idx.put(0, j, val);
				outIndex.put(0, j, val);
            }
        }

        return out;
    }

}

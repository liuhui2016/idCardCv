package com.nbsl.idcard;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.TermCriteria;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_ml.ANN_MLP;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.IntIndexer;
import org.opencv.ml.Ml;
import org.springframework.util.ResourceUtils;

import com.nbsl.cv.utils.CoreFunc;
import com.nbsl.cv.utils.FileUtil;


public class CHAR_ANN {  
	public static String trainImages;
    public static String annXml;
    static{
    	try {
    		trainImages= ResourceUtils.getFile("classpath:data/chars2").getAbsolutePath();
    		annXml = ResourceUtils.getFile("classpath:model/ann.xml").getAbsolutePath();
		} catch (Exception e) {
			// TODO: handle exception
		} 
    } 
	
    private ANN_MLP ann;

    private  int sizeData = 10;
    // 中国车牌
    private final char strCharacters[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'X'/*, 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H',  没有I 
            'J', 'K', 'L', 'M', 'N',  没有O 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'*/ };
    private final int numCharacter = 11; /* 没有I和0,10个数字与24个英文字符之和 */

    public void annTrain(Mat trainData, Mat classes) {
		Mat layerSize=new Mat(1,4,opencv_core.CV_32SC1);
        IntIndexer layerIndex = layerSize.createIndexer();
        layerIndex.put(0,0, trainData.cols());
        layerIndex.put(0,1, 40);
        layerIndex.put(0,2, 20);
        layerIndex.put(0,3, numCharacter);
		getAnn().setLayerSizes(layerSize);

		
		getAnn().setTrainMethod(ANN_MLP.BACKPROP);
     /*   getAnn().setBackpropWeightScale(1);
        getAnn().setBackpropMomentumScale(1);*/
		
	  	TermCriteria criteria=new TermCriteria(TermCriteria.MAX_ITER|TermCriteria.EPS, 300, 0.001);
		getAnn().setTermCriteria(criteria);
		getAnn().setActivationFunction(ANN_MLP.SIGMOID_SYM/*,1,1*/);

        IntIndexer classIndex = classes.createIndexer();

		Mat trainClasses = new Mat(trainData.rows(), numCharacter, opencv_core.CV_32FC1);
        FloatIndexer flaotIndex = trainClasses.createIndexer();
	        for (int i = 0; i < trainClasses.rows(); i++) {
	            for (int k = 0; k < trainClasses.cols(); k++) {
	                if (k == classIndex.get(0,i))
                        flaotIndex.put(i, k,(float)1.0);
	                else
                        flaotIndex.put(i, k,(float)0.0);
	            }
	        }
	boolean r=getAnn().train(trainData, Ml.ROW_SAMPLE, trainClasses);
    	 System.out.println("r:"+r);
    	  getAnn().save(annXml);
          //System.out.println("training result: " + success);  
    }

    public Map<String,Mat> saveTrainData() {
    	Map<String,Mat> result = new HashMap<String,Mat>();
        Mat classes = new Mat();
        Mat trainingData = new Mat();

        Vector<Integer> trainingLabels = new Vector<Integer>();
        String path = trainImages;
        //int count = 1;
        for (int i = 0; i < numCharacter; i++) {
            String str = path + '/' + strCharacters[i];
            Vector<String> files = new Vector<String>();
            FileUtil.getFiles(str, files);

            int size = (int) files.size();
            for (int j = 0; j < size; j++) {
                Mat img = opencv_imgcodecs.imread(files.get(j), 0);
                Mat f10 = CoreFunc.features(img, sizeData);
                trainingData.push_back(f10);
                trainingLabels.add(i); // 每一幅字符图片所对应的字符类别索引下标
                /*System.out.println(count);
                count++;*/
            }
        }


        trainingData.convertTo(trainingData, opencv_core.CV_32FC1);
        Mat classTemMat = new Mat(1,trainingLabels.size(),opencv_core.CV_32FC1);
        IntIndexer temIndex = classTemMat.createIndexer();
        for (int i = 0; i < trainingLabels.size(); ++i){
            temIndex.put(0, i, trainingLabels.get(i).intValue());
        }

        classTemMat.copyTo(classes);
        result.put("trainingData", trainingData);
        result.put("classes", classes);
       // System.out.println("End saveTrainData");
        return result;
    }
    public void saveModel( Map<String, Mat> dataMap) {


         Mat trainingData =dataMap.get("trainingData");
         Mat classes = dataMap.get("classes");
         annTrain(trainingData, classes);


     }

    public static void main(String[] args){

        CHAR_ANN annTrain = new CHAR_ANN();
        StringBuffer idcar = new StringBuffer();
        for (int i = 0; i <= 17; i++) {
       	   Mat img = opencv_imgcodecs.imread("C:/Users/Administrator/Desktop/tt/test/debug_specMat"+i+".jpg");
       	    String charText = annTrain.annFind(img);
            idcar.append(charText);

		}
        System.out.println("idcar:\n" + idcar.toString());

    }
    public   String annFind(Mat charMat) {
        if(!getAnn().isTrained()){
        	System.out.println("train....");
        	 Map<String, Mat> resultMap = saveTrainData();
             saveModel(resultMap);
        }
        Mat f = CoreFunc.features(charMat, sizeData);
        
        //find the nearest neighbours of test data  
       float result = getAnn().predict(f);
        String charText = String.valueOf(strCharacters[(int) result]);
        return charText;
    }

	public ANN_MLP getAnn() {
		if(ann==null){
			ann	=ANN_MLP.create();
		}
		return ann;
	}

	public void setAnn(ANN_MLP ann) {
		this.ann = ann;
	}
}  
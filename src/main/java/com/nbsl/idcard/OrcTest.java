package com.nbsl.idcard;

import static com.nbsl.cv.utils.OpencvUtil.shear;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point2d;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.springframework.util.ResourceUtils;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;

import com.nbsl.cv.utils.CoreFunc;
import com.nbsl.cv.utils.OCRUtil;
import com.nbsl.cv.utils.OpencvUtil;

public class OrcTest {

    public static void main(String[] args) throws  Exception{
        String path=ResourceUtils.getFile("classpath:test/5.jpg").getAbsolutePath();
        Mat mat= opencv_imgcodecs.imread(path);
        idCard(mat);
        // card(mat);
    }
    /**
     * 身份证正面识别
     */
    public static void idCard(Mat mat) throws  Exception{
    	
        Mat begin=mat.clone();
        //截取身份证区域，并校正旋转角度
        mat = OpencvUtil.houghLinesP(begin,mat);
        
        opencv_imgcodecs.imwrite("F:/face/houghLinesP.jpg", mat);
        
        //循环进行人脸识别,校正图片方向
        Rect face=OpencvUtil.faceLocation(mat);
       
        //灰度
     
 		mat = OpencvUtil.gray(mat); 
 		// 二值化 此处绝定图片的清晰度 
 		opencv_imgproc.adaptiveThreshold(mat, mat, 255, opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
 				opencv_imgproc.THRESH_BINARY_INV, 25, 10);  
 		// 腐蚀  去除背景图片
 		mat = OpencvUtil.erode(mat, 1);  
        //膨胀
        //mat=OpencvUtil.dilate(mat,1); 
       
        //降噪
     	//mat = OpencvUtil.navieRemoveNoise(mat, 1);  
        //获取身份证
     	if(face != null){
     		String card=toCard(mat,face);
     		System.out.print("身份证号是："+card);
     	}
        
    }
    public static String toCard(Mat mat,Rect rect){
    	 Point2d point1=new Point2d(mat.cols()*0.40,mat.rows()*0.75);
         Point2d point2=new Point2d(mat.cols()*0.40, mat.rows()*0.75);
         Point2d point3=new Point2d(mat.cols()*0.90, mat.rows()*0.91);
         Point2d point4=new Point2d(mat.cols()*0.90, mat.rows()*0.91);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat card= shear(mat,list);
        
        
       Mat hierarchy = new Mat();
        MatVector charContours = new MatVector();
		opencv_imgproc.findContours(card, charContours, hierarchy, opencv_imgproc.RETR_EXTERNAL,
				opencv_imgproc.CHAIN_APPROX_NONE);

		Vector<Rect> vecRect = new Vector<Rect>();

		for (int k = 0; k < charContours.size(); k++) {
			Rect mr = opencv_imgproc.boundingRect(charContours.get(k));
			if (IdCardCvUtils.verifySizes(mr)) {
				vecRect.add(mr);
			}

		}

		Vector<Rect> sortedRect = CoreFunc.SortRect(vecRect);
		int x = 0;
		StringBuffer idcar = new StringBuffer();
		for (Rect rectSor : sortedRect) {
			Mat specMat = new Mat(card, rectSor);
			specMat = IdCardCvUtils.preprocessChar(specMat);
			// opencv_imgcodecs.imwrite("temp/debug_specMat" + x + ".jpg",
			// specMat);
			opencv_imgcodecs.imwrite("F:/face/debug_specMat" + x + ".jpg", specMat);
			x++; 
			/*String charText = svmTrain.svmFind(specMat);
			idcar.append(charText); */
			
		}  
        
        //原有的
        card=OpencvUtil.drawContours(card,50);
        opencv_imgcodecs.imwrite("F:/face/card.jpg", card);
        BufferedImage cardBuffer=OpencvUtil.Mat2BufImg(card,".jpg");
        return OCRUtil.getImageMessage(cardBuffer,"eng",false)+"\n";
    }
    /**
     * 身份证反面识别
     */
    public static void cardDown(Mat mat){
        //灰度
        mat=OpencvUtil.gray(mat);
        //二值化
        mat=OpencvUtil.binary(mat);
        //腐蚀
        mat=OpencvUtil.erode(mat,3);
        //膨胀
        mat=OpencvUtil.dilate(mat,3);
 
        //检测是否有居民身份证字体，若有为正向，若没有则旋转图片
        for(int i=0;i<4;i++){
            String temp=temp(mat);
            if(!temp.contains("居")&&!temp.contains("民")){
                mat= OpencvUtil.rotate3(mat,90);
            }else{
                break;
            }
        }
 
        opencv_imgcodecs.imwrite("F:/face/result.jpg", mat);
        String organization=organization (mat);
        System.out.print("签发机关是："+organization);
 
        String time=time (mat);
        System.out.print("有效期限是："+time);
    }
 
    public static String temp (Mat mat){
        Point2d point1 = new Point2d(mat.cols() * 0.30, mat.rows() * 0.25);
        Point2d point2 = new Point2d(mat.cols() * 0.30, mat.rows() * 0.25);
        Point2d point3 = new Point2d(mat.cols() * 0.90, mat.rows() * 0.45);
        Point2d point4 = new Point2d(mat.cols() * 0.90, mat.rows() * 0.45);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat temp= OpencvUtil.shear(mat,list);

        MatVector nameContours=OpencvUtil.findContours(temp);
        for (int i = 0; i < nameContours.size(); i++)
        {
            double area=OpencvUtil.area(nameContours.get(i));
            if(area<100){
                opencv_imgproc.drawContours(temp, nameContours, i, new Scalar( 0, 0));
            }
        }
        opencv_imgcodecs.imwrite("F:/face/temp.jpg", temp);
        BufferedImage nameBuffer=OpencvUtil.Mat2BufImg(temp,".jpg");
        String nameStr=OCRUtil.getImageMessage(nameBuffer,"chi_sim",true);
        nameStr=nameStr.replace("\n","");
        return nameStr;
    }
 
    public static String organization (Mat mat){
        Point2d point1 = new Point2d( (mat.cols() * 0.36), mat.rows() * 0.68);
        Point2d point2 = new Point2d( (mat.cols() * 0.36), mat.rows() * 0.68);
        Point2d point3 = new Point2d( (mat.cols() * 0.80), mat.rows() * 0.80);
        Point2d point4 = new Point2d( (mat.cols() * 0.80), mat.rows() * 0.80);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat name= OpencvUtil.shear(mat,list);

        MatVector nameContours=OpencvUtil.findContours(name);
        for (int i = 0; i < nameContours.size(); i++)
        {
            double area=OpencvUtil.area(nameContours.get(i));
            if(area<100){
                opencv_imgproc.drawContours(name, nameContours, i, new Scalar( 0, 0));
            }
        }
        opencv_imgcodecs.imwrite("F:/face/organization.jpg", name);
        BufferedImage nameBuffer=OpencvUtil.Mat2BufImg(name,".jpg");
        String nameStr=OCRUtil.getImageMessage(nameBuffer,"chi_sim",true);
        nameStr=nameStr.replace("\n","");
        return nameStr+"\n";
    }
 
    public static String time (Mat mat){
        Point2d point1 = new Point2d( (mat.cols() * 0.38), mat.rows() * 0.82);
        Point2d point2=new Point2d( (mat.cols()*0.38), mat.rows()*0.82);
        Point2d point3=new Point2d( (mat.cols()*0.85), mat.rows()*0.92);
        Point2d point4=new Point2d( (mat.cols()*0.85), mat.rows()*0.92);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat time= shear(mat,list);

        MatVector timeContours=OpencvUtil.findContours(time);
        for (int i = 0; i < timeContours.size(); i++)
        {
            double area=OpencvUtil.area(timeContours.get(i));
            if(area<100){
                opencv_imgproc.drawContours(time, timeContours, i, new Scalar( 0, 0));
            }
        }
        opencv_imgcodecs.imwrite("F:/face/time.jpg", time);
 
        //起始日期
        Point2d startPoint2d1=new Point2d(0,0);
        Point2d startPoint2d2=new Point2d(0,time.rows());
        Point2d startPoint2d3=new Point2d((time.cols()*0.47),0);
        Point2d startPoint2d4=new Point2d((time.cols()*0.47),time.rows());
        List<Point2d> startList=new ArrayList<>();
        startList.add(startPoint2d1);
        startList.add(startPoint2d2);
        startList.add(startPoint2d3);
        startList.add(startPoint2d4);
        Mat start= shear(time,startList);
        opencv_imgcodecs.imwrite("F:/face/start.jpg", start);
        BufferedImage yearBuffer=OpencvUtil.Mat2BufImg(start,".jpg");
        String startStr=OCRUtil.getImageMessage(yearBuffer,"eng",true);
        startStr=startStr.replace("-","");
        startStr=startStr.replace(" ","");
        startStr=startStr.replace("\n","");
 
        //截止日期
        Point2d endPoint2d1=new Point2d((time.cols()*0.47),0);
        Point2d endPoint2d2=new Point2d((time.cols()*0.47),time.rows());
        Point2d endPoint2d3=new Point2d(time.cols(),0);
        Point2d endPoint2d4=new Point2d(time.cols(),time.rows());
        List<Point2d> endList=new ArrayList<>();
        endList.add(endPoint2d1);
        endList.add(endPoint2d2);
        endList.add(endPoint2d3);
        endList.add(endPoint2d4);
        Mat end= shear(time,endList);
        opencv_imgcodecs.imwrite("F:/face/end.jpg", end);
        BufferedImage endBuffer=OpencvUtil.Mat2BufImg(end,".jpg");
        String endStr=OCRUtil.getImageMessage(endBuffer,"chi_sim",true);
        if(!endStr.contains("长")&&!endStr.contains("期")){
            endStr=OCRUtil.getImageMessage(endBuffer,"eng",true);
            endStr=endStr.replace("-","");
            endStr=endStr.replace(" ","");
        }
 
        return startStr+"-"+endStr;
    }
 
    /**
     * 身份证正面识别
     */
    public static void cardUp (Mat mat) throws  Exception{
    	
        Mat begin=mat.clone();
        //截取身份证区域，并校正旋转角度
        mat = OpencvUtil.houghLinesP(begin,mat);
        
        opencv_imgcodecs.imwrite("F:/face/houghLinesP.jpg", mat);
        
        //循环进行人脸识别,校正图片方向
        Mat face=OpencvUtil.faceLoop(mat);
        opencv_imgcodecs.imwrite("F:/face/face.jpg", face);
        //灰度
        mat=OpencvUtil.flow(mat);
        opencv_imgcodecs.imwrite("F:/face/1.jpg", mat);
        //膨胀
        //mat=OpencvUtil.dilate(mat,1); 
       
        //降噪
     	mat = OpencvUtil.navieRemoveNoise(mat, 1);
     	//mat = OpencvUtil.navieRemoveNoiseBack(mat, 1);
     	
       
        //获取名称
        String name=name(mat);
        System.out.print("姓名是："+name);
        //获取性别
        String sex=sex(mat);
        System.out.print("性别是："+sex);
 
        //获取民族
        String nation=nation(mat);
        System.out.print("民族是："+nation);
 
        //获取出生日期
        String birthday=birthday(mat);
        System.out.print("出生日期是："+birthday);
 
        //获取住址
        String address=address(mat);
        System.out.print("住址是："+address);
 
        //获取身份证
        String card=card(mat);
        System.out.print("身份证号是："+card);
    }
 
    public static String name(Mat mat){

        Point2d point1=new Point2d(mat.cols()*0.18, mat.rows()*0.11);
        Point2d point2=new Point2d(mat.cols()*0.18, mat.rows()*0.24);
        Point2d point3=new Point2d(mat.cols()*0.4, mat.rows()*0.11);
        Point2d point4=new Point2d(mat.cols()*0.4, mat.rows()*0.24);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat name= shear(mat,list);
        name=OpencvUtil.drawContours(name,50);
        opencv_imgcodecs.imwrite("F:/face/name.jpg", name);
        BufferedImage nameBuffer=OpencvUtil.Mat2BufImg(name,".jpg");
        String nameStr=OCRUtil.getImageMessage(nameBuffer,"chi_sim",true);
        nameStr=nameStr.replace("\n","");
        return nameStr+"\n";
    }
 
    public static String sex(Mat mat){
        Point2d point1=new Point2d(mat.cols()*0.18, mat.rows()*0.25);
        Point2d point2=new Point2d(mat.cols()*0.18, mat.rows()*0.35);
        Point2d point3=new Point2d(mat.cols()*0.25, mat.rows()*0.25);
        Point2d point4=new Point2d(mat.cols()*0.25, mat.rows()*0.35);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat sex= shear(mat,list);
        sex=OpencvUtil.drawContours(sex,50);
        opencv_imgcodecs.imwrite("F:/face/sex.jpg", sex);
        BufferedImage sexBuffer=OpencvUtil.Mat2BufImg(sex,".jpg");
        String sexStr=OCRUtil.getImageMessage(sexBuffer,"chi_sim",true);
        sexStr=sexStr.replace("\n","");
        return sexStr+"\n";
    }
 
    public static String nation(Mat mat){
        Point2d point1=new Point2d( (mat.cols()*0.39), mat.rows()*0.25);
        Point2d point2=new Point2d( (mat.cols()*0.39), mat.rows()*0.36);
        Point2d point3=new Point2d( (mat.cols()*0.55), mat.rows()*0.25);
        Point2d point4=new Point2d( (mat.cols()*0.55), mat.rows()*0.36);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat nation= shear(mat,list);
        opencv_imgcodecs.imwrite("F:/face/nation.jpg", nation);
        BufferedImage nationBuffer=OpencvUtil.Mat2BufImg(nation,".jpg");
        String nationStr=OCRUtil.getImageMessage(nationBuffer,"chi_sim",true);
        nationStr=nationStr.replace("\n","");
        return nationStr+"\n";
    }
 
    public static String birthday(Mat mat){
        Point2d point1=new Point2d((mat.cols()*0.18), mat.rows()*0.35);
        Point2d point2=new Point2d((mat.cols()*0.18), mat.rows()*0.35);
        Point2d point3=new Point2d((mat.cols()*0.55), mat.rows()*0.48);
        Point2d point4=new Point2d((mat.cols()*0.55), mat.rows()*0.48);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat birthday= shear(mat,list);
        birthday=OpencvUtil.drawContours(birthday,50);
        opencv_imgcodecs.imwrite("F:/face/birthday.jpg", birthday);
        //年份
        Point2d yearPoint2d1=new Point2d(0,0);
        Point2d yearPoint2d2=new Point2d(0,birthday.rows());
        Point2d yearPoint2d3=new Point2d((birthday.cols()*0.29), 0);
        Point2d yearPoint2d4=new Point2d((birthday.cols()*0.29), birthday.rows());
        List<Point2d> yearList=new ArrayList<>();
        yearList.add(yearPoint2d1);
        yearList.add(yearPoint2d2);
        yearList.add(yearPoint2d3);
        yearList.add(yearPoint2d4);
        Mat year= shear(birthday,yearList);
        opencv_imgcodecs.imwrite("F:/face/year.jpg", year);
        BufferedImage yearBuffer=OpencvUtil.Mat2BufImg(year,".jpg");
        String yearStr=OCRUtil.getImageMessage(yearBuffer,"eng",true);
 
        //月份
        Point2d monthPoint2d1=new Point2d( (birthday.cols()*0.44), 0);
        Point2d monthPoint2d2=new Point2d( (birthday.cols()*0.44), birthday.rows());
        Point2d monthPoint2d3=new Point2d( (birthday.cols()*0.55), 0);
        Point2d monthPoint2d4=new Point2d( (birthday.cols()*0.55), birthday.rows());
        List<Point2d> monthList=new ArrayList<>();
        monthList.add(monthPoint2d1);
        monthList.add(monthPoint2d2);
        monthList.add(monthPoint2d3);
        monthList.add(monthPoint2d4);
        Mat month= shear(birthday,monthList);
        opencv_imgcodecs.imwrite("F:/face/month.jpg", month);
        BufferedImage monthBuffer=OpencvUtil.Mat2BufImg(month,".jpg");
        String monthStr=OCRUtil.getImageMessage(monthBuffer,"eng",true);
 
        //日期
        Point2d dayPoint2d1=new Point2d((birthday.cols()*0.69), 0);
        Point2d dayPoint2d2=new Point2d((birthday.cols()*0.69), birthday.rows());
        Point2d dayPoint2d3=new Point2d((birthday.cols()*0.80), 0);
        Point2d dayPoint2d4=new Point2d((birthday.cols()*0.80), birthday.rows());
        List<Point2d> dayList=new ArrayList<>();
        dayList.add(dayPoint2d1);
        dayList.add(dayPoint2d2);
        dayList.add(dayPoint2d3);
        dayList.add(dayPoint2d4);
        Mat day= shear(birthday,dayList);
        opencv_imgcodecs.imwrite("F:/face/day.jpg", day);
        BufferedImage dayBuffer=OpencvUtil.Mat2BufImg(day,".jpg");
        String dayStr=OCRUtil.getImageMessage(dayBuffer,"eng",true);
 
        String birthdayStr=yearStr+"年"+monthStr+"月"+dayStr+"日";
        birthdayStr=birthdayStr.replace("\n","");
        return birthdayStr+"\n";
    }
 
    public static String address(Mat mat){
        Point2d point1=new Point2d((mat.cols()*0.17), mat.rows()*0.47);
        Point2d point2=new Point2d((mat.cols()*0.17), mat.rows()*0.47);
        Point2d point3=new Point2d((mat.cols()*0.61), mat.rows()*0.76);
        Point2d point4=new Point2d((mat.cols()*0.61), mat.rows()*0.76);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat address= shear(mat,list);
        address=OpencvUtil.drawContours(address,50);
        opencv_imgcodecs.imwrite("F:/face/address.jpg", address);
        BufferedImage addressBuffer=OpencvUtil.Mat2BufImg(address,".jpg");
        return OCRUtil.getImageMessage(addressBuffer,"chi_sim",true)+"\n";
    }
 
    public static String card(Mat mat){
        Point2d point1=new Point2d(mat.cols()*0.34,mat.rows()*0.75);
        Point2d point2=new Point2d(mat.cols()*0.34, mat.rows()*0.75);
        Point2d point3=new Point2d(mat.cols()*0.89, mat.rows()*0.91);
        Point2d point4=new Point2d(mat.cols()*0.89, mat.rows()*0.91);
        List<Point2d> list=new ArrayList<>();
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        Mat card= shear(mat,list);
        card=OpencvUtil.drawContours(card,50);
        opencv_imgcodecs.imwrite("F:/face/card.jpg", card);
        BufferedImage cardBuffer=OpencvUtil.Mat2BufImg(card,".jpg");
        return OCRUtil.getImageMessage(cardBuffer,"eng",true)+"\n";
    }
 
 
}
 
 
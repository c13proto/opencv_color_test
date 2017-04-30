package com.example.idry7lash629.opencv_color_test;

import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.*;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by 86004771 on 2017/04/19.
 */

public class ImageProcessing {

    public static Mat make_frame_function(CameraBridgeViewBase.CvCameraViewFrame Frame)
    {
        Point red_center=new Point(0,0);

        Mat red_mask=hsv_mask(Frame.rgba(),new Scalar(0,150,175),new Scalar(10,255,255));//赤のマスク(赤領域が白)
        red_mask=find_max_area(red_mask,red_center);
        Log.d("make_frame_function", red_center.toString());

        return red_mask;//赤のマスク
    }

    private static Mat hsv_mask(Mat src_rgb,Scalar lower,Scalar upper)
    {
        //H層は0~180
        Mat hsv= new Mat(src_rgb.size(), CvType.CV_8UC3);
        Mat gray=new Mat(src_rgb.size(), CvType.CV_8UC1);

        Imgproc.cvtColor(src_rgb,hsv, Imgproc.COLOR_RGB2HSV);//rgbからhsvへ

        Core.inRange( hsv,  lower,  upper, gray);//グレースケールになる
        hsv.release();


        Imgproc.morphologyEx(gray,gray,Imgproc.MORPH_CLOSE, new Mat(), new Point(-1,-1),2);//Closing


        return gray;
    }

    private static Mat find_max_area(Mat gray,Point center)//グレースケールから最大の白領域を残す
    {
        Mat hierarchy=Mat.zeros(new Size(5,5), CvType.CV_8UC1);
        Mat invsrc=gray.clone();
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        //一番外側のみでOK
        Imgproc.findContours(invsrc, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);
        Mat dst=Mat.zeros(gray.size(),CvType.CV_8UC1);

        int i=0;
        double maxarea = 0;
        if(contours.size()>0){//サイズが0の場合エラーになるので
            while(contours.size() != 1){//contoursが１つなるまで繰り返す
                if(maxarea < Imgproc.contourArea(contours.get(i))){//maxareaより大きいか
                    maxarea = Imgproc.contourArea(contours.get(i));
                    contours.remove(0);
                    i=1;
                }else{contours.remove(i);
                }
            }

            Scalar color=new Scalar(255);
            Imgproc.drawContours(dst, contours, -1, color,-1);//dstに輪郭の描画(塗りつぶし)
            RotatedRect box=Imgproc.minAreaRect(new MatOfPoint2f( contours.get(0).toArray() ));
            center.x=(int)box.center.x;
            center.y=(int)box.center.y;
            //Log.d("find_max_area", center.toString());
        }



        hierarchy.release();
        invsrc.release();
        contours=null;
        return dst;
    }

}

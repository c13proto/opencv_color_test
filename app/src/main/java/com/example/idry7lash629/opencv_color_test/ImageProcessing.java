package com.example.idry7lash629.opencv_color_test;

import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.*;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.List;


public class ImageProcessing {

    static final Size 画像処理サイズ=new Size(640,480);

    static final Scalar[] 黄バンド=new Scalar[]{new Scalar(20, 127, 132), new Scalar(30, 255, 230)};
    static final Scalar[] 緑バンド = new Scalar[] { new Scalar(60,102, 40), new Scalar(80, 255, 179) };
    //赤形の色領域は両端なので引数が多い
    static final Scalar[] 赤バンド = new Scalar[] { new Scalar(175, 127, 76), new Scalar(180, 255, 153), new Scalar(0, 127,76), new Scalar(3, 255, 153)};
    static final Scalar[] 桃バンド = new Scalar[] { new Scalar(170, 43, 204), new Scalar(180, 102, 255) };
    static final Scalar[] 青バンド = new Scalar[] { new Scalar(100, 127, 51), new Scalar(115, 255, 230) };
    static final Scalar[] 紫バンド = new Scalar[] { new Scalar(120, 127, 51), new Scalar(130, 255, 166) };
    static final Scalar[] 黄緑Tシャツ = new Scalar[] { new Scalar(32, 127, 102), new Scalar(43, 255, 204) };

    static final Scalar[] Scalar_右手 = 黄バンド;
    static final Scalar[] Scalar_左手 = 赤バンド;
    static final Scalar[] Scalar_右足 = 緑バンド;
    static final Scalar[] Scalar_左足 = 青バンド;
    static final Scalar[] Scalar_胴 = 黄緑Tシャツ;

    public static Mat make_frame_function(CameraBridgeViewBase.CvCameraViewFrame Frame)
    {
        Mat frame_resize=Frame.rgba();//処理速度向上のため画素を下げる
        Size 描画サイズ=frame_resize.size();
        Imgproc.resize(frame_resize, frame_resize,画像処理サイズ);
        Mat dst=Mat.zeros(画像処理サイズ,CvType.CV_8UC3);

        色抽出と描画(frame_resize,dst);
        //dst=frame_resize;
        中心のHSV値表示(frame_resize,dst);

        Imgproc.resize(dst, dst, 描画サイズ);//元サイズに戻さないと描画されなかった

        frame_resize.release();
        return dst;
    }
    private static void 中心のHSV値表示(Mat src_color,Mat dst_color)//
    {
        //dst_color=src_color;
        Mat hsv=new Mat(src_color.size(),CvType.CV_8UC3);
        Imgproc.cvtColor(src_color,hsv, Imgproc.COLOR_RGB2HSV);//rgbからhsvへ

        double[] color_hsv=hsv.get(hsv.height()/2,hsv.width()/2);
        double[] color_rgb=src_color.get(hsv.height()/2,hsv.width()/2);

        Imgproc.rectangle(dst_color, new Point(0,0),new Point(hsv.width(),hsv.height()/10), new Scalar(color_rgb[0],color_rgb[1],color_rgb[2]),-1);
        Imgproc.circle(dst_color, new Point(hsv.width()/2,hsv.height()/2),10,new Scalar(color_rgb[0],color_rgb[1],color_rgb[2]),-1);
        Imgproc.putText(dst_color,""+(int)color_hsv[0]+','+(int)color_hsv[1]+','+(int)color_hsv[2]+"",
                new Point(hsv.width()/2,hsv.height()/10),1,2.0, new Scalar(Math.abs(color_rgb[0]-127),Math.abs(color_rgb[1]-127),Math.abs(color_rgb[2]-127)));

        Log.d("中心のHSV値表示","hsv="+(int)color_hsv[0]+'\t'+(int)color_hsv[1]+'\t'+(int)color_hsv[2]);
        hsv.release();
    }

    private static void 色抽出と描画(Mat src_color,Mat dst_color)
    {
        String point_info = "";
        Mat mask_combine=Mat.zeros(src_color.size(),CvType.CV_8UC1);//グレースケール

        Point point_右手  = new Point(0,0);
        Point point_左手 = new Point(0,0);
        Point point_右足  = new Point(0,0);
        Point point_左足  = new Point(0,0);
        Point point_胴  = new Point(0,0);

        Mat mask_右手  = Mat.zeros(src_color.size(),    CvType.CV_8UC1);
        Mat mask_左手 =  Mat.zeros(src_color.size(),     CvType.CV_8UC1);
        Mat mask_右足  = Mat.zeros(src_color.size(),    CvType.CV_8UC1);
        Mat mask_左足  = Mat.zeros(src_color.size(),    CvType.CV_8UC1);
        Mat mask_胴  =   Mat.zeros(src_color.size(),      CvType.CV_8UC1);

        色抽出_面積最大マスク(src_color, Scalar_右手, mask_右手 , point_右手);
        色抽出_面積最大マスク(src_color, Scalar_左手, mask_左手 , point_左手);
        色抽出_面積最大マスク(src_color, Scalar_右足, mask_右足, point_右足);
        色抽出_面積最大マスク(src_color, Scalar_左足, mask_左足, point_左足);
        色抽出_面積最大マスク(src_color, Scalar_胴,  mask_胴, point_胴);

        マスク合成(new Mat[] {
                mask_右手 ,
                mask_左手 ,
                mask_右足 ,
                mask_左足,
                mask_胴

        }, mask_combine);

        mask_右手.release();
        mask_左手.release();
        mask_右足.release();
        mask_左足.release();
        mask_胴.release();

        point_info +=""+ point_右手.x + ',' + point_右手.y + ',';
        point_info +=""+ point_左手.x + ',' + point_左手.y + ',';
        point_info +=""+ point_右足.x + ',' + point_右足.y + ',';
        point_info +=""+ point_左足.x + ',' + point_左足.y + ',';
        point_info +=""+ point_胴.x + ',' + point_胴.y   ;


        Log.d("point_info",point_info);


        src_color.copyTo(dst_color,mask_combine);
        mask_combine.release();

    }

    private static void マスク合成(Mat[] src_color,Mat dst_color)
    {
        for (Mat src: src_color )
            Core.bitwise_or(src, dst_color, dst_color);
    }

    private static void hsv_mask(Mat src_rgb,Scalar[] range,Mat dst_gray)
    {
        //H層は0~180
        Mat hsv= new Mat(src_rgb.size(), CvType.CV_8UC3);

        Imgproc.cvtColor(src_rgb,hsv, Imgproc.COLOR_RGB2HSV);//rgbからhsvへ
        Core.inRange( hsv,  range[0],  range[1], dst_gray);//グレースケールになる
        if(range.length>2)
        {
            Mat gray=new Mat(src_rgb.size(), CvType.CV_8UC1);
            Core.inRange( hsv,  range[2],  range[3], gray);//グレースケールになる
            Core.bitwise_or(dst_gray,gray,dst_gray);
            gray.release();
        }

        hsv.release();

        Imgproc.morphologyEx(dst_gray,dst_gray,Imgproc.MORPH_CLOSE, new Mat(), new Point(-1,-1),1);//Closing,リサイズである程度つぶれるからなくていいかも

    }

    private static void 色抽出_面積最大マスク(Mat src_color, Scalar[] range, Mat dst_gray,Point center)//src->color dst->gray
    {
        Mat mask = new Mat(src_color.size(),CvType.CV_8UC1);

        hsv_mask(src_color, range, mask);

        if (dst_gray != null) find_max_area(mask, center,dst_gray);
        else center=find_max_area_point(mask);

        mask.release();

    }

    private static void find_max_area(Mat gray,Point center,Mat dst_gray)//グレースケールから最大の白領域を残す
    {
        Mat hierarchy=Mat.zeros(new Size(5,5), CvType.CV_8UC1);
        Mat invsrc=gray.clone();
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        //一番外側のみでOK
        Imgproc.findContours(invsrc, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);
        //Mat dst=Mat.zeros(gray.size(),CvType.CV_8UC1);

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
            Imgproc.drawContours(dst_gray, contours, -1, color,-1);//dstに輪郭の描画(塗りつぶし)
            RotatedRect box=Imgproc.minAreaRect(new MatOfPoint2f( contours.get(0).toArray() ));
            center.x=(int)box.center.x;
            center.y=(int)box.center.y;
            //Log.d("find_max_area", center.toString());
        }



        hierarchy.release();
        invsrc.release();
        contours=null;
    }
    private static Point find_max_area_point(Mat src_gray)
    {
        Point center = new Point(0, 0);
        Imgproc.threshold(src_gray,src_gray,0,255,Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Mat hierarchy=Mat.zeros(new Size(5,5), CvType.CV_8UC1);
        Mat invsrc=src_gray.clone();

        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        //一番外側のみでOK
        Imgproc.findContours(invsrc, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);
        //Mat dst=Mat.zeros(gray.size(),CvType.CV_8UC1);

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
            RotatedRect box=Imgproc.minAreaRect(new MatOfPoint2f( contours.get(0).toArray() ));
            center.x=(int)box.center.x;
            center.y=(int)box.center.y;
            //Log.d("find_max_area", center.toString());
        }


        hierarchy.release();
        invsrc.release();
        contours=null;
        return center;
    }


}

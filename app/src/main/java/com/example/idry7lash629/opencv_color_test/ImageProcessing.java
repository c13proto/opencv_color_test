package com.example.idry7lash629.opencv_color_test;

import android.graphics.Bitmap;
import android.util.Log;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;

//import wseemann.media.FFmpegMediaMetadataRetriever;



public class ImageProcessing {

    private static Size 画像処理サイズ=new Size(640,480);

    private static Scalar[] 黄バンド=new Scalar[]{new Scalar(20, 127, 132), new Scalar(30, 255, 255)};
    private static Scalar[] 緑バンド = new Scalar[] { new Scalar(60,102, 40), new Scalar(80, 255, 179) };
    //赤形の色領域は両端なので引数が多い
    private static Scalar[] 赤バンド = new Scalar[] { new Scalar(175, 127, 76), new Scalar(180, 255, 200), new Scalar(0, 127,76), new Scalar(3, 255, 200)};
    private static Scalar[] 桃バンド = new Scalar[] { new Scalar(170, 43, 204), new Scalar(180, 102, 255) };
    private static Scalar[] 青バンド = new Scalar[] { new Scalar(100, 127, 51), new Scalar(115, 255, 230) };
    private static Scalar[] 紫バンド = new Scalar[] { new Scalar(120, 127, 51), new Scalar(130, 255, 166) };
    private static Scalar[] 黄緑Tシャツ = new Scalar[] { new Scalar(32, 127, 102), new Scalar(43, 255, 204) };

    private static Scalar[] Scalar_右手 = 黄バンド;
    private static Scalar[] Scalar_左手 = 赤バンド;
    private static Scalar[] Scalar_右足 = 緑バンド;
    private static Scalar[] Scalar_左足 = 青バンド;
    private static Scalar[] Scalar_胴 = 黄緑Tシャツ;



    public static Mat make_frame_function(CameraBridgeViewBase.CvCameraViewFrame Frame)
    {
        Mat frame_resize=Frame.rgba();//処理速度向上のため画素を下げる
        Size 描画サイズ=frame_resize.size();

        Imgproc.resize(frame_resize, frame_resize,画像処理サイズ);
        Mat dst=new Mat(画像処理サイズ,CvType.CV_8UC3);

        色抽出と描画(frame_resize,dst);
        //dst=frame_resize;
        中心のHSV値表示(frame_resize,dst);//srcの中心の値がdstに上書きされる
        //色抽出と座標(dst);

        Imgproc.resize(dst, dst, 描画サイズ);//元サイズに戻さないと描画されなかった

        //frame_resize.release();//何かとdstに影響させるためコメントアウト。RAM消費は変化なし
        return dst;
    }
    private static String 色抽出と座標(Mat src_color)
    {
        String points="";
        Point point_右手 = new Point();
        Point point_左手 = new Point();
        Point point_右足 = new Point();
        Point point_左足 = new Point();
        Point point_胴  = new Point();

        Mat null_mat = new Mat();
        null_mat = null;
        色抽出_面積最大マスク(src_color,Scalar_右手,null_mat, point_右手);
        色抽出_面積最大マスク(src_color,Scalar_左手,null_mat, point_左手);
        色抽出_面積最大マスク(src_color,Scalar_右足,null_mat, point_右足);
        色抽出_面積最大マスク(src_color,Scalar_左足,null_mat, point_左足);
        色抽出_面積最大マスク(src_color, Scalar_胴, null_mat, point_胴);

        //Console.WriteLine(point_pink);
        points += "" +(int)point_右手.x + ',' + (int)point_右手.y + ',';
        points += "" +(int)point_左手.x + ',' + (int)point_左手.y + ',';
        points += "" +(int)point_右足.x + ',' + (int)point_右足.y + ',';
        points += "" +(int)point_左足.x + ',' + (int)point_左足.y + ',';
        points += "" +(int)point_胴.x + ',' + (int)point_胴.y;

//        double angle=Math.abs(table.points_to_angle(point_右手, point_左手, point_右足))*180.0/3.1416;
//        Log.d("色抽出と座標(角度)",""+angle);
        //Log.d("色抽出と座標",points);

        return points;

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
                new Point(hsv.width()/2,hsv.height()/10),1,2.0, new Scalar(255.0-color_rgb[0],255.0-color_rgb[1],255.0-color_rgb[2]));

        //Log.d("中心のHSV値表示","hsv="+(int)color_hsv[0]+'\t'+(int)color_hsv[1]+'\t'+(int)color_hsv[2]);
        hsv.release();
    }

    private static void 色抽出と描画(Mat src_color,Mat dst_color)
    {
        //String point_info = "";
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

//        point_info +=""+ (int)point_右手.x + ',' + (int)point_右手.y + ',';
//        point_info +=""+ (int)point_左手.x + ',' + (int)point_左手.y + ',';
//        point_info +=""+ (int)point_右足.x + ',' + (int)point_右足.y + ',';
//        point_info +=""+ (int)point_左足.x + ',' + (int)point_左足.y + ',';
//        point_info +=""+ (int)point_胴.x + ',' + (int)point_胴.y   ;

        //Log.d("point_info",point_info);

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
        find_max_area(mask, center,dst_gray);
        mask.release();

    }

    private static void find_max_area(Mat gray,Point center,Mat dst_gray)//グレースケールから最大の白領域を残す
    {
        Mat hierarchy=Mat.zeros(new Size(5,5), CvType.CV_8UC1);
        Mat invsrc=gray.clone();
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        //一番外側のみでOK
        Imgproc.findContours(invsrc, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

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

            if (dst_gray != null) {//描画処理
                Scalar color = new Scalar(255);
                Imgproc.drawContours(dst_gray, contours, -1, color, -1);//dstに輪郭の描画(塗りつぶし)
            }
            RotatedRect box=Imgproc.minAreaRect(new MatOfPoint2f( contours.get(0).toArray() ));
            center.x=(int)box.center.x;
            center.y=(int)box.center.y;
            //Log.d("find_max_area", center.toString());
        }

        hierarchy.release();
        invsrc.release();
        //contours=null;
    }

    public static String 動画ファイルから座標データ作成(String path,double 取得開始地点,int fps,int 取得フレーム数)
    {
        String 座標データ="";

        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();

        retriever.setDataSource(path);

        for(int i=0;i<取得フレーム数;i++) {
            //Log.d("ImgProc,座標データ作成",i+":get_frame_start");
            Bitmap frame_bmp=retriever.getFrameAtTime((long)(1000000.0 * (取得開始地点+(float)i*1.0/fps)),FFmpegMediaMetadataRetriever.OPTION_CLOSEST);//0.2~0.3secくらいかかってる
            //Log.d("ImgProc,座標データ作成",i+":convert_start");
            Mat frame_mat=new Mat();
            Utils.bitmapToMat(frame_bmp,frame_mat);
            //Log.d("ImgProc,座標データ作成",i+":resize_start");
            Imgproc.resize(frame_mat, frame_mat,画像処理サイズ);
            座標データ+=色抽出と座標(frame_mat)+"\n";
            //Log.d("ImgProc,座標データ作成",i+":finish");

            frame_bmp.recycle();
            frame_mat.release();
            Log.d("Imgproc,座標データ作成","計算済みフレーム数:"+i);
        }

        retriever.release();
        retriever=null;


        return 座標データ;

    }




}
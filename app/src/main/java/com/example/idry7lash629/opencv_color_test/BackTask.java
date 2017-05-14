package com.example.idry7lash629.opencv_color_test;

import android.os.Handler;
import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.idry7lash629.opencv_color_test.MainActivity.作業ディレクトリ;
import static com.example.idry7lash629.opencv_color_test.Recording.isENCODING;
import static com.example.idry7lash629.opencv_color_test.Recording.isRECORD_CTRL;

/**
 * Created by idry7lash629 on 5/14/2017.
 */

public class BackTask {

    private static List<Mat> RecordFrame=new ArrayList<>();
    private static String TAG="BackTask";

    public static void timer_init()
    {
        Log.d(TAG,"timer_init");
        Timer timer=new Timer(false);//ユーザースレッドとして実行
        final android.os.Handler handler=new android.os.Handler();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                timer_process();

                // TODO Auto-generated method stub
                handler.post(new Runnable(){
                    public void run() {

                    }
                });
            }
        },0,100);//100ms
    }
    private static void timer_process()
    {
        //Log.d(TAG,"timer_process");
        if(Recording.isRECORD_CTRL)
        {
//            Log.d(TAG,"timer_Process_isrecord");
            if(MainActivity.CameraFrame!=null)
            {
                Mat resize=new Mat();
                Imgproc.resize(MainActivity.CameraFrame, resize,ImageProcessing.画像処理サイズ);
                RecordFrame.add(resize);
                resize.release();

                //Log.d(TAG,"RecordFrame->"+RecordFrame.size());
            }
            else Log.d(TAG,"CameraFrame==null");
        }
    }

    public static void thred_init()
    {
        Log.d(TAG,"thred_init");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // マルチスレッドにしたい処理 ここから
                background_process();

            }
        }).start();

    }

    private static void background_process()
    {
//        String 座標データ = "";
//        座標データ=ImageProcessing.動画ファイルから座標データ作成(作業ディレクトリ+"radio1.mp4",5.8,2,80);//激重
//        ファイル出力(作業ディレクトリ+"radio1.csv",座標データ);
//        Scoring.csv_read(作業ディレクトリ+"radio1.csv",Scoring.DATA1);
//
//        座標データ=ImageProcessing.動画ファイルから座標データ作成(作業ディレクトリ+"radio2.mp4",5.2,2,80);
//        ファイル出力(作業ディレクトリ+"radio2.csv",座標データ);
//        Scoring.csv_read(作業ディレクトリ+"radio2.csv",Scoring.DATA2);
//
//        MainActivity.採点結果=Scoring.採点処理();
//
//        Recording.MedeaCodec_Record_test(作業ディレクトリ+"mediacodec.mp4");
//         Recording.jcodec_Record_test(作業ディレクトリ+"jcodec.mp4");

        while(true) {
            //Log.d(TAG, "background_process");
            if (!RecordFrame.isEmpty()) {
                isENCODING = true;
                Recording.jcodec_encode(RecordFrame.get(0));
                RecordFrame.remove(0);
                Log.d(TAG,"jcodec_encode->"+RecordFrame.size());

            } else if (isENCODING && isRECORD_CTRL)//エンコード実行していて，エンコード用フレームがなくなったとき
            {
                Recording.jcodec_finish_encode();
                isENCODING = false;
            }
        }
    }


    private static void ファイル出力(String file,String content)
    {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "UTF-8"));
            bw.write(content);
            bw.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

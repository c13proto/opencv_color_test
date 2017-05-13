package com.example.idry7lash629.opencv_color_test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    int SELECTED_ID=R.id.menu_movie;
    View View_MOVIE;
    View View_IMGPROC;



    private CameraBridgeViewBase mCameraView;

    double 採点結果;



    public static String 作業ディレクトリ="/storage/emulated/0/videokit/";
    //public static String 作業ディレクトリ="/storage/emulated/legacy/videokit/";
    public static int FRAME_WIDTH=640;
    public static int FRAME_HEIGHT=480;

    static {//これ忘れがち！
        System.loadLibrary("opencv_java3");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View_IMGPROC=this.getLayoutInflater().inflate(R.layout.activity_imgproc,null);//画像処理デバッグ
        this.addContentView(View_IMGPROC,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        View_IMGPROC.setVisibility(INVISIBLE);
        mCameraView.setVisibility(INVISIBLE);//これ単体でINVISIBLEしないと消えない
        mCameraView.setCvCameraViewListener(this);// cameraリスナーの設定 (後述)

        View_MOVIE=this.getLayoutInflater().inflate(R.layout.activity_movie,null);//お手本再生画面
        this.addContentView(View_MOVIE,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View_MOVIE.setVisibility(VISIBLE);
        Movie.movie_init(this);



        //setContentView(R.layout.activity_main);//UI何もない．背景が透明にならないから非表示


//        String 座標データ = "";
//        座標データ=ImageProcessing.動画ファイルから座標データ作成(作業ディレクトリ+"radio1.mp4",5.8,2,80);//激重
//        ファイル出力(作業ディレクトリ+"radio1.csv",座標データ);
//        Scoring.csv_read(作業ディレクトリ+"radio1.csv",Scoring.DATA1);
//
//        座標データ=ImageProcessing.動画ファイルから座標データ作成(作業ディレクトリ+"radio2.mp4",5.2,2,80);
//        ファイル出力(作業ディレクトリ+"radio2.csv",座標データ);
//        Scoring.csv_read(作業ディレクトリ+"radio2.csv",Scoring.DATA2);
//
//        採点結果=Scoring.採点処理();
//

        Recording.MedeaCodec_Record_test(作業ディレクトリ+"mediacodec.mp4");
//      Recording.jcodec_Record_test(作業ディレクトリ+"jcodec.mp4");


     }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Store current position.
        savedInstanceState.putInt("CurrentPosition", Movie.mVideoView.getCurrentPosition());
        Movie.mVideoView.pause();
    }
    // After rotating the phone. This method is called.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Get saved position.
        Movie.position_video = savedInstanceState.getInt("CurrentPosition");
        Movie.mVideoView.seekTo(Movie.position_video);
    }

    // ライブラリ初期化完了後に呼ばれるコールバック (onManagerConnected)
    // public abstract class BaseLoaderCallback implements LoaderCallbackInterface
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // 読み込みが成功したらカメラプレビューを開始
                case LoaderCallbackInterface.SUCCESS:
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("onOptionItemSelected", "called onOptionsItemSelected; selected item: " + item);
        SELECTED_ID=item.getItemId();
        View_MOVIE.setVisibility(INVISIBLE);
        View_IMGPROC.setVisibility(INVISIBLE);
        mCameraView.setVisibility(INVISIBLE);

        switch(SELECTED_ID) {
            case R.id.menu_imgproc:
                View_IMGPROC.setVisibility(VISIBLE);
                mCameraView.setVisibility(VISIBLE);
                break;
            case R.id.menu_camera://画像処理してないデータをactivity_imgprocで表示
                View_IMGPROC.setVisibility(VISIBLE);
                mCameraView.setVisibility(VISIBLE);
                break;
            case R.id.menu_movie:
                View_MOVIE.setVisibility(VISIBLE);
                break;

        }
        return true;
    }

    @Override
    public void onPause() {
        if (mCameraView != null) {
            mCameraView.disableView();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // Mat(int rows, int cols, int type)
        // rows(行): height, cols(列): width
        FRAME_WIDTH=width;
        FRAME_HEIGHT=height;
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //if(Recording.isRECORDING)Recording.Record_frame(inputFrame.rgba());

        if(SELECTED_ID==R.id.menu_imgproc)return ImageProcessing.make_frame_function(inputFrame);
        else return inputFrame.rgba();
    }

    public void ファイル出力(String file,String content)
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
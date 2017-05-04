package com.example.idry7lash629.opencv_color_test;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    int SELECTED_ID=R.id.menu_movie;
    View View_MOVIE;
    View View_IMGPROC;

    // カメラビューのインスタンス
    // CameraBridgeViewBase は JavaCameraView/NativeCameraView のスーパークラス
    private CameraBridgeViewBase mCameraView;
    double 採点結果;

    static {//これ忘れがち！
        System.loadLibrary("opencv_java3");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        View_IMGPROC=this.getLayoutInflater().inflate(R.layout.activity_imgproc,null);
        this.addContentView(View_IMGPROC,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        View_IMGPROC.setVisibility(View.INVISIBLE);

        View_MOVIE=this.getLayoutInflater().inflate(R.layout.activity_movie,null);
        this.addContentView(View_MOVIE,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View_MOVIE.setVisibility(View.VISIBLE);

        //setContentView(R.layout.activity_main);








        Scoring.csv_read(this,Scoring.DATA1,"s5.2.csv");
        Scoring.csv_read(this,Scoring.DATA2,"s5.8.csv");
        採点結果=Scoring.採点処理();//うまくいってるっぽい

        // リスナーの設定 (後述)
        mCameraView.setCvCameraViewListener(this);
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

        View_IMGPROC.setVisibility(View.INVISIBLE);
        View_MOVIE.setVisibility(View.INVISIBLE);

        switch(SELECTED_ID) {
            case R.id.menu_imgproc:
                View_IMGPROC.setVisibility(View.VISIBLE);
                break;
            case R.id.menu_movie:
                View_MOVIE.setVisibility(View.VISIBLE);
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        if (mCameraView != null) {
            //mCameraView.disableView();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mLoaderCallback);
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

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        if(SELECTED_ID==R.id.menu_imgproc)return ImageProcessing.make_frame_function(inputFrame);
        //else return Mat.zeros(inputFrame.rgba().size(),CvType.CV_8SC1);
        return inputFrame.rgba();
    }



}
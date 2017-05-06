package com.example.idry7lash629.opencv_color_test;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    int SELECTED_ID=R.id.menu_movie;
    View View_MOVIE;
    View View_IMGPROC;

    private CameraBridgeViewBase mCameraView;
    private VideoView mVideoView;
    private MediaController mMediaController;
    private int position_video = 0;
    double 採点結果;

    static {//これ忘れがち！
        System.loadLibrary("opencv_java3");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View_IMGPROC=this.getLayoutInflater().inflate(R.layout.activity_imgproc,null);//画像処理デバッグ
        this.addContentView(View_IMGPROC,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        View_IMGPROC.setVisibility(View.INVISIBLE);
        mCameraView.setVisibility(View.INVISIBLE);//これ単体でINVISIBLEしないと消えない
        mCameraView.setCvCameraViewListener(this);// cameraリスナーの設定 (後述)

        View_MOVIE=this.getLayoutInflater().inflate(R.layout.activity_movie,null);//お手本再生画面
        this.addContentView(View_MOVIE,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View_MOVIE.setVisibility(View.VISIBLE);
        movie_init();

        //setContentView(R.layout.activity_main);//UI何もない．背景が透明にならないから非表示

        //csvデータからの採点処理，うまくいってるっぽい
        Scoring.csv_read(this,Scoring.DATA1,"s5.2.csv");
        Scoring.csv_read(this,Scoring.DATA2,"s5.8.csv");
        採点結果=Scoring.採点処理();

    }

    private void movie_init()
    {
        mVideoView = (VideoView)findViewById(R.id.videoview);
        // Set the media controller buttons
        if (mMediaController == null) {
            mMediaController = new MediaController(MainActivity.this);
            //mMediaController.setAnchorView(mVideoView);// Set the videoView that acts as the anchor for the MediaController.
            mVideoView.setMediaController(mMediaController);// Set MediaController for VideoView
        }
        try {
            // ID of video file.
            int id = this.getRawResIdByName("radio1");
            mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + id));

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        mVideoView.requestFocus();
        // When the video file ready for playback.
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer) {

                mVideoView.seekTo(position_video);
                //if (position_video == 0)mVideoView.start();

                // When video Screen change size.
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        //mMediaController.setAnchorView(mVideoView);// Re-Set the videoView that acts as the anchor for the MediaController
                        mVideoView.setMediaController(mMediaController);// Set MediaController for VideoView
                    }
                });
            }
        });

    }
    // Find ID corresponding to the name of the resource (in the directory raw).
    public int getRawResIdByName(String resName) {
        String pkgName = this.getPackageName();
        // Return 0 if not found.
        int resID = this.getResources().getIdentifier(resName, "raw", pkgName);
        Log.i("AndroidVideoView", "Res Name: " + resName + "==> Res ID = " + resID);
        return resID;
    }
    // When you change direction of phone, this method will be called.
    // It store the state of video (Current position)
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Store current position.
        savedInstanceState.putInt("CurrentPosition", mVideoView.getCurrentPosition());
        mVideoView.pause();
    }
    // After rotating the phone. This method is called.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Get saved position.
        position_video = savedInstanceState.getInt("CurrentPosition");
        mVideoView.seekTo(position_video);
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
        mCameraView.setVisibility(View.INVISIBLE);

        switch(SELECTED_ID) {
            case R.id.menu_imgproc:
                View_IMGPROC.setVisibility(View.VISIBLE);
                mCameraView.setVisibility(View.VISIBLE);
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
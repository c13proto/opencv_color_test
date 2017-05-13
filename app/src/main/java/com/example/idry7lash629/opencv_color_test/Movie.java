package com.example.idry7lash629.opencv_color_test;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.MediaController;

/**
 * Created by 86004771 on 2017/05/09.
 */

public class Movie {

    public static CustomVideoView mVideoView;
    public static MediaController mMediaController;
    public static int position_video = 0;

    public static void movie_init(MainActivity mv)
    {
        mVideoView = (CustomVideoView)mv.findViewById(R.id.custom_videoview);
        mVideoView.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {

            @Override
            public void onPlay() {
                System.out.println("Play!");
                //if(!Recording.isRECORDING)Recording.Recorder_init_start();
            }

            @Override
            public void onPause() {
                System.out.println("Pause!");
                //if(Recording.isRECORDING)Recording.Record_stop();
            }
        });

        // Set the media controller buttons
        if (mMediaController == null) {
            mMediaController = new MediaController(mv);
            //mMediaController.setAnchorView(mVideoView);// Set the videoView that acts as the anchor for the MediaController.
            mVideoView.setMediaController(mMediaController);// Set MediaController for VideoView
        }
        try {
            // ID of video file.
            int id = getRawResIdByName("radio1",mv);
            mVideoView.setVideoURI(Uri.parse("android.resource://" + mv.getPackageName() + "/" + id));

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        mVideoView.requestFocus();
        // When the video file ready for playback.
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer) {//ビデオ再生準備完了したときに行われる処理

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
    private static int getRawResIdByName(String resName,MainActivity mv) {
        String pkgName = mv.getPackageName();
        // Return 0 if not found.
        int resID = mv.getResources().getIdentifier(resName, "raw", pkgName);
        Log.i("AndroidVideoView", "Res Name: " + resName + "==> Res ID = " + resID);
        return resID;
    }
}

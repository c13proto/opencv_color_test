package com.example.idry7lash629.opencv_color_test;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import org.jcodec.api.android.SequenceEncoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.example.idry7lash629.opencv_color_test.MainActivity.作業ディレクトリ;


public class Recording {
//
    private static String TAG="Recording";

    //mediacodec関係
    private static FileOutputStream FOS;
    private static MediaCodec mMediaCodec;
    private static ByteBuffer[] inputBuffers;
    private static ByteBuffer[] outputBuffers;
    private static BufferedOutputStream BOS;

//    private static MediaMuxer mMediaMuxer;
//    private static int mTrackIndex;
//    private static boolean mMuxerStarted=false;
//    private static MediaCodec.BufferInfo mBufferInfo;

    //jcodec関係
    private static SequenceEncoder mSequenceEncoder;
    public static boolean isRECORDING=false;



    public static void MediaCodec_init(String filename)
    {
        try {
            Log.d(TAG, "MediaCodecInit ");

            FOS=new FileOutputStream(filename);


            File f = new File(filename);
            if (f.exists()) f.delete();
            try {
                BOS = new BufferedOutputStream(new FileOutputStream(f));
                Log.i(TAG, "outputStream initialized");
            } catch (Exception e){
                e.printStackTrace();
            }




            MediaFormat mMediaFormat = MediaFormat.createVideoFormat("video/avc", 640, 480);
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
            mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mMediaCodec = MediaCodec.createEncoderByType("video/avc");
            mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();

            //ダミーの音生成
//            mMediaMuxer = new MediaMuxer(filename,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//            mTrackIndex = -1;
//            mMuxerStarted = false;

        }catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"MediaCodecInit");
        }

    }
    public static void MedeaCodec_Record_test(String filename)
    {
        MediaCodec_init(filename);


        try {

            for(int n=1;n<=50;n++)
            {
//                Bitmap bmp=BitmapFactory.decodeStream(new FileInputStream(MainActivity.作業ディレクトリ + n + ".jpg"));
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                byte[] data = getBitmapAsByteArray(BitmapFactory.decodeStream(new FileInputStream(作業ディレクトリ + n + ".jpg")));
                MediaCodec_encode(data);

                //bmp.recycle();

            }
            MediaCodec_release();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG,"mMediaCodec process error");
        }
    }
    private static synchronized void MediaCodec_encode(byte[] data)
    {
        try {
            inputBuffers = mMediaCodec.getInputBuffers();// here changes
            outputBuffers = mMediaCodec.getOutputBuffers();
            int inputBufferId = mMediaCodec.dequeueInputBuffer(-1);
            if (inputBufferId >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferId];
                inputBuffer.clear();
                inputBuffer.put(data);
                mMediaCodec.queueInputBuffer(inputBufferId, 0, data.length, 0, 0);
            }
            drainEncoder();
        }catch (Throwable t) {
            t.printStackTrace();
        }
    }
    private static void drainEncoder()
    {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        Log.d(TAG, "outputBufferIndex-->" + outputBufferIndex);


        try {
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                BOS.write(outData, 0, outData.length);
                Log.d(TAG, outData.length + " bytes written");

                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        }catch (Throwable t) {
            t.printStackTrace();
        }


//        do {
//            if (outputBufferIndex >= 0) {
//                ByteBuffer outBuffer = outputBuffers[outputBufferIndex];
//                System.out.println("buffer info-->" + bufferInfo.offset + "--"
//                        + bufferInfo.size + "--" + bufferInfo.flags + "--"
//                        + bufferInfo.presentationTimeUs);
//                byte[] outData = new byte[bufferInfo.size];
//                outBuffer.get(outData);
//                try {
//                    if (bufferInfo.offset != 0) {
//                        FOS_MediaCodec.write(outData, bufferInfo.offset, outData.length
//                                - bufferInfo.offset);
//                    } else {
//                        FOS_MediaCodec.write(outData, 0, outData.length);
//                    }
//                    FOS_MediaCodec.flush();
//                    Log.d(TAG, "out data -- > " + outData.length);
//                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
//                    outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo,
//                            0);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.e(TAG, "out data");
//                }
//            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                outputBuffers = mMediaCodec.getOutputBuffers();
//                Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
//            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                //MediaFormat format = mMediaCodec.getOutputFormat();
//                Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
//            }
//        } while (outputBufferIndex >= 0);
    }

//    private static boolean encodeInputFromFile(MediaCodec encoder, ByteBuffer[] encoderInputBuffers, MediaCodec.BufferInfo info, FileChannel channel) throws IOException {
//        boolean sawInputEOS = false;
//        int inputBufIndex = encoder.dequeueInputBuffer(0);
//        if (inputBufIndex >= 0) {
//            ByteBuffer dstBuf = encoderInputBuffers[inputBufIndex];
//
//            int sampleSize = channel.read(dstBuf);
//            if (sampleSize < 0) {
//                sawInputEOS = true;
//                sampleSize = 0;
//            }
//            encoder.queueInputBuffer(inputBufIndex, 0, sampleSize, channel.position(), sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
//        }
//        return sawInputEOS;
//    }
    private static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private static void MediaCodec_release()
    {
        try {
            mMediaCodec.stop();
            mMediaCodec.release();
        } catch (Exception e){
            e.printStackTrace();
        }
//        if (mMediaMuxer != null) {
//            // TODO: stop() throws an exception if you haven't fed it any data.  Keep track
//            //       of frames submitted, and don't call stop() if we haven't written anything.
//            if(mMuxerStarted)mMediaMuxer.stop();
//            mMediaMuxer.release();
//            mMediaMuxer = null;
//        }
    }
//    private static void drainEncoder2()
//    {
//        final int TIMEOUT_USEC = 10000;
//
//        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
//        while (true) {
//            int encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
//            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                Log.d(TAG, "INFO_TRY_AGAIN_LATER");
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                // not expected for an encoder
//                outputBuffers = mMediaCodec.getOutputBuffers();
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                // should happen before receiving buffers, and should only happen once
//                if (mMuxerStarted) {
//                    throw new RuntimeException("format changed twice");
//                }
//                MediaFormat newFormat = mMediaCodec.getOutputFormat();
//                Log.d(TAG, "encoder output format changed: " + newFormat);
//
//                // now that we have the Magic Goodies, start the muxer
//                mTrackIndex = mMediaMuxer.addTrack(newFormat);
//                mMediaMuxer.start();
//                mMuxerStarted = true;
//            } else if (encoderStatus < 0) {
//                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
//                        encoderStatus);
//                // let's ignore it
//            } else {
//                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
//                if (encodedData == null) {
//                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
//                            " was null");
//                }
//
//                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                    // The codec config data was pulled out and fed to the muxer when we got
//                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
//                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
//                    mBufferInfo.size = 0;
//                }
//
//                if (mBufferInfo.size != 0) {
//                    if (!mMuxerStarted) {
//                        throw new RuntimeException("muxer hasn't started");
//                    }
//
//                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
//                    encodedData.position(mBufferInfo.offset);
//                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
//
//                    mMediaMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
//                    Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +mBufferInfo.presentationTimeUs);
//                }
//
//                mMediaCodec.releaseOutputBuffer(encoderStatus, false);
//
//                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    Log.d(TAG, "BUFFER_FLAG_END_OF_STREAM");
//                    break;      // out of while
//                }
//            }
//        }
//    }


    public static void jcodec_init(String filename)
    {
        Log.d(TAG, "jcodec_init");
        try {
            mSequenceEncoder = new SequenceEncoder(new File(filename));
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void jcodec_Record_test(String filename) {

        jcodec_init(filename);
        try {

            for (int n = 1; n <= 10; n++) {
                // getting bitmap from drawable path
                //int bitmapResId = this.getResources().getIdentifier("image" + i, "drawable", this.getPackageName());
                Log.d(TAG, "jcodec_readframe:" + n);

                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(作業ディレクトリ + n + ".jpg"));
                mSequenceEncoder.encodeNativeFrame(getBitmapAsPicture(bitmap));
                bitmap.recycle();
            }
            mSequenceEncoder.finish();
            Log.d(TAG, "jcodec_test_finish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Picture getBitmapAsPicture(Bitmap bmp)
    {
        int width=bmp.getWidth();
        int height=bmp.getHeight();

        Picture picture=Picture.create(width,height, ColorSpace.RGB);

        int[] dstData = picture.getPlaneData(0);
        int[] packed = new int[width*height];
        bmp.getPixels(packed, 0, width, 0, 0,width,height);
        for (int i = 0, srcOff = 0, dstOff = 0; i < height; i++) {
            for (int j = 0; j <width; j++, srcOff++, dstOff += 3) {
                int rgb = packed[srcOff];
                dstData[dstOff] = (rgb >> 16) & 0xff;
                dstData[dstOff + 1] = (rgb >> 8) & 0xff;
                dstData[dstOff + 2] = rgb & 0xff;
            }
        }
        return picture;
    }


//
//    //FFmpeg使おうとしたときの名残
//    public static FFmpegFrameRecorder mRecorder;// = new FFmpegFrameRecorder("Videofilename", 480, 480);
//    public static long START_TIME;
//    public static boolean isRECORDING=false;
//
//    public static void Recorder_init_start()
//    {
//        //とりあえず
//        try {
//            mRecorder = new FFmpegFrameRecorder(MainActivity.作業ディレクトリ + "record.mp4", MainActivity.FRAME_WIDTH, MainActivity.FRAME_HEIGHT);
//            mRecorder.setVideoCodec(13);
//            mRecorder.setFrameRate(0.4d);
//            mRecorder.setPixelFormat(0);
//            mRecorder.setVideoQuality(1.0d);
//            mRecorder.setVideoBitrate(4000);
//            START_TIME = System.currentTimeMillis();
//            mRecorder.start();
//            isRECORDING=true;
//        }catch (Exception e){e.printStackTrace();}
//    }
//
//    public static void Record_stop() {
//        try {
//            mRecorder.stop();
//            mRecorder.release();
//            isRECORDING=false;
//        }catch (Exception e){e.printStackTrace();}
//    }
//
//    public static void Record_frame(Mat frame_mat)
//    {
//        try {
//            long time=1000 * (System.currentTimeMillis() - START_TIME);
//            if (time < mRecorder.getTimestamp()) time = mRecorder.getTimestamp() + 1000;
//
//            mRecorder.setTimestamp(time);
//            mRecorder.record(Mat_to_Frame(frame_mat));
//
//        } catch (Exception e) {e.printStackTrace();}
//
//    }
//
//
//    private  static Frame Mat_to_Frame(Mat src)
//    {
//        int width=MainActivity.FRAME_WIDTH;
//        int height= MainActivity.FRAME_HEIGHT;
//
//        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
//        Utils.matToBitmap(src,bmp);
//
//        AndroidFrameConverter converter2 = new AndroidFrameConverter();
//        Frame frame= converter2.convert(bmp );
//
//        bmp.recycle();
//        return frame;
//    }
//
//

}

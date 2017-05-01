package com.example.idry7lash629.opencv_color_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.core.Point;

/**
 * Created by 86004771 on 2017/05/01.
 */

public class Scoring {

    public static List<Point[]> DATA1 = new ArrayList<Point[]>();
    public static List<Point[]> DATA2 = new ArrayList<Point[]>();
    public static List<Double> SCORE = new ArrayList<Double>();


    public static void csv_read(Context context, List<Point[]> data, String file_name) {
        data.clear();
        // AssetManagerの呼び出し
        AssetManager assetManager = context.getResources().getAssets();
        try {
            // CSVファイルの読み込み
            InputStream is = assetManager.open(file_name);//eg."data.csv"
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);
            String line = "";

            while ((line = bufferReader.readLine()) != null) {
                // 各行が","で区切られていて4つの項目があるとする
                String[] values = line.split(",");
                int num_of_point = values.length / 2;
                Point[] point = new Point[num_of_point];

                for (int i = 0; i < num_of_point; i++)
                    point[i] = new Point(Integer.parseInt(values[i * 2]), Integer.parseInt(values[i * 2 + 1]));

                data.add(point);
            }
            bufferReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String points_data="";
        for (Point[] points : DATA2)
        {
            for (Point point : points)
            {
                points_data += "" + point.x + ',' + point.y + ',' + '\t';
            }
            points_data += "\r\n";
        }
        Log.d("csv_read",points_data);
    }

    public static double 採点処理()
    {
        SCORE.clear();
        //progressBar1.Value = 0;
        for (int i = 0; i < DATA1.size(); i++)
        {
            //progressBar1.Value = i+1;
            Point[] Points_t = DATA1.get(i);
            Point[] Points_s = DATA2.get(i);
            List<Double> score = new ArrayList<Double>();

            角度採点(Points_t[0], Points_t[4], Points_t[1],
                    Points_s[0], Points_s[4], Points_s[1],score);
            角度採点(   Points_t[0], Points_t[4], Points_t[2],
                    Points_s[0], Points_s[4], Points_s[2],score);
            角度採点(   Points_t[2], Points_t[4], Points_t[3],
                    Points_s[2], Points_s[4], Points_s[3], score);
            角度採点(   Points_t[4], Points_t[0], Points_t[2],
                    Points_s[4], Points_s[0], Points_s[2], score);
            角度採点(   Points_t[2], Points_t[3], Points_t[4],
                    Points_s[2], Points_s[3], Points_s[4], score);
            角度採点(   Points_t[3], Points_t[1], Points_t[4],
                    Points_s[3], Points_s[1], Points_s[4], score);
            角度採点(   Points_t[4], Points_t[1], Points_t[0],
                    Points_s[4], Points_s[1], Points_s[0], score);

            String debug = i + ":";
            if (score.size() != 0)
            {
                double average=calculateAverage(score);
                SCORE.add((average * 10.0));
                debug+=average+"：データ数"+score.size();
            }

            Log.d("採点処理",debug);
            score.clear();

        }
        double average=calculateAverage(SCORE);

        Log.d("総合平均スコア:",average+":データ数:"+SCORE.size());
        return average;

    }

    private static double calculateAverage(List <Double> marks) {
        double sum = 0;
        if(!marks.isEmpty()) {
            for (double mark : marks) {
                sum += mark;
            }
            return sum / marks.size();
        }
        return sum;
    }

    private static void 角度採点(Point teacher1, Point teacher2, Point teacher3,
              Point sample1, Point sample2, Point sample3, List<Double> score)//お手本とサンプルの3座標を見て0~5点で返す
    {
        if (角度計算可否判定(teacher1, teacher2, teacher3, sample1, sample2, sample3))
        {
            double PI = 3.1416f;

            double angle_teacher = Math.abs(table.points_to_angle(teacher1, teacher2, teacher3));//-は認めないようにする
            double angle_sample = Math.abs((table.points_to_angle(sample1, sample2, sample3)));
            double angle_diff = Math.abs(angle_teacher - angle_sample);


            if (angle_diff < PI / 10) score.add(10.0);
            else if (angle_diff < PI / 9) score.add(9.0);
            else if (angle_diff < PI / 8) score.add(8.0);
            else if (angle_diff < PI / 7) score.add(7.0);
            else if (angle_diff < PI / 6) score.add(6.0);
            else if (angle_diff < PI / 5) score.add(5.0);
            else if (angle_diff < PI / 4) score.add(4.0);
            else if (angle_diff < PI / 3) score.add(3.0);
            else if (angle_diff < PI / 2) score.add(2.0);
            else if (angle_diff < PI / 1) score.add(1.0);
            else score.add(0.0);

            Log.d("角度採点",""+score.get(score.size() - 1));
        }
        else { }

    }

    private static boolean 角度計算可否判定(Point p1_t, Point p2_t, Point p3_t, Point p1_s, Point p2_s, Point p3_s)
    {
        boolean 教師データ判定可否 = false;
        boolean 生徒データ判定可否 = false;
        double 基準長さ = 75;

        double distance1_t = points_to_distance(p1_t, p2_t);
        double distance2_t = points_to_distance(p2_t, p3_t);

        double distance1_s = points_to_distance(p1_s, p2_s);
        double distance2_s = points_to_distance(p2_s, p3_s);

        if (distance1_t > 基準長さ && distance2_t > 基準長さ
                && p1_t.x != 0 && p2_t.x != 0 && p3_t.x != 0) 教師データ判定可否 = true;
        if (distance1_s > 基準長さ && distance2_s > 基準長さ
                && p1_s.x != 0 && p2_s.x != 0 && p3_s.x != 0) 生徒データ判定可否 = true;

        if (教師データ判定可否 && 生徒データ判定可否) return true;

        else
        {
            String debug = "";

            if (教師データ判定可否 == false)
            {
                debug += "skip_scoring";
                debug += "\t教師:";
                if (distance1_t > 基準長さ || distance2_t > 基準長さ) debug += "距離:";
                debug +="0";
            }
            if(生徒データ判定可否==false){

                debug += "\t生徒:";
                if (distance1_s > 基準長さ || distance2_s > 基準長さ) debug += "距離:";
                debug += "0";

            }
            Log.d("角度計算可否判定",debug);
            return false;
        }
    }

    private static double points_to_distance(Point p1, Point p2)
    {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
}

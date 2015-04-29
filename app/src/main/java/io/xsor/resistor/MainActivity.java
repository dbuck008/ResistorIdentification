package io.xsor.resistor;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.inRange;
import static org.opencv.core.Core.rectangle;
import static org.opencv.core.Core.vconcat;
import static org.opencv.highgui.Highgui.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV_FULL;
import static org.opencv.imgproc.Imgproc.COLOR_HSV2RGB;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV_FULL;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2RGBA;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2RGB;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.filter2D;
import static org.opencv.imgproc.Imgproc.threshold;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, View.OnTouchListener {

    final static String TAG = "Main Activity";

    private CameraView mOpenCvCameraView;
    private int rectangleHeight;
    private int rectangleWidth;

    private final static int RED_MINL = 0 / 2;   // Divide by 2 since HSV ranges from 0 to 180.
    private final static int RED_MAXL = 30 / 2;
    private final static int RED_MINU = 330 / 2;   // Divide by 2 since HSV ranges from 0 to 180.
    private final static int RED_MAXU = 360 / 2;
    Scalar redLBL = new Scalar(RED_MINL, 0, 30);
    Scalar redUBL = new Scalar(RED_MAXL, 255, 255);
    Scalar redLBU = new Scalar(RED_MINU, 0, 30);
    Scalar redUBU = new Scalar(RED_MAXU, 255, 255);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraView) findViewById(R.id.OpenCvCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);

        rectangleHeight = dpToPx(35);
        rectangleWidth = dpToPx(50);

    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        mOpenCvCameraView.focusOnTouch(arg1);
        return true;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.flashOn(false);
                    //mOpenCvCameraView.setFlashMode(getBaseContext(),4);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        //Mat frameRGB = new Mat();
        //Mat frameHSV = new Mat();
        Mat frameCopy = new Mat();
        //cvtColor(frame,frameRGB,COLOR_RGBA2RGB);
        //cvtColor(frameRGB, frameHSV, COLOR_RGB2HSV);
        double width = frame.size().width;
        double height = frame.size().height;
        frame.copyTo(frameCopy);
        Point tl = new Point(width/2-rectangleWidth/2,height/2-rectangleHeight/2);
        Point br = new Point(width/2+rectangleWidth/2,height/2+rectangleHeight/2);
        rectangle(frameCopy, tl, br, new Scalar(255, 255, 255), -1);
        double alpha = 0.3;
        addWeighted(frameCopy, alpha, frame,1.0-alpha,0,frameCopy);
        Rect roi = new Rect((int)width/2-rectangleWidth/2,(int)height/2-rectangleHeight/2,rectangleWidth,rectangleHeight);
        Mat frameROI = new Mat(frame, roi);
        Rect roiLine = new Rect(0,rectangleHeight/2,rectangleWidth,1);
        Mat frameROILine = new Mat(frameROI,roiLine);
        Mat frameROILineHSV = new Mat();
        Mat frameROILineRGB = new Mat();
        cvtColor(frameROILine, frameROILineRGB,COLOR_RGBA2RGB,3);
        cvtColor(frameROILineRGB, frameROILineHSV, COLOR_RGB2HSV,3);

        SaveImage(frameROILine,"r");

        String hValues = "";
        String sValues = "";
        String vValues = "";

        for(int i = 0; i < frameROILineHSV.size().width; i++ ) {
            double[] HSV = frameROILineHSV.get(0,i);
            hValues += " " + HSV[0];
            sValues += " " + HSV[1];
            vValues += " " + HSV[2];
        }





        Log.d(TAG, "hValues: " + hValues);
        Log.d("", "");
        Log.d(TAG, "sValues: " + sValues);
        Log.d("","");
        Log.d(TAG ,"vValues: " + vValues);

        return frameCopy;
    }

    public void SaveImage (Mat mat, String filename) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        filename = filename + ".png";
        File file = new File(path, filename);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGR);

        Boolean bool;
        filename = file.toString();
        bool = imwrite(filename, mat);

        if (bool)
            Log.d(TAG, "SUCCESS Save: " + path + filename);
        else
            Log.d(TAG, "Fail writing image to external storage");
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}

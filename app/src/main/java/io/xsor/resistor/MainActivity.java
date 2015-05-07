package io.xsor.resistor;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Arrays;

import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.rectangle;
import static org.opencv.highgui.Highgui.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2RGB;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, View.OnTouchListener {

    final static String TAG = "Main Activity";

    private static int FIRST_DERIVATIVE = 1;
    private static int SECOND_DERIVATIVE = 2;

    private CameraView mOpenCvCameraView;
    private TextView resistorValueText, band1, band2, band3, band4, band5, band6, band7, band8, band9, band10;


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

    static double colorRGBValues[][] = {
            { 20, 20, 20 }, // black
//            { 71, 53, 38 }, // brown
            { 180, 20, 53 }, // red
//            { 160, 90, 50 }, // orange
//            { 157, 123, 39 }, // yellow
            { 41, 90, 46 }, // green
//            { 40, 73, 86 }, // blue
//            { 75, 55, 75 }, // violet
//            { 73, 65, 62 }, // gray
            { 200, 200, 200 } // white
    };
    private int WHITE = 3;
    private int LINE_SIZE = 137;
    private boolean filter = true;

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
        resistorValueText = (TextView) findViewById(R.id.resistorValue);
        band1 = (TextView) findViewById(R.id.band1);
        band2 = (TextView) findViewById(R.id.band2);
        band3= (TextView) findViewById(R.id.band3);
        band4= (TextView) findViewById(R.id.band4);
        band5= (TextView) findViewById(R.id.band5);
        band6= (TextView) findViewById(R.id.band6);
        band7= (TextView) findViewById(R.id.band7);
        band8= (TextView) findViewById(R.id.band8);
        band9= (TextView) findViewById(R.id.band9);
        band10= (TextView) findViewById(R.id.band10);


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
//                    mOpenCvCameraView.flashOn(false);
//                    mOpenCvCameraView.setFlashMode(getBaseContext(),4);
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

    public void onStop() {
        super.onStop();
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

//        Log.d(TAG, "hValues: " + hValues);
//        Log.d("", "");
//        Log.d(TAG, "sValues: " + sValues);
//        Log.d("","");
//        Log.d(TAG ,"vValues: " + vValues);


        doMagic(frameROILine);

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

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGBA);

    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private double doMagic(final Mat image) {
//        Mat newImage = new Mat();
        if(filter){
            Size size = new Size(1, 5);
            GaussianBlur(image, image, size, 0);
        }

        // colorize the image
        // Makes the elements go from RGB to COLOR where COLOR is 0-WHITE (0-5 for demo)
        int [] newImage = new int[(int)image.size().width];
        for(int i = 0; i < image.size().width; i++){
            newImage[i] = (int) findColor(image.get(0,i));
        }

        // threshold the image to find edges more accurately and then average
        int [] threshholdImage = threshold(newImage, WHITE);

        // take derivative and threshold image clearly define edges
        int[] derivativeImage = imageDerivative(threshholdImage, FIRST_DERIVATIVE);

        // find position of edges as pixel cord.
        int[] edgePositions = segmentBands(derivativeImage);

        // find average pixel color between edge locations
        int [] bandValues = new int[edgePositions.length + 1];
        if(edgePositions.length > 0){
            int i;
            for(i = 0; i < edgePositions.length; i++){
                if(i == 0){
                    bandValues[i] = averageArray(Arrays.copyOfRange(newImage, 0, edgePositions[0])); // first band
                }else{
                    bandValues[i] = averageArray(Arrays.copyOfRange(newImage, edgePositions[i-1], edgePositions[i])); // get all values between to edges
                }
            }
            bandValues[i] = averageArray(Arrays.copyOfRange(newImage, edgePositions[i-1], newImage.length)); // last band
        }
        Log.e(TAG, "Band Values: " + Arrays.toString(bandValues));

        int uniqueBands = 1;
        for(int i = 1; i < bandValues.length; i++){
            if(bandValues[i-1] != bandValues[i]){
                if(bandValues[i] != WHITE){
                    uniqueBands++;
                }
            }
        }

        int lastInputValue = 255;
        int input = 0;
        final int[] resBands = new int[uniqueBands+1];
        resBands[0] = bandValues[0];
        for(int i = 1; i < bandValues.length; i++){
            if(bandValues[i] != WHITE){
                if(bandValues[i] != lastInputValue || bandValues[i - 1] == WHITE){
                    lastInputValue = bandValues[i];
                    resBands[++input] = bandValues[i];
                }
            }
        }

        Log.e(TAG, "Unique Bands: " + Arrays.toString(resBands));

        // calc value
        final double resistorValue = calcResValue(resBands);

        runOnUiThread(new Runnable() {
            public void run() {
                resistorValueText.setText("" + resistorValue);
            }
        });

        Log.e(TAG, "RESISTOR VALUE: " + resistorValue);
        Log.e(TAG, "***********************************");

        return resistorValue;
    }

    /* Find Color
     * @params:
     *	rgb_value: rgb array of a single pixel
     * @return:
     *	colorHyp: closest color found to that pixel from those defined above
     */
    public double findColor(final double rgb_value[]){
//        Log.i(TAG, "RGB: " + Arrays.toString(rgb_value));
        double colorHyp = 0;
        double minDist = 255;
        double dist = 0;

        for(int i = 0; i < colorRGBValues.length; i++){
            // find euclidian distance between RGB of current pixel with predefined colors
            // sqrt((x1-x2)^2 + (y1-y2)^2 + (z1-z2)^2)
            dist = Math.sqrt(Math.pow((rgb_value[0]-colorRGBValues[i][0]),2)+Math.pow((rgb_value[1]-colorRGBValues[i][1]),2)+Math.pow((rgb_value[2]-colorRGBValues[i][2]),2));
//            Log.e(TAG, "dist: " + dist);

            if(dist < minDist){ // if this distance is smaller than the rest, this reg_value is more likely this new color
                minDist = dist;
//                Log.i(TAG, "minDist: " + minDist);
                colorHyp = i;
            }
        }

//        Log.e(TAG, "Color: " + colorCodeName[(int)colorHyp]);
        return colorHyp;
    }

    /* Calculate Resistor Value
     * @params:
     *	rings: Array of ints that correspond to the colors of the resistor bands as defined above
     * @return:
     *	result: int value of resistor
     */
    static double calcResValue(final int rings[]) {
        if(rings.length < 2) return 0;
        double result = 0;

        for(int i = 0; i < rings.length - 2; i++){
            result += rings[i] * Math.pow(10,(rings.length - 3 - i));
        }
        result *= Math.pow(10, rings[rings.length - 2]);

        return result;
    }

    /*
     * @params:
     *	image: 1D image
     *	method:
     *		- 1st derivative
     *		- 1nd derivative
     * @return:
     *	averageImage: Mat of calculated derivative values for each pixel using method @param{method}
     */
    public int[] imageDerivative(final int[] image, int method){
        int[] imageDerivative = new int[LINE_SIZE];

        // scan every pixel and determine change in gradient using 1st derivative
        // ignore first and last pixel
        for(int i = 1; i < image.length-1; i++){
            if(method == FIRST_DERIVATIVE){
                imageDerivative[i] = (image[i+1] - image[i]);
//                imageDerivative.put(0, i, (image.get(0,i+1)[0] - image.get(0,i)[0]));
            }else if(method == SECOND_DERIVATIVE){
                imageDerivative[i] = (image[i+1] + image[i-1] - 2 * image[i]);
//                imageDerivative.put(0, i, (image.get(0,i+1)[0] + image.get(0,i-1)[0] - 2 * image.get(0,i)[0]));
            }
        }
        return imageDerivative;
    }

//    public Mat imageDerivative(final Mat image){
//        return imageDerivative(image, FIRST_DERIVATIVE);
//    }


    /* Threshold
     * @params:
     *	image: 1D Mat image
     *	threshold: threshold value
     * @return:
     *	result: binary image
     */
    public int[] threshold(final int[] image, int threshold){
        int[] result = new int[image.length];

        for(int i = 0; i < image.length; i++){
            if(image[i] < threshold){
                result[i] = 0;
            }else{
                result[i] = 255;
            }
        }
        return result;
    }

    /* Segment Bands
     * @params:
     *	binary image
     * @return:
     *	array of edge locations as pixel value
     */
    public int[] segmentBands(final int[] image){
//        int[] threshold = threshold(image, 10); // play around with this number until we like the results
        int[] bandEdges = new int[LINE_SIZE]; // must change if planing on scaling
        int edgeCount = 0;

        // find location of edges
        for(int i = 0; i < image.length; i++){
            if(Math.abs(image[i]) > 0){ // if edge is detected
                bandEdges[edgeCount] = i; // keep track of position in original image
                edgeCount++;
            }
        }

        return Arrays.copyOfRange(bandEdges, 0, edgeCount);
    }

    /* Average Array
     * @parmams:
     *	array of pixel values (ints)
     * @return:
     *	average pixel value
     */
    public int averageArray(final int[] array){
        double average = 0;

        for (int anArray : array) {
            average += anArray;
        }
        average /= (double) array.length;

        return (int) Math.round(average);
    }
}

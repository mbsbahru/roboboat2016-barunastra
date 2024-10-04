package com.mbsbahru.roboboat2016_barunastra;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private static final String TAG = BluetoothConf.class.getName();
    //private static final String TAG2 = "OCVSample::Activity";  
    private static final int VIEW_MODE_SEEKBAR = 2;
    private static final int VIEW_MODE_ASLI = 0;
    private static final int VIEW_MODE_TRACK = 1;

    private final int batHorzonA = SeekBarVal22.batHorzonA;
    private final int batHorzonB = SeekBarVal22.batHorzonB;
    private final int  LIM_T2 = 5;
    private final int  LIM_L2 = 5;
    private final int batTO = SeekBarVal23.TObs;
    private final int batTG = SeekBarVal23.TGate;

    private boolean obsAct = false;
    private boolean procGerakObs = false;

    private boolean prefKan = false;
    private boolean prefKir = false;

    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private Sensor mOrienta;
    private final float[] mLastAccelerometer = new float[3];
    private final float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private final float[] mR = new float[9];
    private final float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private int gateCtr = 0;
    private boolean gateInd = false;
    private SensorEventListener mySensorEventListener;
    private final double nRatHW = 0;
    private final int batasTiang = SeekBarVal11.batTiangBir;
    private final int batasTiangH = SeekBarVal11.batTiangH;
    private static final int  LIM_T = 15;
    private static final int  LIM_L = 2;
    private int val = 0;
    private SensorManager sManager;
    private int mViewMode;
    private Mat mRgba;
    private Mat mTampil;
    private Mat mBiner11;
    private Mat mBiner12;
    private Mat mBiner21;
    private Mat mBiner22;
    private Mat mBiner23;
    private Mat mBiner24;
    private Mat mBiner25;
    private Mat mBiner31;
    private Mat mBiner32;
    private Mat mBiner33;
    private Mat mBiner34;
    private Mat mBiner41;
    private Mat mGray;
    private Mat mEdge;
    private int indGate = 0;
    float nLux1;
    float nLux2;
    private static final byte SYMBOL_CRUCIFORM = 1;
    private static final byte SYMBOL_CIRCLE = 2;
    private static final byte SYMBOL_TRIANGLE = 3;
    private static final byte DOCK_HIJAU = 1;
    private static final byte DOCK_MERAH = 2;
    private static final byte DOCK_BIRU = 3;
    private static final byte DOCK_HITAM = 4;
    private Mat mHSV;
    private int iXroll;
    private float Xroll;
    private float Xr;
    private float Xasli;
    private int xC;
    private int yC;
    private double latitude;
    private double longitude;

    private MenuItem mItemPreviewSeekBar;
    private MenuItem mItemPreviewAsli;
    private MenuItem mItemPreviewTracking;
    private MenuItem mItemTombolKalibrasi;
    private MisiView mOpenCvCameraView;

    private static final UUID MAGIC_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // magic UUID
    private BluetoothAdapter bluetoothAdapter;
    private BTConnectionThread btConnectionThread;
    private ProgressDialog pd;

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                // Load native library after(!) OpenCV initialization
                //System.loadLibrary("mixed_sample");
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

//    private MenuItem mItemExposure;
    private Scalar WARNA_BIRU;
    private Scalar WARNA_MERAH;
    private Scalar WARNA_HIJAU;

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    BluetoothDevice device;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera_view);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        device = getIntent().getParcelableExtra("device");
        if (device != null) {
            connectToBTDevice(device);
        } else {
            Toast.makeText(this, "Tidak ada devais yang dipilih", Toast.LENGTH_SHORT).show();

        }

        mOpenCvCameraView = findViewById(R.id.activity_surface_view);
//		mOpenCvCameraView.setMaxFrameSize(480, 320);
        mOpenCvCameraView.setVisibility(MisiView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mySensorEventListener = new MySensorEventListener();
        mAccelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mOrienta = sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sManager.registerListener(mySensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        sManager.registerListener(mySensorEventListener, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        sManager.registerListener(mySensorEventListener, mOrienta, SensorManager.SENSOR_DELAY_GAME);
        Sensor LightSensor = sManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sManager.registerListener(LightSensorListener, LightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        LocationManager myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener myLocationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewSeekBar = menu.add("SeekBar");
        mItemPreviewAsli = menu.add("Asli");
        mItemPreviewTracking = menu.add("Tracking");
        mItemTombolKalibrasi = menu.add("Kalibrasi");
//        mItemExposure = menu.add("exp");
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if (btConnectionThread != null) {
            btConnectionThread.cancel();
        }
        sManager.unregisterListener(mySensorEventListener);
        super.onDestroy();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mTampil = new Mat();
        mHSV = new Mat(height, width, CvType.CV_8UC4);
        mBiner11 = new Mat(height, width, CvType.CV_8UC1);
        mBiner12 = new Mat(height, width, CvType.CV_8UC1);
        mBiner21 = new Mat(height, width, CvType.CV_8UC1);
        mBiner22 = new Mat(height, width, CvType.CV_8UC1);
        mBiner23 = new Mat(height, width, CvType.CV_8UC1);
        mBiner24 = new Mat(height, width, CvType.CV_8UC1);
        mBiner25 = new Mat(height, width, CvType.CV_8UC1);
        mBiner31 = new Mat(height, width, CvType.CV_8UC1);
        mBiner32 = new Mat(height, width, CvType.CV_8UC1);
        mBiner33 = new Mat(height, width, CvType.CV_8UC1);
        mBiner34 = new Mat(height, width, CvType.CV_8UC1);
        mBiner41 = new Mat(height, width, CvType.CV_8UC1);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mEdge = new Mat(height, width, CvType.CV_8UC1);
        WARNA_BIRU = new Scalar(0, 0, 255, 255);
        WARNA_MERAH = new Scalar(255, 0, 0, 255);
        WARNA_HIJAU = new Scalar(0, 255, 0, 255);
    }

    public void onCameraViewStopped() {
        mGray.release();
        mEdge.release();
        mRgba.release();
        mHSV.release();
        mTampil.release();
        mBiner11.release();
        mBiner12.release();
        mBiner21.release();
        mBiner22.release();
        mBiner23.release();
        mBiner24.release();
        mBiner25.release();
        mBiner31.release();
        mBiner32.release();
        mBiner33.release();
        mBiner34.release();
        mBiner41.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        final int viewMode = mViewMode;
        mRgba = inputFrame.rgba();

        applyCameraSettings();

        Button bMenu = findViewById(R.id.tombolMenu);
        bMenu.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            openOptionsMenu();
            invalidateOptionsMenu();
        });

        if (viewMode == VIEW_MODE_ASLI) {
            Imgproc.putText(mRgba, "misi: " + dataMisi, new Point(5, 270), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 255), 1);
            Imgproc.putText(mRgba, "nLUX1:   " + nLux1, new Point(350, 70), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
            Imgproc.putText(mRgba, "nLUX2:   " + nLux2, new Point(350, 85), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

            Imgproc.line(mRgba, new Point(0, batHorzonA), new Point(480, batHorzonA), new Scalar(255, 0, 255), 1, 8, 0);
            Imgproc.line(mRgba, new Point(0, batHorzonB), new Point(480, batHorzonB), new Scalar(255, 0, 255), 1, 8, 0);
            Imgproc.line(mRgba, new Point(0, batHorzonB - batTG), new Point(480, batHorzonB - batTG), new Scalar(0, 255, 255), 1, 8, 0);
            Imgproc.putText(mRgba, "G " + batTG, new Point(0, batHorzonB - batTG), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
            Imgproc.putText(mRgba, " " + (batHorzonB - batHorzonA), new Point(0, (batHorzonB + batHorzonA) / 2), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
            Imgproc.putText(mRgba, " " + batHorzonA, new Point(0, batHorzonA), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
            Imgproc.putText(mRgba, " " + batHorzonB, new Point(0, batHorzonB), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

            Imgproc.putText(mRgba, "x, y, z: " + xPitch + ", " + yRoll + ", " + zAzim, new Point(240, 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(0, 0, 255), 1);

            Imgproc.putText(mRgba, "Long: " + longitude + "   Lat: " + latitude, new Point(0, mRgba.rows() * 0.95), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 1);
            Imgproc.putText(mRgba, "Exp:  " + val, new Point(5, 210), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

            return MODE_AWAL_MISI0();
        } else if (viewMode == VIEW_MODE_SEEKBAR) {
            Intent intent = new Intent(MainActivity.this, SeekBarVal11.class);
            startActivity(intent);
            finish();
        } else if (viewMode == VIEW_MODE_TRACK) {
            btConnectionThread.procParamSTM();
            Imgproc.putText(mRgba, "asli: " + dataAsli + " Misi: " + dataMisi + " In: " + dataIngate + " Out: " + dataOutgate + " sim: " + dataSimbol + " War: " + dataColor, new Point(5, 250), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 255), 1);


            switch (dataMisi) {
                case 0:
                    fungsikanKompas();
                    mTampil = mRgba;
                    indGate = 0;
                    break;
                case 1:
                    mTampil = MISI_GATE_ENTRANCE();
                    indGate = 0;
                    break;
                case 2:
                    mTampil = MISI_OBSTACLE_AVOIDANCE();
                    break;
                case 3:
                    mTampil = MISI_AUTOMATED_DOCKING();
                    indGate = 0;
                    break;
                case 4:
                    mTampil = MISI_INTEROP_CHALLENGE();
                    indGate = 0;
                    break;
                case 5:
                    mTampil = MISI_PINGER();
                    indGate = 0;
                    break;
                default:
                    fungsikanKompas();
                    mTampil = mRgba;
                    indGate = 0;
                    break;
            }
        }
        Imgproc.putText(mRgba, "misi: " + dataMisi, new Point(5, 270), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 255), 1);

        Imgproc.putText(mRgba, "nLUX1:   " + nLux1, new Point(350, 70), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
        Imgproc.putText(mRgba, "nLUX2:   " + nLux2, new Point(350, 85), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

        Imgproc.line(mRgba, new Point(0, batHorzonA), new Point(480, batHorzonA), new Scalar(255, 0, 255), 1, 8, 0);
        Imgproc.line(mRgba, new Point(0, batHorzonB), new Point(480, batHorzonB), new Scalar(255, 0, 255), 1, 8, 0);
        Imgproc.line(mRgba, new Point(0, batHorzonB - batTG), new Point(480, batHorzonB - batTG), new Scalar(0, 255, 255), 1, 8, 0);
        Imgproc.putText(mRgba, "G " + batTG, new Point(0, batHorzonB - batTG), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
        Imgproc.putText(mRgba, " " + (batHorzonB - batHorzonA), new Point(0, (batHorzonB + batHorzonA) / 2), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
        Imgproc.putText(mRgba, " " + batHorzonA, new Point(0, batHorzonA), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
        Imgproc.putText(mRgba, " " + batHorzonB, new Point(0, batHorzonB), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

        Imgproc.putText(mRgba, "Long: " + longitude + "   Lat: " + latitude, new Point(0, mRgba.rows() * 0.95), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 1);
        Imgproc.putText(mRgba, "Exp:  " + val, new Point(5, 210), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

        Imgproc.putText(mRgba, "z: " + zAzim, new Point(5, 130), Imgproc.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(0, 0, 255), 1);
        Imgproc.putText(mRgba, "x: " + xPitch, new Point(5, 100), Imgproc.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(0, 0, 255), 1);
        Imgproc.putText(mRgba, "y: " + yRoll, new Point(5, 115), Imgproc.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(0, 0, 255), 1);

        return mTampil;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewSeekBar) {
            mViewMode = VIEW_MODE_SEEKBAR;
        } else if (item == mItemPreviewAsli) {
            mViewMode = VIEW_MODE_ASLI;
        } else if (item == mItemPreviewTracking) {
            mViewMode = VIEW_MODE_TRACK;
        } else if (item == mItemTombolKalibrasi) {
            Xr = Xasli;
            gateCtr = 0;
        }
//        else if (item == mItemExposure) {
//            if (val == 1)
//                val = 0;
//            else
//                val = 1;
//        }
        return true;
    }


    ///////////////////MISI///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Mat MODE_AWAL_MISI0() {
        Scalar hsv_min1 = new Scalar(SeekBarVal11.Hmin1, SeekBarVal11.Smin1, SeekBarVal11.Vmin1, 0);
        Scalar hsv_max1 = new Scalar(SeekBarVal11.Hmax1, SeekBarVal11.Smax1, SeekBarVal11.Vmax1, 0);

        Scalar hsv_min2 = new Scalar(SeekBarVal12.Hmin2, SeekBarVal12.Smin2, SeekBarVal12.Vmin2, 0);
        Scalar hsv_max2 = new Scalar(SeekBarVal12.Hmax2, SeekBarVal12.Smax2, SeekBarVal12.Vmax2, 0);

        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL, 4);

        Core.inRange(mHSV, hsv_min1, hsv_max1, mBiner11);
        Core.inRange(mHSV, hsv_min2, hsv_max2, mBiner12);

        Imgproc.erode(mBiner11, mBiner11, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal11.Erode1 + 1, 2 * SeekBarVal11.Erode1 + 1), new Point(SeekBarVal11.Erode1, SeekBarVal11.Erode1)));
        Imgproc.dilate(mBiner11, mBiner11, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal11.Dilate1 + 1, 2 * SeekBarVal11.Dilate1 + 1), new Point(SeekBarVal11.Dilate1, SeekBarVal11.Dilate1)));
        Imgproc.dilate(mBiner11, mBiner11, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal11.Dilate1 + 1, 2 * SeekBarVal11.Dilate1 + 1), new Point(SeekBarVal11.Dilate1, SeekBarVal11.Dilate1)));
        Imgproc.erode(mBiner11, mBiner11, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal11.Erode1 + 1, 2 * SeekBarVal11.Erode1 + 1), new Point(SeekBarVal11.Erode1, SeekBarVal11.Erode1)));
//    Imgproc.GaussianBlur(mBiner1, mBiner1, new Size(9,9),0,0);  

        Imgproc.erode(mBiner12, mBiner12, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal12.Erode2 + 1, 2 * SeekBarVal12.Erode2 + 1), new Point(SeekBarVal12.Erode2, SeekBarVal12.Erode2)));
        Imgproc.dilate(mBiner12, mBiner12, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal12.Dilate2 + 1, 2 * SeekBarVal12.Dilate2 + 1), new Point(SeekBarVal12.Dilate2, SeekBarVal12.Dilate2)));
        Imgproc.dilate(mBiner12, mBiner12, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal12.Dilate2 + 1, 2 * SeekBarVal12.Dilate2 + 1), new Point(SeekBarVal12.Dilate2, SeekBarVal12.Dilate2)));
        Imgproc.erode(mBiner12, mBiner12, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal12.Erode2 + 1, 2 * SeekBarVal12.Erode2 + 1), new Point(SeekBarVal12.Erode2, SeekBarVal12.Erode2)));
//    Imgproc.GaussianBlur(mBiner2, mBiner2, new Size(9,9),0,0); 

        List<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner11, contours1, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner12, contours2, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        if (contours1.size() > 0 && contours2.size() == 0) {
            int counter1 = getObjek(contours1);
            if (counter1 >= 0) {
                Rect rect = Imgproc.boundingRect(contours1.get(counter1));
                drawOnObject(rect, WARNA_MERAH);
            }
        } else if (contours1.size() == 0 && contours2.size() > 0) {
            int counter2 = getObjek(contours2);
            if (counter2 >= 0) {
                Rect rect = Imgproc.boundingRect(contours2.get(counter2));
                drawOnObject(rect, WARNA_MERAH);
            }
        } else if (contours1.size() > 0 && contours2.size() > 0) {
            int counter1 = getObjek(contours1);
            int counter2 = getObjek(contours2);

            if (counter1 >= 0 && counter2 <= 0) {
                Rect rect = Imgproc.boundingRect(contours1.get(counter1));
                drawOnObject(rect, WARNA_MERAH);
            } else if (counter1 < 0 && counter2 >= 0) {
                Rect rect = Imgproc.boundingRect(contours2.get(counter2));
                drawOnObject(rect, WARNA_MERAH);
            } else if (counter1 >= 0 && counter2 >= 0) {
                Rect rect1 = Imgproc.boundingRect(contours1.get(counter1));
                Rect rect2 = Imgproc.boundingRect(contours2.get(counter2));
                drawOnObject(rect1, WARNA_MERAH);
                drawOnObject(rect2, WARNA_MERAH);
            }
        }
        return mRgba;
    }

    public Mat MISI_GATE_ENTRANCE() {
        Scalar hsv_min1 = new Scalar(SeekBarVal11.Hmin1, SeekBarVal11.Smin1, SeekBarVal11.Vmin1, 0);
        Scalar hsv_max1 = new Scalar(SeekBarVal11.Hmax1, SeekBarVal11.Smax1, SeekBarVal11.Vmax1, 0);

        Scalar hsv_min2 = new Scalar(SeekBarVal12.Hmin2, SeekBarVal12.Smin2, SeekBarVal12.Vmin2, 0);
        Scalar hsv_max2 = new Scalar(SeekBarVal12.Hmax2, SeekBarVal12.Smax2, SeekBarVal12.Vmax2, 0);

        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL, 4);

        Core.inRange(mHSV, hsv_min1, hsv_max1, mBiner11);
        Core.inRange(mHSV, hsv_min2, hsv_max2, mBiner12);

        Imgproc.erode(mBiner11, mBiner11, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal11.Erode1 + 1, 2 * SeekBarVal11.Erode1 + 1), new Point(SeekBarVal11.Erode1, SeekBarVal11.Erode1)));
        Imgproc.dilate(mBiner11, mBiner11, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal11.Dilate1 + 1, 2 * SeekBarVal11.Dilate1 + 1), new Point(SeekBarVal11.Dilate1, SeekBarVal11.Dilate1)));
        Imgproc.dilate(mBiner11, mBiner11, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal11.Dilate1 + 1, 2 * SeekBarVal11.Dilate1 + 1), new Point(SeekBarVal11.Dilate1, SeekBarVal11.Dilate1)));
        Imgproc.erode(mBiner11, mBiner11, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal11.Erode1 + 1, 2 * SeekBarVal11.Erode1 + 1), new Point(SeekBarVal11.Erode1, SeekBarVal11.Erode1)));
//    Imgproc.GaussianBlur(mBiner1, mBiner1, new Size(9,9),0,0);  

        Imgproc.erode(mBiner12, mBiner12, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal12.Erode2 + 1, 2 * SeekBarVal12.Erode2 + 1), new Point(SeekBarVal12.Erode2, SeekBarVal12.Erode2)));
        Imgproc.dilate(mBiner12, mBiner12, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal12.Dilate2 + 1, 2 * SeekBarVal12.Dilate2 + 1), new Point(SeekBarVal12.Dilate2, SeekBarVal12.Dilate2)));
        Imgproc.dilate(mBiner12, mBiner12, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal12.Dilate2 + 1, 2 * SeekBarVal12.Dilate2 + 1), new Point(SeekBarVal12.Dilate2, SeekBarVal12.Dilate2)));
        Imgproc.erode(mBiner12, mBiner12, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal12.Erode2 + 1, 2 * SeekBarVal12.Erode2 + 1), new Point(SeekBarVal12.Erode2, SeekBarVal12.Erode2)));
//    Imgproc.GaussianBlur(mBiner2, mBiner2, new Size(9,9),0,0); 

        List<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner11, contours1, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner12, contours2, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        Imgproc.putText(mRgba, "valBot1:   " + SeekBarVal11.Hmin1 + ",  " + SeekBarVal11.Smin1 + ",  " + SeekBarVal11.Vmin1 + ",  " + SeekBarVal11.Erode1, new Point(5, mRgba.rows() * 0.05), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
        Imgproc.putText(mRgba, "valMax1:   " + SeekBarVal11.Hmax1 + ",  " + SeekBarVal11.Smax1 + ",  " + SeekBarVal11.Vmax1 + ",  " + SeekBarVal11.Dilate1, new Point(5, mRgba.rows() * 0.1), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);

        Imgproc.putText(mRgba, "valBot2:   " + SeekBarVal12.Hmin2 + ",  " + SeekBarVal12.Smin2 + ",  " + SeekBarVal12.Vmin2 + ",  " + SeekBarVal12.Erode2, new Point(5, mRgba.rows() * 0.2), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
        Imgproc.putText(mRgba, "valMax2:   " + SeekBarVal12.Hmax2 + ",  " + SeekBarVal12.Smax2 + ",  " + SeekBarVal12.Vmax2 + ",  " + SeekBarVal12.Dilate2, new Point(5, mRgba.rows() * 0.15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);

        Imgproc.putText(mRgba, "" + gateCtr, new Point(mRgba.cols() / 1.2, 300), Imgproc.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 0, 255), 3);

        if (contours1.size() == 0 && contours2.size() == 0) {
            fungsikanKompas();
        } else if (contours1.size() > 0 && contours2.size() == 0) {
            int counter1 = getObjek(contours1);
            if (counter1 >= 0) {
                prosesObjekMerahSj(contours1, counter1);
            } else {
                fungsikanKompas();
            }
        } else if (contours1.size() == 0 && contours2.size() > 0) {
            int counter2 = getObjek(contours2);
            if (counter2 >= 0) {
                prosesObjekHijauSj(contours2, counter2);
            } else {
                fungsikanKompas();
            }
        } else if (contours1.size() > 0 && contours2.size() > 0) {
            int counter1 = getObjek(contours1);
            int counter2 = getObjek(contours2);

            if (counter1 < 0 && counter2 < 0) {
                fungsikanKompas();
            } else if (counter1 >= 0 && counter2 <= 0) {
                prosesObjekMerahSj(contours1, counter1);
            } else if (counter1 < 0 && counter2 >= 0) {
                prosesObjekHijauSj(contours2, counter2);
            } else if (counter1 >= 0 && counter2 >= 0) {
                prosesObjekMerahHijau(contours1, contours2, counter1, counter2);
            }
        }

        return mRgba;
    }

    public Mat MISI_OBSTACLE_AVOIDANCE() {
        Scalar hsv_min1 = new Scalar(SeekBarVal21.Hmin1, SeekBarVal21.Smin1, SeekBarVal21.Vmin1, 0);
        Scalar hsv_max1 = new Scalar(SeekBarVal21.Hmax1, SeekBarVal21.Smax1, SeekBarVal21.Vmax1, 0);

        Scalar hsv_min2 = new Scalar(SeekBarVal22.Hmin, SeekBarVal22.Smin, SeekBarVal22.Vmin, 0);
        Scalar hsv_max2 = new Scalar(SeekBarVal22.Hmax, SeekBarVal22.Smax, SeekBarVal22.Vmax, 0);

        Scalar hsv_min3 = new Scalar(SeekBarVal23.Hmin3, SeekBarVal23.Smin3, SeekBarVal23.Vmin3, 0);
        Scalar hsv_max3 = new Scalar(SeekBarVal23.Hmax3, SeekBarVal23.Smax3, SeekBarVal23.Vmax3, 0);

        Scalar hsv_min4 = new Scalar(SeekBarVal24.Hmin, SeekBarVal24.Smin, SeekBarVal24.Vmin, 0);
        Scalar hsv_max4 = new Scalar(SeekBarVal24.Hmax, SeekBarVal24.Smax, SeekBarVal24.Vmax, 0);

        Scalar hsv_min5 = new Scalar(SeekBarVal25.Hmin, SeekBarVal25.Smin, SeekBarVal25.Vmin, 0);
        Scalar hsv_max5 = new Scalar(SeekBarVal25.Hmax, SeekBarVal25.Smax, SeekBarVal25.Vmax, 0);

        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL, 4);

        Core.inRange(mHSV, hsv_min1, hsv_max1, mBiner21);
        Core.inRange(mHSV, hsv_min2, hsv_max2, mBiner22);
        Core.inRange(mHSV, hsv_min3, hsv_max3, mBiner23);
        Core.inRange(mHSV, hsv_min4, hsv_max4, mBiner24);
        Core.inRange(mHSV, hsv_min5, hsv_max5, mBiner25);

        Imgproc.erode(mBiner21, mBiner21, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal21.Erode1 + 1, 2 * SeekBarVal21.Erode1 + 1), new Point(SeekBarVal21.Erode1, SeekBarVal21.Erode1)));
        Imgproc.dilate(mBiner21, mBiner21, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal21.Dilate1 + 1, 2 * SeekBarVal21.Dilate1 + 1), new Point(SeekBarVal21.Dilate1, SeekBarVal21.Dilate1)));
//    Imgproc.GaussianBlur(mBiner1, mBiner1, new Size(9,9),3,3);  

        Imgproc.erode(mBiner22, mBiner22, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal22.Erode + 1, 2 * SeekBarVal22.Erode + 1), new Point(SeekBarVal22.Erode, SeekBarVal22.Erode)));
        Imgproc.dilate(mBiner22, mBiner22, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal22.Dilate + 1, 2 * SeekBarVal22.Dilate + 1), new Point(SeekBarVal22.Dilate, SeekBarVal22.Dilate)));
//    Imgproc.GaussianBlur(mBiner2, mBiner2, new Size(9,9),3,3);

        Imgproc.erode(mBiner23, mBiner23, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal23.Erode3 + 1, 2 * SeekBarVal23.Erode3 + 1), new Point(SeekBarVal23.Erode3, SeekBarVal23.Erode3)));
        Imgproc.dilate(mBiner23, mBiner23, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal23.Dilate3 + 1, 2 * SeekBarVal23.Dilate3 + 1), new Point(SeekBarVal23.Dilate3, SeekBarVal23.Dilate3)));
//    Imgproc.GaussianBlur(mBiner3, mBiner3, new Size(9,9),3,3);

        Imgproc.erode(mBiner24, mBiner24, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal24.Erode + 1, 2 * SeekBarVal24.Erode + 1), new Point(SeekBarVal24.Erode, SeekBarVal24.Erode)));
        Imgproc.dilate(mBiner24, mBiner24, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal24.Dilate + 1, 2 * SeekBarVal24.Dilate + 1), new Point(SeekBarVal24.Dilate, SeekBarVal24.Dilate)));
//    Imgproc.GaussianBlur(mBiner4, mBiner4, new Size(9,9),3,3);

        Imgproc.erode(mBiner25, mBiner25, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal25.Erode + 1, 2 * SeekBarVal25.Erode + 1), new Point(SeekBarVal25.Erode, SeekBarVal25.Erode)));
        Imgproc.dilate(mBiner25, mBiner25, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal25.Dilate + 1, 2 * SeekBarVal25.Dilate + 1), new Point(SeekBarVal25.Dilate, SeekBarVal25.Dilate)));
//    Imgproc.GaussianBlur(mBiner5, mBiner5, new Size(9,9),3,3);

        List<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner21, contours1, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner22, contours2, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours3 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner23, contours3, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours4 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner24, contours4, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours5 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner25, contours5, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        procGerakObs = false;
        hindarHiKu(contours4, contours5);
//    if (viewMode==VIEW_MODE_1A){
        if (dataIngate == 1 && dataOutgate == 1) {
            prefKan = true;
            prefKir = false;
            Imgproc.putText(mRgba, "1X", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            if (obsAct) {
                procGerakObs = true;
                hindarHiKu(contours4, contours5);
            } else
                mRgba = Gate1(contours1, contours2, contours3);
        }

//    if (viewMode==VIEW_MODE_2A)
        if (dataIngate == 2 && dataOutgate == 1) {
            prefKan = false;
            prefKir = false;
            Imgproc.putText(mRgba, "2X", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            if (indGate == 0) {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate2(contours1, contours2, contours3);
            } else {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate1(contours1, contours2, contours3);
            }
        }

//    if (viewMode==VIEW_MODE_3A){
        if (dataIngate == 3 && dataOutgate == 1) {
            prefKan = false;
            prefKir = true;
            Imgproc.putText(mRgba, "3X", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            if (indGate == 0) {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate3(contours1, contours2, contours3);
            } else {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate1(contours1, contours2, contours3);
            }
        }

//    if (viewMode==VIEW_MODE_1B){
        if (dataIngate == 1 && dataOutgate == 2) {
            prefKan = true;
            prefKir = false;
            Imgproc.putText(mRgba, "1Y", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            if (indGate == 0) {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate1(contours1, contours2, contours3);
            } else {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate2(contours1, contours2, contours3);
            }
        }

//    if (viewMode==VIEW_MODE_2B)
        if (dataIngate == 2 && dataOutgate == 2) {
            prefKan = false;
            prefKir = false;
            Imgproc.putText(mRgba, "2Y", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            if (obsAct) {
                procGerakObs = true;
                hindarHiKu(contours4, contours5);
            } else
                mRgba = Gate2(contours1, contours2, contours3);
        }

//    if (viewMode==VIEW_MODE_3B){
        if (dataIngate == 3 && dataOutgate == 2) {
            prefKan = false;
            prefKir = true;
            Imgproc.putText(mRgba, "3Y", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            if (indGate == 0) {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate3(contours1, contours2, contours3);
            } else {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate2(contours1, contours2, contours3);
            }
        }

//    if (viewMode==VIEW_MODE_1C){
        if (dataIngate == 1 && dataOutgate == 3) {
            prefKan = true;
            prefKir = false;
            Imgproc.putText(mRgba, "1Z", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            if (indGate == 0) {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate1(contours1, contours2, contours3);
            } else {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate3(contours1, contours2, contours3);
            }
        }

//    if (viewMode==VIEW_MODE_2C)
        if (dataIngate == 2 && dataOutgate == 3) {
            prefKan = false;
            prefKir = false;
            Imgproc.putText(mRgba, "2Z", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            if (indGate == 0) {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate2(contours1, contours2, contours3);
            } else {
                if (obsAct) {
                    procGerakObs = true;
                    hindarHiKu(contours4, contours5);
                } else
                    mRgba = Gate3(contours1, contours2, contours3);
            }
        }

//    if (viewMode==VIEW_MODE_3C){
        if (dataIngate == 3 && dataOutgate == 3) {
            prefKan = false;
            prefKir = true;
            Imgproc.putText(mRgba, "3Z", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            if (obsAct) {
                procGerakObs = true;
                hindarHiKu(contours4, contours5);
            } else
                mRgba = Gate3(contours1, contours2, contours3);
        }
        return mRgba;
    }

    public Mat MISI_AUTOMATED_DOCKING() {
        Scalar hsv_min1 = new Scalar(SeekBarVal31.Hmin, SeekBarVal31.Smin, SeekBarVal31.Vmin, 0);
        Scalar hsv_max1 = new Scalar(SeekBarVal31.Hmax, SeekBarVal31.Smax, SeekBarVal31.Vmax, 0);

        Scalar hsv_min2 = new Scalar(SeekBarVal32.Hmin, SeekBarVal32.Smin, SeekBarVal32.Vmin, 0);
        Scalar hsv_max2 = new Scalar(SeekBarVal32.Hmax, SeekBarVal32.Smax, SeekBarVal32.Vmax, 0);

        Scalar hsv_min3 = new Scalar(SeekBarVal33.Hmin, SeekBarVal33.Smin, SeekBarVal33.Vmin, 0);
        Scalar hsv_max3 = new Scalar(SeekBarVal33.Hmax, SeekBarVal33.Smax, SeekBarVal33.Vmax, 0);

        Scalar hsv_min4 = new Scalar(SeekBarVal34.Hmin, SeekBarVal34.Smin, SeekBarVal34.Vmin, 0);
        Scalar hsv_max4 = new Scalar(SeekBarVal34.Hmax, SeekBarVal34.Smax, SeekBarVal34.Vmax, 0);

        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL, 4);

        Core.inRange(mHSV, hsv_min1, hsv_max1, mBiner31);
        Core.inRange(mHSV, hsv_min2, hsv_max2, mBiner32);
        Core.inRange(mHSV, hsv_min3, hsv_max3, mBiner33);
        Core.inRange(mHSV, hsv_min4, hsv_max4, mBiner34);

        Imgproc.erode(mBiner31, mBiner31, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal31.Erode + 1, 2 * SeekBarVal31.Erode + 1), new Point(SeekBarVal31.Erode, SeekBarVal31.Erode)));
        Imgproc.dilate(mBiner31, mBiner31, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal31.Dilate + 1, 2 * SeekBarVal31.Dilate + 1), new Point(SeekBarVal31.Dilate, SeekBarVal31.Dilate)));
        Imgproc.GaussianBlur(mBiner31, mBiner31, new Size(9, 9), 0, 0);

        Imgproc.erode(mBiner32, mBiner32, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal32.Erode + 1, 2 * SeekBarVal32.Erode + 1), new Point(SeekBarVal32.Erode, SeekBarVal32.Erode)));
        Imgproc.dilate(mBiner32, mBiner32, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal32.Dilate + 1, 2 * SeekBarVal32.Dilate + 1), new Point(SeekBarVal32.Dilate, SeekBarVal32.Dilate)));
        Imgproc.GaussianBlur(mBiner32, mBiner32, new Size(9, 9), 0, 0);

        Imgproc.erode(mBiner33, mBiner33, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal33.Erode + 1, 2 * SeekBarVal33.Erode + 1), new Point(SeekBarVal33.Erode, SeekBarVal33.Erode)));
        Imgproc.dilate(mBiner33, mBiner33, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal33.Dilate + 1, 2 * SeekBarVal33.Dilate + 1), new Point(SeekBarVal33.Dilate, SeekBarVal33.Dilate)));
        Imgproc.GaussianBlur(mBiner33, mBiner33, new Size(9, 9), 0, 0);

        Imgproc.erode(mBiner34, mBiner34, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal34.Erode + 1, 2 * SeekBarVal34.Erode + 1), new Point(SeekBarVal34.Erode, SeekBarVal34.Erode)));
        Imgproc.dilate(mBiner34, mBiner34, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal34.Dilate + 1, 2 * SeekBarVal34.Dilate + 1), new Point(SeekBarVal34.Dilate, SeekBarVal34.Dilate)));
        Imgproc.GaussianBlur(mBiner34, mBiner34, new Size(9, 9), 0, 0);

        List<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner31, contours1, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner32, contours2, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours3 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner33, contours3, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

//    Imgproc.Canny(mBiner34, mEdge, SeekBarVal34.minT, SeekBarVal34.maxT);

//    List<MatOfPoint> contours4 = new ArrayList<MatOfPoint>();
//    Imgproc.findContours(mEdge.clone(), contours4, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<MatOfPoint> contours4 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner34, contours4, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
//    Imgproc.line(mRgba, new Point(0, batHorzon), new Point(480, batHorzon), new Scalar( 255, 0, 255 ), 1, 8 ,0 );

//    if (viewMode==VIEW_MODE_BUNDARHITAM){
        if (dataSimbol == this.SYMBOL_CIRCLE && dataColor == this.DOCK_HIJAU) {
            Imgproc.putText(mRgba, "OHj", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariBundar(contours1);
        }

//    if (viewMode==VIEW_MODE_BUNDARMERAH)
        else if (dataSimbol == this.SYMBOL_CIRCLE && dataColor == this.DOCK_MERAH) {
            Imgproc.putText(mRgba, "OM", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariBundar(contours2);
        }
//    if (viewMode==VIEW_MODE_BUNDARHIJAU)
        else if (dataSimbol == this.SYMBOL_CIRCLE && dataColor == this.DOCK_BIRU) {
            Imgproc.putText(mRgba, "OB", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariBundar(contours3);
        }
//    if (viewMode==VIEW_MODE_BUNDARBIRU)
        else if (dataSimbol == this.SYMBOL_CIRCLE && dataColor == this.DOCK_HITAM) {
            Imgproc.putText(mRgba, "OHt", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariBundar(contours4);
        }
//    if (viewMode==VIEW_MODE_SEGITIGAHITAM)
        else if (dataSimbol == this.SYMBOL_TRIANGLE && dataColor == this.DOCK_HIJAU) {
            Imgproc.putText(mRgba, "^Hj", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariSegitiga(contours1);
        }
//    if (viewMode==VIEW_MODE_SEGITIGAMERAH)
        else if (dataSimbol == this.SYMBOL_TRIANGLE && dataColor == this.DOCK_MERAH) {
            Imgproc.putText(mRgba, "^M", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariSegitiga(contours2);
        }
//    if (viewMode==VIEW_MODE_SEGITIGAHIJAU)
        else if (dataSimbol == this.SYMBOL_TRIANGLE && dataColor == this.DOCK_BIRU) {
            Imgproc.putText(mRgba, "^B", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariSegitiga(contours3);
        }
//    if (viewMode==VIEW_MODE_SEGITIGABIRU)
        else if (dataSimbol == this.SYMBOL_TRIANGLE && dataColor == this.DOCK_HITAM) {
            Imgproc.putText(mRgba, "^Ht", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
//        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGR2GRAY,1);
//        Imgproc.Canny(mGray, mEdge, SeekBarVal34.minT, SeekBarVal34.maxT);
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Imgproc.findContours(mEdge.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            cariSegitiga(contours4);
        }
//    if (viewMode==VIEW_MODE_CROSSHITAM)
        else if (dataSimbol == this.SYMBOL_CRUCIFORM && dataColor == this.DOCK_HIJAU) {
            Imgproc.putText(mRgba, "+Hj", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariCross(contours1);
        }
//    if (viewMode==VIEW_MODE_CROSSMERAH)
        else if (dataSimbol == this.SYMBOL_CRUCIFORM && dataColor == this.DOCK_MERAH) {
            Imgproc.putText(mRgba, "+M", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariCross(contours2);
        }
//    if (viewMode==VIEW_MODE_CROSSHIJAU)
        else if (dataSimbol == this.SYMBOL_CRUCIFORM && dataColor == this.DOCK_BIRU) {
            Imgproc.putText(mRgba, "+B", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
            cariCross(contours3);
        }
//    if (viewMode==VIEW_MODE_CROSSBIRU)
        else if (dataSimbol == this.SYMBOL_CRUCIFORM && dataColor == this.DOCK_HITAM) {
            Imgproc.putText(mRgba, "+Ht", new Point(370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
//		Imgproc.putText(mRgba, "OHt", new Point (370, 310), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
//        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGR2GRAY,1);
//        Imgproc.Canny(mGray, mEdge, SeekBarVal34.minT, SeekBarVal34.maxT);
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Imgproc.findContours(mEdge.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            cariCross(contours4);
        }
        return mRgba;
    }

    public Mat MISI_INTEROP_CHALLENGE() {
        Scalar hsv_min = new Scalar(SeekBarVal41.Hmin, SeekBarVal41.Smin, SeekBarVal41.Vmin, 0);
        Scalar hsv_max = new Scalar(SeekBarVal41.Hmax, SeekBarVal41.Smax, SeekBarVal41.Vmax, 0);

        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL, 4);

        Core.inRange(mHSV, hsv_min, hsv_max, mBiner41);

        Imgproc.erode(mBiner41, mBiner41, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal41.Erode + 1, 2 * SeekBarVal41.Erode + 1), new Point(SeekBarVal41.Erode, SeekBarVal41.Erode)));
        Imgproc.dilate(mBiner41, mBiner41, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal41.Dilate + 1, 2 * SeekBarVal41.Dilate + 1), new Point(SeekBarVal41.Dilate, SeekBarVal41.Dilate)));
        Imgproc.dilate(mBiner41, mBiner41, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal41.Dilate + 1, 2 * SeekBarVal41.Dilate + 1), new Point(SeekBarVal41.Dilate, SeekBarVal41.Dilate)));
        Imgproc.erode(mBiner41, mBiner41, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * SeekBarVal41.Erode + 1, 2 * SeekBarVal41.Erode + 1), new Point(SeekBarVal41.Erode, SeekBarVal41.Erode)));
//    Imgproc.GaussianBlur(mBiner1, mBiner1, new Size(9,9),0,0);  

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner41, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        if (contours.size() > 0) {
            int counter = getObjek(contours);
            if (counter >= 0) {
                Rect rect = Imgproc.boundingRect(contours.get(counter));
                drawOnObject(rect, WARNA_HIJAU);

                xC = rect.x + rect.width / 2;
                yC = rect.y + rect.height / 2;

                Point centerC = new Point(xC, yC);

                Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(5, mRgba.rows() * 0.25), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
                Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);
                cariTengah(rect.height, rect.height);
            } else fungsikanKompas();
        }
        return mRgba;
    }

    public Mat MISI_PINGER() {
        Scalar hsv_min1 = new Scalar(SeekBarVal21.Hmin1, SeekBarVal21.Smin1, SeekBarVal21.Vmin1, 0);
        Scalar hsv_max1 = new Scalar(SeekBarVal21.Hmax1, SeekBarVal21.Smax1, SeekBarVal21.Vmax1, 0);

        Scalar hsv_min3 = new Scalar(SeekBarVal23.Hmin3, SeekBarVal23.Smin3, SeekBarVal23.Vmin3, 0);
        Scalar hsv_max3 = new Scalar(SeekBarVal23.Hmax3, SeekBarVal23.Smax3, SeekBarVal23.Vmax3, 0);

        Scalar hsv_min4 = new Scalar(SeekBarVal24.Hmin, SeekBarVal24.Smin, SeekBarVal24.Vmin, 0);
        Scalar hsv_max4 = new Scalar(SeekBarVal24.Hmax, SeekBarVal24.Smax, SeekBarVal24.Vmax, 0);

        Scalar hsv_min5 = new Scalar(SeekBarVal25.Hmin, SeekBarVal25.Smin, SeekBarVal25.Vmin, 0);
        Scalar hsv_max5 = new Scalar(SeekBarVal25.Hmax, SeekBarVal25.Smax, SeekBarVal25.Vmax, 0);

        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL, 4);

        Core.inRange(mHSV, hsv_min1, hsv_max1, mBiner21);
        Core.inRange(mHSV, hsv_min3, hsv_max3, mBiner23);
        Core.inRange(mHSV, hsv_min4, hsv_max4, mBiner24);
        Core.inRange(mHSV, hsv_min5, hsv_max5, mBiner25);

        Imgproc.erode(mBiner21, mBiner21, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal21.Erode1 + 1, 2 * SeekBarVal21.Erode1 + 1), new Point(SeekBarVal21.Erode1, SeekBarVal21.Erode1)));
        Imgproc.dilate(mBiner21, mBiner21, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal21.Dilate1 + 1, 2 * SeekBarVal21.Dilate1 + 1), new Point(SeekBarVal21.Dilate1, SeekBarVal21.Dilate1)));
//    Imgproc.GaussianBlur(mBiner1, mBiner1, new Size(9,9),3,3);  

        Imgproc.erode(mBiner23, mBiner23, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal23.Erode3 + 1, 2 * SeekBarVal23.Erode3 + 1), new Point(SeekBarVal23.Erode3, SeekBarVal23.Erode3)));
        Imgproc.dilate(mBiner23, mBiner23, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal23.Dilate3 + 1, 2 * SeekBarVal23.Dilate3 + 1), new Point(SeekBarVal23.Dilate3, SeekBarVal23.Dilate3)));
//    Imgproc.GaussianBlur(mBiner3, mBiner3, new Size(9,9),3,3);

        Imgproc.erode(mBiner24, mBiner24, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal24.Erode + 1, 2 * SeekBarVal24.Erode + 1), new Point(SeekBarVal24.Erode, SeekBarVal24.Erode)));
        Imgproc.dilate(mBiner24, mBiner24, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal24.Dilate + 1, 2 * SeekBarVal24.Dilate + 1), new Point(SeekBarVal24.Dilate, SeekBarVal24.Dilate)));
//    Imgproc.GaussianBlur(mBiner4, mBiner4, new Size(9,9),3,3);

        Imgproc.erode(mBiner25, mBiner25, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal25.Erode + 1, 2 * SeekBarVal25.Erode + 1), new Point(SeekBarVal25.Erode, SeekBarVal25.Erode)));
        Imgproc.dilate(mBiner25, mBiner25, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * SeekBarVal25.Dilate + 1, 2 * SeekBarVal25.Dilate + 1), new Point(SeekBarVal25.Dilate, SeekBarVal25.Dilate)));
//    Imgproc.GaussianBlur(mBiner5, mBiner5, new Size(9,9),3,3);

        List<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner21, contours1, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours3 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner23, contours3, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours4 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner24, contours4, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> contours5 = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mBiner25, contours5, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        if (contours1.size() > 0) {
            int counter = getObjek(contours1);
            if (counter >= 0) {
                Rect rect = Imgproc.boundingRect(contours1.get(counter));
                drawOnObject(rect, WARNA_HIJAU);

                xC = rect.x + rect.width / 2;
                yC = rect.y + rect.height / 2;

                Point centerC = new Point(xC, yC);

                Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(5, mRgba.rows() * 0.25), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
                Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);
                cariTengah(rect.height, rect.height);
            }
        } else if (contours3.size() > 0) {
            int counter = getObjek(contours3);
            if (counter >= 0) {
                Rect rect = Imgproc.boundingRect(contours3.get(counter));
                drawOnObject(rect, WARNA_HIJAU);

                xC = rect.x + rect.width / 2;
                yC = rect.y + rect.height / 2;

                Point centerC = new Point(xC, yC);

                Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(5, mRgba.rows() * 0.25), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
                Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);
                cariTengah(rect.height, rect.height);
            }
        } else if (contours4.size() > 0) {
            int counter = getObjek(contours4);
            if (counter >= 0) {
                Rect rect = Imgproc.boundingRect(contours4.get(counter));
                drawOnObject(rect, WARNA_HIJAU);

                xC = rect.x + rect.width / 2;
                yC = rect.y + rect.height / 2;

                Point centerC = new Point(xC, yC);

                Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(5, mRgba.rows() * 0.25), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
                Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);
                cariTengah(rect.height, rect.height);
            }
        } else if (contours5.size() > 0) {
            int counter = getObjek(contours5);
            if (counter >= 0) {
                Rect rect = Imgproc.boundingRect(contours5.get(counter));
                drawOnObject(rect, WARNA_HIJAU);

                xC = rect.x + rect.width / 2;
                yC = rect.y + rect.height / 2;

                Point centerC = new Point(xC, yC);

                Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(5, mRgba.rows() * 0.25), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
                Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);
                cariTengah(rect.height, rect.height);
            }
        }
        return mRgba;
    }

    ///////////////////PROPERTI MISI 1//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void prosesObjekMerahHijau(List<MatOfPoint> contours1, List<MatOfPoint> contours2, int counter1, int counter2) {
        Rect rect1 = Imgproc.boundingRect(contours1.get(counter1));
        Rect rect2 = Imgproc.boundingRect(contours2.get(counter2));

        int tinggiObjek1 = rect1.height;
        int tinggiObjek2 = rect2.height;

        int ratHW1 = (rect1.height / rect1.width) - 1;
        int ratHW2 = (rect2.height / rect2.width) - 1;

        // Proses untuk benda berbentuk tiang atau tidak
        if (ratHW1 < nRatHW && ratHW2 < nRatHW) {
            drawOnObject(rect1, WARNA_HIJAU);
            drawOnObject(rect2, WARNA_HIJAU);
            int tinggiObjek = (tinggiObjek1 > tinggiObjek2) ? tinggiObjek1 : tinggiObjek2;

            if (tinggiObjek1 < batasTiangH && tinggiObjek2 < batasTiangH) {
                fungsikanKompas(tinggiObjek);
            } else if (tinggiObjek1 > batasTiangH && tinggiObjek2 < batasTiangH) {
                cariKeKanan(tinggiObjek1);
            } else if (tinggiObjek1 < batasTiangH && tinggiObjek2 > batasTiangH) {
                cariKeKiri(tinggiObjek2);
            } else {
                fungsikanKompas(tinggiObjek);
            }
            gateInd = false;
        } else if (ratHW1 > nRatHW && ratHW2 < nRatHW) {
            drawOnObject(rect1, WARNA_BIRU);
            drawOnObject(rect2, WARNA_HIJAU);

            if (tinggiObjek2 > batasTiangH) {
                cariKeKiri(tinggiObjek2);
                gateInd = false;
            }
            if (tinggiObjek1 <= batasTiang) {
                fungsikanKompas(tinggiObjek1);
                gateInd = false;
            } else {
                gateInd = false;
                cariKeKanan(tinggiObjek1);
            }
        } else if (ratHW1 < nRatHW && ratHW2 > nRatHW) {
            drawOnObject(rect1, WARNA_HIJAU);
            drawOnObject(rect2, WARNA_BIRU);

            if (tinggiObjek1 > batasTiangH) {

                cariKeKanan(tinggiObjek1);
                gateInd = false;
            }

            if (tinggiObjek2 <= batasTiang) {
                fungsikanKompas(tinggiObjek2);
                gateInd = false;
            } else {
                cariKeKiri(tinggiObjek2);
                gateInd = false;
            }
        } else if (ratHW1 > nRatHW && ratHW2 > nRatHW) {
            drawOnObject(rect1, WARNA_BIRU);
            drawOnObject(rect2, WARNA_BIRU);

            ctrAktif(rect1, rect2, tinggiObjek1, tinggiObjek2);
            if (tinggiObjek1 <= batasTiang && tinggiObjek2 <= batasTiang)
                gateInd = true;
            if (tinggiObjek1 > batasTiang && tinggiObjek2 > batasTiang)
                gateIns();

        }
    }

    public void ctrAktif(Rect rect1, Rect rect2, int tinggiObjek1, int tinggiObjek2) {
        xC = (rect1.x + (rect1.width + rect2.width) / 2 + rect2.x) / 2;
        yC = (rect1.y + rect2.y + (rect1.height + rect2.height) / 2) / 2;

        Point centerC = new Point(xC, yC);

        Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(5, mRgba.rows() * 0.25), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
        Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);

        if (rect1.x < rect2.x)
            cariTengah(tinggiObjek1, tinggiObjek2);
        else {
            if (rect1.height > rect2.height)
                cariKeKanan(tinggiObjek1);                // jika kondisi gate yang dideteksi salah, menampilkan tinggi objek merah
            else
                cariKeKiri(tinggiObjek2);            // jika kondisi gate salah lebih besar yang hijau, nampilin hijau juga
        }
    }

    public void prosesObjekMerahSj(List<MatOfPoint> contours, int counter) {
        Rect rect = Imgproc.boundingRect(contours.get(counter));

        int tinggiObjek = rect.height;
        int ratHW = (rect.height / rect.width) - 1;

        // Proses untuk benda berbentuk tiang
        if (ratHW > nRatHW) {
            drawOnObject(rect, WARNA_BIRU);
            if (tinggiObjek <= batasTiang) {
                fungsikanKompas(tinggiObjek);
                gateInd = false;
            } else {
                cariKeKanan(tinggiObjek);
//		 	  gateInd = ;
            }
        }
        // Proses untuk benda berbentuk bukan tiang
        else {
            drawOnObject(rect, WARNA_HIJAU);

            if (tinggiObjek > batasTiangH) {
                cariKeKanan(tinggiObjek);
                gateInd = false;
            } else {
                fungsikanKompas(tinggiObjek);
                gateInd = false;
            }
        }
    }

    public void prosesObjekHijauSj(List<MatOfPoint> contours, int counter) {
        Rect rect = Imgproc.boundingRect(contours.get(counter));

        int tinggiObjek = rect.height;

        int ratHW = (rect.height / rect.width) - 1;

        // Proses untuk benda berbentuk tiang
        if (ratHW > nRatHW) {
            drawOnObject(rect, WARNA_BIRU);

            if (tinggiObjek <= batasTiang) {
                fungsikanKompas(tinggiObjek);
                gateInd = false;
            } else {
                cariKeKiri(tinggiObjek);
//			 	  gateInd = true;
            }
        }
        // Proses untuk benda berbentuk bukan tiang
        else {
            drawOnObject(rect, WARNA_HIJAU);

            if (tinggiObjek > batasTiangH) {
                cariKeKiri(tinggiObjek);
                gateInd = false;
            } else {
                fungsikanKompas(tinggiObjek);
                gateInd = false;
            }
        }
    }

    public void drawOnObject(Rect rect, Scalar warna) {
        Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), warna, 2);

        int x = rect.x + rect.width / 2;
        int y = rect.y + rect.height;
        Point center = new Point(x, y);
        Imgproc.circle(mRgba, center, 4, WARNA_MERAH, 3, 8, 0);
    }

    public void cariTengah(int tinggiObjek1, int tinggiObjek2) {
        Imgproc.putText(mRgba, "Height1:  " + tinggiObjek1, new Point(5, mRgba.rows() * 0.3), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
        Imgproc.putText(mRgba, "Height2:  " + tinggiObjek2, new Point(5, mRgba.rows() * 0.35), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
        Imgproc.putText(mRgba, "Compass: " + Xroll, new Point(mRgba.cols() / 2, mRgba.rows() * 0.05), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

        if (xC < mRgba.cols() / 2)
            Imgproc.putText(mRgba, "Cari Tengah (keKiri)", new Point(mRgba.cols() / 2.5, mRgba.rows() * 0.15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(0, 0, 255), 2);
        else
            Imgproc.putText(mRgba, "Cari Tengah (keKanan)", new Point(mRgba.cols() / 2.5, mRgba.rows() * 0.15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(0, 0, 255), 2);

        iXroll = (int) Xroll;

        btConnectionThread.write(("a").getBytes());
        btConnectionThread.write(("i").getBytes());
        btConnectionThread.write(("u").getBytes());
        btConnectionThread.write(charToByteArray(xC));
        btConnectionThread.write(charToByteArray(yC));
        btConnectionThread.write(charToByteArray(iXroll));
        btConnectionThread.write(oneByteToByteArray(1));
        btConnectionThread.write(dataGpsToByteArray(longitude));    // 4byte
        btConnectionThread.write(dataGpsToByteArray(latitude));
    }

    public void fungsikanKompas(int tinggiObjek) {
        Imgproc.putText(mRgba, "Height:  " + tinggiObjek, new Point(5, mRgba.rows() * 0.3), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
        Imgproc.putText(mRgba, "Gps-Kompas", new Point(mRgba.cols() / 2, mRgba.rows() * 0.15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(0, 0, 255), 2);
        Imgproc.putText(mRgba, "Compass: " + Xroll, new Point(mRgba.cols() / 2, mRgba.rows() * 0.05), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

        iXroll = (int) Xroll;

        btConnectionThread.write(("a").getBytes());
        btConnectionThread.write(("i").getBytes());
        btConnectionThread.write(("u").getBytes());
        btConnectionThread.write(charToByteArray(240));
        btConnectionThread.write(charToByteArray(160));
        btConnectionThread.write(charToByteArray(iXroll));
        btConnectionThread.write(oneByteToByteArray(0));
        btConnectionThread.write(dataGpsToByteArray(longitude));    // 4byte
        btConnectionThread.write(dataGpsToByteArray(latitude));
    }

    public void fungsikanKompas() {
        Imgproc.putText(mRgba, "Gps-Kompas", new Point(mRgba.cols() / 2, mRgba.rows() * 0.15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(0, 0, 255), 2);
        Imgproc.putText(mRgba, "Compass: " + Xroll, new Point(mRgba.cols() / 2, mRgba.rows() * 0.05), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

        iXroll = (int) Xroll;

        btConnectionThread.write(("a").getBytes());
        btConnectionThread.write(("i").getBytes());
        btConnectionThread.write(("u").getBytes());
        btConnectionThread.write(charToByteArray(240));
        btConnectionThread.write(charToByteArray(160));
        btConnectionThread.write(charToByteArray(iXroll));
        btConnectionThread.write(oneByteToByteArray(0));
        btConnectionThread.write(dataGpsToByteArray(longitude));    // 4byte
        btConnectionThread.write(dataGpsToByteArray(latitude));
    }

    public void cariKeKanan(int tinggiObjek) {
        Imgproc.putText(mRgba, "Height:  " + tinggiObjek, new Point(5, mRgba.rows() * 0.3), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
        Imgproc.putText(mRgba, "Ke Kanan", new Point(mRgba.cols() / 2, mRgba.rows() * 0.15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(0, 0, 255), 2);
        Imgproc.putText(mRgba, "Compass: " + Xroll, new Point(mRgba.cols() / 2, mRgba.rows() * 0.05), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
        iXroll = (int) Xroll;

        btConnectionThread.write(("a").getBytes());
        btConnectionThread.write(("i").getBytes());
        btConnectionThread.write(("u").getBytes());
        btConnectionThread.write(charToByteArray(480));
        btConnectionThread.write(charToByteArray(160));
        btConnectionThread.write(charToByteArray(iXroll));
        btConnectionThread.write(oneByteToByteArray(1));
        btConnectionThread.write(dataGpsToByteArray(longitude));    // 4byte
        btConnectionThread.write(dataGpsToByteArray(latitude));
    }

    public void cariKeKiri(int tinggiObjek) {
        Imgproc.putText(mRgba, "Height:  " + tinggiObjek, new Point(5, mRgba.rows() * 0.3), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
        Imgproc.putText(mRgba, "Ke Kiri", new Point(mRgba.cols() / 2, mRgba.rows() * 0.15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(0, 0, 255), 2);
        Imgproc.putText(mRgba, "Compass: " + Xroll, new Point(mRgba.cols() / 2, mRgba.rows() * 0.05), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
        iXroll = (int) Xroll;

        btConnectionThread.write(("a").getBytes());
        btConnectionThread.write(("i").getBytes());
        btConnectionThread.write(("u").getBytes());
        btConnectionThread.write(charToByteArray(0));
        btConnectionThread.write(charToByteArray(160));
        btConnectionThread.write(charToByteArray(iXroll));
        btConnectionThread.write(oneByteToByteArray(1));
        btConnectionThread.write(dataGpsToByteArray(longitude));    // 4byte
        btConnectionThread.write(dataGpsToByteArray(latitude));
    }

    public void gateIns() {
        while (gateInd == true) {
            gateCtr++;
            gateInd = false;
        }
    }

    public int getObjek(List<MatOfPoint> contours) {
        int maxHeight = 0;
        int counter = -1;

        MatOfPoint approx = new MatOfPoint();

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint tempContour = contours.get(i);
            MatOfPoint2f newMat = new MatOfPoint2f(tempContour.toArray());
            MatOfPoint2f newApprox = new MatOfPoint2f(approx.toArray());

            Imgproc.approxPolyDP(newMat, newApprox, Imgproc.arcLength(newMat, true) * 0.02, true);

            Rect rect = Imgproc.boundingRect(contours.get(i));

            if (rect.height > maxHeight && newApprox.toArray().length > 3 && rect.height >  LIM_T && rect.width >  LIM_L) {
                maxHeight = rect.height;
                counter = i;
            }
        }
        return counter;
    }


    ///////////////////PROPERTI MISI 2//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void hindarHiKu(List<MatOfPoint> contours4, List<MatOfPoint> contours5) {
        int counter4 = -2;
        int counter5 = -2;

        if (contours4.size() > 0) {
            counter4 = cekLingkObs(contours4);
        }

        if (contours5.size() > 0) {
            counter5 = cekLingkObs(contours5);
        }

        if (counter4 >= 0 && counter5 >= 0) {
            Rect rect4 = Imgproc.boundingRect(contours4.get(counter4));
            Rect rect5 = Imgproc.boundingRect(contours5.get(counter5));

            if (rect4.area() > rect5.area()) {
                prosesHindarObs(rect4);
            } else {
                prosesHindarObs(rect5);
            }
        } else if (counter4 >= 0 && counter5 < 0) {
            Rect rect4 = Imgproc.boundingRect(contours4.get(counter4));

            prosesHindarObs(rect4);
        } else if (counter4 < 0 && counter5 >= 0) {
            Rect rect5 = Imgproc.boundingRect(contours5.get(counter5));

            prosesHindarObs(rect5);
        } else obsAct = false;
    }

    public Mat Gate1(List<MatOfPoint> contours1, List<MatOfPoint> contours2, List<MatOfPoint> contours3) {
        if (contours1.size() > 0 && contours2.size() > 0 && contours3.size() > 0) {
            int counter1 = cekLingk(contours1);
            int counter2 = cariPalingKiri(contours2);
            int counter3 = cekLingk(contours3);

            if (counter1 >= 0) {
                ketemuTarget1Sj(contours1, counter1);
            } else {
                if (counter3 >= 0) {
                    ketemuLawTar1Sj(contours3, counter3);
                } else if (counter2 >= 0) {
                    ketemuLawTar1Sj(contours2, counter2);
                } else fungsikanKompas();
            }
        } else if (contours1.size() > 0 && contours2.size() > 0 && contours3.size() == 0) {
            int counter1 = cekLingk(contours1);
            int counter2 = cariPalingKiri(contours2);
            Imgproc.putText(mRgba, "Counter1  " + counter1, new Point(mRgba.cols() / 12, mRgba.rows() * 0.7), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
            Imgproc.putText(mRgba, "Counter2  " + counter2, new Point(mRgba.cols() / 12, mRgba.rows() * 0.9), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));

            if (counter1 >= 0 && counter2 >= 0) {
                Rect rect1 = Imgproc.boundingRect(contours1.get(counter1));
                Rect rect2 = Imgproc.boundingRect(contours2.get(counter2));

                Imgproc.putText(mRgba, "Height" + rect1.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.3), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
                Imgproc.putText(mRgba, "Height" + rect2.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));

                int x1 = rect1.x + rect1.width / 2;
                int y1 = rect1.y + rect1.height / 2;
                Point center = new Point(x1, y1);

                int x2 = rect2.x + rect2.width / 2;
                int y2 = rect2.y + rect2.height / 2;
                Point center2 = new Point(x2, y2);

                Imgproc.rectangle(mRgba, new Point(rect1.x, rect1.y), new Point(rect1.x + rect1.width, rect1.y + rect1.height), new Scalar(0, 0, 255));
                Imgproc.rectangle(mRgba, new Point(rect2.x, rect2.y), new Point(rect2.x + rect2.width, rect2.y + rect2.height), new Scalar(0, 0, 255));

                Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 0), 2, 8, 0);
                Imgproc.circle(mRgba, center2, 4, new Scalar(255, 0, 0), 2, 8, 0);

                xC = (x1 + x2) / 2;
                yC = (y1 + y2) / 2;
                Point centerC = new Point(xC, yC);

                Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(mRgba.cols() / 8, mRgba.rows() * 0.1),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 0));
                Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);

                if (rect2.x > rect1.x) {
                    cariTengah(rect1.height, rect2.height);
                } else {
                    ketemuLawTar1Sj(contours2, counter2);
                }

                if (rect1.height > batTO && rect2.height > batTO) {
                    indGate = 1;
                }

            } else if (counter1 >= 0 && counter2 < 0) {
                ketemuTarget1Sj(contours1, counter1);
            } else if (counter1 < 0 && counter2 >= 0) {
                ketemuPutihDiKa(contours2, counter2);
            } else fungsikanKompas();

        } else if (contours1.size() == 0 && contours2.size() > 0 && contours3.size() > 0) {
            int counter2 = cariPalingKiri(contours2);
            int counter3 = cekLingk(contours3);
            if (counter3 >= 0) {
                ketemuLawTar1Sj(contours3, counter3);
            } else if (counter2 >= 0) {
                ketemuLawTar1Sj(contours2, counter2);
            } else fungsikanKompas();
        } else if (contours1.size() > 0 && contours2.size() == 0 && contours3.size() > 0) {
            int counter1 = cekLingk(contours1);
            int counter3 = cekLingk(contours3);

            if (counter1 >= 0) {
                ketemuTarget1Sj(contours1, counter1);
            } else {
                if (counter3 >= 0) {
                    ketemuLawTar1Sj(contours3, counter3);
                } else fungsikanKompas();
            }
        } else if (contours1.size() == 0 && contours2.size() == 0 && contours3.size() > 0) {
            int counter3 = cekLingk(contours3);
            if (counter3 >= 0)
                ketemuLawTar1Sj(contours3, counter3);
            else fungsikanKompas();
        } else if (contours1.size() > 0 && contours2.size() == 0 && contours3.size() == 0) {
            int counter1 = cekLingk(contours1);
            if (counter1 >= 0)
                ketemuTarget1Sj(contours1, counter1);
            else fungsikanKompas();
        } else if (contours1.size() == 0 && contours2.size() > 0 && contours3.size() == 0) {
            int counter2 = cariPalingKiri(contours2);
            if (counter2 >= 0)
                ketemuLawTar1Sj(contours2, counter2);
            else fungsikanKompas();
        } else fungsikanKompas();
        return mRgba;
    }

    public Mat Gate2(List<MatOfPoint> contours1, List<MatOfPoint> contours2, List<MatOfPoint> contours3) {

        if (contours1.size() > 0 && contours2.size() > 0 && contours3.size() > 0) {
            int counter1 = cekLingk(contours1);
            int counter3 = cekLingk(contours3);

            if (counter1 >= 0 && counter3 >= 0) {
                Rect rect1 = Imgproc.boundingRect(contours1.get(counter1));
                Rect rect3 = Imgproc.boundingRect(contours3.get(counter3));

                Imgproc.putText(mRgba, "Height" + rect1.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.3), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
                Imgproc.putText(mRgba, "Height" + rect3.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));

                int x1 = rect1.x + rect1.width / 2;
                int y1 = rect1.y + rect1.height / 2;
                Point center = new Point(x1, y1);

                int x3 = rect3.x + rect3.width / 2;
                int y3 = rect3.y + rect3.height / 2;
                Point center2 = new Point(x3, y3);

                Imgproc.rectangle(mRgba, new Point(rect1.x, rect1.y), new Point(rect1.x + rect1.width, rect1.y + rect1.height), new Scalar(0, 0, 255));
                Imgproc.rectangle(mRgba, new Point(rect3.x, rect3.y), new Point(rect3.x + rect3.width, rect3.y + rect3.height), new Scalar(0, 0, 255));

                Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 0), 2, 8, 0);
                Imgproc.circle(mRgba, center2, 4, new Scalar(255, 0, 0), 2, 8, 0);

                xC = (x1 + x3) / 2;
                yC = (y1 + y3) / 2;
                Point centerC = new Point(xC, yC);

                Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(mRgba.cols() / 8, mRgba.rows() * 0.1),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 0));
                Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);

                cariTengah(rect1.height, rect3.height);

                if (rect1.height > batTG && rect3.height > batTG) {
                    indGate = 1;
                }
            } else if (counter1 < 0 && counter3 >= 0) {
                ketemuLawTar1Sj(contours3, counter3);
            } else if (counter1 >= 0 && counter3 < 0) {
                ketemuLawTar2Sj(contours1, counter1);
            } else {
                int counterPKi = cariPalingKiri(contours2);
                int counterPKa = cariPalingKanan(contours2);

                if (counterPKi != counterPKa) {
                    caTeng2BolPut(contours2, counterPKi, counterPKa);
                } else {
                    if (counterPKa >= 0 && counterPKi >= 0) {
                        if (counterPKi != counterPKa) {
                            caTeng2BolPut(contours2, counterPKi, counterPKa);
                        } else {
                            Rect rect = Imgproc.boundingRect(contours2.get(counterPKa));
                            if ((rect.x + (rect.width / 2)) > 240)
                                ketemuPutihDiKa(contours2, counterPKa);
                            else
                                ketemuPutihDiKi(contours2, counterPKi);
                        }
                    } else if (counterPKa >= 0 && counterPKi < 0) {
                        Rect rect = Imgproc.boundingRect(contours2.get(counterPKa));
                        if ((rect.x + (rect.width / 2)) > 240)
                            ketemuPutihDiKa(contours2, counterPKa);
                        else
                            ketemuPutihDiKi(contours2, counterPKa);
                    } else if (counterPKa < 0 && counterPKi >= 0) {
                        Rect rect = Imgproc.boundingRect(contours2.get(counterPKi));
                        if ((rect.x + (rect.width / 2)) > 240)
                            ketemuPutihDiKa(contours2, counterPKi);
                        else
                            ketemuPutihDiKi(contours2, counterPKi);
                    } else fungsikanKompas();
                }

            }

        } else if (contours1.size() == 0 && contours2.size() > 0 && contours3.size() > 0) {
            int counter3 = cekLingk(contours3);
            int counterPKi = cariPalingKiri(contours2);
            int counterPKa = cariPalingKanan(contours2);

            if (counter3 >= 0) {
                ketemuLawTar1Sj(contours3, counter3);
            } else {
                if (counterPKi != counterPKa) {
                    caTeng2BolPut(contours2, counterPKi, counterPKa);
                } else {
                    if (counterPKa >= 0 && counterPKi >= 0) {
                        if (counterPKi != counterPKa) {
                            caTeng2BolPut(contours2, counterPKi, counterPKa);
                        } else {
                            Rect rect = Imgproc.boundingRect(contours2.get(counterPKa));
                            if ((rect.x + (rect.width / 2)) > 240)
                                ketemuPutihDiKa(contours2, counterPKa);
                            else
                                ketemuPutihDiKi(contours2, counterPKi);
                        }
                    } else if (counterPKa >= 0 && counterPKi < 0) {
                        Rect rect = Imgproc.boundingRect(contours2.get(counterPKa));
                        if ((rect.x + (rect.width / 2)) > 240)
                            ketemuPutihDiKa(contours2, counterPKa);
                        else
                            ketemuPutihDiKi(contours2, counterPKa);
                    } else if (counterPKa < 0 && counterPKi >= 0) {
                        Rect rect = Imgproc.boundingRect(contours2.get(counterPKi));
                        if ((rect.x + (rect.width / 2)) > 240)
                            ketemuPutihDiKa(contours2, counterPKi);
                        else
                            ketemuPutihDiKi(contours2, counterPKi);
                    } else fungsikanKompas();
                }
            }
        } else if (contours1.size() > 0 && contours2.size() == 0 && contours3.size() > 0) {
            int counter1 = cekLingk(contours1);
            int counter3 = cekLingk(contours3);

            if (counter1 >= 0 && counter3 >= 0) {
                Rect rect1 = Imgproc.boundingRect(contours1.get(counter1));
                Rect rect3 = Imgproc.boundingRect(contours3.get(counter3));

                Imgproc.putText(mRgba, "Height" + rect1.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.3), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
                Imgproc.putText(mRgba, "Height" + rect3.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));

                int x1 = rect1.x + rect1.width / 2;
                int y1 = rect1.y + rect1.height / 2;
                Point center = new Point(x1, y1);

                int x3 = rect3.x + rect3.width / 2;
                int y3 = rect3.y + rect3.height / 2;
                Point center2 = new Point(x3, y3);

                Imgproc.rectangle(mRgba, new Point(rect1.x, rect1.y), new Point(rect1.x + rect1.width, rect1.y + rect1.height), new Scalar(0, 0, 255));
                Imgproc.rectangle(mRgba, new Point(rect3.x, rect3.y), new Point(rect3.x + rect3.width, rect3.y + rect3.height), new Scalar(0, 0, 255));

                Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 0), 2, 8, 0);
                Imgproc.circle(mRgba, center2, 4, new Scalar(255, 0, 0), 2, 8, 0);

                xC = (x1 + x3) / 2;
                yC = (y1 + y3) / 2;
                Point centerC = new Point(xC, yC);

                Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(mRgba.cols() / 8, mRgba.rows() * 0.1),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 0));
                Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);

                cariTengah(rect1.height, rect3.height);

                if (rect1.height > batTG && rect3.height > batTG) {
                    indGate = 1;
                }
            } else if (counter1 < 0 && counter3 >= 0) {
                ketemuLawTar1Sj(contours3, counter3);
            } else if (counter1 >= 0 && counter3 < 0) {
                ketemuLawTar2Sj(contours1, counter1);
            } else {
                fungsikanKompas();
            }
        } else if (contours1.size() > 0 && contours2.size() > 0 && contours3.size() == 0) {
            int counter1 = cekLingk(contours1);
            int counterPKi = cariPalingKiri(contours2);
            int counterPKa = cariPalingKanan(contours2);

            if (counter1 >= 0) {
                ketemuLawTar2Sj(contours1, counter1);
            } else {
                if (counterPKa >= 0 && counterPKi >= 0) {
                    if (counterPKi != counterPKa) {
                        caTeng2BolPut(contours2, counterPKi, counterPKa);
                    } else {
                        Rect rect = Imgproc.boundingRect(contours2.get(counterPKa));
                        if ((rect.x + (rect.width / 2)) > 240)
                            ketemuPutihDiKa(contours2, counterPKa);
                        else
                            ketemuPutihDiKi(contours2, counterPKi);
                    }
                } else if (counterPKa >= 0 && counterPKi < 0) {
                    Rect rect = Imgproc.boundingRect(contours2.get(counterPKa));
                    if ((rect.x + (rect.width / 2)) > 240)
                        ketemuPutihDiKa(contours2, counterPKa);
                    else
                        ketemuPutihDiKi(contours2, counterPKa);
                } else if (counterPKa < 0 && counterPKi >= 0) {
                    Rect rect = Imgproc.boundingRect(contours2.get(counterPKi));
                    if ((rect.x + (rect.width / 2)) > 240)
                        ketemuPutihDiKa(contours2, counterPKi);
                    else
                        ketemuPutihDiKi(contours2, counterPKi);
                } else fungsikanKompas();
            }
        } else if (contours1.size() == 0 && contours2.size() == 0 && contours3.size() > 0) {
            int counter3 = cekLingk(contours3);
            if (counter3 >= 0) {
                ketemuLawTar1Sj(contours3, counter3);
            } else {
                fungsikanKompas();
            }
        } else if (contours1.size() > 0 && contours2.size() == 0 && contours3.size() == 0) {
            int counter1 = cekLingk(contours1);
            if (counter1 >= 0) {
                ketemuLawTar2Sj(contours1, counter1);
            } else {
                fungsikanKompas();
            }
        } else {
            fungsikanKompas();
        }
        return mRgba;
    }

    public Mat Gate3(List<MatOfPoint> contours1, List<MatOfPoint> contours2, List<MatOfPoint> contours3) {

        if (contours1.size() > 0 && contours2.size() > 0 && contours3.size() > 0) {
            int counter1 = cekLingk(contours1);
            int counter2 = cariPalingKanan(contours2);
            int counter3 = cekLingk(contours3);

            if (counter3 >= 0) {
                ketemuTarget2Sj(contours3, counter3);
            } else {
                if (counter1 >= 0) {
                    ketemuLawTar2Sj(contours1, counter1);
                } else if (counter2 >= 0) {
                    ketemuLawTar2Sj(contours2, counter2);
                } else fungsikanKompas();
            }
        } else if (contours1.size() == 0 && contours2.size() > 0 && contours3.size() > 0) {
            int counter3 = cekLingk(contours3);
            int counter2 = cariPalingKanan(contours2);
            Imgproc.putText(mRgba, "Counter1  " + counter3, new Point(mRgba.cols() / 12, mRgba.rows() * 0.7), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
            Imgproc.putText(mRgba, "Counter2  " + counter2, new Point(mRgba.cols() / 12, mRgba.rows() * 0.9), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));

            if (counter3 >= 0 && counter2 >= 0) {
                Rect rect3 = Imgproc.boundingRect(contours3.get(counter3));
                Rect rect2 = Imgproc.boundingRect(contours2.get(counter2));

                Imgproc.putText(mRgba, "Height" + rect3.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.3), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
                Imgproc.putText(mRgba, "Height" + rect2.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));

                int x1 = rect3.x + rect3.width / 2;
                int y1 = rect3.y + rect3.height / 2;
                Point center = new Point(x1, y1);

                int x2 = rect2.x + rect2.width / 2;
                int y2 = rect2.y + rect2.height / 2;
                Point center2 = new Point(x2, y2);

                Imgproc.rectangle(mRgba, new Point(rect3.x, rect3.y), new Point(rect3.x + rect3.width, rect3.y + rect3.height), new Scalar(0, 0, 255));
                Imgproc.rectangle(mRgba, new Point(rect2.x, rect2.y), new Point(rect2.x + rect2.width, rect2.y + rect2.height), new Scalar(0, 0, 255));

                Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 0), 2, 8, 0);
                Imgproc.circle(mRgba, center2, 4, new Scalar(255, 0, 0), 2, 8, 0);

                xC = (x1 + x2) / 2;
                yC = (y1 + y2) / 2;
                Point centerC = new Point(xC, yC);

                Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(mRgba.cols() / 8, mRgba.rows() * 0.1),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 0));
                Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);

                if (rect3.x > rect2.x) {
                    cariTengah(rect3.height, rect2.height);
                } else {
                    ketemuLawTar2Sj(contours2, counter2);
                }


                if (rect3.height > batTO && rect2.height > batTO) {
                    indGate = 1;
                }

            } else if (counter3 >= 0 && counter2 < 0) {
                ketemuTarget2Sj(contours3, counter3);
            } else if (counter3 < 0 && counter2 >= 0) {
                ketemuPutihDiKi(contours2, counter2);
            } else fungsikanKompas();

        } else if (contours1.size() > 0 && contours2.size() > 0 && contours3.size() == 0) {
            int counter2 = cariPalingKanan(contours2);
            int counter1 = cekLingk(contours1);

            if (counter1 >= 0) {
                ketemuLawTar2Sj(contours1, counter1);
            } else if (counter2 >= 0) {
                ketemuLawTar2Sj(contours2, counter2);
            } else fungsikanKompas();
        } else if (contours1.size() > 0 && contours2.size() == 0 && contours3.size() > 0) {
            int counter1 = cekLingk(contours1);
            int counter3 = cekLingk(contours3);

            if (counter3 >= 0) {
                ketemuTarget2Sj(contours3, counter3);
            } else {
                if (counter1 >= 0) {
                    ketemuLawTar2Sj(contours1, counter1);
                } else fungsikanKompas();
            }
        } else if (contours1.size() == 0 && contours2.size() == 0 && contours3.size() > 0) {
            int counter3 = cekLingk(contours3);
            if (counter3 >= 0)
                ketemuTarget2Sj(contours3, counter3);
            else fungsikanKompas();
        } else if (contours1.size() > 0 && contours2.size() == 0 && contours3.size() == 0) {
            int counter1 = cekLingk(contours1);
            if (counter1 >= 0)
                ketemuLawTar2Sj(contours1, counter1);
            else fungsikanKompas();
        } else if (contours1.size() == 0 && contours2.size() > 0 && contours3.size() == 0) {
            int counter2 = cariPalingKanan(contours2);
            if (counter2 >= 0)
                ketemuLawTar2Sj(contours2, counter2);
            else fungsikanKompas();
        } else fungsikanKompas();

        return mRgba;
    }

    public void caTeng2BolPut(List<MatOfPoint> contours2, int counter1, int counter2) {
        Rect rect1 = Imgproc.boundingRect(contours2.get(counter1));
        Rect rect2 = Imgproc.boundingRect(contours2.get(counter2));

        Imgproc.putText(mRgba, "Height1" + rect1.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.3), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
        Imgproc.putText(mRgba, "Height2" + rect2.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));

        int x1 = rect1.x + rect1.width / 2;
        int y1 = rect1.y + rect1.height / 2;
        Point center = new Point(x1, y1);

        int x2 = rect2.x + rect2.width / 2;
        int y2 = rect2.y + rect2.height / 2;
        Point center2 = new Point(x2, y2);

//	        double selPus = Math.sqrt((x2-x1)^2 + (y2-y1)^2);
        int selisihX = Math.abs(x2 - x1);
//	        int selisihY = Math.abs(y2-y1);		
        Imgproc.putText(mRgba, "selY:   " + selisihX, new Point(mRgba.cols() / 1.3, mRgba.rows() * 0.1), Imgproc.FONT_HERSHEY_SIMPLEX, 0.3, new Scalar(0, 255, 0), 1);
//	        if(selPus >= 4){
        if (selisihX >= 5) {
            Imgproc.rectangle(mRgba, new Point(rect1.x, rect1.y), new Point(rect1.x + rect1.width, rect1.y + rect1.height), new Scalar(0, 0, 255));
            Imgproc.rectangle(mRgba, new Point(rect2.x, rect2.y), new Point(rect2.x + rect2.width, rect2.y + rect2.height), new Scalar(0, 0, 255));

            Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 0), 2, 8, 0);
            Imgproc.circle(mRgba, center2, 4, new Scalar(255, 0, 0), 2, 8, 0);

            xC = (x1 + x2) / 2;
            yC = (y1 + y2) / 2;
            Point centerC = new Point(xC, yC);

            Imgproc.putText(mRgba, "XCenter: " + xC + ", YCenter: " + yC, new Point(mRgba.cols() / 8, mRgba.rows() * 0.1),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 0));
            Imgproc.circle(mRgba, centerC, 4, new Scalar(255, 0, 0), 2, 8, 0);

            cariTengah(rect1.height, rect2.height);

            if (rect1.height > batTO && rect2.height > batTO)
                indGate = 1;
        }
    }

    public void ketemuPutihDiKa(List<MatOfPoint> contours, int counter) {
        Imgproc.putText(mRgba, "Counter  " + counter, new Point(mRgba.cols() / 12, mRgba.rows() * 0.7), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));

        Rect rect = Imgproc.boundingRect(contours.get(counter));
        Imgproc.putText(mRgba, "Height" + rect.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
        int x = rect.x + rect.width / 2;
        int y = rect.y + rect.height;
        Point center = new Point(x, y);

        Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
        Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 255), 3, 8, 0);

        cariKeKiri(rect.height);
        if (rect.height > batTO) {
            indGate = 1;
        }
    }

    public void ketemuPutihDiKi(List<MatOfPoint> contours, int counter) {

        Imgproc.putText(mRgba, "Counter  " + counter, new Point(mRgba.cols() / 12, mRgba.rows() * 0.7), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
        Rect rect = Imgproc.boundingRect(contours.get(counter));
        Imgproc.putText(mRgba, "Height" + rect.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
        int x = rect.x + rect.width / 2;
        int y = rect.y + rect.height;
        Point center = new Point(x, y);
        Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255));
        Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 255), 3, 8, 0);

        cariKeKanan(rect.height);
        if (rect.height > batTO)
            indGate = 1;
    }

    public void ketemuLawTar2Sj(List<MatOfPoint> contours, int counter) {
        Rect rect2 = Imgproc.boundingRect(contours.get(counter));
        Imgproc.putText(mRgba, "Height" + rect2.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
        int x = rect2.x + rect2.width / 2;
        int y = rect2.y + rect2.height;
        Point center = new Point(x, y);

        Imgproc.rectangle(mRgba, new Point(rect2.x, rect2.y), new Point(rect2.x + rect2.width, rect2.y + rect2.height), new Scalar(0, 0, 255));
        Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 255), 3, 8, 0);

        cariKeKanan(rect2.height);
        if (rect2.height >= batTO) {
            indGate = 1;
        }
    }

    public void ketemuLawTar1Sj(List<MatOfPoint> contours, int counter) {

        Rect rect1 = Imgproc.boundingRect(contours.get(counter));
        Imgproc.putText(mRgba, "Height" + rect1.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
        Imgproc.rectangle(mRgba, new Point(rect1.x, rect1.y), new Point(rect1.x + rect1.width, rect1.y + rect1.height), new Scalar(0, 0, 255));
        int x = rect1.x + rect1.width / 2;
        int y = rect1.y + rect1.height;
        Point center = new Point(x, y);
        Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 255), 3, 8, 0);
        cariKeKiri(rect1.height);

        if (rect1.height >= batTO) {
            indGate = 1;
        }
    }

    public void ketemuTarget1Sj(List<MatOfPoint> contours, int counter) {

        Rect rect1 = Imgproc.boundingRect(contours.get(counter));
        Imgproc.putText(mRgba, "Height" + rect1.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
        Imgproc.rectangle(mRgba, new Point(rect1.x, rect1.y), new Point(rect1.x + rect1.width, rect1.y + rect1.height), new Scalar(0, 0, 255));
        int x = rect1.x + rect1.width / 2;
        int y = rect1.y + rect1.height;
        Point center = new Point(x, y);
        Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 255), 3, 8, 0);

        if ((rect1.y + rect1.height) < (batHorzonB - batTG)) {
            xC = x;
            yC = y;
            cariTengah(rect1.height, rect1.height);
        } else {
            cariKeKanan(rect1.height);
            indGate = 1;
        }
    }

    public void ketemuTarget2Sj(List<MatOfPoint> contours, int counter) {

        Rect rect1 = Imgproc.boundingRect(contours.get(counter));
        Imgproc.putText(mRgba, "Height" + rect1.height, new Point(mRgba.cols() / 12, mRgba.rows() * 0.5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 0, 0));
        Imgproc.rectangle(mRgba, new Point(rect1.x, rect1.y), new Point(rect1.x + rect1.width, rect1.y + rect1.height), new Scalar(0, 0, 255));
        int x = rect1.x + rect1.width / 2;
        int y = rect1.y + rect1.height;
        Point center = new Point(x, y);
        Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 255), 3, 8, 0);

        if ((rect1.y + rect1.height) < (batHorzonB - batTG)) {
            xC = x;
            yC = y;
            cariTengah(rect1.height, rect1.height);
        } else {
            cariKeKiri(rect1.height);
            indGate = 1;
        }
    }

    public void prosesHindarObs(Rect rect) {

        int x = rect.x + rect.width / 2;
        int y = rect.y + rect.height / 2;
        Point center = new Point(x, y);

        Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 255));
        Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 0), 2, 8, 0);

        if ((rect.y + rect.height) >= (batHorzonB - batTG)) {
            obsAct = true;
            indGate = 1;
        } else obsAct = false;

        if (procGerakObs) {
            if (prefKan && !prefKir) {
                if (x <= 277) {
                    cariKeKanan(rect.height);
                } else {
                    cariKeKiri(rect.height);
                }
            } else if (!prefKan && prefKir) {
                if (x < 200) {
                    cariKeKanan(rect.height);
                } else {
                    cariKeKiri(rect.height);
                }
            } else {
                if (x < 203) {
                    cariKeKanan(rect.height);
                } else {
                    cariKeKiri(rect.height);
                }
            }
        }
    }

    public int cekLingk(List<MatOfPoint> kontur) {

        MatOfPoint approx = new MatOfPoint();
        double max = 0;
        int counter = -1;

        for (int i = 0; i < kontur.size(); i++) {
            MatOfPoint tempContour = kontur.get(i);
            MatOfPoint2f newMat = new MatOfPoint2f(tempContour.toArray());
            MatOfPoint2f newApprox = new MatOfPoint2f(approx.toArray());

            Imgproc.approxPolyDP(newMat, newApprox, Imgproc.arcLength(newMat, true) * 0.02, true);

            Rect rect = Imgproc.boundingRect(kontur.get(i));

            double ratTl = ((double) rect.height / (double) rect.width) - 1.0;
//	   double ratLt = ((double)rect.width/(double)rect.height)-1.0;
            int y = rect.y + (rect.height / 2);
            if (Imgproc.contourArea(kontur.get(i)) > max && y > batHorzonA && y < batHorzonB && rect.height >  LIM_T2 && rect.width >  LIM_L2 && rect.height <= 55 && rect.width <= 75 && ratTl <= 0.33 && newApprox.toArray().length >= 8 && newApprox.toArray().length <= 12) {
                counter = i;
                Imgproc.putText(mRgba, "L:  " + newApprox.toArray().length, new Point(mRgba.cols(), mRgba.rows() * 0.2), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 255, 0));
                max = Imgproc.contourArea(kontur.get(i));
            }
        }
        return counter;

    }


    public int cekLingkObs(List<MatOfPoint> kontur) {

        MatOfPoint approx = new MatOfPoint();
        double max = 0;
        int counter = -1;

        for (int i = 0; i < kontur.size(); i++) {
            MatOfPoint tempContour = kontur.get(i);
            MatOfPoint2f newMat = new MatOfPoint2f(tempContour.toArray());
            MatOfPoint2f newApprox = new MatOfPoint2f(approx.toArray());

            Imgproc.approxPolyDP(newMat, newApprox, Imgproc.arcLength(newMat, true) * 0.02, true);

            Rect rect = Imgproc.boundingRect(kontur.get(i));

            int y = rect.y + (rect.height / 2);
            if (y >= batHorzonA && y <= batHorzonB && Imgproc.contourArea(kontur.get(i)) > max && rect.height >  LIM_T2 && rect.width >  LIM_L2 && rect.height < 50 && rect.width < 65) {
                counter = i;
                Imgproc.putText(mRgba, "L:  " + newApprox.toArray().length, new Point(mRgba.cols(), mRgba.rows() * 0.2), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 255, 0));
                max = Imgproc.contourArea(kontur.get(i));
            }
        }
        return counter;

    }

    public boolean cekLingkP(List<MatOfPoint> kontur, int counter) {
        MatOfPoint approx = new MatOfPoint();
        MatOfPoint tempContour = kontur.get(counter);
        MatOfPoint2f newMat = new MatOfPoint2f(tempContour.toArray());
        MatOfPoint2f newApprox = new MatOfPoint2f(approx.toArray());

        Imgproc.approxPolyDP(newMat, newApprox, Imgproc.arcLength(newMat, true) * 0.02, true);

        Rect rect = Imgproc.boundingRect(kontur.get(counter));

        boolean ret;

        double ratTl = ((double) rect.height / (double) rect.width) - 1.0;
        double ratlT = ((double) rect.width / (double) rect.height) - 1.0;

        int y = rect.y + (rect.height / 2);
        if (y > batHorzonA && y < batHorzonB && rect.height > 10 && rect.width > 13 && ratTl <= 0.2 && ratlT < 0.77 && rect.height <= 50 && rect.width <= 65 && newApprox.toArray().length >= 8 && newApprox.toArray().length <= 12) {
            Imgproc.putText(mRgba, "LP:  " + newApprox.toArray().length, new Point(mRgba.cols(), mRgba.rows() * 0.4), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 255, 0));
            ret = true;
        } else ret = false;
        return ret;
    }

    public int cariPalingKiri(List<MatOfPoint> contours) {
        int kiri = 480;
        int counter = -1;

        for (int i = 0; i < contours.size(); i++) {
            boolean bentuk = cekLingkP(contours, i);
//        	int bentuk = cekLingk(contours);
            if (bentuk == true) {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                if (rect.x < kiri) {
                    kiri = rect.x;
                    counter = i;
                }
            }
        }
        return counter;

    }

    public int cariPalingKanan(List<MatOfPoint> contours) {
        int kanan = 0;
        int counter = -1;

        for (int i = 0; i < contours.size(); i++) {
            boolean bentuk = cekLingkP(contours, i);
            if (bentuk == true) {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                if (rect.x > kanan) {
                    kanan = rect.x;
                    counter = i;
                }
            }
        }
        return counter;

    }


    ///////////////////PROPERTI MISI 2//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void cariSegitiga(List<MatOfPoint> contours) {
        if (contours.size() > 0) {
            int counter = cekSegitiga(contours);
            if (counter >= 0) {
                setLabel(mRgba, "segi3", contours.get(counter));
                Rect r = Imgproc.boundingRect(contours.get(counter));

                xC = r.x + r.width / 2;
                yC = r.y + r.height / 2;

                Point center = new Point(xC, yC);
                Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 0), 4, 8, 0);
                cariTengah(xC, yC);
            } else fungsikanKompas();
        } else fungsikanKompas();
    }


    public void cariCross(List<MatOfPoint> contours) {
        if (contours.size() > 0) {
            int counter = cekCross(contours);

            if (counter >= 0) {
                setLabel(mRgba, "Cross", contours.get(counter));
                Rect r = Imgproc.boundingRect(contours.get(counter));
                xC = r.x + r.width / 2;
                yC = r.y + r.height / 2;
                Point center = new Point(xC, yC);
                Imgproc.circle(mRgba, center, 4, new Scalar(255, 0, 0), 4, 8, 0);
                cariTengah(xC, yC);
            } else fungsikanKompas();
        } else fungsikanKompas();
    }


    public void cariBundar(List<MatOfPoint> contours) {
        if (contours.size() > 0) {
            int counter = cekCir(contours);

            if (counter >= 0) {
                setLabel(mRgba, "CIR", contours.get(counter));
                Rect r = Imgproc.boundingRect(contours.get(counter));
                xC = r.x + r.width / 2;
                yC = r.y + r.height / 2;
                Point center = new Point(xC, yC);
                Imgproc.circle(mRgba, center, 8, new Scalar(255, 0, 0), 4, 8, 0);
                cariTengah(xC, yC);
            } else fungsikanKompas();
        } else fungsikanKompas();
    }

    public void setLabel(Mat im, String label, MatOfPoint contour) {
        int fontface = Imgproc.FONT_HERSHEY_SIMPLEX;
        double scale = 0.4;
        int thickness = 1;
        int[] baseline = {0};

        Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
        Rect r = Imgproc.boundingRect(contour);

        Point pt = new Point(r.x + ((r.width - text.width) / 2), r.y + ((r.height + text.height) / 2));
        Point pt1 = new Point(0, 0);
        Point pt2 = new Point(text.width, -text.height);
        double pt1x = pt.x + pt1.x;
        double pt1y = pt.y + pt1.y;
        double pt2x = pt.x + pt2.x;
        double pt2y = pt.y + pt2.y;
        Point ptf1 = new Point(pt1x, pt1y);
        Point ptf2 = new Point(pt2x, pt2y);

        Imgproc.rectangle(im, ptf1, ptf2, new Scalar(0, 255, 255), Core.FILLED);
        Imgproc.putText(im, label, pt, fontface, scale, new Scalar(255, 0, 0), thickness);
    }

    public int cekSegitiga(List<MatOfPoint> kontur) {
        MatOfPoint approx = new MatOfPoint();
        double max = 0;
        int counter = -1;

        for (int i = 0; i < kontur.size(); i++) {

            MatOfPoint tempContour = kontur.get(i);
            MatOfPoint2f newMat = new MatOfPoint2f(tempContour.toArray());
            MatOfPoint2f newApprox = new MatOfPoint2f(approx.toArray());

            Imgproc.approxPolyDP(newMat, newApprox, Imgproc.arcLength(newMat, true) * 0.02, true);

            if (Imgproc.contourArea(kontur.get(i)) > max && Math.abs(Imgproc.contourArea(newMat)) > 400 && newApprox.toArray().length >= 3 && newApprox.toArray().length <= 4) {
                counter = i;
                Imgproc.putText(mRgba, "L:  " + newApprox.toArray().length, new Point(mRgba.cols(), mRgba.rows() * 0.2), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(255, 255, 0));
                max = Imgproc.contourArea(kontur.get(i));
            }
        }
        return counter;
    }

    public int cekCross(List<MatOfPoint> kontur) {
        MatOfPoint approx = new MatOfPoint();
        double max = 0;
        int counter = -1;

        for (int i = 0; i < kontur.size(); i++) {

            MatOfPoint tempContour = kontur.get(i);
            MatOfPoint2f newMat = new MatOfPoint2f(tempContour.toArray());
            MatOfPoint2f newApprox = new MatOfPoint2f(approx.toArray());

            Imgproc.approxPolyDP(newMat, newApprox, Imgproc.arcLength(newMat, true) * 0.02, true);

            if (Imgproc.contourArea(kontur.get(i)) > max && Math.abs(Imgproc.contourArea(newMat)) > 400 && !Imgproc.isContourConvex(kontur.get(i)) && newApprox.toArray().length >= 11 && newApprox.toArray().length <= 13) {
                counter = i;
                Imgproc.putText(mRgba, "L:  " + newApprox.toArray().length, new Point(mRgba.cols(), mRgba.rows() * 0.2), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(0, 0, 255));
                max = Imgproc.contourArea(kontur.get(i));
            }
        }
        return counter;
    }

    public int cekCir(List<MatOfPoint> kontur) {
        MatOfPoint approx = new MatOfPoint();
        double max = 0;
        int counter = -1;

        for (int i = 0; i < kontur.size(); i++) {

            MatOfPoint tempContour = kontur.get(i);
            MatOfPoint2f newMat = new MatOfPoint2f(tempContour.toArray());
            MatOfPoint2f newApprox = new MatOfPoint2f(approx.toArray());

            Imgproc.approxPolyDP(newMat, newApprox, Imgproc.arcLength(newMat, true) * 0.02, true);

            double area = Imgproc.contourArea(kontur.get(i));
            Rect r = Imgproc.boundingRect(kontur.get(i));
            double ratTl = Math.abs(1 - (double) r.height / r.width);
            int radius = r.width / 2;

            if (Imgproc.contourArea(kontur.get(i)) > max && ratTl <= 0.25 && Math.abs(1 - (area / (Math.PI * Math.pow(radius, 2)))) <= 0.25 && Math.abs(Imgproc.contourArea(newMat)) > 400 && newApprox.toArray().length >= 8 && newApprox.toArray().length <= 12) {
                counter = i;
                Imgproc.putText(mRgba, "L:  " + newApprox.toArray().length, new Point(mRgba.cols(), mRgba.rows() * 0.2), Imgproc.FONT_HERSHEY_SIMPLEX, 0.75, new Scalar(0, 0, 255));
                max = Imgproc.contourArea(kontur.get(i));
            }
        }
        return counter;
    }


    ///////////////////Parsing Data Kirim//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static byte[] charToByteArray(int a) {
        byte[] ret = new byte[2];
        ret[1] = (byte) (a & 0xFF);
        ret[0] = (byte) ((a >> 8) & 0xFF);
        return ret;
    }

    public static byte[] oneByteToByteArray(int a) {
        byte[] ret = new byte[1];
        ret[0] = (byte) (a & 0xFF);
        return ret;
    }

    public static byte[] dataGpsToByteArray(double a) {
        byte[] ret = new byte[5];
        ret[4] = (byte) ((a / 0.0000001) % 100);
        ret[3] = (byte) ((a / 0.00001) % 100);
        ret[2] = (byte) ((a / 0.001) % 100);
        ret[1] = (byte) ((a / 0.1) % 100);
        ret[0] = (byte) ((a / 10) % 100);
        return ret;
    }


///////////////////LISTENER SENSOR-SENSOR//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final SensorEventListener LightSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
//			  nLuxAcc = accuracy;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            nLux1 = event.values[0];
//            nLux2 = event.values[1];
        }

    };

    float xPitch;
    float yRoll;
    float zAzim;

    public class MySensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor == mAccelerometer) {
                event.values[1] = 0;
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                mLastAccelerometerSet = true;
            } else if (event.sensor == mMagnetometer) {
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mLastMagnetometerSet = true;
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(mR, mOrientation);

                float azimuthInRadians = mOrientation[0];
                float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
                mCurrentDegree = -azimuthInDegress;
            }
            if (event.sensor == mOrienta) {
                if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                    return;
                }

                xPitch = event.values[1];
                yRoll = event.values[2];
                zAzim = event.values[0];
            }

            Xasli = Math.abs(mCurrentDegree);

            Xroll = 0;

            float Xselisih = Xasli - Xr;

            if (Xselisih < 0) {
                Xroll = 360 - Math.abs(Xselisih);
            } else {
                Xroll = Xselisih + Xroll;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    }

    public class MyLocationListener implements LocationListener {

        // Dipanggil saat ada perubahan lokasi geografis pengguna
        @Override
        public void onLocationChanged(Location location) {
            // Mendapatkan nilai latitude dari lokasi terbaru
            latitude = location.getLatitude();

            // Mendapatkan nilai longitude dari lokasi terbaru
            longitude = location.getLongitude();
        }

        // Dipanggil saat provider dinon-aktifkan oleh pengguna
        @Override
        public void onProviderDisabled(String provider) {
            String message = "GPS disabled";
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }

        // dipanggil saat provider diaktifkan oleh pengguna
        @Override
        public void onProviderEnabled(String provider) {
            String message = "GPS enabled";
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }

        // dipanggil saat ada perubahan status pada provider
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }


    ///////////////////BLUETOOTH//////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
    protected void connectToBTDevice(final BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        new AlertDialog.Builder(this).setMessage("Terhubung ke " + device.getName() + " ?").setNegativeButton("Tidak", null).setPositiveButton("Ya", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Menunggu devais BT");
                pd.setCancelable(true);
                pd.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (btConnectionThread != null) {
                            btConnectionThread.cancel();
                        }
                    }
                });
                pd.show();

                btConnectionThread = new BTConnectionThread(device);
                btConnectionThread.start();
            }
        }).create().show();

    }

    private InputStream inStream;

    class BTChannelThread extends Thread {

        private boolean keepAlive = true;
        private OutputStream outStream;
        //		private InputStream inStream;
        private BluetoothSocket btSocket;
        private int a = 0;

        public BTChannelThread(BluetoothSocket btSocket) {

            this.btSocket = btSocket;

            try {
                outStream = btSocket.getOutputStream();
                inStream = btSocket.getInputStream();
            } catch (IOException e) {
            }
        }

        @Override
        public void run() {
            while (keepAlive) {
                // do nothing
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }

        public void sendCommand(byte[] bytes) {
            try {
                outStream.write(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int receiveCommand() {
            byte b[] = new byte[1];
            try {
                if (inStream.available() > 0) {
                    inStream.read(b, 0, 1);
                    a = (int) b[0] & 0xff;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return a;
        }

        public int getAvailable() {
            int a = 0;
            try {
                a = inStream.available();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return a;


        }

        public void cancel() {

            keepAlive = false;

            try {
                btSocket.close();
            } catch (IOException e) {
            }
        }
    }

    int dataAsli;
    int dataMisi;
    int dataIngate;
    int dataOutgate;
    int dataSimbol;
    int dataColor;


    public class BTConnectionThread extends Thread {
        private Context context = null;
        private BluetoothDevice device;
        private BluetoothSocket btSocket;
        private BTChannelThread btChannelThread;
        private byte data_ke;
        private byte penanda;

        private int BToothVal;

        // Add the constant here
        private static final int REQUEST_BLUETOOTH_PERMISSION = 1001;

        public BTConnectionThread(BluetoothDevice device) {
            this.device = device;
            this.context = context;
        }

        @Override
        public void run() {
            // Check Bluetooth permissions
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN},
                        REQUEST_BLUETOOTH_PERMISSION);
                return;
            }

            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            try {
                btSocket = device.createRfcommSocketToServiceRecord(MAGIC_UUID);
                btChannelThread = new BTChannelThread(btSocket);

                btSocket.connect();

                if (pd != null) {
                    pd.dismiss();
                }

                btChannelThread.start();
            } catch (IOException e) {
                Log.e(TAG, "Tidak dapat terhubung ke devais bluetooth " + device.getName(), e);
                try {
                    btSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "Error closing socket", e1);
                }
            }
        }

        public void cancel() {
            try {
                if (btSocket != null) {
                    btSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing Bluetooth socket", e);
            }
        }

        public void write(byte[] bytes) {
            if (btChannelThread != null) {
                btChannelThread.sendCommand(bytes);
            } else {
                Log.e(TAG, "BTChannelThread is not initialized");
            }
        }

        public int read() {
            BToothVal = btChannelThread.receiveCommand();
            return BToothVal;
        }

        private void procParamSTM() {
            try {
                while (inStream != null && inStream.available() > 0) {
                    byte dataAsli = (byte) inStream.read();

                    if (penanda == 0 && dataAsli == 97) {
                        penanda = 1;
                    } else if (penanda == 1 && dataAsli == 105) {
                        penanda = 2;
                    } else if (penanda == 2 && dataAsli == 117) {
                        penanda = 3;
                    } else if (penanda == 3) {
                        switch (data_ke) {
                            case 0: dataMisi = dataAsli; data_ke = 1; break;
                            case 1: dataIngate = dataAsli; data_ke = 2; break;
                            case 2: dataOutgate = dataAsli; data_ke = 3; break;
                            case 3: dataSimbol = dataAsli; data_ke = 4; break;
                            case 4: dataColor = dataAsli; data_ke = 0; penanda = 0; break;
                        }
                    } else {
                        penanda = 0;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error processing STM data", e);
            }
        }
    }

    private void applyCameraSettings() {
        try {
            mOpenCvCameraView.setCameraFeature(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            mOpenCvCameraView.setFocusDistance(SeekBarVal11.focusDistanceValue);

            mOpenCvCameraView.setCameraFeature(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);  // Disable auto exposure
            mOpenCvCameraView.setCameraFeature(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, SeekBarVal11.ExVal);

            if (SeekBarVal11.whiteBalanceMode == CaptureRequest.CONTROL_AWB_MODE_OFF) {
                mOpenCvCameraView.setCameraFeature(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
            } else {
                mOpenCvCameraView.setCameraFeature(CaptureRequest.CONTROL_AWB_LOCK, false);
                mOpenCvCameraView.setCameraFeature(CaptureRequest.CONTROL_AWB_MODE, SeekBarVal11.whiteBalanceMode);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error applying camera settings: ", e);
        }
    }
}

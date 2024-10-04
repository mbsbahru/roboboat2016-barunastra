package com.mbsbahru.roboboat2016_barunastra;
//import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
//import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
//import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
//import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.widget.Toast;

public class SeekBarVal22 extends Activity implements OnTouchListener, CvCameraViewListener2 {
	private static final String TAG = "SeekActivity";
	private static final int    VIEW_MODE_HSV21   = 1;
	private static final int    VIEW_MODE_HSV22   = 0;
	private static final int    VIEW_MODE_HSV23   = 2;
	private static final int    VIEW_MODE_HSV24   = 3;
	private static final int    VIEW_MODE_HSV25   = 4;
	private static final int    VIEW_MODE_BALIK   = 6;
	private MenuItem     mItemHSV21;
	private MenuItem     mItemHSV22;
	private MenuItem     mItemHSV23;
	private MenuItem     mItemHSV24;
	private MenuItem     mItemHSV25;
	private MenuItem     mItemBalik;

	private static final int    VIEW_MODE_HSV11   = 5;
	private MenuItem     mItemHsv11;

	private MenuItem 	mItemExposure;
	private int val = 0;
	private int          mViewMode;
	private MisiView  mOpenCvCameraView;
	private Mat mRgba;
	private Mat mHSV;
	public static int Hmin = 0;
	public static int Hmax = 3;
	public static int Smin= 0;
	public static int Smax = 3;
	public static int Vmin = 230;
	public static int Vmax = 255;
	public static int Dilate = 1;
	public static int Erode = 1;

	private Mat mBiner;
	private boolean idxBiner = false;
	private Mat mDisplay;

	//	private boolean              mIsColorSelected = false;
	private Scalar               mBlobColorRgba;
	private Scalar               mBlobColorHsv;
	private BlobDetector  		 mDetector;
	private Mat                  mSpectrum;
	private Size                 SPECTRUM_SIZE;

	private Scalar CONTOUR_COLOR;

	public static int batHorzonA = 65;
	public static int batHorzonB = 200;

	protected boolean bIsHPressed;
	protected boolean bIsSPressed;
	protected boolean bIsVPressed;
	protected boolean bIsEDPressed;

	private SeekBar seekHmin;
	private SeekBar seekHmax;
	private SeekBar seekSmin;
	private SeekBar seekSmax;
	private SeekBar seekVmin;
	private SeekBar seekVmax;
	private SeekBar seekErode;
	private SeekBar seekDilate;
	private boolean mIsDisplayTouched;
	private SeekAdapter sH;
	private SeekAdapter sS;
	private SeekAdapter sV;
	private SeekAdapter sED;


	private boolean bIsHorPressed;
	private SeekBar seekHorMin;
	private SeekBar seekHorMax;
	private SeekAdapter sHor;

	TextView HSV;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
//		           Log.i(TAG, "OpenCV loaded successfully");  
					// Load native library after(!) OpenCV initialization
					//System.loadLibrary("mixed_sample");
					mOpenCvCameraView.enableView();
					mOpenCvCameraView.setOnTouchListener(SeekBarVal22.this);
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
		}
	};
	private MySensorEventListener mySensorEventListener;
	private Sensor mOrienta;
	public SeekBarVal22() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.biner22);

		SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		Sensor LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mySensorManager.registerListener(LightSensorListener, LightSensor, SensorManager.SENSOR_DELAY_NORMAL);

		mySensorEventListener = new MySensorEventListener();
		mOrienta = mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mySensorManager.registerListener(mySensorEventListener, mOrienta, SensorManager.SENSOR_DELAY_GAME);

		mOpenCvCameraView = findViewById(R.id.activity_surface_view);
//		mOpenCvCameraView.setMaxFrameSize(480, 320);
		mOpenCvCameraView.setVisibility(MisiView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		mItemHSV21 = menu.add("HSV21");
//		mItemHSV22 = menu.add("HSV22");
		mItemHSV23 = menu.add("HSV23");
		mItemHSV24 = menu.add("HSV24");
		mItemHSV25 = menu.add("HSV25");
		mItemHsv11 = menu.add("HSV11");
		mItemBalik = menu.add("BT Pairing");
//		mItemExposure = menu.add("exp");
		return true;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	@Override
	protected void onDestroy(){
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		super.onDestroy();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mHSV = new Mat(height, width, CvType.CV_8UC4);
		mBiner = new Mat(height,width,CvType.CV_8UC1);
		mDisplay = new Mat();
		mDetector = new BlobDetector();
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
		SPECTRUM_SIZE = new Size(mRgba.cols()/1.5, 64);
		sH = new SeekAdapter();
		sS = new SeekAdapter();
		sV = new SeekAdapter();
		sED = new SeekAdapter();
		sHor = new SeekAdapter();
		CONTOUR_COLOR = new Scalar(255,0,0,255);

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		mRgba.release();
		mHSV.release();
		mBiner.release();
		mDisplay.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		final int viewMode = mViewMode;
		mRgba = inputFrame.rgba();

		applyCameraSettings();

		Imgproc.putText(mRgba, "nLUX1:   " + nLux1, new Point ( 350, 70), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
		Imgproc.putText(mRgba, "nLUX2:   " + nLux2, new Point ( 350, 85), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

		if (viewMode==VIEW_MODE_HSV23){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal22.this, SeekBarVal23.class);
			startActivity(intent);
			finish();
		}
		else if (viewMode==VIEW_MODE_HSV21){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal22.this, SeekBarVal21.class);
			startActivity(intent);
			finish();
		}
		else if (viewMode==VIEW_MODE_HSV24){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal22.this, SeekBarVal24.class);
			startActivity(intent);
			finish();
		}
		else if (viewMode==VIEW_MODE_HSV25){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal22.this, SeekBarVal25.class);
			startActivity(intent);
			finish();
		}
		else if (viewMode==VIEW_MODE_HSV11){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal22.this, SeekBarVal11.class);
			startActivity(intent);
			finish();
		}
		else if (viewMode==VIEW_MODE_BALIK){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal22.this, BluetoothConf.class);
			startActivity(intent);
			finish();
		}
		else if (viewMode==VIEW_MODE_HSV22){

//			if (mIsColorSelected) {

			Button bH = findViewById(R.id.tombolH);
			Button bS = findViewById(R.id.tombolS);
			Button bV = findViewById(R.id.tombolV);
			Button bED = findViewById(R.id.tombolED);
			Button bHor = findViewById(R.id.tombolHor);

			Button bMenu = findViewById(R.id.tombolMenu);
			bMenu.setOnClickListener(v -> {
				// TODO Auto-generated method stub
				openOptionsMenu();
				invalidateOptionsMenu();
			});

			seekHmin= findViewById(R.id.min);
			seekHmax= findViewById(R.id.max);
			seekSmin= findViewById(R.id.min);
			seekSmax= findViewById(R.id.max);
			seekVmin= findViewById(R.id.min);
			seekVmax= findViewById(R.id.max);
			seekErode= findViewById(R.id.min);
			seekDilate= findViewById(R.id.max);
			seekHorMin= findViewById(R.id.min);
			seekHorMax= findViewById(R.id.max);

			bH.setOnClickListener(v -> {
				// TODO Auto-generated method stub
				sH.setProgress(seekHmin, seekHmax, Hmin, Hmax, 262);
				sH.seekProgress();
				bIsHPressed = true;
				bIsSPressed = false;
				bIsVPressed = false;
				bIsEDPressed = false;
				bIsHorPressed = false;
				mIsDisplayTouched = false;

			});
			bS.setOnClickListener(v -> {
				// TODO Auto-generated method stub
				sS.setProgress(seekSmin, seekSmax, Smin, Smax, 262);
				sS.seekProgress();
				bIsSPressed = true;
				bIsHPressed = false;
				bIsVPressed = false;
				bIsEDPressed = false;
				bIsHorPressed = false;
				mIsDisplayTouched = false;
			});
			bV.setOnClickListener(v -> {
				// TODO Auto-generated method stub
				sV.setProgress(seekVmin, seekVmax, Vmin, Vmax, 262);
				sV.seekProgress();
				sV.seekBarChange();
				bIsHPressed = false;
				bIsSPressed = false;
				bIsEDPressed = false;
				bIsVPressed = true;
				bIsHorPressed = false;
				mIsDisplayTouched = false;
			});
			bED.setOnClickListener(v -> {
				// TODO Auto-generated method stub
				sED.setProgress(seekErode, seekDilate, Erode, Dilate, 21);
				sED.seekProgress();
				sED.seekBarChange();
				bIsEDPressed = true;
				bIsHPressed = false;
				bIsSPressed = false;
				bIsVPressed = false;
				bIsHorPressed = false;
				mIsDisplayTouched = false;

				if(idxBiner)
					idxBiner = false;
				else
					idxBiner = true;
			});

			bHor.setOnClickListener(v -> {
				// TODO Auto-generated method stub
				sHor.setProgress(seekHorMin, seekHorMax, batHorzonA, batHorzonB, mRgba.rows());
				sHor.seekProgress();
				sHor.seekBarChange();
				bIsEDPressed = false;
				bIsHPressed = false;
				bIsSPressed = false;
				bIsVPressed = false;
				bIsHorPressed = true;
				mIsDisplayTouched = false;

			});


			if(bIsHPressed){
				Hmin = sH.getFirstVal();
				Hmax = sH.getSecondVal();
				sH.setProgress(seekHmin, seekHmax, Hmin, Hmax, 262);
				sH.seekProgress();
				sH.seekBarChange();
			}

			if(bIsSPressed){
				Smin = sS.getFirstVal();
				Smax = sS.getSecondVal();
				sS.setProgress(seekSmin, seekSmax, Smin, Smax, 262);
				sS.seekProgress();
				sS.seekBarChange();
			}

			if(bIsVPressed){
				Vmin = sV.getFirstVal();
				Vmax = sV.getSecondVal();
				sV.setProgress(seekVmin, seekVmax, Vmin, Vmax, 262);
				sV.seekProgress();
				sV.seekBarChange();
			}

			if(bIsEDPressed){
				Erode = sED.getFirstVal();
				Dilate = sED.getSecondVal();
				sED.setProgress(seekErode, seekDilate, Erode, Dilate, 10);
				sED.seekProgress();
				sED.seekBarChange();
			}

			if(bIsHorPressed){
				batHorzonA = sHor.getFirstVal();
				batHorzonB = sHor.getSecondVal();
				sHor.setProgress(seekHorMin, seekHorMax, batHorzonA, batHorzonB, mRgba.rows());
				sHor.seekProgress();
				sHor.seekBarChange();
			}

			if(idxBiner){
				Scalar hsv_min2 = new Scalar(Hmin, Smin, Vmin, 0);
				Scalar hsv_max2 = new Scalar(Hmax, Smax, Vmax, 0);
				Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL,4);
				Core.inRange(mHSV, hsv_min2, hsv_max2, mBiner);
				Imgproc.erode(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Erode+1,2*Erode+1), new Point (Erode,Erode)));
				Imgproc.dilate(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Dilate+1,2*Dilate+1), new Point (Dilate,Dilate)));
				Imgproc.dilate(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Dilate+1,2*Dilate+1), new Point (Dilate,Dilate)));
				Imgproc.erode(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Erode+1,2*Erode+1), new Point (Erode,Erode)));
				mDisplay = mBiner;
			}
			else{

				if(mIsDisplayTouched){
					Hmin = (int) mDetector.getHmin();
					Hmax = (int) mDetector.getHmax();
					Smin = (int) mDetector.getSmin();
					Smax = (int) mDetector.getSmax();
					Vmin = (int) mDetector.getVmin();
					Vmax = (int) mDetector.getVmax();
				}

				mDetector.process(mRgba, new Scalar(Hmin, Smin, Vmin), new Scalar(Hmax, Smax, Vmax), Dilate, Erode);

				List<MatOfPoint> contours = mDetector.getContours();
				Log.e(TAG, "Contours count: " + contours.size());

//	            param1 = mDetector.getParam1();
//	            param2 = mDetector.getParam2();

				Imgproc.fillPoly(mRgba, contours, CONTOUR_COLOR);
				Imgproc.drawContours(mRgba, contours, -1, new Scalar(0, 255, 0), 3);
//	            Imgproc.drawContours(mRgba, contours, -1, new Scalar((mBlobColorRgba.val[0]+63>255)?mBlobColorRgba.val[0]-63:mBlobColorRgba.val[0]+63, (mBlobColorRgba.val[1]+63>255)?mBlobColorRgba.val[1]-63:mBlobColorRgba.val[1]+63, (mBlobColorRgba.val[2]+63>255)?mBlobColorRgba.val[2]-63:mBlobColorRgba.val[2]+63), 5);

				Imgproc.putText(mRgba, "valBot:   " + Hmin + ",  " + Smin + ",  " + Vmin + ",  " + Erode, new Point (mRgba.cols() / 8, mRgba.rows() * 0.05), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
				Imgproc.putText(mRgba, "valMax:   " + Hmax + ",  " + Smax + ",  " + Vmax + ",  " + Dilate, new Point (mRgba.cols() / 8, mRgba.rows() * 0.1), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);

				Mat colorLabel = mRgba.submat(2, 34, 446, 478);
				colorLabel.setTo(mBlobColorRgba);

//	            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
//	            mSpectrum.copyTo(spectrumLabel);
				mDisplay = mRgba;
			}
			Imgproc.line(mRgba, new Point(0, batHorzonA), new Point(480, batHorzonA), new Scalar( 255, 0, 255 ), 1, 8 ,0 );
			Imgproc.line(mRgba, new Point(0, batHorzonB), new Point(480, batHorzonB), new Scalar( 255, 0, 255 ), 1, 8 ,0 );
			Imgproc.line(mRgba, new Point(0, batHorzonB-SeekBarVal23.TGate), new Point(480, batHorzonB-SeekBarVal23.TGate), new Scalar( 0, 255, 255 ), 1, 8 ,0 );
			Imgproc.line(mRgba, new Point(0, batHorzonB-SeekBarVal23.TObs), new Point(480, batHorzonB-SeekBarVal23.TObs), new Scalar( 0, 255, 255 ), 1, 8 ,0 );
			Imgproc.line(mRgba, new Point(0, batHorzonB), new Point(480, batHorzonB), new Scalar( 255, 0, 255 ), 1, 8 ,0 );
			Imgproc.putText(mRgba, "G "+ SeekBarVal23.TGate , new Point (0, batHorzonB-SeekBarVal23.TGate), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
			Imgproc.putText(mRgba, " "+ (batHorzonB-batHorzonA) , new Point (0, (batHorzonB+batHorzonA)/2), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
			Imgproc.putText(mRgba, " "+ batHorzonA, new Point (0, batHorzonA), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
			Imgproc.putText(mRgba, " " + batHorzonB, new Point (0, batHorzonB), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
			Imgproc.putText(mRgba, "Exp:  " + val, new Point (5, 210), Imgproc.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
			Imgproc.putText(mRgba, "HSV 2", new Point (380, 210), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
			Imgproc.putText(mRgba, "x, y, z: " + xPitch + ", " + yRoll + ", "  + zAzim, new Point (240, 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(0, 0, 255), 1);
		}
		return mDisplay;

	}

	public boolean onOptionsItemSelected(MenuItem item) {

//	if (item == mItemAsli) {  
//      mViewMode = VIEW_MODE_ASLI;      
//    } else 
		if (item == mItemHSV21) {
			mViewMode = VIEW_MODE_HSV21;
		} else if (item == mItemHSV22) {
			mViewMode = VIEW_MODE_HSV22;
		} else if (item == mItemHSV23) {
//    	if(val){
//    	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//         String currentDateandTime = sdf.format(new Date());
//         String fileName = Environment.getExternalStorageDirectory().getPath() +
//                                "/sample_picture_" + currentDateandTime + ".jpg";
//         mOpenCvCameraView.takePicture(fileName);
//         Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
//    	}
			mViewMode = VIEW_MODE_HSV23;
		} else if (item == mItemHSV24) {
			mViewMode = VIEW_MODE_HSV24;
		} else if (item == mItemHSV25) {
			mViewMode = VIEW_MODE_HSV25;
		} else if (item == mItemHsv11) {
			mViewMode = VIEW_MODE_HSV11;
		} else if (item == mItemBalik){
			mViewMode = VIEW_MODE_BALIK;
		}  else if (item == mItemExposure){
			if(val== 1)
				val = 0;
			else
				val = 1;
		}
		return true;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int cols = mRgba.cols();
		int rows = mRgba.rows();

//    int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
//    int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

//    int x = (int)event.getX() - xOffset;
//    int y = (int)event.getY() - yOffset;

//		int x = (int)(event.getX() * 0.25);
//		int y = (int)(event.getY() * 0.296296296);

		int x = (int)(event.getX() - (v.getMeasuredWidth() - cols)/2);
		int y = (int)(event.getY());

		Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

		if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

		Rect touchedRect = new Rect();

		touchedRect.x = (x>4) ? x-4 : 0;
		touchedRect.y = (y>4) ? y-4 : 0;

		touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
		touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

		Mat touchedRegionRgba = mRgba.submat(touchedRect);

		Mat touchedRegionHsv = new Mat();
		Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

		// Calculate average color of touched region
		int pointCount = touchedRect.width*touchedRect.height;

//		mBlobColorHsv = Core.sumElems(touchedRegionHsv);

		for (int i = 0; i < mBlobColorHsv.val.length; i++)
			mBlobColorHsv.val[i] /= pointCount;

		mBlobColorRgba = converScalarHSV22Rgba(mBlobColorHsv);

		Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
				", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

		mDetector.setHsvColorPH(mBlobColorHsv);

		Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

//    mIsColorSelected = true;

		bIsHPressed = false;
		bIsSPressed = false;
		bIsVPressed = false;
		bIsEDPressed = false;
		bIsHorPressed = false;
		mIsDisplayTouched = true;

		touchedRegionRgba.release();
		touchedRegionHsv.release();

		return false;
	}
	private Scalar converScalarHSV22Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

		return new Scalar(pointMatRgba.get(0, 0));
	}

	float nLux1;
	float nLux2;
	private final SensorEventListener LightSensorListener = new SensorEventListener(){

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
//			  nLuxAcc = accuracy;
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			nLux1 = event.values[0];
//			nLux2 = event.values[1];
		}

	};

	float xPitch;
	float yRoll;
	float zAzim;
	public class MySensorEventListener implements SensorEventListener{

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub

			if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
			{
				return;
			}

			xPitch	= event.values[1];
			yRoll	= event.values[2];
			zAzim	= event.values[0];
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

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
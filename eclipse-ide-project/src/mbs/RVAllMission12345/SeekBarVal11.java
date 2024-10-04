package mbs.RVAllMission12345;

// y78

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
import org.opencv.core.MatOfPoint2f;
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
import android.os.Bundle;
//import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
//import android.widget.Toast;

public class SeekBarVal11 extends Activity implements CvCameraViewListener2, OnTouchListener {
   private static final String TAG = "SeekActivity";
   private static final int    VIEW_MODE_HSV11   = 0;  
   private static final int    VIEW_MODE_HSV12   = 1;
   private static final int    VIEW_MODE_HSV21   = 2;
   private static final int    VIEW_MODE_BALIK   = 3; 
   
   private MenuItem     mItemHsv11; 
   private MenuItem     mItemHsv12; 
   private MenuItem     mItemHsv21; 
   private MenuItem     mItemBalik;
   private int          mViewMode; 
   
   private float nLux;
   private float nLuxAcc;

   private MisiView  mOpenCvCameraView;
   
   private Mat mRgba;
   private Mat mHSV;
   
//   private SubMenu mWhiteBalanceMenu;
//   private MenuItem[] mWhiteBalanceMenuItems;
   
   public static int Hmin1 = 0;
   public static int Hmax1 = 25;
   public static int Smin1 = 104;
   public static int Smax1 = 255;
   public static int Vmin1 = 44;
   public static int Vmax1 = 255;
   public static int Dilate1;
   public static int Erode1;
   public static int ExVal = 0;
   public static int WBidx = 2;
   
   public static int batTiangBir = 45;
   public static int batTiangH = 280;
   
   public static int ExpVal = 8;
   public static String valWB = "daylight";
   
   private Boolean val = false;
   private Sensor		mOrienta;
   
   private SeekBar seekHmin1;
   private SeekBar seekHmax1;
   private SeekBar seekSmin1;
   private SeekBar seekSmax1;
   private SeekBar seekVmin1;
   private SeekBar seekVmax1;
   private SeekBar seekErode;
   private SeekBar seekDilate;
   private SeekBar seekEV;
   private SeekBar seekWB;
   private SeekBar seekBatBi;
   private SeekBar seekBatHi;
   private boolean mIsDisplayTouched;
   private SeekAdapter sH;
   private SeekAdapter sS;
   private SeekAdapter sV;
   private SeekAdapter sED;
   private SeekAdapter sEW;
   private SeekAdapter sT;
   private Mat mSpectrum;
   private Scalar mBlobColorRgba;
   private Scalar mBlobColorHsv;
   private Size SPECTRUM_SIZE;
   private BlobDetector mDetector;  
   protected boolean bIsHPressed;
   protected boolean bIsSPressed;
   protected boolean bIsVPressed;
   protected boolean bIsEDPressed;
   protected boolean bIsEWPressed;
   private boolean bIsBatPressed;
   
   private SensorEventListener mySensorEventListener;
	
   private Mat mDisplay;
   private Mat mBiner;
   private boolean idxBiner =false;
   
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {  
		
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
		           mOpenCvCameraView.setOnTouchListener(SeekBarVal11.this);
		         } break;  
		         default:
		         {  
		           super.onManagerConnected(status);  
		         } break;  
		       }  
		     }  
		   };
	private MenuItem mItemExposure;
	private Scalar CONTOUR_COLOR;
		   public SeekBarVal11() {  
			     Log.i(TAG, "Instantiated new " + this.getClass());  
			   }  
		   
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	 Log.i(TAG, "called onCreate"); 
    	super.onCreate(savedInstanceState); 
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
		setContentView(R.layout.biner11);
		
		SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    Sensor LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
	    mySensorManager.registerListener(LightSensorListener, LightSensor, SensorManager.SENSOR_DELAY_NORMAL);
	    
	    mySensorEventListener = new MySensorEventListener();
	    mOrienta = mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    mySensorManager.registerListener(mySensorEventListener, mOrienta, SensorManager.SENSOR_DELAY_GAME);
		 
		mOpenCvCameraView = (MisiView) findViewById(R.id.activity_surface_view);
		mOpenCvCameraView.setMaxFrameSize(480, 320);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    
    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
    	Log.i(TAG, "called onCreateOptionsMenu");
    	mItemHsv11 = menu.add("HSV11");  
    	mItemHsv12 = menu.add("HSV12");
    	mItemHsv21 = menu.add("HSV21");
    	mItemBalik = menu.add("Balik");
        mItemExposure = menu.add("exp");
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
      OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);  
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
    	mDetector = new BlobDetector();
	    sH = new SeekAdapter();
	    sS = new SeekAdapter();
	    sV = new SeekAdapter();
	    sED = new SeekAdapter();
	    sEW = new SeekAdapter();
	    sT = new SeekAdapter();
	    mSpectrum = new Mat();
	    mBlobColorRgba = new Scalar(255);
	    mBlobColorHsv = new Scalar(255);
	    SPECTRUM_SIZE = new Size(mRgba.cols()/1.5, 64);
	    CONTOUR_COLOR = new Scalar(255,0,0,255);
    	mDisplay = new Mat();
    	mBiner = new Mat(height,width,CvType.CV_8UC1);
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		mRgba.release();
	    mHSV.release();
	    mDisplay.release();
	    mBiner.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		final int viewMode = mViewMode;
		mRgba = inputFrame.rgba();
		mOpenCvCameraView.setFocus("manual");
		mOpenCvCameraView.setWhite();
		mOpenCvCameraView.setExposure(val);
        Imgproc.putText(mRgba, "nLUX1:   " + nLux, new Point ( 300, mRgba.rows() * 0.3), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 1);
        Imgproc.putText(mRgba, "nLUX2:   " + nLuxAcc, new Point ( 300, mRgba.rows() * 0.4), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 1);
		Button bMenu = (Button)findViewById(R.id.tombolMenu);
		bMenu.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openOptionsMenu();
			}
		});
		
		
		if (viewMode==VIEW_MODE_HSV12){	
			Intent intent = new Intent(SeekBarVal11.this, SeekBarVal12.class);
        	startActivity(intent);	
        	finish();
        }
		else if (viewMode==VIEW_MODE_HSV21){	
			Intent intent = new Intent(SeekBarVal11.this, SeekBarVal21.class);
        	startActivity(intent);	
        	finish();
        }
		else if (viewMode==VIEW_MODE_BALIK){
			Intent intent = new Intent(SeekBarVal11.this, BluetoothConf.class);
        	startActivity(intent);	
        	finish();
		}
		else if (viewMode==VIEW_MODE_HSV11){
			Button bH = (Button)findViewById(R.id.tombolH);
    		Button bS = (Button)findViewById(R.id.tombolS);
			Button bV = (Button)findViewById(R.id.tombolV);
			Button bED = (Button)findViewById(R.id.tombolED);
			Button bEW = (Button)findViewById(R.id.tombolEW);
			Button bT = (Button)findViewById(R.id.tombolTiang);
			
			seekHmin1=(SeekBar) findViewById(R.id.min);
			seekHmax1=(SeekBar) findViewById(R.id.max);
			seekSmin1=(SeekBar) findViewById(R.id.min);
			seekSmax1=(SeekBar) findViewById(R.id.max);
			seekVmin1=(SeekBar) findViewById(R.id.min);
			seekVmax1=(SeekBar) findViewById(R.id.max);
			seekErode=(SeekBar) findViewById(R.id.min);
			seekDilate=(SeekBar) findViewById(R.id.max);
			seekEV=(SeekBar) findViewById(R.id.min);
			seekWB=(SeekBar) findViewById(R.id.max);
			seekBatBi=(SeekBar) findViewById(R.id.min);
			seekBatHi=(SeekBar) findViewById(R.id.max);
			

			bH.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sH.setProgress(seekHmin1, seekHmax1, Hmin1, Hmax1, 262);
					sH.seekProgress();
					bIsHPressed = true;
					bIsSPressed = false;
					bIsVPressed = false;
					bIsEDPressed = false;
					bIsEWPressed = false;
					bIsBatPressed = false;
					mIsDisplayTouched = false;
//					val = true;
				}
			});
			bS.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sS.setProgress(seekSmin1, seekSmax1, Smin1, Smax1, 262);
					sS.seekProgress();
					bIsSPressed = true;
					bIsHPressed = false;
					bIsVPressed = false;
					bIsEDPressed = false;
					bIsEWPressed = false;
					bIsBatPressed = false;
					mIsDisplayTouched = false;
//					val = true;
				}
			});
			bV.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sV.setProgress(seekVmin1, seekVmax1, Vmin1, Vmax1, 262);
					sV.seekProgress();
					sV.seekBarChange();
					bIsHPressed = false;
					bIsSPressed = false;
					bIsEDPressed = false;
					bIsEWPressed = false;
					bIsBatPressed = false;
					bIsVPressed = true;
					mIsDisplayTouched = false;
//					val = true;
				}
			});
			bED.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sED.setProgress(seekErode, seekDilate, Erode1, Dilate1, 21);
					sED.seekProgress();
					sED.seekBarChange();
					bIsEDPressed = true;
					bIsHPressed = false;
					bIsSPressed = false;
					bIsVPressed = false;
					bIsEWPressed = false;
					bIsBatPressed = false;
					mIsDisplayTouched = false;
					
					if(idxBiner)
						idxBiner = false;
						else
							idxBiner = true;
				}
			});
			
			bEW.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sEW.setProgress(seekEV, seekWB, ExpVal, WBidx, 25);
					sEW.seekProgress();
					sEW.seekBarChange();
					bIsEDPressed = false;
					bIsHPressed = false;
					bIsSPressed = false;
					bIsVPressed = false;
					bIsEWPressed = true;
					bIsBatPressed = false;
					mIsDisplayTouched = false;
				}
			});
			
			bT.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sT.setProgress(seekBatBi, seekBatHi, batTiangBir, batTiangH, mRgba.rows());
					sT.seekProgress();
					sT.seekBarChange();
					bIsEDPressed = false;
					bIsHPressed = false;
					bIsSPressed = false;
					bIsVPressed = false;
					bIsEWPressed = false;
					bIsBatPressed = true;
					mIsDisplayTouched = false;
				}
			});

			if(bIsHPressed){
				Hmin1 = sH.getFirstVal();
				Hmax1 = sH.getSecondVal();
				sH.setProgress(seekHmin1, seekHmax1, Hmin1, Hmax1, 262);
				sH.seekProgress();
				sH.seekBarChange();
			}
			
			if(bIsSPressed){
				Smin1 = sS.getFirstVal();
				Smax1 = sS.getSecondVal();
				sS.setProgress(seekSmin1, seekSmax1, Smin1, Smax1, 262);
				sS.seekProgress();
				sS.seekBarChange();
			}
			
			if(bIsVPressed){
				Vmin1 = sV.getFirstVal();
				Vmax1 = sV.getSecondVal();
				sV.setProgress(seekVmin1, seekVmax1, Vmin1, Vmax1, 262);
				sV.seekProgress();
				sV.seekBarChange();
			}
			
			if(bIsEDPressed){
				Erode1 = sED.getFirstVal();
				Dilate1 = sED.getSecondVal();
				sED.setProgress(seekErode, seekDilate, Erode1, Dilate1, 21);
				sED.seekProgress();
				sED.seekBarChange();
			}
			
			if(bIsEWPressed){
				ExpVal = sEW.getFirstVal();
				WBidx = sEW.getSecondVal();
				sEW.setProgress(seekEV, seekWB, ExpVal, WBidx, 25);
				sEW.seekProgress();
				sEW.seekBarChange();
			}
			
			if(bIsBatPressed){
				batTiangBir = sT.getFirstVal();
				batTiangH = sT.getSecondVal();
				sT.setProgress(seekBatBi, seekBatHi, batTiangBir, batTiangH, mRgba.rows());
				sT.seekProgress();
				sT.seekBarChange();
			}

			if(idxBiner){
		        Scalar hsv_min2 = new Scalar(Hmin1, Smin1, Vmin1, 0);  
		        Scalar hsv_max2 = new Scalar(Hmax1, Smax1, Vmax1, 0);
		        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL,4);    
		        Core.inRange(mHSV, hsv_min2, hsv_max2, mBiner);           
		        Imgproc.erode(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Erode1+1,2*Erode1+1), new Point (Erode1,Erode1))); 
		        Imgproc.dilate(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Dilate1+1,2*Dilate1+1), new Point (Dilate1,Dilate1)));
		        Imgproc.dilate(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Dilate1+1,2*Dilate1+1), new Point (Dilate1,Dilate1)));
		        Imgproc.erode(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Erode1+1,2*Erode1+1), new Point (Erode1,Erode1)));
		        mDisplay = mBiner;
			}
			else{
				if(mIsDisplayTouched){
					Hmin1 = (int) mDetector.getHmin();
					Hmax1 = (int) mDetector.getHmax();
					Smin1 = (int) mDetector.getSmin();
					Smax1 = (int) mDetector.getSmax();
					Vmin1 = (int) mDetector.getVmin();
					Vmax1 = (int) mDetector.getVmax();
					}			
            mDetector.process(mRgba, new Scalar(Hmin1, Smin1, Vmin1), new Scalar(Hmax1, Smax1, Vmax1), Dilate1, Erode1);
            List<MatOfPoint> contours = mDetector.getContours();
//            Imgproc.fillPoly(mRgba, contours, new Scalar((mBlobColorRgba.val[0]+63>255)?mBlobColorRgba.val[0]-63:mBlobColorRgba.val[0]+63, (mBlobColorRgba.val[1]+63>255)?mBlobColorRgba.val[1]-63:mBlobColorRgba.val[1]+63, (mBlobColorRgba.val[2]+63>255)?mBlobColorRgba.val[2]-63:mBlobColorRgba.val[2]+63));
            Imgproc.fillPoly(mRgba, contours, CONTOUR_COLOR);
            
            Log.e(TAG, "Contours count: " + contours.size());
            
            
            MatOfPoint approx =  new MatOfPoint();
            
            double max = 0;
            int jmlSudut = 0;
            for (int i = 0; i < contours.size(); i++){
            	MatOfPoint tempContour = contours.get(i);
            	MatOfPoint2f newMat = new MatOfPoint2f( tempContour.toArray() );
            	MatOfPoint2f newApprox = new MatOfPoint2f( approx.toArray() );
                       
            	Imgproc.approxPolyDP(newMat, newApprox, Imgproc.arcLength(newMat, true)*0.02, true);
     	   
     	   if (Imgproc.contourArea(contours.get(i)) > max){
     		   max = Imgproc.contourArea(contours.get(i));
//      		   counter = i;
      		   jmlSudut = newApprox.toArray().length;
     	   }
           }
            switch(WBidx){
	            case 0	: valWB = "auto"; break;
	            case 1	: valWB = "auto"; break;
	            case 2	: valWB = "daylight"; break;
	            case 3	: valWB = "daylight"; break;
	            case 4	: valWB = "cloudy-daylight"; break;
	            case 5	: valWB = "cloudy-daylight"; break;
	            case 6	: valWB = "twilight"; break;
	            case 7	: valWB = "twilight"; break;
	            case 8	: valWB = "incandescent"; break;
	            case 9	: valWB = "incandescent"; break;
	            case 10	: valWB = "warm-fluorescent"; break;
	            case 11	: valWB = "warm-fluorescent"; break;
	            case 12	: valWB = "fluorescent"; break;
	            case 13	: valWB = "fluorescent"; break;
	            case 14	: valWB = "shade"; break;
	            case 15	: valWB = "shade"; break;
	            case 16	: valWB = "manual-cct"; break;
	            case 17	: valWB = "manual-cct"; break;
	            default : valWB = "daylight"; break;
            }
            
            ExVal = ExpVal - 12;
            
            Imgproc.putText(mRgba, "Curve:   " + jmlSudut, new Point (8, 240), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
//            Imgproc.putText(mRgba, "param1:   " + mDetector.getParam1(), new Point (8, 195), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
//            Imgproc.putText(mRgba, "param2:   " + mDetector.getParam2(), new Point (8, 210), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
            
//            Imgproc.drawContours(mRgba, contours, -1, new Scalar((mBlobColorRgba.val[0]+63>255)?mBlobColorRgba.val[0]-63:mBlobColorRgba.val[0]+63, (mBlobColorRgba.val[1]+63>255)?mBlobColorRgba.val[1]-63:mBlobColorRgba.val[1]+63, (mBlobColorRgba.val[2]+63>255)?mBlobColorRgba.val[2]-63:mBlobColorRgba.val[2]+63), 5);
            Imgproc.drawContours(mRgba, contours, -1, new Scalar(0, 255, 0, 255), 2);

            Imgproc.putText(mRgba, "valBot:   " + Hmin1 + ",  " + Smin1 + ",  " + Vmin1 + ",  " + Erode1, new Point (8, mRgba.rows() * 0.05), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
			Imgproc.putText(mRgba, "valMax:   " + Hmax1 + ",  " + Smax1 + ",  " + Vmax1 + ",  " + Dilate1, new Point (8, mRgba.rows() * 0.1), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
			
			Imgproc.putText(mRgba, "EV:   " + ExVal, new Point (8, mRgba.rows() * 0.15), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
            Imgproc.putText(mRgba, "WB:   " + valWB, new Point (8, mRgba.rows() * 0.2), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);

            
			Imgproc.putText(mRgba, "TingTiangB:   " + batTiangBir, new Point (8, mRgba.rows() * 0.25), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(255, 0, 0), 1);
            Imgproc.putText(mRgba, "TingTiangH:   " + batTiangH, new Point (8, mRgba.rows() * 0.3), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(255, 0, 0), 1);
            
            Mat colorLabel = mRgba.submat(2, 34, 446, 478);
            colorLabel.setTo(mBlobColorRgba);
//            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
//            mSpectrum.copyTo(spectrumLabel);
			mDisplay = mRgba;
			}
			Imgproc.putText(mRgba, "HSV 1", new Point (380, 210), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
			Imgproc.putText(mRgba, "Exp:  " + val, new Point (5, 210), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
			Imgproc.putText(mRgba, "z: " + zAzim, new Point (5, 130), Core.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(0, 0, 255), 1);
			Imgproc.putText(mRgba, "x: " + xPitch, new Point (5, 100), Core.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(0, 0, 255), 1);
			Imgproc.putText(mRgba, "y: " + yRoll, new Point (5, 115), Core.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(0, 0, 255), 1);
		}
        return mDisplay;	
		 
}

public boolean onOptionsItemSelected(MenuItem item) {  
    
    	if (item == mItemHsv11) {  
      mViewMode = VIEW_MODE_HSV11;  
    } else if (item == mItemHsv12) { 
//    	if(val){
//    	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//         String currentDateandTime = sdf.format(new Date());
//         String fileName = Environment.getExternalStorageDirectory().getPath() +
//                                "/sample_picture_" + currentDateandTime + ".jpg";
//         mOpenCvCameraView.takePicture(fileName);
//         Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
//    	}
      mViewMode = VIEW_MODE_HSV12;      
    } else if (item == mItemHsv21) {  
        mViewMode = VIEW_MODE_HSV21;      
    }else if (item == mItemBalik){  
      mViewMode = VIEW_MODE_BALIK;      
    } else if (item == mItemExposure){
    	if(val)
			val = false;
			else
				val = true;
    }
    return true;
}

@SuppressLint("ClickableViewAccessibility")
@Override
public boolean onTouch(View v, MotionEvent event) {
	// TODO Auto-generated method stub
	int cols = mRgba.cols();
    int rows = mRgba.rows();
//    
    int x = (int)(event.getX() * 0.25);
    int y = (int)(event.getY() * 0.296296296);
    
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
    
    mBlobColorHsv = Core.sumElems(touchedRegionHsv);
    
    for (int i = 0; i < mBlobColorHsv.val.length; i++)
        mBlobColorHsv.val[i] /= pointCount;
    
    mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
    
    Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
            ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

    mDetector.setHsvColor(mBlobColorHsv);
    
    Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

    mDetector.process(mRgba);
    
//    mIsColorSelected = true;
    
	bIsHPressed = false;
	bIsSPressed = false;
	bIsVPressed = false;
	bIsEDPressed = false;
	bIsEWPressed = false;
	bIsBatPressed = false;

    mIsDisplayTouched = true;

    touchedRegionRgba.release();
    touchedRegionHsv.release();
	
	return false;
}


private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
    Mat pointMatRgba = new Mat();
    Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
    Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

    return new Scalar(pointMatRgba.get(0, 0));
}

private final SensorEventListener LightSensorListener = new SensorEventListener(){

	@Override
	  public void onAccuracyChanged(Sensor sensor, int accuracy) {
	   // TODO Auto-generated method stub
//		  nLuxAcc = accuracy;
	  }

	  @Override
	  public void onSensorChanged(SensorEvent event) {
		  nLux = event.values[0];
		  nLuxAcc = event.values[1];
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
}
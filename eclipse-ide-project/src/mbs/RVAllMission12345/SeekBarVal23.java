package mbs.RVAllMission12345;

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
import android.os.Bundle;
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

public class SeekBarVal23 extends Activity implements OnTouchListener, CvCameraViewListener2 {
   private static final String TAG = "SeekActivity";
//   private static final int    VIEW_MODE_ASLI   = 0;
   private static final int    VIEW_MODE_HSV21   = 1;  
   private static final int    VIEW_MODE_HSV22   = 2; 
   private static final int    VIEW_MODE_HSV23   = 0; 
   private static final int    VIEW_MODE_HSV24   = 3; 
   private static final int    VIEW_MODE_HSV25   = 4; 
   private static final int    VIEW_MODE_BALIK   = 6; 
   private MenuItem     mItemHSV21; 
   private MenuItem     mItemHSV22; 
   private MenuItem     mItemHSV23; 
   private MenuItem     mItemHSV24; 
   private MenuItem     mItemHSV25; 
   private Boolean val= true;
   
   
   private static final int    VIEW_MODE_HSV11   = 5;
   private MenuItem     mItemHsv11;
   
   
//   private MenuItem     mItemAsli;
   private MenuItem     mItemBalik;
   private int          mViewMode;  
   private MenuItem 	mItemExposure;
	private MisiView  mOpenCvCameraView;
	private Mat mRgba;
	private Mat mHSV;
	
  public static int Hmin3 = 97;
  public static int Hmax3 = 115;
  public static int Smin3 = 122;
  public static int Smax3 = 257;
  public static int Vmin3 = 134;
  public static int Vmax3 = 259;
  public static int Dilate3 = 0;
  public static int Erode3 = 0;

	public static int TGate = 33;
	public static int TObs = 25;  
	
	
	private Mat mBiner;
	private boolean idxBiner =false;
	private Mat mDisplay;
	
//	private boolean              mIsColorSelected = false;
	private Scalar               mBlobColorRgba;
	private Scalar               mBlobColorHsv;
	private BlobDetector  		 mDetector;
	private Mat                  mSpectrum;
	private Size                 SPECTRUM_SIZE;
	
    protected boolean bIsHPressed;
	protected boolean bIsSPressed;
	protected boolean bIsVPressed;
	protected boolean bIsEDPressed;

	protected boolean bIsTBolPressed;
	private SeekAdapter sTBol;
	private SeekBar seekBolGate;
	private SeekBar seekBolObs;
	
	private SeekBar seekHmin3;
	private SeekBar seekHmax3;
	private SeekBar seekSmin3;
	private SeekBar seekSmax3;
	private SeekBar seekVmin3;
	private SeekBar seekVmax3;
	private SeekBar seekErode3;
	private SeekBar seekDilate3;
	private boolean mIsDisplayTouched;  
	private SeekAdapter sH;
    private SeekAdapter sS;
	private SeekAdapter sV;
	private SeekAdapter sED;

	
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
		           mOpenCvCameraView.setOnTouchListener(SeekBarVal23.this);
		         } break;  
		         default:
		         {  
		           super.onManagerConnected(status);  
		         } break;  
		       }  
		     }  
		   };
	private Scalar CONTOUR_COLOR;
	private MySensorEventListener mySensorEventListener;
	private Sensor mOrienta;  
		   public SeekBarVal23() {  
			     Log.i(TAG, "Instantiated new " + this.getClass());  
			   }  
		   
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	 Log.i(TAG, "called onCreate"); 
    	super.onCreate(savedInstanceState); 
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
		setContentView(R.layout.biner23);
		
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
//     mItemAsli = menu.add("Asli"); 
      mItemHSV21 = menu.add("HSV21");  
      mItemHSV22 = menu.add("HSV22");
      mItemHSV23 = menu.add("HSV23");
      mItemHSV24 = menu.add("HSV24");
      mItemHSV25 = menu.add("HSV25");
      mItemHsv11 = menu.add("HSV11");
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
	    sTBol = new SeekAdapter();
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
		mOpenCvCameraView.setFocus("manual");
		mOpenCvCameraView.setWhite();
		mOpenCvCameraView.setExposure(val);
		
        Imgproc.putText(mRgba, "nLUX1:   " + nLux1, new Point ( 350, 70), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
        Imgproc.putText(mRgba, "nLUX2:   " + nLux2, new Point ( 350, 85), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
		
//		if (viewMode==VIEW_MODE_ASLI)return mRgba;
		if (viewMode==VIEW_MODE_HSV21){	
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal23.this, SeekBarVal21.class);
        	startActivity(intent);	
        	finish();
        }
		else if (viewMode==VIEW_MODE_HSV22){	
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal23.this, SeekBarVal22.class);
        	startActivity(intent);	
        	finish();
        }
		else if (viewMode==VIEW_MODE_HSV24){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal23.this, SeekBarVal24.class);
        	startActivity(intent);	
        	finish();
        }
		else if (viewMode==VIEW_MODE_HSV25){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal23.this, SeekBarVal25.class);
        	startActivity(intent);	
        	finish();
        }
		else if (viewMode==VIEW_MODE_HSV11){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal23.this, SeekBarVal11.class);
        	startActivity(intent);	
        	finish();
        }
		else if (viewMode==VIEW_MODE_BALIK){
			onCameraViewStopped();
			Intent intent = new Intent(SeekBarVal23.this, BluetoothConf.class);
        	startActivity(intent);	
        	finish();
		}
		else if (viewMode==VIEW_MODE_HSV23){
//			if (mIsColorSelected) {
				Button bH = (Button)findViewById(R.id.tombolH);
        		Button bS = (Button)findViewById(R.id.tombolS);
				Button bV = (Button)findViewById(R.id.tombolV);
				Button bED = (Button)findViewById(R.id.tombolED);
				Button bTBol = (Button)findViewById(R.id.tombolHeight);
				
				Button bMenu = (Button)findViewById(R.id.tombolMenu);
				bMenu.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						openOptionsMenu();
					}
				});
				
				seekHmin3=(SeekBar) findViewById(R.id.min);
				seekHmax3=(SeekBar) findViewById(R.id.max);
				seekSmin3=(SeekBar) findViewById(R.id.min);
				seekSmax3=(SeekBar) findViewById(R.id.max);
				seekVmin3=(SeekBar) findViewById(R.id.min);
				seekVmax3=(SeekBar) findViewById(R.id.max);
				seekErode3=(SeekBar) findViewById(R.id.min);
				seekDilate3=(SeekBar) findViewById(R.id.max);
				seekBolGate=(SeekBar) findViewById(R.id.min);
				seekBolObs=(SeekBar) findViewById(R.id.max);
				
				bH.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						sH.setProgress(seekHmin3, seekHmax3, Hmin3, Hmax3, 262);
						sH.seekProgress();
						bIsHPressed = true;
						bIsSPressed = false;
						bIsVPressed = false;
						bIsEDPressed = false;
						bIsTBolPressed = false;
						mIsDisplayTouched = false;
						
					}
				});
				bS.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						sS.setProgress(seekSmin3, seekSmax3, Smin3, Smax3, 262);
						sS.seekProgress();
						bIsSPressed = true;
						bIsHPressed = false;
						bIsVPressed = false;
						bIsEDPressed = false;
						bIsTBolPressed = false;
						mIsDisplayTouched = false;
					}
				});
				bV.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						sV.setProgress(seekVmin3, seekVmax3, Vmin3, Vmax3, 262);
						sV.seekProgress();
						sV.seekBarChange();
						bIsHPressed = false;
						bIsSPressed = false;
						bIsEDPressed = false;

						bIsVPressed = true;
						bIsTBolPressed = false;
						mIsDisplayTouched = false;
					}
				});
				bED.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						sED.setProgress(seekErode3, seekDilate3, Erode3, Dilate3, 21);
						sED.seekProgress();
						sED.seekBarChange();
						bIsEDPressed = true;
						bIsHPressed = false;
						bIsSPressed = false;
						bIsVPressed = false;
						bIsTBolPressed = false;
						mIsDisplayTouched = false;
						
						if(idxBiner)
							idxBiner = false;
							else
								idxBiner = true;
					}
				});
				

				bTBol.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						sTBol.setProgress(seekBolGate, seekBolObs, TGate, TObs, 100);
						sTBol.seekProgress();
						sTBol.seekBarChange();
						bIsEDPressed = false;
						bIsHPressed = false;
						bIsSPressed = false;
						bIsVPressed = false;
						bIsTBolPressed = true;
						mIsDisplayTouched = false;
					}
				});
				

				if(bIsHPressed){
					Hmin3 = sH.getFirstVal();
					Hmax3 = sH.getSecondVal();
					sH.setProgress(seekHmin3, seekHmax3, Hmin3, Hmax3, 262);
					sH.seekProgress();
					sH.seekBarChange();
				}
				
				if(bIsSPressed){
					Smin3 = sS.getFirstVal();
					Smax3 = sS.getSecondVal();
					sS.setProgress(seekSmin3, seekSmax3, Smin3, Smax3, 262);
					sS.seekProgress();
					sS.seekBarChange();
				}
				
				if(bIsVPressed){
					Vmin3 = sV.getFirstVal();
					Vmax3 = sV.getSecondVal();
					sV.setProgress(seekVmin3, seekVmax3, Vmin3, Vmax3, 262);
					sV.seekProgress();
					sV.seekBarChange();
				}
				
				if(bIsEDPressed){
					Erode3 = sED.getFirstVal();
					Dilate3 = sED.getSecondVal();
					sED.setProgress(seekErode3, seekDilate3, Erode3, Dilate3, 10);
					sED.seekProgress();
					sED.seekBarChange();
				}
				
				if(bIsTBolPressed){
					TGate = sTBol.getFirstVal();
					TObs = sTBol.getSecondVal();
					sTBol.setProgress(seekBolGate, seekBolObs, TGate, TObs, 100);
					sTBol.seekProgress();
					sTBol.seekBarChange();
				}

				if(idxBiner){
			        Scalar hsv_min2 = new Scalar(Hmin3, Smin3, Vmin3, 0);  
			        Scalar hsv_max2 = new Scalar(Hmax3, Smax3, Vmax3, 0);
			        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL,4);    
			        Core.inRange(mHSV, hsv_min2, hsv_max2, mBiner);           
			        Imgproc.erode(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Erode3+1,2*Erode3+1), new Point (Erode3,Erode3))); 
			        Imgproc.dilate(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Dilate3+1,2*Dilate3+1), new Point (Dilate3,Dilate3)));
			        Imgproc.dilate(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Dilate3+1,2*Dilate3+1), new Point (Dilate3,Dilate3)));
			        Imgproc.erode(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Erode3+1,2*Erode3+1), new Point (Erode3,Erode3)));
			        mDisplay = mBiner;
				}
				else{
					
					if(mIsDisplayTouched){
						Hmin3 = (int) mDetector.getHmin();
						Hmax3 = (int) mDetector.getHmax();
						Smin3 = (int) mDetector.getSmin();
						Smax3 = (int) mDetector.getSmax();
						Vmin3 = (int) mDetector.getVmin();
						Vmax3 = (int) mDetector.getVmax();
						}				

					
	            mDetector.process(mRgba, new Scalar(Hmin3, Smin3, Vmin3), new Scalar(Hmax3, Smax3, Vmax3), Dilate3, Erode3);
	            
	            List<MatOfPoint> contours = mDetector.getContours();
	            Log.e(TAG, "Contours count: " + contours.size());
		        

	            
	            Imgproc.fillPoly(mRgba, contours, CONTOUR_COLOR);
	            Imgproc.drawContours(mRgba, contours, -1, new Scalar(0, 255, 0), 3);
//	            Imgproc.drawContours(mRgba, contours, -1, new Scalar((mBlobColorRgba.val[0]+63>255)?mBlobColorRgba.val[0]-63:mBlobColorRgba.val[0]+63, (mBlobColorRgba.val[1]+63>255)?mBlobColorRgba.val[1]-63:mBlobColorRgba.val[1]+63, (mBlobColorRgba.val[2]+63>255)?mBlobColorRgba.val[2]-63:mBlobColorRgba.val[2]+63), 5);
	            
	            Imgproc.putText(mRgba, "valBot:   " + Hmin3 + ",  " + Smin3 + ",  " + Vmin3 + ",  " + Erode3, new Point (mRgba.cols() / 8, mRgba.rows() * 0.05), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
				Imgproc.putText(mRgba, "valMax:   " + Hmax3 + ",  " + Smax3 + ",  " + Vmax3 + ",  " + Dilate3, new Point (mRgba.cols() / 8, mRgba.rows() * 0.1), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);

	            Mat colorLabel = mRgba.submat(2, 34, 446, 478);
	            colorLabel.setTo(mBlobColorRgba);
	            
//	            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
//	            mSpectrum.copyTo(spectrumLabel);
				mDisplay = mRgba;
				}
				Imgproc.putText(mRgba, "HSV 3", new Point (380, 210), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255), 2);
				Imgproc.putText(mRgba, "Exp:  " + val, new Point (5,210), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
				Imgproc.putText(mRgba, "TingGat: " + TGate, new Point (5, 200), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 1);
				Imgproc.putText(mRgba, "TingObs: " + TObs, new Point (5, 190), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 1);
				Imgproc.line(mRgba, new Point(0, SeekBarVal22.batHorzonA), new Point(480, SeekBarVal22.batHorzonA), new Scalar( 255, 0, 255 ), 1, 8 ,0 );
		        Imgproc.line(mRgba, new Point(0, SeekBarVal22.batHorzonB), new Point(480, SeekBarVal22.batHorzonB), new Scalar( 255, 0, 255 ), 1, 8 ,0 );
		        Imgproc.line(mRgba, new Point(0, SeekBarVal22.batHorzonB-TGate), new Point(480, SeekBarVal22.batHorzonB-TGate), new Scalar( 0, 255, 255 ), 1, 8 ,0 );
		        Imgproc.line(mRgba, new Point(0, SeekBarVal22.batHorzonB-TObs), new Point(480, SeekBarVal22.batHorzonB-TObs), new Scalar( 0, 255, 255 ), 1, 8 ,0 );
		        Imgproc.line(mRgba, new Point(0, SeekBarVal22.batHorzonB), new Point(480, SeekBarVal22.batHorzonB), new Scalar( 255, 0, 255 ), 1, 8 ,0 );
				Imgproc.putText(mRgba, "G "+ TGate , new Point (0, SeekBarVal22.batHorzonB -TGate), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
				Imgproc.putText(mRgba, " "+ (SeekBarVal22.batHorzonB-SeekBarVal22.batHorzonA) , new Point (0, (SeekBarVal22.batHorzonB+SeekBarVal22.batHorzonA)/2), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
				Imgproc.putText(mRgba, " "+ SeekBarVal22.batHorzonA, new Point (0, SeekBarVal22.batHorzonA), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
				Imgproc.putText(mRgba, " " + SeekBarVal22.batHorzonB, new Point (0, SeekBarVal22.batHorzonB), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
				Imgproc.putText(mRgba, "x, y, z: " + xPitch + ", " + yRoll + ", "  + zAzim, new Point (240, 10), Core.FONT_HERSHEY_SIMPLEX, 0.35, new Scalar(0, 0, 255), 1);
		}
        return mDisplay;	
		 
}

public boolean onOptionsItemSelected(MenuItem item) {  
    
    	if (item == mItemHSV21) {  
      mViewMode = VIEW_MODE_HSV21;  
    } else if (item == mItemHSV22) {  
      mViewMode = VIEW_MODE_HSV22;
    } else if (item == mItemHSV23) {  
      mViewMode = VIEW_MODE_HSV23;
    } else if (item == mItemHSV24) {
//    	if(val){
//    	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//         String currentDateandTime = sdf.format(new Date());
//         String fileName = Environment.getExternalStorageDirectory().getPath() +
//                                "/sample_picture_" + currentDateandTime + ".jpg";
//         mOpenCvCameraView.takePicture(fileName);
//         Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
//    	}
        mViewMode = VIEW_MODE_HSV24;
    } else if (item == mItemHSV25) {  
        mViewMode = VIEW_MODE_HSV25;
    } else if (item == mItemHsv11) {  
        mViewMode = VIEW_MODE_HSV11;      
    } else if (item == mItemBalik){  
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
    
//    int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
//    int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
//
//    int x = (int)event.getX() - xOffset;
//    int y = (int)event.getY() - yOffset;
    
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
    
    mBlobColorRgba = converScalarHSV22Rgba(mBlobColorHsv);
    
    Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
            ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

    mDetector.setHsvColor(mBlobColorHsv);
    
    Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

//    mIsColorSelected = true;
    
    bIsHPressed = false;
	bIsSPressed = false;
	bIsVPressed = false;
	bIsEDPressed = false;
	bIsTBolPressed = false;
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
			  nLux2 = event.values[1];
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
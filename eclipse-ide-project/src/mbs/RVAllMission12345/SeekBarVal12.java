package mbs.RVAllMission12345;

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
//import android.widget.Toast;

public class SeekBarVal12 extends Activity implements CvCameraViewListener2 , OnTouchListener{
   private static final String TAG = "SeekActivity";
   private static final int    VIEW_MODE_HSV11   = 1;  
   private static final int    VIEW_MODE_HSV12   = 0; 
   private static final int    VIEW_MODE_HSV21   = 2; 
   private static final int    VIEW_MODE_BALIK   = 3; 
   private MenuItem     mItemHsv11;  
   private MenuItem     mItemHsv12;
   private MenuItem     mItemHsv21;
   private MenuItem     mItemBalik;
   private int          mViewMode;  
   private MisiView  mOpenCvCameraView;
   private Mat mRgba;
   private Mat mHSV;
   public static int Hmin2=84;
   public static int Hmax2=117;
   public static int Smin2=124;
   public static int Smax2=260;
   public static int Vmin2=86;
   public static int Vmax2=259;
   public static int Dilate2;
   public static int Erode2;
   private SeekBar seekHmin2;
   private SeekBar seekHmax2;
   private SeekBar seekSmin2;
   private SeekBar seekSmax2;
   private SeekBar seekVmin2;
   private SeekBar seekVmax2;
   private SeekBar seekErode;
   private SeekBar seekDilate;
   private boolean mIsDisplayTouched;
   private SeekAdapter sH;
   private SeekAdapter sS;
   private SeekAdapter sV;
   private SeekAdapter sED;
   private Mat mSpectrum;
   private Scalar mBlobColorRgba;
   private Scalar mBlobColorHsv;
   private Size SPECTRUM_SIZE;
   private BlobDetector mDetector;  
   protected boolean bIsHPressed;
   protected boolean bIsSPressed;
   protected boolean bIsVPressed;
   protected boolean bIsEDPressed;
   private Boolean val = true;
	private boolean idxBiner =false;
	private Mat mDisplay;
   
   private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {  
		
		   @Override  
		     public void onManagerConnected(int status) {  
		       switch (status) {  
		         case LoaderCallbackInterface.SUCCESS:  
		         {  
		        	 Log.i(TAG, "OpenCV loaded successfully"); 
		           mOpenCvCameraView.enableView();  
		           mOpenCvCameraView.setOnTouchListener(SeekBarVal12.this);
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
private Mat mBiner;
private MySensorEventListener mySensorEventListener;
private Sensor mOrienta;  
		   public SeekBarVal12() {  
			     Log.i(TAG, "Instantiated new " + this.getClass());  
			   }  
		   
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	 Log.i(TAG, "called onCreate"); 
    	super.onCreate(savedInstanceState); 
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
		setContentView(R.layout.biner12);
		
		SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    Sensor LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
	    mySensorManager.registerListener(LightSensorListener, LightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		 
		mOpenCvCameraView = (MisiView) findViewById(R.id.activity_surface_view);
		mOpenCvCameraView.setMaxFrameSize(480, 320);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
	    mySensorEventListener = new MySensorEventListener();
	    mOrienta = mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    mySensorManager.registerListener(mySensorEventListener, mOrienta, SensorManager.SENSOR_DELAY_GAME);
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
		
        Imgproc.putText(mRgba, "nLUX1:   " + nLux1, new Point ( 350, 70), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
        Imgproc.putText(mRgba, "nLUX2:   " + nLux2, new Point ( 350, 85), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 0, 255), 1);
		
		Button bMenu = (Button)findViewById(R.id.tombolMenu);
		bMenu.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openOptionsMenu();
			}
		});
		
		
		if (viewMode==VIEW_MODE_HSV11){	
			Intent intent = new Intent(SeekBarVal12.this, SeekBarVal11.class);
        	startActivity(intent);	
        	finish();
        }
		else if (viewMode==VIEW_MODE_HSV21){	
			Intent intent = new Intent(SeekBarVal12.this, SeekBarVal21.class);
        	startActivity(intent);	
        	finish();
        }
		else if (viewMode==VIEW_MODE_BALIK){
			Intent intent = new Intent(SeekBarVal12.this, BluetoothConf.class);
        	startActivity(intent);	
        	finish();
		}
		else if (viewMode==VIEW_MODE_HSV12){
			
			Button bH = (Button)findViewById(R.id.tombolH);
    		Button bS = (Button)findViewById(R.id.tombolS);
			Button bV = (Button)findViewById(R.id.tombolV);
			Button bED = (Button)findViewById(R.id.tombolED);
			
			seekHmin2=(SeekBar) findViewById(R.id.min);
			seekHmax2=(SeekBar) findViewById(R.id.max);
			seekSmin2=(SeekBar) findViewById(R.id.min);
			seekSmax2=(SeekBar) findViewById(R.id.max);
			seekVmin2=(SeekBar) findViewById(R.id.min);
			seekVmax2=(SeekBar) findViewById(R.id.max);
			seekErode=(SeekBar) findViewById(R.id.min);
			seekDilate=(SeekBar) findViewById(R.id.max);
			
			if(mIsDisplayTouched){
				Hmin2 = (int) mDetector.getHmin();
				Hmax2 = (int) mDetector.getHmax();
				Smin2 = (int) mDetector.getSmin();
				Smax2 = (int) mDetector.getSmax();
				Vmin2 = (int) mDetector.getVmin();
				Vmax2 = (int) mDetector.getVmax();
				}				

			bH.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sH.setProgress(seekHmin2, seekHmax2, Hmin2, Hmax2, 262);
					sH.seekProgress();
					bIsHPressed = true;
					bIsSPressed = false;
					bIsVPressed = false;
					bIsEDPressed = false;
					mIsDisplayTouched = false;
					
				}
			});
			bS.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sS.setProgress(seekSmin2, seekSmax2, Smin2, Smax2, 262);
					sS.seekProgress();
					bIsSPressed = true;
					bIsHPressed = false;
					bIsVPressed = false;
					bIsEDPressed = false;
					mIsDisplayTouched = false;
				}
			});
			bV.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sV.setProgress(seekVmin2, seekVmax2, Vmin2, Vmax2, 262);
					sV.seekProgress();
					sV.seekBarChange();
					bIsHPressed = false;
					bIsSPressed = false;
					bIsEDPressed = false;

					bIsVPressed = true;
					mIsDisplayTouched = false;
				}
			});
			bED.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sED.setProgress(seekErode, seekDilate, Erode2, Dilate2, 21);
					sED.seekProgress();
					sED.seekBarChange();
					bIsEDPressed = true;
					bIsHPressed = false;
					bIsSPressed = false;
					bIsVPressed = false;
					mIsDisplayTouched = false;
					
					if(idxBiner)
						idxBiner = false;
						else
							idxBiner = true;
				}
			});

			if(bIsHPressed){
				Hmin2 = sH.getFirstVal();
				Hmax2 = sH.getSecondVal();
				sH.setProgress(seekHmin2, seekHmax2, Hmin2, Hmax2, 262);
				sH.seekProgress();
				sH.seekBarChange();
			}
			
			if(bIsSPressed){
				Smin2 = sS.getFirstVal();
				Smax2 = sS.getSecondVal();
				sS.setProgress(seekSmin2, seekSmax2, Smin2, Smax2, 262);
				sS.seekProgress();
				sS.seekBarChange();
			}
			
			if(bIsVPressed){
				Vmin2 = sV.getFirstVal();
				Vmax2 = sV.getSecondVal();
				sV.setProgress(seekVmin2, seekVmax2, Vmin2, Vmax2, 262);
				sV.seekProgress();
				sV.seekBarChange();
			}
			
			if(bIsEDPressed){
				Erode2 = sED.getFirstVal();
				Dilate2 = sED.getSecondVal();
				sED.setProgress(seekErode, seekDilate, Erode2, Dilate2, 21);
				sED.seekProgress();
				sED.seekBarChange();
			}

			if(idxBiner){
		        Scalar hsv_min2 = new Scalar(Hmin2, Smin2, Vmin2, 0);  
		        Scalar hsv_max2 = new Scalar(Hmax2, Smax2, Vmax2, 0);
		        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL,4);    
		        Core.inRange(mHSV, hsv_min2, hsv_max2, mBiner);           
		        Imgproc.erode(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Erode2+1,2*Erode2+1), new Point (Erode2,Erode2))); 
		        Imgproc.dilate(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Dilate2+1,2*Dilate2+1), new Point (Dilate2,Dilate2)));
		        Imgproc.dilate(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Dilate2+1,2*Dilate2+1), new Point (Dilate2,Dilate2)));
		        Imgproc.erode(mBiner, mBiner, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*Erode2+1,2*Erode2+1), new Point (Erode2,Erode2)));
		        mDisplay = mBiner;
			}
			else{
				
				if(mIsDisplayTouched){
					Hmin2 = (int) mDetector.getHmin();
					Hmax2 = (int) mDetector.getHmax();
					Smin2 = (int) mDetector.getSmin();
					Smax2 = (int) mDetector.getSmax();
					Vmin2 = (int) mDetector.getVmin();
					Vmax2 = (int) mDetector.getVmax();
					}				
			
            mDetector.process(mRgba, new Scalar(Hmin2, Smin2, Vmin2), new Scalar(Hmax2, Smax2, Vmax2), Dilate2, Erode2);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            
//            Imgproc.drawContours(mRgba, contours, -1, new Scalar((mBlobColorRgba.val[0]+63>255)?mBlobColorRgba.val[0]-63:mBlobColorRgba.val[0]+63, (mBlobColorRgba.val[1]+63>255)?mBlobColorRgba.val[1]-63:mBlobColorRgba.val[1]+63, (mBlobColorRgba.val[2]+63>255)?mBlobColorRgba.val[2]-63:mBlobColorRgba.val[2]+63), 5);

//            param1 = mDetector.getParam1();
//            param2 = mDetector.getParam2();
            
//            Imgproc.fillPoly(mRgba, contours, new Scalar((mBlobColorRgba.val[0]+63>255)?mBlobColorRgba.val[0]-63:mBlobColorRgba.val[0]+63, (mBlobColorRgba.val[1]+63>255)?mBlobColorRgba.val[1]-63:mBlobColorRgba.val[1]+63, (mBlobColorRgba.val[2]+63>255)?mBlobColorRgba.val[2]-63:mBlobColorRgba.val[2]+63));
            Imgproc.fillPoly(mRgba, contours, CONTOUR_COLOR);
            Imgproc.drawContours(mRgba, contours, -1, new Scalar(0, 255, 0, 255), 2);
            Imgproc.putText(mRgba, "valBot:   " + Hmin2 + ",  " + Smin2 + ",  " + Vmin2 + ",  " + Erode2, new Point (mRgba.cols() / 8, mRgba.rows() * 0.05), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
			Imgproc.putText(mRgba, "valMax:   " + Hmax2 + ",  " + Smax2 + ",  " + Vmax2 + ",  " + Dilate2, new Point (mRgba.cols() / 8, mRgba.rows() * 0.1), Core.FONT_HERSHEY_SIMPLEX, 0.4, new Scalar(0, 255, 0), 1);
            
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
    
//	if (item == mItemAsli) {  
//      mViewMode = VIEW_MODE_ASLI;      
//    } else 
    	if (item == mItemHsv11) {  
      mViewMode = VIEW_MODE_HSV11;  
    } else if (item == mItemHsv12) {  
      mViewMode = VIEW_MODE_HSV12;      
    } else if (item == mItemHsv21) { 
//    	if(val){
//    	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//         String currentDateandTime = sdf.format(new Date());
//         String fileName = Environment.getExternalStorageDirectory().getPath() +
//                                "/sample_picture_" + currentDateandTime + ".jpg";
//         mOpenCvCameraView.takePicture(fileName);
//         Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
//    	}
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
//    int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
//    int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

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
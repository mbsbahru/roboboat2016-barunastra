package mbs.RVAllMission12345;


import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
//import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;

public class MisiView extends JavaCameraView implements PictureCallback {
	private static final String TAG = "TestFoFla";
	private String mPictureFileName;
    boolean initReady = false;
	private int counter;

public MisiView(Context context, AttributeSet attrs) {
    super(context, attrs);
}

public boolean isEffectSupported() {
    return (mCamera.getParameters().getColorEffect() != null);
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public List<String> getAntibandingList() {
    return mCamera.getParameters().getSupportedAntibanding();
}

public List<String> getEffectList() {
    return mCamera.getParameters().getSupportedColorEffects();
}

public List<String> getFlashList() {
    return mCamera.getParameters().getSupportedFlashModes();
}

public List<String> getFocusList() {
    return mCamera.getParameters().getSupportedFocusModes();
}

public List<String> getSceneList() {
    return mCamera.getParameters().getSupportedSceneModes();
}

public List<String> getWhiteBalanceList() {
    return mCamera.getParameters().getSupportedWhiteBalance();
}

public List<Size> getResolutionList() {     
    return mCamera.getParameters().getSupportedPreviewSizes();        
}

public List<Boolean> getExposureList() {    
	List<Boolean> ret = new ArrayList<Boolean>(2);
	ret.add(0, true);
	ret.add(0, false);
    return ret;        
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public String getAntibanding() {
    return mCamera.getParameters().getAntibanding();
}

public String getEffect() {
    return mCamera.getParameters().getColorEffect();
}

public String getFlash() {
	return mCamera.getParameters().getFlashMode();
}

public String getFocus() {
	return mCamera.getParameters().getFocusMode();
}

public String getScene() {
    return mCamera.getParameters().getSceneMode();
}

public String getWhiteBalance() {
    return mCamera.getParameters().getWhiteBalance();
}

public Size getResolution() {
	return mCamera.getParameters().getPreviewSize();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void setAntibanding(String antibanding) {
    Camera.Parameters params = mCamera.getParameters();
    params.setAntibanding(antibanding);;
    mCamera.setParameters(params);
}

public void setEffect(String effect) {
    Camera.Parameters params = mCamera.getParameters();
    params.setColorEffect(effect);
    mCamera.setParameters(params);
}

public void setFlash(String flash) {
    Camera.Parameters params = mCamera.getParameters();
    params.setFlashMode(flash);
    mCamera.setParameters(params);
}

public void setFocus(String focus) {
    Camera.Parameters params = mCamera.getParameters();
    params.setFocusMode(focus);
    mCamera.setParameters(params);
}

public void setScene(String value) {
    Camera.Parameters params = mCamera.getParameters();
    params.setSceneMode(value);
    mCamera.setParameters(params);
}

public void setWhite() {
    Camera.Parameters params = mCamera.getParameters();
    params.setWhiteBalance(SeekBarVal11.valWB);
    mCamera.setParameters(params);
}

public void setResolution(Size resolution) {
    disconnectCamera();
    connectCamera((int)resolution.width, (int)resolution.height);       
}

public void setExposure(Boolean bool) {
    Camera.Parameters params = mCamera.getParameters();
    params.setVideoStabilization(bool);
    params.setExposureCompensation(SeekBarVal11.ExVal);
    counter++;
    if(counter < 50){
        params.setAutoExposureLock(false);
    }
    else{
    	params.setAutoExposureLock(bool);
    	counter = 51;
    }
    mCamera.setParameters(params);
}

public void takePicture(final String fileName) {
    Log.i(TAG, "Taking picture");
    this.mPictureFileName = fileName;
    // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
    // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
    mCamera.setPreviewCallback(null);

    // PictureCallback is implemented by the current class
    mCamera.takePicture(null, null, this);
}

@Override
public void onPictureTaken(byte[] data, Camera camera) {
    Log.i(TAG, "Saving a bitmap to file");
    // The camera preview was automatically stopped. Start it again.
    mCamera.startPreview();
    mCamera.setPreviewCallback(this);

    // Write the image in a file (in jpeg format)
    try {
        FileOutputStream fos = new FileOutputStream(mPictureFileName);

        fos.write(data);
        fos.close();

    } catch (java.io.IOException e) {
        Log.e("PictureDemo", "Exception in photoCallback", e);
    }

}
}
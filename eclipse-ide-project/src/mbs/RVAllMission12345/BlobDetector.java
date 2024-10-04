package mbs.RVAllMission12345;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class BlobDetector {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.0001;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(5,20,77,0);
    private Scalar mColorRadiusPH = new Scalar(50,50,10,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    
//    private Scalar mColor;
//    private Scalar mColor2;
    
    Scalar param1;
    Scalar param2;

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        double minS = (hsvColor.val[1] - mColorRadius.val[1] <= 0) ? 0 : hsvColor.val[1] - mColorRadius.val[1];
        double maxS = (hsvColor.val[1] + mColorRadius.val[1] >= 255) ? 255 : hsvColor.val[1] + mColorRadius.val[1];
        
        double minV = (hsvColor.val[2] - mColorRadius.val[2] <= 0) ? 0 : hsvColor.val[2] - mColorRadius.val[2];
        double maxV = (hsvColor.val[2] + mColorRadius.val[2] >= 255) ? 255 : hsvColor.val[2] + mColorRadius.val[2];
        
        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = minS;
        mUpperBound.val[1] = maxS;

        mLowerBound.val[2] = minV;
        mUpperBound.val[2] = maxV;
        
        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;
        
        param1 = new Scalar (mLowerBound.val[0], mLowerBound.val[1], mLowerBound.val[2], mLowerBound.val[3]);
        param2 = new Scalar (mUpperBound.val[0], mUpperBound.val[1], mUpperBound.val[2], mUpperBound.val[3]);

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }
    
    public void setHsvColorPH(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadiusPH.val[0]) ? hsvColor.val[0]-mColorRadiusPH.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadiusPH.val[0] <= 255) ? hsvColor.val[0]+mColorRadiusPH.val[0] : 255;

        double minS = (hsvColor.val[1] - mColorRadiusPH.val[1] <= 0) ? 0 : hsvColor.val[1] - mColorRadiusPH.val[1];
        double maxS = (hsvColor.val[1] + mColorRadiusPH.val[1] >= 255) ? 255 : hsvColor.val[1] + mColorRadiusPH.val[1];
        
        double minV = (hsvColor.val[2] - mColorRadiusPH.val[2] <= 0) ? 0 : hsvColor.val[2] - mColorRadiusPH.val[2];
        double maxV = (hsvColor.val[2] + mColorRadiusPH.val[2] >= 255) ? 255 : hsvColor.val[2] + mColorRadiusPH.val[2];
        
        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = minS;
        mUpperBound.val[1] = maxS;

        mLowerBound.val[2] = minV;
        mUpperBound.val[2] = maxV;
        
        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;
        
        param1 = new Scalar (mLowerBound.val[0], mLowerBound.val[1], mLowerBound.val[2], mLowerBound.val[3]);
        param2 = new Scalar (mUpperBound.val[0], mUpperBound.val[1], mUpperBound.val[2], mUpperBound.val[3]);

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }
    
    public void process(Mat rgbaImage) {
//        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
//        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(rgbaImage, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
//        mColor = Core.sumElems(mContours);
    	
        mContours.clear();
       
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
//                mColor = Core.sumElems(contour);
//                param1 = mColor;
                mContours.add(contour);
//                mColor2 = Core.sumElems(mContours.get(0));
//                param2 = mColor2;
            }
        }
    }

    public void process(Mat rgbaImage, Scalar mMinBound, Scalar mMaxBound, int dilate, int erode) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, mMinBound, mMaxBound, mMask);
//        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        Imgproc.erode(mMask, mDilatedMask, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*erode+1,2*erode+1), new Point (erode,erode))); 
        Imgproc.dilate(mDilatedMask, mDilatedMask, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*dilate+1,2*dilate+1), new Point (dilate,dilate)));
        Imgproc.dilate(mDilatedMask, mDilatedMask, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*dilate+1,2*dilate+1), new Point (dilate,dilate)));
        Imgproc.erode(mDilatedMask, mDilatedMask, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size (2*erode+1,2*erode+1), new Point (erode,erode)));

        
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        // mColor = Core.sumElems(mContours);
    	
        mContours.clear();
       
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                mContours.add(contour);
            }
        }
    }
    public Scalar getParam1(){
    	
		return param1;
    	
    }
    
    public Scalar getParam2(){
    	
		return param2;
    	
    }
    
    public double getHmin(){
		return mLowerBound.val[0];
    	
    }
    
    public double getHmax(){
		return mUpperBound.val[0];
    	
    }
    
    public double getSmin(){
		return mLowerBound.val[1];
    	
    }
    
    public double getSmax(){
		return mUpperBound.val[1];
    	
    }
    
    public double getVmin(){
		return mLowerBound.val[2];
    	
    }
    
    public double getVmax(){
		return mUpperBound.val[2];
    	
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }
    
    public Mat getMask() {
        return mHsvMat;
    }
}

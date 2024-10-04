package com.mbsbahru.roboboat2016_barunastra;

import static com.mbsbahru.roboboat2016_barunastra.SeekBarVal11.ExVal;
import static com.mbsbahru.roboboat2016_barunastra.SeekBarVal11.focusDistanceValue;
import static com.mbsbahru.roboboat2016_barunastra.SeekBarVal11.whiteBalanceMode;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Range;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MisiView extends JavaCameraView {
    private static final String TAG = "MisiView";
    public static final int REQUEST_CAMERA_PERMISSION = 200;
    private CameraDevice cameraDevice;
    public static CameraCaptureSession captureSession;
    public static CaptureRequest.Builder previewRequestBuilder;
    private CaptureRequest.Builder captureBuilder;
    private ImageReader imageReader;

    public static Handler backgroundHandler;
    private static HandlerThread backgroundThread;

    public MisiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static void startBackgroundThread() {
        if (backgroundThread == null || backgroundHandler == null) {
            backgroundThread = new HandlerThread("CameraBackground");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
        }
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeImageReader(int width, int height) {
        if (imageReader == null) {
            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2);
            imageReader.setOnImageAvailableListener(reader -> {
                try (FileOutputStream fos = new FileOutputStream("path/to/save/image.jpg")) {
                    ByteBuffer buffer = reader.acquireNextImage().getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    fos.write(bytes);
                    reader.acquireNextImage().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, backgroundHandler);
        }
    }

    public void openCamera() {
        startBackgroundThread();
        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = manager.getCameraIdList()[0];

            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getContext(),
                        new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }

//            initializeImageReader(getWidth(), getHeight());

            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            Log.e(TAG, "camera permission is not granted", e);
        }
    }

    public void releaseCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        stopBackgroundThread();
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
            Log.e(TAG, "Camera device error: " + error);
        }
    };


    private void createCameraPreview() {
        try {
            SurfaceHolder holder = getHolder();
            if (holder == null || !holder.getSurface().isValid()) {
                Log.e(TAG, "SurfaceHolder is not ready.");
                return;
            }

            Surface surface = holder.getSurface();
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            if (imageReader == null || imageReader.getSurface() == null) {
                Log.e(TAG, "ImageReader or its surface is not initialized.");
                return;
            }

            // checking the Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                OutputConfiguration outputConfig1 = new OutputConfiguration(surface);
                OutputConfiguration outputConfig2 = new OutputConfiguration(imageReader.getSurface());

                SessionConfiguration sessionConfig = new SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        Arrays.asList(outputConfig1, outputConfig2),
                        getContext().getMainExecutor(),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                if (cameraDevice == null) {
                                    return;
                                }
                                captureSession = session;
                                updatePreview();
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                Log.e(TAG, "Configuration change failed");
                            }
                        }
                );

                cameraDevice.createCaptureSession(sessionConfig);

            } else {
                cameraDevice.createCaptureSession(
                        Arrays.asList(surface, imageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                if (cameraDevice == null) {
                                    return;
                                }
                                captureSession = session;
                                updatePreview();
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                Log.e(TAG, "Configuration change failed");
                            }
                        },
                        backgroundHandler
                );
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            Log.e(TAG, "updatePreview error, return");
            return;
        }

        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_OFF);

            setFocusDistance(focusDistanceValue);
            setCameraFeature(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ExVal);

            captureSession.setRepeatingRequest(previewRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void setFocusDistance(float focusDistance) {
        if (cameraDevice == null || captureSession == null) {
            Log.e(TAG, "Camera device or capture session is not initialized.");
            return;
        }

        try {
            CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            String cameraId = manager.getCameraIdList()[0]; // using the first camera
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            Range<Integer> exposureRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
            if (exposureRange != null && exposureRange.contains(ExVal)) {
                setCameraFeature(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ExVal);
            }

            Float minimumFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            if (minimumFocusDistance != null && minimumFocusDistance > 0) {
                focusDistance = Math.max(0, Math.min(focusDistance, minimumFocusDistance));
                setCameraFeature(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance);
            } else {
                Log.e(TAG, "Manual focus is not supported by this camera.");
            }

            Log.d(TAG, "Focus distance set to: " + focusDistance);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public <T> void setCameraFeature(CaptureRequest.Key<T> featureType, T value) {
        if (cameraDevice == null || captureSession == null) return;

        try {
//            previewRequestBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, false);
//            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);

            previewRequestBuilder.set(featureType, value);
            captureSession.setRepeatingRequest(previewRequestBuilder.build(), captureCallback, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            Log.d(TAG, "Capture completed.");
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureFailure failure) {
            Log.e(TAG, "Capture failed: " + failure.getReason());
        }
    };

    public void setManualWhiteBalance(RggbChannelVector gains) {
        if (cameraDevice == null || captureSession == null) return;

        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
            previewRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
            previewRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, gains);

            captureSession.setRepeatingRequest(previewRequestBuilder.build(), captureCallback, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    public void takePicture(final String fileName) {
        if (cameraDevice == null || captureSession == null) return;

        try {
            captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF);

            captureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.d(TAG, "Saved: " + fileName);
                    createCameraPreview();
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        openCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        releaseCamera();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
